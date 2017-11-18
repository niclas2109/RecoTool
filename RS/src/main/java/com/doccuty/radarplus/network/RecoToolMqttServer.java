package com.doccuty.radarplus.network;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.jboss.logging.Logger;
import org.json.JSONObject;

import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.network.callback.RecToolMqttCallback;

public class RecoToolMqttServer implements Runnable {

	private static final Logger LOG = Logger.getLogger(RecoToolMqttServer.class);

	public static final String PROPERTY_SERVER_TOPIC = "RecoToolServer";
	public static final String PROPERTY_DATA_GLASSES_TOPIC = "DataGlasses";
	public static final String PROPERTY_CAVE_TOPIC = "CAVE";

	// Network commands

	public static final String MQTT_CMD_SUBSCRIBE = "subscribe";
	public static final String MQTT_CMD_UNSUBSCRIBE = "unsubscribe";
	public static final String MQTT_CMD_SET_SIMULATED_TIME = "setSimulatedTime";
	public static final String MQTT_CMD_START_POSITION = "startPosition";
	public static final String MQTT_CMD_CALCULATED_ROUTE = "calculatedRoute";
	public static final String MQTT_CMD_FINISHED_NAVIGATION = "finishedNavigation";
	public static final String MQTT_CMD_CURRENT_USER_POSITION = "currentUserPosition";
	public static final String MQTT_CMD_USER_IS_MOVING = "userIsMoving";
	public static final String MQTT_CMD_ESTIMATED_DISTANCE = "estimatedDistance";
	public static final String MQTT_CMD_SYSTEM_ALERT = "systemAlert";
	public static final String MQTT_CMD_CLEAR_SYSTEM_PROMPT_QUEUE = "clearSystemPromptQueue";
	public static final String MQTT_CMD_RECOMMENDATIONS = "recommendations";
	public static final String MQTT_CMD_START_NAVIGATION = "startNavigation";
	public static final String MQTT_CMD_CANCEL_NAVIGATION = "cancelNavigation";
	public static final String MQTT_CMD_STOP_NAVIGATION = "stopNavigation";
	public static final String MQTT_CMD_SWITCHED_RECOMMENDATION_MODE = "switchedRecommendationMode";
	public static final String MQTT_CMD_HIDE_ALL = "hideAll";
	public static final String MQTT_CMD_RESET = "reset";
	public static final String MQTT_CMD_QUIT = "quit";

	public static final String MQTT_CMD_SELECT_ITEM = "selectItem";
	public static final String MQTT_CMD_UNSELECT_ITEM = "unselectItem";
	public static final String MQTT_CMD_SHOW_DETAIL_VIEW = "showDetailView";
	public static final String MQTT_CMD_HIDE_DETAIL_VIEW = "hideDetailView";

	private String brokerURI;

	private int brokerPort;

	private MqttClient client = null;
	private LinkedBlockingQueue<String> messageQueue;

	RecToolMqttCallback callback;

	private volatile boolean running;

	private Set<String> subscriberTopics;

	private RecoTool app;

	public RecoToolMqttServer(RecoTool recoTool) {
		this.messageQueue = new LinkedBlockingQueue<String>();

		this.subscriberTopics = new HashSet<String>();

		this.callback = new RecToolMqttCallback();
		this.callback.setMessageQueue(messageQueue);

		this.running = false;

		this.app = recoTool;
	}

	public boolean connect() throws MqttException {

		if (this.brokerURI == null || this.brokerPort == 0 || this.brokerURI.equals("")) {
			LOG.error("No IP or port given!");
			return false;
		}

		this.client = new MqttClient(this.brokerURI + ":" + this.brokerPort, PROPERTY_SERVER_TOPIC + UUID.randomUUID());

		this.client.setCallback(this.callback);
		this.client.connect();
		this.client.subscribe(PROPERTY_SERVER_TOPIC, 2);

		JSONObject json = new JSONObject();
		json.put("action", "subscribe");

		this.send(json.toString(), PROPERTY_DATA_GLASSES_TOPIC, PROPERTY_CAVE_TOPIC);

		return true;
	}

	public boolean isConnected() {
		if (this.client == null)
			return false;

		return this.client.isConnected();
	}

	public void disonnect() throws MqttException {
		this.client.disconnect();
	}

	public void terminate() {
		this.running = false;
	}

	@Override
	public void run() {

		this.running = true;

		try {
			while (this.running) {
				String message = messageQueue.take();

				JSONObject json = new JSONObject(message);
				String action = (String) json.get("action");

				switch (action) {
				case MQTT_CMD_SUBSCRIBE:
					this.newSubscriber((String) json.get("value"));
					break;
				case MQTT_CMD_UNSUBSCRIBE:
					this.newUnsubscribe((String) json.get("value"));
					break;
				case MQTT_CMD_CALCULATED_ROUTE:
					this.app.calculatedRoute((JSONObject) json.optJSONObject("item"));
					break;
				case MQTT_CMD_FINISHED_NAVIGATION:
					this.app.finishedNavigation((JSONObject) json.optJSONObject("item"));
					break;
				case MQTT_CMD_CURRENT_USER_POSITION:
					this.app.updateUserPosition((JSONObject) json.optJSONObject("value"));
					break;
				case MQTT_CMD_HIDE_ALL:
					this.app.setHideAll(false);
					break;
				case MQTT_CMD_ESTIMATED_DISTANCE:
					LOG.info("Estimated distance: " + json.getDouble("value") + " m");
					LOG.info("Estimated arrival: "
							+ (json.getDouble("value") * 60 / this.app.getCurrentUser().getCurrentWalkingSpeed())
							+ " min");
					break;
				default:
					break;
				}
			}

		} catch (MqttException | InterruptedException e) {

			this.running = false;
			e.printStackTrace();

		} finally {
			if (client != null) {
				try {
					client.disconnect();
					client.close();
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void newSubscriber(String topicFilter) throws MqttException {
		this.withSubscriberTopic(topicFilter);
	}

	public void newUnsubscribe(String topicFilter) throws MqttException {
		this.withoutSubscriberTopic(topicFilter);
	}

	/**
	 * Send MQTT message to specific topic
	 * 
	 * @param topic
	 * @param message
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */

	public void send(String message, String... topic) throws MqttPersistenceException, MqttException {
		MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setPayload(message.getBytes());

		for (String t : topic) {
			client.publish(t, mqttMessage);
		}
	}

	public MqttClient getClient() {
		return this.client;
	}

	// =============================================

	public static final String PROPERTY_TOPIC_UPDATE = "mqttTopicUpdate";

	public Set<String> getSubscribedTopic() {
		return this.subscriberTopics;
	}

	public void setSubscriberTopic(String... values) {
		for (String value : values) {
			this.subscriberTopics.add(value);
		}

		this.firePropertyChange(PROPERTY_TOPIC_UPDATE, null, this.subscriberTopics);
	}

	public RecoToolMqttServer withSubscriberTopic(String... values) {
		setSubscriberTopic(values);
		return this;
	}

	public void removeSubscriberTopic(String... values) {
		for (String value : values)
			this.subscriberTopics.remove(value);

		this.firePropertyChange(PROPERTY_TOPIC_UPDATE, null, this.subscriberTopics);
	}

	public RecoToolMqttServer withoutSubscriberTopic(String... values) {
		this.removeSubscriberTopic(values);
		return this;
	}

	public String getBrokerURI() {
		return this.brokerURI;
	}

	public void setBrokerURI(String value) {
		this.brokerURI = value;
	}

	public RecoToolMqttServer withBrokerURI(String value) {
		this.setBrokerURI(value);
		return this;
	}

	// =============================================

	public int getBrokerPort() {
		return this.brokerPort;
	}

	public void setBrokerPort(int value) {
		this.brokerPort = value;
	}

	public RecoToolMqttServer withBrokerPort(int value) {
		this.setBrokerPort(value);
		return this;
	}

	// =============================================

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
