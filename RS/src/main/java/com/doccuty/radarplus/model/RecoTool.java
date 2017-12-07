package com.doccuty.radarplus.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.doccuty.radarplus.MainApp;
import com.doccuty.radarplus.evaluation.ResultTracker;
import com.doccuty.radarplus.network.RecoToolMqttServer;
import com.doccuty.radarplus.persistence.AttributeDAO;
import com.doccuty.radarplus.persistence.ItemDAO;
import com.doccuty.radarplus.persistence.UserDAO;
import com.doccuty.radarplus.recommender.Recommender;
import com.doccuty.radarplus.recommender.WalkingSpeedCalculator;
import com.doccuty.radarplus.trainer.Trainer;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is the main model in RecoTool, which provides functionality
 * to control all included functions and enables usage of network connection
 * via MQTT.
 * 
 * @author Niclas Kannengießer
 *
 */

public class RecoTool {

	private final static Logger LOG = Logger.getLogger(RecoTool.class);

	private long version;

	private Recommender recommender;
	private Trainer trainer;

	private ItemDAO itemDao = new ItemDAO();
	private UserDAO userDao = new UserDAO();

	private User user;

	private Item currentItem;
	private List<Item> items;

	private static Setting setting;

	private RecoToolMqttServer server;
	private Thread networkThread;

	private Calendar startTime;
	private Duration evaluationDuration;

	private int maxNumOfItems;
	private int maxNumOfProductivityItems;
	private int numOfItemsToUse;

	private boolean randomizeItemGeoposition;

	private boolean useGeocoordinates;
	private boolean realtimeUserPositionUpdateAccuracyEvaluationMap;

	// Hide item list in data glasses
	private boolean hideAll;

	// Updates time during evaluation
	Timer evaluationTimer;
	boolean timerRunning;

	Item navigationDestination;

	String nextConnectionPositionIdentifier;

	// Current traffic junction
	private TrafficJunction startTrafficJunction;

	// Next traffic junction
	private TrafficJunction endTrafficJunction;

	// Position in the beginning of the evaluation
	private Geoposition startPosition;

	// Destination that has to be reached in the end of the evaluation
	private Geoposition endPosition;

	// Start position for walking Training
	private Geoposition walkingTrainingPosition;
	private WalkingSpeedCalculator walkingSpeedCalculator;

	// Save and read file items scores to/from file
	private ResultTracker resultTracker;

	// Duration of automated delay
	private Duration delayDuration;

	// Timestamp when to send delay prompt
	private Duration delayPromptTimer;
	private boolean delayPromptSent;

	private boolean newEvaluation = false;

	// List of all prompts in the system
	List<SystemPrompt> prompts;

	// Preferences
	public static Preferences prefs;

	public RecoTool() {

		this.user = new User();
		RecoTool.setting = new Setting();

		this.recommender = new Recommender().withApp(this);
		this.trainer = new Trainer().withStudyApp(this).withUser(this.user);

		this.evaluationTimer = new Timer();
		this.timerRunning = false;

		this.server = new RecoToolMqttServer(this);

		this.startTime = Calendar.getInstance();
		RecoTool.setting.setCurrentTime(this.startTime.getTime());

		this.startPosition = new Geoposition();
		this.endPosition = new Geoposition();

		this.walkingTrainingPosition = new Geoposition();

		this.walkingSpeedCalculator = new WalkingSpeedCalculator(this);

		this.resultTracker = new ResultTracker();

		RecoTool.prefs = Preferences.userNodeForPackage(getClass());

		this.delayPromptSent = false;
		this.hideAll = true;
	}

	public void init() {

		ObjectMapper mapper = new ObjectMapper();
		InputStream from = null;

		try {

			// Simulation settings
			this.startTime.setTime(new Date(RecoTool.prefs.getLong("startTime", 0)));
			RecoTool.setting.setCurrentTime(this.startTime.getTime());

			this.evaluationDuration = Duration.ofMinutes(RecoTool.prefs.getLong("evaluationDuration", 30));
			
			Date departure = new Date(this.startTime.getTime().getTime() + this.evaluationDuration.toMillis());
			RecoTool.setting.withCurrentTime(this.startTime.getTime()).withEstimatedDepartureTime(departure).withCurrentDepartureTime(departure);
			
			this.recommender.getContextBasedPostFilter().setTimeMaximizer(RecoTool.prefs.getDouble("timeMaximizer", 1));

			this.startTrafficJunction = mapper.treeToValue(
					mapper.readTree(RecoTool.prefs.get("startTrafficJunction", "{}")), TrafficJunction.class);
			this.endTrafficJunction = mapper.treeToValue(
					mapper.readTree(RecoTool.prefs.get("endTrafficJunction", "{}")), TrafficJunction.class);

			this.nextConnectionPositionIdentifier = RecoTool.prefs.get("nextConnectionPositionIdentifier", "Gleis 23");

			this.startPosition = mapper.treeToValue(mapper.readTree(RecoTool.prefs.get("startPosition", "{}")),
					Geoposition.class);
			this.endPosition = mapper.treeToValue(mapper.readTree(RecoTool.prefs.get("endPosition", "{}")),
					Geoposition.class);

			RecoTool.setting.setGeoposition(this.startPosition);

			// Item settings
			this.withMaxNumOfItems(RecoTool.prefs.getInt("maxNumOfItems", 1))
					.withMaxNumOfProductivityItems(RecoTool.prefs.getInt("maxNumOfProductivityItems", 0))
					.withUseGeocoordinates(RecoTool.prefs.getBoolean("useGeocoordinates", false))
					.withRandomizeItemGeoposition(RecoTool.prefs.getBoolean("randomizeItemGeoposition", true));

			this.recommender.withSerendipityEnabled(RecoTool.prefs.getBoolean("serendipityEnabled", false))
					.withWeightingEnabled(RecoTool.prefs.getBoolean("weightingEnabled", true));

			// Network settings
			this.server.withBrokerURI(RecoTool.prefs.get("brokerIP", "tcp://localhost"))
					.withBrokerPort(RecoTool.prefs.getInt("brokerPort", 1883));

			// Other setting
			this.withRealtimeUserPositionUpdateAccuracyEvaluationMap(
					RecoTool.prefs.getBoolean("realtimeUserPositionUpdateAccuracyEvaluationMap", false))
					.withDelayDuration(Duration.ofMinutes(RecoTool.prefs.getLong("delayDuration", 0)))
					.withDelayPromptTimer(Duration.ofMinutes(RecoTool.prefs.getLong("delayPromptTimer", 0)));

			this.walkingTrainingPosition = mapper.treeToValue(
					mapper.readTree(RecoTool.prefs.get("walkingTrainingPosition", "{}")), Geoposition.class);

			this.withNumOfItemsToUse(RecoTool.prefs.getInt("maxNumOfItemsToUse", 5));

		} catch (NullPointerException | IOException e) {
			e.printStackTrace();
		}

		try {
			// Load system prompt
			this.prompts = new ArrayList<SystemPrompt>();

			from = this.getClass().getResourceAsStream(MainApp.SYSTEM_PROMPTS_JSON_FILE);

			if (from == null)
				throw new FileNotFoundException("File " + MainApp.SYSTEM_PROMPTS_JSON_FILE + " not found!");

			for (SystemPrompt sP : mapper.readValue(from, SystemPrompt[].class))
				this.prompts.add(sP);

		} catch (NullPointerException | IOException e) {
			e.printStackTrace();
		}

		List<Item> t = this.retreiveItemsFromDB().stream().filter(i -> i.getIsTrainingItem())
				.collect(Collectors.toList());

		this.trainer.setTrainingItems(t);

		this.initializeMQTT();
	}

	// ====================================

	public static final String PROPERTY_RECOMMENDATIONS = "recommendations";

	public LinkedHashMap<Item, Double> generateRecommendations() {

		LinkedHashMap<Item, Double> recommendations = this.getRecommender().startFullFilterProcess(this.user,
				RecoTool.setting, this.assignTrafficJunctionToRandomItems());

		this.firePropertyChange(PROPERTY_RECOMMENDATIONS, null, recommendations);

		if (!this.getMQTTClient().isConnected())
			return recommendations;

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			JSONObject json = new JSONObject();
			json.put("action", RecoToolMqttServer.MQTT_CMD_RECOMMENDATIONS);

			JSONArray arr = new JSONArray();

			for (Iterator<Item> it = recommendations.keySet().iterator(); it.hasNext();) {
				Item item = it.next();
				arr.put(new JSONObject(mapper.writeValueAsString(item)));
			}

			json.put("items", arr);

			this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

			/**
			 * Prepare items for CAVE topic Items need to be added without contextual
			 * filter. Otherwise, items might not be displayed in CAVE after delay.
			 */

			arr = new JSONArray();

			for (Iterator<Item> it = this.items.iterator(); it.hasNext();) {
				Item item = it.next();

				if (item.getTrafficJunction() != null
						&& item.getTrafficJunction().equals(RecoTool.setting.getTrafficJunction())
						&& !item.getIsProductivityItem()) {

					arr.put(new JSONObject(mapper.writeValueAsString(item)));
				}
			}

			// Add randomized productivity items for CAVE topic
			List<Item> productivityItems = this.items.stream().filter(i -> i.getIsProductivityItem())
					.collect(Collectors.toList());

			Random rand = new Random();
			int oldLength = arr.length();
			List<Integer> s = new ArrayList<Integer>();
			while (arr.length() <= oldLength + this.maxNumOfProductivityItems && s.size() < productivityItems.size()) {

				int idx = rand.nextInt(productivityItems.size());

				if (!s.contains(idx)) {

					Item item = productivityItems.get(idx);

					if (item.getAttributeList().size() > 0) {
						Attribute domainAttribute = item.getAttributeList().get(0);
						item.setDomain(domainAttribute);
					}

					arr.put(new JSONObject(mapper.writeValueAsString(item)));
				}
			}

			json.put("items", arr);

			this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_CAVE_TOPIC);

			LOG.info("Sent new recommendations: " + RecoToolMqttServer.MQTT_CMD_RECOMMENDATIONS);

		} catch (MqttException | JSONException | JsonProcessingException e1) {
			e1.printStackTrace();
		}

		return recommendations;
	}

	/**
	 * Select items from overall items, which are no training or productivity items
	 * and assign current traffic junction.
	 * 
	 * @return
	 */

	public List<Item> assignTrafficJunctionToRandomItems() {
		Random rand = new Random();

		List<Item> list = new ArrayList<Item>(this.items);
		List<Geoposition> pos = new ArrayList<Geoposition>();

		list = list.stream().filter(i -> !i.getIsTrainingItem() && !i.getIsProductivityItem())
				.collect(Collectors.toList());

		// Reset traffic junction for each item
		for (Iterator<Item> it = list.iterator(); it.hasNext();) {
			it.next().setTrafficJunction(null);
		}

		// Assign traffic junction to random items
		Set<Integer> usedRandomNumbers = new HashSet<Integer>();

		while (usedRandomNumbers.size() < this.maxNumOfItems && usedRandomNumbers.size() < list.size()) {
			try {
				int idx = rand.nextInt(list.size());

				if (usedRandomNumbers.contains(idx))
					continue;

				list.get(idx).setTrafficJunction(this.startTrafficJunction);

				usedRandomNumbers.add(idx);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (this.randomizeItemGeoposition)
			randomizeItemGeoposition(list, pos);

		return list;
	}

	/**
	 * Get unique and randomized Coordinates for selected Items geoCoordinates is a
	 * list of additional position objects. Maximum number of items equals maximum
	 * number of different positions.
	 * 
	 * @param list
	 * @param geoCoordinates
	 * @return
	 */

	public List<Item> randomizeItemGeoposition(List<Item> list, List<Geoposition> geoCoordinates) {

		for (Item item : list) {
			if (item.getGeoposition() != null && !geoCoordinates.contains(item.getGeoposition()))
				geoCoordinates.add(item.getGeoposition());

			if (geoCoordinates.size() >= this.maxNumOfItems)
				break;
		}

		if (geoCoordinates.size() < this.maxNumOfItems)
			this.maxNumOfItems = geoCoordinates.size();

		Random rand = new Random();

		for (Iterator<Item> it = list.iterator(); it.hasNext();) {
			Item item = it.next();

			if (!this.startTrafficJunction.equals(item.getTrafficJunction()))
				continue;

			if (geoCoordinates.size() == 0) {
				// Remove items from traffic junction to avoid duplicates on same position
				item.setTrafficJunction(null);
				continue;
			}

			int idx = rand.nextInt(geoCoordinates.size());

			item.setGeoposition(geoCoordinates.get(idx));
			geoCoordinates.remove(idx);
		}

		LOG.info("Shuffled item geoposition");

		return list;
	}

	// ===================================================

	public LinkedHashMap<Item, Double> updateBySetting(User user) {

		LinkedHashMap<Item, Double> oldRecommendations = this.recommender.getRecommendations();
		LinkedHashMap<Item, Double> recommendations = this.recommender.updateBySetting(user, RecoTool.setting);

		// Check old and new order of recommendations to minimize network traffic
		boolean equal = oldRecommendations.size() == recommendations.size();

		Iterator<Item> it1 = oldRecommendations.keySet().iterator();
		Iterator<Item> it2 = recommendations.keySet().iterator();
		while (equal && it1.hasNext()) {
			Item e1 = it1.next();
			Item e2 = it2.next();

			if (!e1.equals(e2))
				equal = false;
		}

		this.firePropertyChange(PROPERTY_RECOMMENDATIONS, null, recommendations);

		if (equal || !this.getMQTTClient().getClient().isConnected()
				|| !this.getMQTTClient().getSubscribedTopic().contains(RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC))
			return recommendations;

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			JSONObject json = new JSONObject();
			json.put("action", RecoToolMqttServer.MQTT_CMD_RECOMMENDATIONS);

			JSONArray arr = new JSONArray();

			for (Iterator<Item> it = recommendations.keySet().iterator(); it.hasNext();) {
				Item item = it.next();

				if (!item.getIsProductivityItem())
					arr.put(new JSONObject(mapper.writeValueAsString(item)));
			}

			json.put("items", arr);

			this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

			LOG.info("Sent new recommendations: " + RecoToolMqttServer.MQTT_CMD_RECOMMENDATIONS);

		} catch (MqttException | JSONException | JsonProcessingException e1) {
			e1.printStackTrace();
		}

		return recommendations;
	}

	/**
	 * Hide all recommendation displayed in data glasses
	 * 
	 * @throws MqttException
	 * @throws MqttPersistenceException
	 */

	public void hideAll() throws MqttPersistenceException, MqttException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		JSONObject json = new JSONObject();
		json.put("action", RecoToolMqttServer.MQTT_CMD_HIDE_ALL);
		json.put("value", (this.hideAll) ? "true" : "false");

		this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

		this.hideAll = !this.hideAll;

		LOG.info("Sent command: " + RecoToolMqttServer.MQTT_CMD_HIDE_ALL);
	}

	// ===================================================

	public static final String PROPERTY_SAVED = "save";
	public static final String PROPERTY_UPDATED = "updated";

	/**
	 * get all used items and their respective rating and save into database
	 */

	public void saveUser() {

		for (Iterator<Entry<Item, Setting>> it = RecoTool.setting.getUsedItem().entrySet().iterator(); it.hasNext();) {
			Entry<Item, Setting> e = it.next();

			if (e.getKey().getId() == 0)
				continue;

			// Save the original score for used items
			double rating = 0;

			if (!this.recommender.getOriginalRecommendations().isEmpty())
				rating = this.recommender.getOriginalRecommendations().get(e.getKey());

			Setting s = e.getValue();

			Rating r = new Rating().withItem(e.getKey()).withRating(rating).withSetting(s);
			this.user.withRatings(r);

			it.remove();
		}

		if (this.user.getId() == 0) {
			userDao.save(this.user);
			this.firePropertyChange(PROPERTY_SAVED, null, this.user);
		} else {
			userDao.update(this.user);
			this.firePropertyChange(PROPERTY_UPDATED, null, this.user);
		}
	}

	// ===================================================

	public void sendWalkingSpeedTrainingPosition()
			throws JSONException, JsonProcessingException, MqttPersistenceException, MqttException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		// send start position
		JSONObject json = new JSONObject();
		json.put("action", RecoToolMqttServer.MQTT_CMD_START_POSITION);
		json.put("value", new JSONObject(mapper.writeValueAsString(this.getWalkingTrainingPosition())));
		this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_CAVE_TOPIC);
	}

	// ===================================================

	public void startEvaluation() throws MqttPersistenceException, MqttException, JsonProcessingException {

		if (this.user == null || this.user.getId() == 0)
			return;

		this.prepareEvaluation(true);

		this.startClock();

		LOG.info("Evaluation started");

		if (!this.getMQTTClient().isConnected())
			return;

		// Send introduction for new station to data glasses
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			int id = (this.recommender.getMode().equals(Recommender.PROPERTY_ABIDANCE_MODE)) ? 6 : 9;
			SystemPrompt sP = this.prompts.stream().filter(p -> p.getID() == id).collect(Collectors.toList()).get(0);
			this.sendSystemPrompt(sP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Start evaluation countdown
	public void startClock() {
		
		this.timerRunning = true;

		this.evaluationTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {

				setting.setCurrentTime(new Date(setting.getCurrentTime().getTime() + 1000));
				firePropertyChange(PROPERTY_EVALUATION_DURATION, null,
						Duration.ofMillis(RecoTool.setting.getTimeToDeparture()));

				if (delayDuration.toMinutes() > 0 && !delayPromptSent && delayPromptTimer
						.toMillis() <= (evaluationDuration.toMillis() - setting.getTimeToDeparture())) {
					SystemPrompt sP = prompts.stream().filter(p -> p.getID() == 18).collect(Collectors.toList()).get(0);

					try {
						sendSystemPrompt(sP);
						delayPromptSent = true;
					} catch (JSONException | JsonProcessingException | MqttException e) {
						e.printStackTrace();
					}
				}

				if (setting.getTimeToDeparture() <= 0 && timerRunning || !timerRunning) {
					evaluationTimer.cancel();
					evaluationTimer.purge();

					timerRunning = false;
					evaluationTimer = new Timer();
					return;
				}
			}
		}, 0, 1000);
	}

	public static final String PROPERTY_EVALUATION_PREPARED = "evaluationPrepared";

	public void prepareEvaluation(boolean generateRecommendations)
			throws MqttPersistenceException, MqttException, JsonProcessingException {

		RecoTool.setting.setCurrentTime(new Date(this.startTime.getTimeInMillis()));
		RecoTool.setting.setTrafficJunction(this.startTrafficJunction);

		this.assignTrafficJunctionToRandomItems();

		this.setNewEvaluation(true);
		this.delayPromptSent = false;

		// send start position and simulated start time to CAVE subscriber
		if (this.getMQTTClient().isConnected()
				&& this.getMQTTClient().getSubscribedTopic().contains(RecoToolMqttServer.PROPERTY_CAVE_TOPIC)) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Include.NON_NULL);

				// send start position
				JSONObject json = new JSONObject();
				json.put("action", RecoToolMqttServer.MQTT_CMD_START_POSITION);
				json.put("value", new JSONObject(mapper.writeValueAsString(this.getStartPosition())));
				this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_CAVE_TOPIC);

				// send start time
				json = new JSONObject();
				json.put("action", RecoToolMqttServer.MQTT_CMD_SET_SIMULATED_TIME);
				json.put("value", new JSONObject().put("hour", this.startTime.get(Calendar.HOUR_OF_DAY)).put("minute",
						this.startTime.get(Calendar.MINUTE)));
				this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_CAVE_TOPIC);
			} catch (JSONException | JsonProcessingException e) {
				e.printStackTrace();
			}
		}

		if (this.getMQTTClient().isConnected()
				&& this.getMQTTClient().getSubscribedTopic().contains(RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC)) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Include.NON_NULL);

				// Reset Data-Glasses
				JSONObject json = new JSONObject();
				json.put("action", RecoToolMqttServer.MQTT_CMD_RESET);
				this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (generateRecommendations) {
				// Send welcome information for current traffic junction
				SystemPrompt prompt = this.prompts.stream().filter(p -> p.getID() == 5).collect(Collectors.toList())
						.get(0);
				this.sendSystemPrompt(prompt);
			}
		}

		if (this.timerRunning) {
			this.evaluationTimer.cancel();
			this.timerRunning = false;
			this.evaluationTimer = new Timer();
		}

		// Set evaluation duration
		Date time = new Date(RecoTool.setting.getCurrentTime().getTime() + this.evaluationDuration.toMillis());
		RecoTool.setting.withEstimatedDepartureTime(time).withCurrentDepartureTime(time);
		this.stopNavigation();

		// Calculate recommendation scores and send them via MQTT
		if (generateRecommendations) {
			this.recommender.resetRecommendations();
			this.generateRecommendations();
		}

		this.updateBySetting(user);

		this.firePropertyChange(PROPERTY_EVALUATION_PREPARED, null, null);
	}

	public void stopEvaluation(boolean clearSetting) {

		if (this.evaluationTimer != null) {
			this.evaluationTimer.cancel();
			this.evaluationTimer.purge();
			this.evaluationTimer = new Timer();
			this.timerRunning = false;
		}

		this.recommender.withTimerRunning(false);
		this.withNavigationDestination(null).withCurrentItem(null);

		// Reset evaluation timer
		RecoTool.setting.setCurrentTime(this.getStartTime().getTime());

		firePropertyChange(PROPERTY_EVALUATION_DURATION, null,
				Duration.ofMillis(RecoTool.setting.getTimeToDeparture()));

		// Save used items to *.csv file
		if (RecoTool.setting.getUsedItem().size() > 0) {
			String filename = "used-items-"
					+ this.getResultTracker().getNumOfAccuracyEvaluationsOfUser(this.getCurrentUser(), "used-items")
					+ ".csv";

			filename = this.getCurrentUser().getId() + "-" + filename;
			LinkedHashMap<Item, Setting> list = RecoTool.setting.getUsedItem();

			this.resultTracker.writeItemMapToCSV(list, filename);

			if (clearSetting)
				RecoTool.setting.getUsedItem().clear();
		}

		this.firePropertyChange(PROPERTY_RECOMMENDATIONS, null,
				this.recommender.updateBySetting(this.user, RecoTool.setting));
	}

	public boolean getEvaluationRunning() {
		return this.timerRunning;
	}

	// ===================================================

	public void startNavigation(Item item)
			throws JSONException, JsonProcessingException, MqttPersistenceException, MqttException {

		this.setNavigationDestination(item);

		SystemPrompt prompt = this.prompts.stream().filter(sP -> sP.getID() == 12).collect(Collectors.toList()).get(0);
		prompt = this.replacePlaceholders(prompt);

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		JSONObject json = new JSONObject();
		json.put("action", RecoToolMqttServer.MQTT_CMD_START_NAVIGATION);
		json.put("item", new JSONObject(mapper.writeValueAsString(item)));
		json.put("systemAlert", new JSONObject(mapper.writeValueAsString(prompt)));

		this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC,
				RecoToolMqttServer.PROPERTY_CAVE_TOPIC);

		// Send information for further recommendations
		SystemPrompt sP = this.prompts.stream().filter(p -> p.getID() == 12).collect(Collectors.toList()).get(0);
		this.sendSystemPrompt(sP);

		LOG.info("Navigation started");
	}

	public void startNavigationToLastDestination() {
		Item item = new Item().withName(this.getNextConnectionPositionIdentifier()).withGeoposition(this.endPosition);

		AttributeDAO ad = new AttributeDAO();
		Attribute attribute = ad.findById(74);

		item.withDomain(attribute);

		try {
			this.startNavigation(item);
		} catch (JSONException | JsonProcessingException | MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cancel current navigation
	 * 
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 * @throws JSONException
	 * @throws JsonProcessingException
	 */
	public void cancelNavigation()
			throws MqttPersistenceException, MqttException, JSONException, JsonProcessingException {

		SystemPrompt sP = this.prompts.stream().filter(p -> p.getID() == 15).collect(Collectors.toList()).get(0);
		sP = this.replacePlaceholders(sP);

		this.firePropertyChange(RecoTool.PROPERTY_NAVIGATION_FINISHED, this.getNavigationDestination(), null);

		this.setNavigationDestination(null);

		// Send commands to clients
		if (!this.getMQTTClient().isConnected())
			return;

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		JSONObject json = new JSONObject();
		json.put("action", RecoToolMqttServer.MQTT_CMD_CANCEL_NAVIGATION);
		json.put("systemAlert", new JSONObject(mapper.writeValueAsString(sP)));

		this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC,
				RecoToolMqttServer.PROPERTY_CAVE_TOPIC);

		LOG.info("Navigation canceled");
	}

	/**
	 * Stop current Navigation
	 * 
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */
	public void stopNavigation() throws MqttPersistenceException, MqttException {

		this.firePropertyChange(RecoTool.PROPERTY_NAVIGATION_FINISHED, this.getNavigationDestination(), null);
		this.setNavigationDestination(null);

		if (!this.getMQTTClient().isConnected())
			return;

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		JSONObject json = new JSONObject();
		json.put("action", RecoToolMqttServer.MQTT_CMD_STOP_NAVIGATION);

		this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC,
				RecoToolMqttServer.PROPERTY_CAVE_TOPIC);

		LOG.info("Navigation stopped");
	}

	/**
	 * Requested route calculated notification
	 */
	public static final String PROPERTY_ROUTE_CALCULATED = "routeCalculated";

	public void calculatedRoute(JSONObject jsonObj) {

		ObjectMapper objectMapper = new ObjectMapper();

		try {
			Item item = null;
			if (jsonObj != null)
				item = objectMapper.readValue(jsonObj.toString(), Item.class);

			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			JSONObject json = new JSONObject();
			json.put("action", RecoToolMqttServer.MQTT_CMD_CALCULATED_ROUTE);

			SystemPrompt sP = null;

			if (item != null) {
				json.put("item", new JSONObject(mapper.writeValueAsString(item)));
				sP = this.prompts.stream().filter(p -> p.getID() == 13).collect(Collectors.toList()).get(0);
			} else {
				sP = this.prompts.stream().filter(p -> p.getID() == 14).collect(Collectors.toList()).get(0);
			}

			json.put("systemAlert", new JSONObject(mapper.writeValueAsString(this.replacePlaceholders(sP))));
			this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

			if (item == null)
				this.setNavigationDestination(item);

			this.firePropertyChange(PROPERTY_ROUTE_CALCULATED, null, item);

		} catch (IOException | MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called after a navigation is finished. Automatically marks current
	 * destination as used.
	 */

	public static final String PROPERTY_NAVIGATION_FINISHED = "navigationFinished";

	public void finishedNavigation(JSONObject jsonObj) {

		ObjectMapper mapper = new ObjectMapper();

		Item item = null;

		try {
			item = mapper.readValue(jsonObj.toString(), Item.class);
			mapper.setSerializationInclusion(Include.NON_NULL);

			JSONObject json = new JSONObject();
			json.put("action", RecoToolMqttServer.MQTT_CMD_FINISHED_NAVIGATION);
			json.put("item", jsonObj);
			this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

			if (item.getId() == 0 && this.recommender.getMode().equals(Recommender.PROPERTY_EFFICIENCY_MODE)) {
				// Send information that evaluation is done
				SystemPrompt sP1 = this.prompts.stream().filter(p -> p.getID() == 10).collect(Collectors.toList())
						.get(0);
				sP1 = this.replacePlaceholders(sP1);

				SystemPrompt sP2 = this.prompts.stream().filter(p -> p.getID() == 11).collect(Collectors.toList())
						.get(0);
				sP2 = this.replacePlaceholders(sP2);

				this.sendSystemPrompt(sP1, sP2);

				this.stopEvaluation(false);
			} else if (this.recommender.getMode().equals(Recommender.PROPERTY_ABIDANCE_MODE)) {

				SystemPrompt sP = null;

				sP = this.prompts.stream().filter(p -> p.getID() == 16).collect(Collectors.toList()).get(0);
				this.sendSystemPrompt(this.replacePlaceholders(sP));

				if (item.getId() > 0) {
					// Send information for further recommendations
					sP = this.prompts.stream().filter(p -> p.getID() == 8).collect(Collectors.toList()).get(0);
					this.sendSystemPrompt(this.replacePlaceholders(sP));

					System.out.println(this.replacePlaceholders(sP).getMessage());
				}

				this.itemUsed();
			}
		} catch (IOException | MqttException e) {
			e.printStackTrace();
		}

		// Set current navigation Destination to null, if reached item is equal to
		// current navigationDestination
		if (this.navigationDestination != null && item != null && this.navigationDestination.getId() == item.getId())
			this.setNavigationDestination(null);

		this.firePropertyChange(PROPERTY_NAVIGATION_FINISHED, null, item);
	}

	/**
	 * Called to update current user Position
	 * 
	 * @param jsonObj
	 */

	public void updateUserPosition(JSONObject jsonObj) {

		ObjectMapper objectMapper = new ObjectMapper();

		try {
			Geoposition pos = objectMapper.readValue(jsonObj.toString(), Geoposition.class);
			RecoTool.setting.setGeoposition(pos);

			// calculate movement speed
			this.walkingSpeedCalculator.withPosition(pos);
			this.user.setCurrentWalkingSpeed(this.walkingSpeedCalculator.getCurrentWalkingSpeed());

			// Only update recommendations, if navigation is not started
			// and no Item is selected
			if (this.getEvaluationRunning() && this.navigationDestination == null && this.currentItem == null)
				this.updateBySetting(this.user);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called to mark a selected item as used. Simulated time is updated an updated
	 * recommendations are sent to dataglasses.
	 */

	public static final String PROPERTY_ITEM_USAGE_DONE = "itemUsageDone";

	public void itemUsed() {

		if (this.getCurrentItem() == null)
			return;

		Item usedItem = this.getCurrentItem();

		this.getSetting().withUsedItem(usedItem);
		this.withCurrentItem(null);

		// Save current relevance scores to file
		this.resultTracker.writeToCSV(this.getRecommender().getRecommendations(), this.getCurrentUser(),
				RecoTool.setting);

		Date timeOfUsage = null;

		// Check whether optimized usage duration is used or estimated average usage
		// duration
		if (numOfItemsToUse > 0) {
			long additionalTime = this.getOptimizedItemUsageDuration();
			timeOfUsage = new Date(RecoTool.setting.getCurrentTime().getTime() + additionalTime);
		} else {
			timeOfUsage = new Date(
					RecoTool.setting.getCurrentTime().getTime() + usedItem.getEstimatedUsageDuration().toMillis());
		}

		RecoTool.setting.setCurrentTime(timeOfUsage);
		this.setNewEvaluation(false);

		// Send current state of used item to view controller
		int idx = (this.recommender.getRecommendations().size() == 0) ? 0
				: new ArrayList<Item>(this.recommender.getRecommendations().keySet()).indexOf(usedItem) + 1;
		double score = this.recommender.getRecommendations().get(usedItem);

		this.firePropertyChange(PROPERTY_ITEM_USAGE_DONE, null, new UsedItem(idx, score, usedItem));

		this.updateBySetting(this.user);

		if (!this.getMQTTClient().getClient().isConnected())
			return;

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			// update data glasses with new recommendations
			JSONObject json = new JSONObject();
			json.put("action", RecoToolMqttServer.MQTT_CMD_RECOMMENDATIONS);

			JSONArray arr = new JSONArray();
			for (Iterator<Item> it = this.getRecommender().getRecommendations().keySet().iterator(); it.hasNext();) {
				Item item = it.next();
				arr.put(new JSONObject(mapper.writeValueAsString(item)));
			}

			json.put("items", arr);
			this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

			// update simulated time in CAVE
			json = new JSONObject();
			json.put("action", RecoToolMqttServer.MQTT_CMD_SET_SIMULATED_TIME);

			// TODO: Timezone
			json.put("value",
					new JSONObject()
							.put("hour", 1 + (RecoTool.setting.getCurrentTime().getTime() / (60 * 60 * 1000)) % 24)
							.put("minute", (RecoTool.setting.getCurrentTime().getTime() / (60 * 1000)) % 60));

			this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_CAVE_TOPIC);

			// Send system prompt for further instructions to data glasses
			if (this.recommender.getMode().equals(Recommender.PROPERTY_ABIDANCE_MODE)) {
				SystemPrompt sP = this.prompts.stream().filter(p -> p.getID() == 6).collect(Collectors.toList()).get(0);
				this.sendSystemPrompt(sP);
			}

		} catch (MqttException | JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	// ===================================================

	public static final String PROPERTY_SWITCH_MODE = "switchMode";

	public void switchedRecommendationMode(String oldValue, String value)
			throws MqttPersistenceException, MqttException {

		LOG.info("Recommendation mode changed to " + value);

		if (Recommender.PROPERTY_ABIDANCE_MODE.compareTo(value) == 0) {
			try {
				this.cancelNavigation();
				this.updateBySetting(user);
			} catch (JSONException | JsonProcessingException e) {
				e.printStackTrace();
			}
		}

		this.firePropertyChange(PROPERTY_SWITCH_MODE, oldValue, value);

		if (!this.server.getClient().isConnected())
			return;

		// Send message that route to final train is calculated
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {

			JSONObject json = new JSONObject();
			json.put("action", RecoToolMqttServer.MQTT_CMD_SWITCHED_RECOMMENDATION_MODE);
			json.put("value", value);

			if (Recommender.PROPERTY_EFFICIENCY_MODE.equals(value)) {
				// Temporary item with Geocoordinates
				Item tmpItem = new Item().withName(this.getNextConnectionPositionIdentifier())
						.withGeoposition(this.endPosition);

				AttributeDAO ad = new AttributeDAO();
				Attribute attribute = ad.findById(74);

				tmpItem.withDomain(attribute);

				this.setNavigationDestination(tmpItem);

				json.put("item", new JSONObject(mapper.writeValueAsString(tmpItem)));

				SystemPrompt sP = this.prompts.stream().filter(p -> p.getID() == 9).collect(Collectors.toList()).get(0);
				json.put("systemAlert", new JSONObject(mapper.writeValueAsString(this.replacePlaceholders(sP))));

			} else if (Recommender.PROPERTY_EFFICIENCY_MODE.equals(oldValue)) {
				SystemPrompt sP = this.prompts.stream().filter(p -> p.getID() == 6).collect(Collectors.toList()).get(0);
				this.sendSystemPrompt(sP);
			}

			this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC,
					RecoToolMqttServer.PROPERTY_CAVE_TOPIC);

		} catch (JSONException | JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public void sendSystemPrompt(SystemPrompt... values)
			throws JSONException, JsonProcessingException, MqttPersistenceException, MqttException {

		for (SystemPrompt value : values) {
			// Replace placeholders
			value = this.replacePlaceholders(value);

			// Check for evaluation duration changes
			int seconds = value.analyzeSystemPromptMessage();
			boolean checkMode = false;
			if (seconds != 0 && RecoTool.setting.getCurrentDepartureTime() != null) {
				RecoTool.setting.setCurrentDepartureTime(
						new Date(RecoTool.setting.getCurrentDepartureTime().getTime() + seconds * 1000));

				checkMode = true;
			}

			if (!this.getMQTTClient().isConnected()) {
				LOG.info("No connection to broker");
				this.recommender.checkMode(RecoTool.setting);
				return;
			}

			// Send as JSON
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			JSONObject json = new JSONObject();
			json.put("action", RecoToolMqttServer.MQTT_CMD_SYSTEM_ALERT);
			json.put("systemAlert", new JSONObject(mapper.writeValueAsString(value)));

			this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

			if (checkMode) {
				this.recommender.checkMode(RecoTool.setting);
				this.updateBySetting(this.user);
			}
		}

	}

	private SystemPrompt replacePlaceholders(SystemPrompt sp) {
		SystemPrompt prompt = new SystemPrompt().withID(sp.getID()).withMessage(sp.getMessage()).withMode(sp.getMode());

		String msg = prompt.getMessage().replace("%duration%",
				"" + (Duration.ofMillis(RecoTool.setting.getTimeToDeparture()).toMinutes() + 1));

		if (this.getCurrentItem() != null) {
			msg = msg.replace("%item%", this.getCurrentItem().getName());

			long min = 0;

			if (this.getNumOfItemsToUse() == 0) {
				min = this.getCurrentItem().getEstimatedUsageDuration().toMinutes();
			} else {
				min = Duration.ofMillis(this.getOptimizedItemUsageDuration()).toMinutes();
			}

			msg = msg.replace("%min%", min + "");
		}

		if (this.getCurrentUser() != null && this.getCurrentUser().getId() > 0) {
			msg = msg.replace("%firstname%", this.getCurrentUser().getFirstname());
			msg = msg.replace("%lastname%", this.getCurrentUser().getLastname());
		}

		if (this.getStartTrafficJunction() != null && this.getStartTrafficJunction().getName() != null) {
			msg = msg.replace("%startTrafficJunction%", this.getStartTrafficJunction().getName());
		}

		if (this.getEndTrafficJunction() != null && this.getEndTrafficJunction().getName() != null) {
			msg = msg.replace("%endTrafficJunction%", this.getEndTrafficJunction().getName());
		}

		if (this.getNavigationDestination() != null && this.getNavigationDestination().getName() != null) {
			msg = msg.replace("%navigationDestination%", this.getNavigationDestination().getName());

			long min = 0;

			if (this.getNumOfItemsToUse() == 0) {
				min = this.getCurrentItem().getEstimatedUsageDuration().toMinutes();
			} else {
				min = Duration.ofMillis(this.getOptimizedItemUsageDuration()).toMinutes();
			}

			msg = msg.replace("%min%", min + "");
		}

		if (this.getNextConnectionPositionIdentifier() != null) {
			msg = msg.replace("%nextConnectionPosition%", this.getNextConnectionPositionIdentifier());
		}

		if (this.getNextConnectionPositionIdentifier() != null) {
			DateFormat df = new SimpleDateFormat("HH:mm");
			df.setTimeZone(TimeZone.getDefault());

			msg = msg.replace("%currentTime%", df.format(RecoTool.setting.getCurrentTime()));
		}

		msg = msg.replace("%delayDuration%", this.getDelayDuration().toMinutes() + "");
		msg = msg.replace("%mode%", this.getRecommender().getMode());

		prompt.setMessage(msg);

		return prompt;
	}

	public void clearSystemPromptQueue()
			throws JSONException, JsonProcessingException, MqttPersistenceException, MqttException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		JSONObject json = new JSONObject();
		json.put("action", RecoToolMqttServer.MQTT_CMD_CLEAR_SYSTEM_PROMPT_QUEUE);

		this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);
	}

	// ===================================================

	public static final String PROPERTY_CURRENT_USER = "currentUser";

	public User getCurrentUser() {
		return this.user;
	}

	public void setCurrentUser(User value) {
		if (this.user == null || !this.user.equals(value)) {
			User oldValue = this.user;
			this.user = value;

			firePropertyChange(PROPERTY_CURRENT_USER, oldValue, value);
		}
	}

	public RecoTool withCurrentUser(User value) {
		setCurrentUser(value);
		return this;
	}

	// ===================================================

	public static final String PROPERTY_VERSION = "version";

	public long getVersion() {
		return this.version;
	}

	public void setVersion(long value) {
		if (this.version != value) {
			long oldValue = this.version;
			this.version = value;

			firePropertyChange(PROPERTY_VERSION, oldValue, value);
		}
	}

	public RecoTool withVersion(long value) {
		setVersion(value);
		return this;
	}

	// ===================================================

	public Recommender getRecommender() {
		return this.recommender;
	}

	public void setRecommender(Recommender value) {
		this.recommender = value;
	}

	public RecoTool withRecommender(Recommender value) {
		setRecommender(value);
		return this;
	}

	// ===================================================

	public static final String PROPERTY_TRAINER = "trainer";

	public Trainer getTrainer() {
		return this.trainer;
	}

	public void setTrainer(Trainer value) {
		if (this.trainer == null || !this.trainer.equals(value)) {
			User oldValue = this.user;
			this.trainer = value;

			firePropertyChange(PROPERTY_TRAINER, oldValue, value);
		}
	}

	public RecoTool withTrainer(Trainer value) {
		setTrainer(value);
		return this;
	}

	// ===================================================

	public List<Item> getItems() {
		return this.items;
	}

	public void setItems(Item... values) {

		if (this.items == null)
			this.items = new ArrayList<Item>();

		for (Item item : values) {
			if (!this.items.contains(item)) {
				this.items.add(item);
			}
		}
	}

	public RecoTool withItems(Item... values) {
		this.setItems(values);
		return this;
	}

	public List<Item> retreiveItemsFromDB() {
		this.items = itemDao.findAll();
		return this.items;
	}

	// ===================================================

	public Setting getSetting() {
		return RecoTool.setting;
	}

	public void setSetting(Setting value) {
		RecoTool.setting = value;
	}

	public RecoTool withSetting(Setting value) {
		this.setSetting(value);
		return this;
	}

	// ===================================================

	public static String PROPERTY_CURRENT_ITEM = "currentItem";

	public Item getCurrentItem() {
		return currentItem;
	}

	public void setCurrentItem(Item value) {
		if (this.currentItem == null || !this.currentItem.equals(value)) {
			Item oldValue = this.currentItem;
			this.currentItem = value;

			this.firePropertyChange(PROPERTY_CURRENT_ITEM, oldValue, value);
		}
	}

	public RecoTool withCurrentItem(Item value) {
		this.setCurrentItem(value);
		return this;
	}

	// ===================================================

	public static String PROPERTY_START_TIME = "startTime";

	public Calendar getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Calendar value) {
		if (this.startTime != value) {
			this.startTime = value;
			this.firePropertyChange(PROPERTY_START_TIME, null, value);
		}
	}

	public RecoTool withStartTime(Calendar value) {
		this.setStartTime(value);
		return this;
	}

	// ===================================================

	public static String PROPERTY_EVALUATION_DURATION = "evaluationDuration";

	public Duration getEvaluationDuration() {
		return this.evaluationDuration;
	}

	public void setEvaluationDuration(Duration value) {
		if (this.evaluationDuration != value) {
			this.evaluationDuration = value;
			this.firePropertyChange(PROPERTY_EVALUATION_DURATION, null, value);
		}
	}

	public RecoTool withEvaluationDuration(Duration value) {
		this.setEvaluationDuration(value);
		return this;
	}

	/**
	 * Calculate usage duration for items. Used when numOfItemsToUse > 0.
	 * 
	 * @return
	 */

	public long getOptimizedItemUsageDuration() {
		long tUsage = 0;

		if (this.newEvaluation || RecoTool.setting.getUsedItem().size() % this.numOfItemsToUse != 0) {

			long evaluationTime = 300000;

			if (this.delayDuration.toMillis() == 0 || this.delayDuration.toMillis() > 0 && this.delayPromptSent) {
				evaluationTime = RecoTool.setting.getTimeToDeparture();
			} else {
				evaluationTime = this.evaluationDuration.toMillis();
			}

			tUsage = (long) ((evaluationTime - this.recommender.getRequiredTimeToReachDestination(RecoTool.setting)
					- 110000)
					/ (this.numOfItemsToUse - (RecoTool.setting.getUsedItem().size() % this.getNumOfItemsToUse())));

		} else {
			tUsage = setting.getTimeToDeparture();

			double distance = 0;

			if (this.useGeocoordinates)
				distance = setting.getGeoposition().distance(this.endPosition);
			else
				distance = setting.getGeoposition().euclideanDistance(this.endPosition);

			tUsage -= distance * 3600 * 1000 / this.user.getMinWalkingSpeed();
		}

		return tUsage;
	}

	// ===================================================

	public boolean getUseGeocoordinates() {
		return this.useGeocoordinates;
	}

	public void setUseGeocoordinates(boolean value) {
		this.useGeocoordinates = value;
	}

	public RecoTool withUseGeocoordinates(boolean value) {
		this.setUseGeocoordinates(value);
		return this;
	}

	// ===================================================

	public int getMaxNumOfItems() {
		return this.maxNumOfItems;
	}

	public void setMaxNumOfItems(int value) {
		this.maxNumOfItems = value;
	}

	public RecoTool withMaxNumOfItems(int value) {
		this.setMaxNumOfItems(value);
		return this;
	}

	// ===================================================

	public int getMaxNumOfProductivityItems() {
		return this.maxNumOfProductivityItems;
	}

	public void setMaxNumOfProductivityItems(int value) {
		this.maxNumOfProductivityItems = value;
	}

	public RecoTool withMaxNumOfProductivityItems(int value) {
		this.setMaxNumOfProductivityItems(value);
		return this;
	}

	// ===================================================

	public int getNumOfItemsToUse() {
		return this.numOfItemsToUse;
	}

	public void setNumOfItemsToUse(int value) {
		this.numOfItemsToUse = value;
	}

	public RecoTool withNumOfItemsToUse(int value) {
		this.setNumOfItemsToUse(value);
		return this;
	}

	// =========================================

	public Item getNavigationDestination() {
		return this.navigationDestination;
	}

	public void setNavigationDestination(Item value) {
		this.navigationDestination = value;
	}

	public RecoTool withNavigationDestination(Item value) {
		this.setNavigationDestination(value);
		return this;
	}

	// =========================================

	public String getNextConnectionPositionIdentifier() {
		return this.nextConnectionPositionIdentifier;
	}

	public void setNextConnectionPosition(String value) {
		this.nextConnectionPositionIdentifier = value;
	}

	public RecoTool withNextConnectionPosition(String value) {
		this.setNextConnectionPosition(value);
		return this;
	}

	// =========================================

	public boolean getRandomizeItemGeoposition() {
		return this.randomizeItemGeoposition;
	}

	public void setRandomizeItemGeoposition(boolean value) {
		this.randomizeItemGeoposition = value;
	}

	public RecoTool withRandomizeItemGeoposition(boolean value) {
		this.setRandomizeItemGeoposition(value);
		return this;
	}

	// =========================================

	public TrafficJunction getStartTrafficJunction() {
		return this.startTrafficJunction;
	}

	public void setStartTrafficJunction(TrafficJunction value) {
		this.startTrafficJunction = value;
	}

	public RecoTool withStartTrafficJunction(TrafficJunction value) {
		this.setStartTrafficJunction(value);
		return this;
	}

	// =========================================

	public Geoposition getWalkingTrainingPosition() {
		return this.walkingTrainingPosition;
	}

	public void setWalkingTrainingPosition(Geoposition value) {
		this.walkingTrainingPosition = value;
	}

	public RecoTool withWalkingTrainingPosition(Geoposition value) {
		this.setWalkingTrainingPosition(value);
		return this;
	}

	// =========================================

	public Geoposition getStartPosition() {
		return this.startPosition;
	}

	public void setStartPosition(Geoposition value) {
		this.startPosition = value;
	}

	public RecoTool withStartPosition(Geoposition value) {
		this.setStartPosition(value);
		return this;
	}

	// =========================================

	public TrafficJunction getEndTrafficJunction() {
		return this.endTrafficJunction;
	}

	public void setEndTrafficJunction(TrafficJunction value) {
		this.endTrafficJunction = value;
	}

	public RecoTool withEndTrafficJunction(TrafficJunction value) {
		this.setEndTrafficJunction(value);
		return this;
	}

	// =========================================

	public Geoposition getEndPosition() {
		return this.endPosition;
	}

	public void setEndPosition(Geoposition value) {
		this.endPosition = value;
	}

	public RecoTool withEndPosition(Geoposition value) {
		this.setEndPosition(value);
		return this;
	}

	// =========================================

	public WalkingSpeedCalculator getWalkingSpeedCalculator() {
		return this.walkingSpeedCalculator;
	}

	public void setWalkingSpeedCalculator(WalkingSpeedCalculator value) {
		this.walkingSpeedCalculator = value;
	}

	public RecoTool withWalkingSpeedCalculator(WalkingSpeedCalculator value) {
		this.setWalkingSpeedCalculator(value);
		return this;
	}

	// =========================================

	public Duration getDelayDuration() {
		if (this.delayDuration == null)
			this.delayDuration = Duration.ofMinutes(0);

		return this.delayDuration;
	}

	public void setDelayDuration(Duration value) {
		this.delayDuration = value;
	}

	public RecoTool withDelayDuration(Duration value) {
		this.setDelayDuration(value);
		return this;
	}

	// =========================================

	public Duration getDelayPromptTimer() {
		if (this.delayPromptTimer == null)
			this.delayPromptTimer = Duration.ofMinutes(0);

		return this.delayPromptTimer;
	}

	public void setDelayPromptTimer(Duration value) {
		this.delayPromptTimer = value;
	}

	public RecoTool withDelayPromptTimer(Duration value) {
		this.setDelayPromptTimer(value);
		return this;
	}

	// =========================================

	public static String PROPERTY_HIDE_ALL = "hideAll";

	public boolean getHideAll() {
		return this.hideAll;
	}

	public void setHideAll(boolean value) {
		if (value != this.hideAll) {
			this.hideAll = value;
			this.firePropertyChange(PROPERTY_HIDE_ALL, !value, value);
		}
	}

	public RecoTool withHideAll(boolean value) {
		this.setHideAll(value);
		return this;
	}

	// =========================================

	public boolean getNewEvaluation() {
		return this.newEvaluation;
	}

	public void setNewEvaluation(boolean value) {
		this.newEvaluation = value;
	}

	public RecoTool withNewEvaluation(boolean value) {
		this.setNewEvaluation(value);
		return this;
	}

	// =========================================

	public ResultTracker getResultTracker() {
		return this.resultTracker;
	}

	public void setResultTracker(ResultTracker value) {
		if (value != this.resultTracker) {
			this.resultTracker = value;
		}
	}

	public RecoTool withResultTracker(ResultTracker value) {
		this.setResultTracker(value);
		return this;
	}

	// =========================================

	public boolean getRealtimeUserPositionUpdateAccuracyEvaluationMap() {
		return this.realtimeUserPositionUpdateAccuracyEvaluationMap;
	}

	public void setRealtimeUserPositionUpdateAccuracyEvaluationMap(boolean value) {
		this.realtimeUserPositionUpdateAccuracyEvaluationMap = value;
	}

	public RecoTool withRealtimeUserPositionUpdateAccuracyEvaluationMap(boolean value) {
		this.setRealtimeUserPositionUpdateAccuracyEvaluationMap(value);
		return this;
	}

	/**
	 * Network Configurations
	 */

	public RecoToolMqttServer getMQTTClient() {
		return this.server;
	}

	public void initializeMQTT() {
		try {
			if (this.server.isConnected())
				this.server.disonnect();

			if (this.server.connect()) {
				this.networkThread = new Thread(this.server);
				this.networkThread.start();
			}

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void disconnectMQTT() {
		try {
			if (this.server.isConnected()) {

				JSONObject json = new JSONObject();
				json.put("action", RecoToolMqttServer.MQTT_CMD_QUIT);

				this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_CAVE_TOPIC,
						RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

				this.server.disonnect();
			}

		} catch (MqttException e) {
			e.printStackTrace();
		}
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

	/**
	 * UsedItem Class is used to Communicate with WoZ-Controller
	 * 
	 * @author mac
	 *
	 */

	public class UsedItem {
		private int index;
		private double score;
		private Item item;

		UsedItem(int index, double score, Item item) {
			this.index = index;
			this.score = score;
			this.item = item;
		}

		public int getIndex() {
			return this.index;
		}

		public double getScore() {
			return this.score;
		}

		public Item getItem() {
			return this.item;
		}
	}
}
