package com.doccuty.radarplus.recommender;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.jboss.logging.Logger;

import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.Setting;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.User;

/**
 * Component for generating recommendations based on constraint-based filtering
 * content-based filtering using enhanced, weighted vector space model
 * context-based post-filtering
 *
 */

public class Recommender {

	public final static String PROPERTY_ABIDANCE_MODE = "abidanceMode";
	public final static String PROPERTY_EFFICIENCY_MODE = "efficiencyMode";

	private final static Logger LOG = Logger.getLogger(Recommender.class);

	private RecoTool app;

	private ContentBasedFilter cntnbf;
	private ConstraintBasedFilter cnstbf;
	private ContextBasedPostFilter cxbpf;
	private SerendipityMechanism serp;

	private LinkedHashMap<Item, Double> originalItemMap;
	private LinkedHashMap<Item, Double> itemMap;
	private LinkedHashMap<Item, LinkedHashMap<Item, Double>> similarityMap;

	private List<Item> productivityItems;

	Timer timer;
	double lastModeCheck;

	// Check recommendation mode
	private String mode;
	private boolean timerRunning;

	// Enable/disable serpenting
	private boolean serendipityEnabled;
	private boolean weightingEnabled;

	public Recommender() {
		cntnbf = new ContentBasedFilter(this);
		cnstbf = new ConstraintBasedFilter(this);
		cxbpf = new ContextBasedPostFilter(this);
		serp = new SerendipityMechanism(this);

		this.timer = new Timer();
		this.timerRunning = false;
		this.lastModeCheck = 0;

		this.mode = PROPERTY_ABIDANCE_MODE;
	}

	public LinkedHashMap<Item, Double> startFullFilterProcess(User user, Setting setting, List<Item> itemList) {

		if (this.timerRunning) {
			this.timer.cancel();
			this.timerRunning = false;
			timer = new Timer();
		}

		LOG.info("Started full filtering process...");

		itemList.addAll(this.app.getTrainer().getTrainingItems());

		// Content based filtering
		this.originalItemMap = cntnbf.calculateRelevance(user, itemList);

		// Constraint based filtering
		this.originalItemMap = cnstbf.filterItems(user, this.originalItemMap);

		// Create similarities of all items
		this.similarityMap = this.cntnbf.calculateSimilarityMatrix(this.cntnbf.getAttributeMatrix());

		// Apply serendipity mechanism
		if (this.getSerendipityEnabled()) {
			this.originalItemMap = this.serp.serendipityProcessing(this.originalItemMap);
		}

		// Copy current recommendations for online filtering
		this.itemMap = new LinkedHashMap<Item, Double>();
		for (Iterator<Entry<Item, Double>> it = this.originalItemMap.entrySet().iterator(); it.hasNext();) {
			Entry<Item, Double> e = it.next();
			this.itemMap.put(e.getKey(), e.getValue());
		}

		// Update items scores by setting
		this.itemMap = cxbpf.filterBySetting(user, setting, this.itemMap);

		this.itemMap = this.itemMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> {
					throw new AssertionError();
				}, LinkedHashMap::new));

		if (this.timer == null)
			this.timer = new Timer();

		// Timer to check whether mode must be switched or not
		this.timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				try {
					if (app.getSetting().getTimeToDeparture() <= 0 || !timerRunning) {
						timerRunning = false;
						cancel();
						return;
					}

					if (timerRunning && lastModeCheck + 10000 < new Date().getTime())
						checkMode(setting);

				} catch (MqttException e) {
					e.printStackTrace();
					timerRunning = false;
					cancel();
				}
			}
		}, 0, 15000);

		LOG.info("Full filtering process done");

		return this.itemMap;
	}

	@SuppressWarnings("unchecked")
	public LinkedHashMap<Item, Double> updateBySetting(User user, Setting setting) {

		if (this.itemMap == null && this.mode.compareTo(PROPERTY_ABIDANCE_MODE) == 0) {
			this.itemMap = new LinkedHashMap<Item, Double>();
			return this.itemMap;
		}

		// Filtered productivity items
		if (this.mode.compareTo(PROPERTY_EFFICIENCY_MODE) == 0) {
			if (this.productivityItems == null)
				this.productivityItems = this.app.getItems().stream().filter(i -> i.getIsProductivityItem())
						.collect(Collectors.toList());

			return cxbpf.filterBySetting(user, setting, this.productivityItems);
		}

		this.itemMap = (LinkedHashMap<Item, Double>) this.getOriginalRecommendations().clone();

		// Filter regular items by setting
		this.itemMap = cxbpf.filterBySetting(user, setting, this.itemMap);

		this.itemMap = this.itemMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> {
					throw new AssertionError();
				}, LinkedHashMap::new));

		try {
			this.checkMode(setting);
		} catch (MqttException e) {
			e.printStackTrace();
		}

		return this.itemMap;
	}

	/**
	 * Check current circumstances and switch to efficiency mode, if necessary
	 * 
	 * @throws MqttException
	 * @throws MqttPersistenceException
	 */

	public String checkMode(Setting setting) throws MqttPersistenceException, MqttException {

		if (this.itemMap.size() == 0 && this.mode.compareTo(PROPERTY_ABIDANCE_MODE) == 0
				&& this.app.getNumOfItemsToUse() == 0) {
			String oldValue = this.mode;
			this.mode = PROPERTY_EFFICIENCY_MODE;
			this.app.switchedRecommendationMode(oldValue, this.mode);
			return this.mode;
		}

		double requiredTimeToReachDestination = this.getRequiredTimeToReachDestination(setting);

		// change mode
		if (this.mode.compareTo(PROPERTY_ABIDANCE_MODE) == 0
				&& (setting.getTimeToDeparture() < requiredTimeToReachDestination)) {
			this.mode = PROPERTY_EFFICIENCY_MODE;
			this.app.switchedRecommendationMode(PROPERTY_ABIDANCE_MODE, this.mode);
		} else if (setting.getTimeToDeparture() > requiredTimeToReachDestination
				&& this.mode.compareTo(PROPERTY_EFFICIENCY_MODE) == 0) {
			this.mode = PROPERTY_ABIDANCE_MODE;
			this.app.switchedRecommendationMode(PROPERTY_EFFICIENCY_MODE, this.mode);
		}

		this.lastModeCheck = new Date().getTime();

		return this.mode;
	}

	public double getRequiredTimeToReachDestination(Setting setting) {

		// estimated time to reach next destination in [min]
		double requiredTimeToReachDestination = 0;

		double distance = 0;
		if (this.app.getUseGeocoordinates())
			distance = this.app.getEndPosition().distance(setting.getGeoposition());
		else
			distance = this.app.getEndPosition().euclideanDistance(setting.getGeoposition());

		requiredTimeToReachDestination = distance * 60 / this.app.getCurrentUser().getMinWalkingSpeed();

		// add additional buffer [1;5] in [min]
		if (requiredTimeToReachDestination * 0.1 < 1)
			requiredTimeToReachDestination += 1;
		else if (requiredTimeToReachDestination * 0.1 < 5)
			requiredTimeToReachDestination *= 1.1;
		else
			requiredTimeToReachDestination += 5;

		// convert to [ms]
		requiredTimeToReachDestination *= 60000;

		return requiredTimeToReachDestination;
	}

	public void resetRecommendations() {
		this.mode = PROPERTY_ABIDANCE_MODE;

		this.originalItemMap = new LinkedHashMap<Item, Double>();
		this.itemMap = new LinkedHashMap<Item, Double>();
		this.similarityMap = new LinkedHashMap<Item, LinkedHashMap<Item, Double>>();

		this.productivityItems = this.app.getItems().stream().filter(i -> i.getIsProductivityItem())
				.collect(Collectors.toList());
	}

	// =============================================

	public ContentBasedFilter getContentBasedFilter() {
		return this.cntnbf;
	}

	// =============================================

	public ConstraintBasedFilter getConstraintBasedFilter() {
		return this.cnstbf;
	}

	// =============================================

	public ContextBasedPostFilter getContextBasedPostFilter() {
		return this.cxbpf;
	}

	// =============================================

	/**
	 * returns current item ranking after content- and constraint-based and
	 * contextual filtering
	 * 
	 * @return
	 */

	public LinkedHashMap<Item, Double> getRecommendations() {
		if (this.itemMap == null)
			this.itemMap = new LinkedHashMap<Item, Double>();

		return this.itemMap;
	}

	// =============================================

	/**
	 * returns item ranking after content- and constraint-based filtering
	 * 
	 * @return
	 */

	public LinkedHashMap<Item, Double> getOriginalRecommendations() {
		if (this.originalItemMap == null)
			this.originalItemMap = new LinkedHashMap<Item, Double>();

		return this.originalItemMap;
	}

	// ===================================================

	public RecoTool getApp() {
		return this.app;
	}

	public void setApp(RecoTool value) {
		this.app = value;
	}

	public Recommender withApp(RecoTool value) {
		setApp(value);
		return this;
	}

	// ===================================================

	public String getMode() {
		return this.mode;
	}

	public void setMode(String value) {
		this.mode = value;
	}

	public Recommender withMode(String value) {
		setMode(value);
		return this;
	}

	// ===================================================

	public boolean getTimerRunning() {
		return this.timerRunning;
	}

	public void setTimerRunning(boolean value) {
		this.timerRunning = value;
	}

	public Recommender withTimerRunning(boolean value) {
		setTimerRunning(value);
		return this;
	}

	// ===================================================

	public boolean getSerendipityEnabled() {
		return this.serendipityEnabled;
	}

	public void setSerendipityEnabled(boolean value) {
		this.serendipityEnabled = value;
	}

	public Recommender withSerendipityEnabled(boolean value) {
		this.setSerendipityEnabled(value);
		return this;
	}

	// ===================================================

	public boolean getWeightingEnabled() {
		return this.weightingEnabled;
	}

	public void setWeightingEnabled(boolean value) {
		this.weightingEnabled = value;
	}

	public Recommender withWeightingEnabled(boolean value) {
		this.setWeightingEnabled(value);
		return this;
	}

	// ===================================================

	public LinkedHashMap<Item, LinkedHashMap<Item, Double>> getSimilarityMap() {
		if (this.similarityMap == null)
			this.similarityMap = new LinkedHashMap<Item, LinkedHashMap<Item, Double>>();

		return this.similarityMap;
	}

	/**
	 * Event handling
	 */

	protected PropertyChangeSupport listeners = null;

	public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		if (listeners != null) {
			listeners.firePropertyChange(propertyName, oldValue, newValue);
			return true;
		}
		return false;
	}

	public boolean addPropertyChangeListener(PropertyChangeListener listener) {
		if (listeners == null) {
			listeners = new PropertyChangeSupport(this);
		}
		listeners.addPropertyChangeListener(listener);
		return true;
	}

	public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (listeners == null) {
			listeners = new PropertyChangeSupport(this);
		}
		listeners.addPropertyChangeListener(propertyName, listener);
		return true;
	}

	public boolean removePropertyChangeListener(PropertyChangeListener listener) {
		if (listeners == null) {
			listeners.removePropertyChangeListener(listener);
		}
		listeners.removePropertyChangeListener(listener);
		return true;
	}

	public boolean removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (listeners != null) {
			listeners.removePropertyChangeListener(propertyName, listener);
		}
		return true;
	}
}
