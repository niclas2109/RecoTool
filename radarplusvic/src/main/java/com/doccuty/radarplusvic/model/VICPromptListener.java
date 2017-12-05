package com.doccuty.radarplusvic.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;

import com.doccuty.radarplusvic.network.VICMqttClient;

public class VICPromptListener {

	public static final String PROMPT_TYPE_ERROR = "system-prompt-error";
	public static final String PROMPT_TYPE_INFO = "system-prompt-info";

	public static final String ABIDANCE_MODE = "abidanceMode";
	public static final String EFFICIENCY_MODE = "efficiencyMode";

	// Preferences
	public static Preferences prefs;

	// Thread MQTT Client is running on
	private Thread networkThread;
	private VICMqttClient mqttClient;

	// Thread handling prompt presentation
	private PromptPresentationManager promptPresentationManager;

	// Buffers incoming prompts
	List<SystemPrompt> newPrompts;

	private SystemPrompt currentPrompt;

	// Ids of navigation prompts that must be removed after recieving a new one
	List<Long> ids;

	public VICPromptListener() {
		ids = new ArrayList<Long>();
		ids.add(12l);
		ids.add(13l);
		ids.add(14l);
		ids.add(15l);
		ids.add(16l);

		this.newPrompts = Collections.synchronizedList(new ArrayList<SystemPrompt>());
		this.promptPresentationManager = new PromptPresentationManager(this);

		VICPromptListener.prefs = Preferences.userNodeForPackage(getClass());
	}

	public void init() {
		String mqttBrokerURI = prefs.get("mqttBrokerURI", "tcp://localhost");
		int mqttBrokerPort = prefs.getInt("mqttBrokerURI", 1883);

		this.mqttClient = new VICMqttClient(this).withBrokerURI(mqttBrokerURI).withBrokerPort(mqttBrokerPort);

		this.promptPresentationManager.start();
	}

	public void newSystemPrompt(JSONObject value) {

		if (value == null)
			return;

		SystemPrompt prompt = new SystemPrompt().withID(value.getInt("id")).withMessage(value.getString("message"))
				.withMode(value.getString("mode"));

		this.addNewSystemPrompt(prompt);
	}

	public void finishedNavigation(JSONObject optJSONObject) {

	}

	public void calculatedRoute(JSONObject json) {
		SystemPrompt prompt = new SystemPrompt();

		if (json == null) {
			prompt.withID(0).withMessage("Es konnte keine Route gefunden werden!")
					.withMode(VICPromptListener.PROMPT_TYPE_ERROR);
		} else {
			prompt.withID(json.getInt("id")).withMessage("Die Route zu " + json.getString("name") + " wurde berechnet!")
					.withMode(VICPromptListener.PROMPT_TYPE_INFO);
		}

		this.addNewSystemPrompt(prompt);
	}

	public void cancelNavigation() {

		SystemPrompt prompt = new SystemPrompt().withMessage("Die Navigation wurde abgebrochen!")
				.withMode(VICPromptListener.PROMPT_TYPE_INFO);

		this.addNewSystemPrompt(prompt);

	}

	public void addNewSystemPrompt(SystemPrompt newPrompt) {
		// Remove navigation prompts
		if (ids.contains(newPrompt.getID())) {
			Iterator<SystemPrompt> iterator = newPrompts.iterator();
			while (iterator.hasNext()) {
				SystemPrompt prompt = iterator.next();
				if (ids.contains(prompt.getID())) {
					iterator.remove();
				}
			}
		}

		newPrompts.add(newPrompt);

	}

	/**
	 * MQTT connector actions
	 */

	public static final String PROPERTY_CONNECTED = "connected";

	public boolean connectToMQTTBroker() {
		if (this.mqttClient != null && !this.mqttClient.isConnected()) {

			try {
				this.mqttClient.connect();
				this.firePropertyChange(PROPERTY_CONNECTED, null, true);
			} catch (MqttException e) {
				e.printStackTrace();
				this.firePropertyChange(PROPERTY_CONNECTED, null, false);
			}

			try {
				if (this.mqttClient.connect()) {
					this.networkThread = new Thread(this.mqttClient);
					this.networkThread.start();
					return true;
				}
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	public static final String PROPERTY_DISCONNECTED = "disconnected";

	public boolean disconnectMQTT() {
		try {
			if (this.mqttClient.isConnected()) {

				this.mqttClient.disonnect();
			}

			this.firePropertyChange(PROPERTY_DISCONNECTED, null, true);

			return true;

		} catch (MqttException e) {
			e.printStackTrace();
			this.firePropertyChange(PROPERTY_DISCONNECTED, null, false);
		}
		return false;
	}

	// =============================================

	public VICMqttClient getMQTTClient() {
		return this.mqttClient;
	}

	public void setMQTTClient(VICMqttClient value) {
		this.mqttClient = value;
	}

	public VICPromptListener withMQTTClient(VICMqttClient value) {
		this.setMQTTClient(value);
		return this;
	}

	// =============================================

	public List<SystemPrompt> getNewPrompts() {
		return this.newPrompts;
	}

	public void setNewPrompts(SystemPrompt... values) {

		for (SystemPrompt prompt : newPrompts) {
			this.newPrompts.add(prompt);
		}
	}

	public VICPromptListener withNewPrompt(SystemPrompt... values) {
		this.setNewPrompts(values);
		return this;
	}

	public void removeNewPrompts(SystemPrompt... values) {

		for (Iterator<SystemPrompt> it = newPrompts.iterator(); it.hasNext();) {

			SystemPrompt prompt = it.next();

			for (SystemPrompt rmPrompt : values) {
				if (prompt.equals(rmPrompt)) {
					it.remove();
					break;
				}
			}
		}
	}

	public VICPromptListener withoutNewPrompts(SystemPrompt... values) {
		this.removeNewPrompts(values);
		return this;
	}

	// =============================================

	public static final String PROPERTY_NEW_SYSTEM_PROMPT = "newSystemPrompt";

	public SystemPrompt getCurrentSystemPrompt() {
		return this.currentPrompt;
	}

	public void setCurrentSystemPrompt(SystemPrompt value) {
		this.currentPrompt = value;
		this.firePropertyChange(VICPromptListener.PROPERTY_NEW_SYSTEM_PROMPT, null, this.currentPrompt);
	}

	public VICPromptListener withCurrentSystemPrompt(SystemPrompt value) {
		this.setCurrentSystemPrompt(value);
		return this;
	}

	// =============================================

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
