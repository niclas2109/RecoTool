package com.doccuty.radarplus.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	private boolean randomizeItemGeoposition;

	private boolean useGeocoordinates;
	private boolean realtimeUserPositionUpdateAccuracyEvaluationMap;

	private boolean hideAll;

	Timer evaluationTimer;
	boolean timerRunning;

	Item navigationDestination;

	String nextConnectionPosition;

	private TrafficJunction startTrafficJunction;
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

	List<SystemPrompt> prompts;

	public RecoTool() {

		this.user = new User();
		RecoTool.setting = new Setting();

		this.recommender = new Recommender().withStudyApp(this);
		this.trainer = new Trainer().withStudyApp(this).withUser(this.user);

		this.evaluationTimer = new Timer();
		this.timerRunning = false;

		this.server = new RecoToolMqttServer(this);

		this.startTime = Calendar.getInstance();
		this.startPosition = new Geoposition();
		this.endPosition = new Geoposition();

		this.walkingTrainingPosition = new Geoposition();

		this.walkingSpeedCalculator = new WalkingSpeedCalculator(this);

		this.hideAll = true;

		this.resultTracker = new ResultTracker();
	}

	public void init() {
		try {
			// Set setting parameters
			ObjectMapper mapper = new ObjectMapper();
			File from = new File(getClass().getClassLoader().getResource("settings/appSettings.json").getPath());

			JsonNode json = mapper.readTree(from);

			long version = json.get("version").asLong();
			boolean randomize = json.get("randomizeItemGeoposition").asBoolean();
			boolean useGeocoordinates = json.get("useGeocoordinates").asBoolean();

			int maxNumOfItems = json.get("maxNumOfItems").asInt();

			int maxNumOfProductivityItems = json.get("maxNumOfProductivityItems").asInt();

			if (json.has("startTime")) {
				startTime.setTimeInMillis(json.get("startTime").asLong());
			}

			int evaluationDuration = json.get("evaluationDuration").asInt();

			if (json.has("nextConnectionPosition"))
				this.nextConnectionPosition = json.get("nextConnectionPosition").asText();

			if (json.has("startTrafficJunction"))
				this.startTrafficJunction = mapper.treeToValue(json.get("startTrafficJunction"), TrafficJunction.class);

			if (json.has("endTrafficJunction"))
				this.endTrafficJunction = mapper.treeToValue(json.get("endTrafficJunction"), TrafficJunction.class);

			this.startPosition.withLatitude(json.get("startPosition").get("latitude").asDouble())
					.withLongitude(json.get("startPosition").get("longitude").asDouble());

			this.endPosition.withLatitude(json.get("endPosition").get("latitude").asDouble())
					.withLongitude(json.get("endPosition").get("longitude").asDouble());

			this.walkingTrainingPosition.withLatitude(json.get("walkingTrainingPosition").get("latitude").asDouble())
					.withLongitude(json.get("walkingTrainingPosition").get("longitude").asDouble());

			RecoTool.setting.withGeoposition(this.startPosition).setNextDeparture(this.endPosition);

			this.withVersion(version).withRandomizeItemGeoposition(randomize).withMaxNumOfItems(maxNumOfItems)
					.withMaxNumOfProductivityItems(maxNumOfProductivityItems)
					.withEvaluationDuration(Duration.ofMinutes(evaluationDuration))
					.withUseGeocoordinates(useGeocoordinates).withStartTime(startTime);

			double timeMaximizer = json.get("timeMaximizer").asDouble();
			this.recommender.getContextBasedPostFilter().setTimeMaximizer(timeMaximizer);

			if (json.has("realtimeUserPositionUpdateAccuracyEvaluationMap")) {
				this.realtimeUserPositionUpdateAccuracyEvaluationMap = json
						.get("realtimeUserPositionUpdateAccuracyEvaluationMap").asBoolean();
			} else {
				this.realtimeUserPositionUpdateAccuracyEvaluationMap = false;
			}

			if (json.has("serendipityEnabled"))
				this.recommender.setSerendipityEnabled(json.get("serendipityEnabled").asBoolean());

			if (json.has("weightingEnabled"))
				this.recommender.setWeightingEnabled(json.get("weightingEnabled").asBoolean());

			String brokerIP = json.get("brokerIP").asText();
			String brokerPort = json.get("brokerPort").asText();

			this.server.withBrokerURI(brokerIP).withBrokerPort(brokerPort);

			// Load system prompt

			this.prompts = new ArrayList<SystemPrompt>();

			from = new File(getClass().getClassLoader().getResource("settings/systemPromptMessages.json").getPath());
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
			while (arr.length() <= oldLength + this.maxNumOfProductivityItems && s.size() <= productivityItems.size()) {
				
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

		while (usedRandomNumbers.size() < this.maxNumOfItems) {
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
	 * Get different and randomized Coordinates for selected Items geoCoordinates is
	 * a list of additional Geoposition objects
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

		Random rand = new Random();

		for (Iterator<Item> it = list.iterator(); it.hasNext();) {
			Item item = it.next();

			if (geoCoordinates.size() == 0)
				break;

			int idx = rand.nextInt(geoCoordinates.size());

			item.setGeoposition(geoCoordinates.get(idx));
			geoCoordinates.remove(idx);
		}

		LOG.info("Shuffled item geoposition");

		return list;
	}

	// ===================================================

	public LinkedHashMap<Item, Double> updateBySetting(User user) {

		// Pointer zeigen auf gleiches obj... vllt echte kopie machen
		LinkedHashMap<Item, Double> oldRecommendations = this.recommender.getRecommendations();
		LinkedHashMap<Item, Double> recommendations = this.recommender.updateBySetting(user, RecoTool.setting);

		// Check of old and new order of recommendations to minimize network traffic
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

			// calculate new score
			double rating = this.recommender.getOriginalRecommendations().get(e.getKey());

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

		RecoTool.setting.setCurrentTime(new Date(this.startTime.getTimeInMillis()));

		this.prepareEvaluation(true);

		// Start evaluation countdown
		this.timerRunning = true;

		this.evaluationTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {

				setting.setCurrentTime(new Date(setting.getCurrentTime().getTime() + 1000));

				firePropertyChange(PROPERTY_EVALUATION_DURATION, null,
						Duration.ofMillis(RecoTool.setting.getTimeToDeparture()));

				if (setting.getTimeToDeparture() <= 0 && timerRunning || !timerRunning) {
					evaluationTimer.cancel();
					timerRunning = false;
					evaluationTimer = new Timer();
					return;
				}

				timerRunning = true;
			}
		}, 0, 1000);

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

	public static final String PROPERTY_EVALUATION_PREPARED = "evaluationPrepared";

	public void prepareEvaluation(boolean generateRecommendations)
			throws MqttPersistenceException, MqttException, JsonProcessingException {

		RecoTool.setting.setTrafficJunction(this.startTrafficJunction);

		this.assignTrafficJunctionToRandomItems();

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

		this.recommender.setMode(Recommender.PROPERTY_ABIDANCE_MODE);
		this.switchedRecommendationMode(null, Recommender.PROPERTY_ABIDANCE_MODE);

		// Set evaluation duration
		Date time = new Date(RecoTool.setting.getCurrentTime().getTime() + this.evaluationDuration.toMillis());
		RecoTool.setting.withEstimatedDepartureTime(time).withCurrentDepartureTime(time);

		// Calculate recommendation scores and send them via MQTT
		if (generateRecommendations)
			this.generateRecommendations();

		this.firePropertyChange(PROPERTY_EVALUATION_PREPARED, null, null);
	}

	public void stopEvaluation(boolean clearSetting) {
		this.timerRunning = false;

		this.withNavigationDestination(null).withCurrentItem(null);

		RecoTool.setting.setEstimatedDepartureTime(null);
		this.recommender.withTimerRunning(false).resetRecommendations();

		if (clearSetting)
			RecoTool.setting.getUsedItem().clear();

		this.firePropertyChange(PROPERTY_RECOMMENDATIONS, null, this.recommender.getRecommendations());
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
		Item item = new Item().withName(this.getNextConnectionPosition()).withGeoposition(this.endPosition);

		AttributeDAO ad = new AttributeDAO();
		Attribute attribute = ad.findById(74);

		item.withDomain(attribute);

		try {
			this.startNavigation(item);
		} catch (JSONException | JsonProcessingException | MqttException e) {
			e.printStackTrace();
		}
	}

	public void cancelNavigation()
			throws MqttPersistenceException, MqttException, JSONException, JsonProcessingException {

		SystemPrompt sP = this.prompts.stream().filter(p -> p.getID() == 15).collect(Collectors.toList()).get(0);
		sP = this.replacePlaceholders(sP);

		this.setNavigationDestination(null);

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		JSONObject json = new JSONObject();
		json.put("action", RecoToolMqttServer.MQTT_CMD_CANCEL_NAVIGATION);
		json.put("systemAlert", new JSONObject(mapper.writeValueAsString(sP)));

		this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC,
				RecoToolMqttServer.PROPERTY_CAVE_TOPIC);

		LOG.info("Navigation canceled");
	}

	public void stopNavigation() throws MqttPersistenceException, MqttException {

		this.setNavigationDestination(null);

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		JSONObject json = new JSONObject();
		json.put("action", RecoToolMqttServer.MQTT_CMD_STOP_NAVIGATION);

		this.server.send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC,
				RecoToolMqttServer.PROPERTY_CAVE_TOPIC);

		LOG.info("Navigation stopped");
	}

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

				RecoTool.setting.setCurrentDepartureTime(new Date(this.startTime.getTimeInMillis()));

			} else if (this.recommender.getMode().equals(Recommender.PROPERTY_ABIDANCE_MODE)) {

				SystemPrompt sP = null;

				if (item.getId() > 0) {
					// Send information for further recommendations
					sP = this.prompts.stream().filter(p -> p.getID() == 8).collect(Collectors.toList()).get(0);
				} else {
					sP = this.prompts.stream().filter(p -> p.getID() == 16).collect(Collectors.toList()).get(0);
				}

				this.sendSystemPrompt(this.replacePlaceholders(sP));
				this.itemUsed();
			}

			LOG.info("Navigation finished");
		} catch (IOException | MqttException e) {
			e.printStackTrace();
		}

		this.firePropertyChange(PROPERTY_NAVIGATION_FINISHED, null, item);

		if (this.navigationDestination != null && item != null && this.navigationDestination.getId() == item.getId())
			this.setNavigationDestination(null);
	}

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

	// ===================================================

	public static final String PROPERTY_ITEM_USAGE_DONE = "itemusageDone";

	public void itemUsed() {

		if (this.getCurrentItem() == null)
			return;

		Item usedItem = this.getCurrentItem();

		this.firePropertyChange(PROPERTY_ITEM_USAGE_DONE, null, usedItem);

		this.getSetting().withUsedItem(usedItem);
		this.withCurrentItem(null);

		// save current relevance scores to file
		this.resultTracker.writeToCSV(this.getRecommender().getRecommendations(), this.getCurrentUser(),
				RecoTool.setting);

		RecoTool.setting.setCurrentTime(new Date(
				RecoTool.setting.getCurrentTime().getTime() + usedItem.getEstimatedUsageDuration().toMillis()));

		this.updateBySetting(this.user);

		if (!this.getMQTTClient().getClient().isConnected())
			return;

		// update data glasses with new recommendations

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			JSONObject json = new JSONObject();
			json.put("action", RecoToolMqttServer.MQTT_CMD_RECOMMENDATIONS);

			JSONArray arr = new JSONArray();
			for (Iterator<Item> it = this.getRecommender().getRecommendations().keySet().iterator(); it.hasNext();) {
				Item item = it.next();
				arr.put(new JSONObject(mapper.writeValueAsString(item)));
			}

			json.put("items", arr);
			this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

			int timePassed = (int) (this.evaluationDuration.toMillis() - RecoTool.setting.getTimeToDeparture());
			this.startTime.add(Calendar.MILLISECOND, timePassed);

			// update simulated time in CAVE
			json = new JSONObject();
			json.put("action", RecoToolMqttServer.MQTT_CMD_SET_SIMULATED_TIME);
			json.put("value", new JSONObject().put("hour", this.startTime.get(Calendar.HOUR_OF_DAY)).put("minute",
					this.startTime.get(Calendar.MINUTE)));
			this.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_CAVE_TOPIC);

			// Send information for further recommendations to data glasses
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

		this.firePropertyChange(PROPERTY_SWITCH_MODE, oldValue, value);

		LOG.info("Recommendation mode changed to " + value);

		if (Recommender.PROPERTY_ABIDANCE_MODE.compareTo(value) == 0) {
			this.setNavigationDestination(null);
		}

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
				Item tmpItem = new Item().withName(this.getNextConnectionPosition()).withGeoposition(this.endPosition);
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
			msg = msg.replace("%min%", this.getCurrentItem().getEstimatedUsageDuration().toMinutes() + "");
		}

		if (this.getCurrentUser() != null && this.getCurrentUser().getId() > 0) {
			msg = msg.replace("%firstname%", this.getCurrentUser().getFirstname());
			msg = msg.replace("%lastname%", this.getCurrentUser().getLastname());
		}

		if (this.getStartTrafficJunction() != null) {
			msg = msg.replace("%startTrafficJunction%", this.getStartTrafficJunction().getName());
		}

		if (this.getEndTrafficJunction() != null) {
			msg = msg.replace("%endTrafficJunction%", this.getEndTrafficJunction().getName());
		}

		if (this.getNavigationDestination() != null) {
			msg = msg.replace("%navigationDestination%", this.getNavigationDestination().getName());
		}

		if (this.getNextConnectionPosition() != null) {
			msg = msg.replace("%nextConnectionPosition%", this.getNextConnectionPosition());
		}

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

	public String getNextConnectionPosition() {
		return this.nextConnectionPosition;
	}

	public void setNextConnectionPosition(String value) {
		this.nextConnectionPosition = value;
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
}