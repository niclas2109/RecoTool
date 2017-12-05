package com.doccuty.radarplusvic.view.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import com.doccuty.radarplusvic.model.SystemPrompt;
import com.doccuty.radarplusvic.model.VICPromptListener;
import com.doccuty.radarplusvic.network.VICMqttClient;
import com.doccuty.radarplusvic.view.controller.PromptController;

public class PromptListener implements PropertyChangeListener {

	PromptController controller;

	public PromptListener(PromptController controller) {
		this.controller = controller;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().compareTo(VICPromptListener.PROPERTY_NEW_SYSTEM_PROMPT) == 0) {
			this.controller.addNewSystemPrompt((SystemPrompt) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(VICMqttClient.MQTT_CMD_CALCULATED_ROUTE) == 0) {
			SystemPrompt prompt = new SystemPrompt().withID(1).withMessage("hi").withMode("info");
			this.controller.addNewSystemPrompt(prompt);
		} else if (evt.getPropertyName().compareTo(VICPromptListener.PROPERTY_CONNECTED) == 0) {
			this.controller.connected((boolean) evt.getNewValue());
		} else if(evt.getPropertyName().compareTo(VICPromptListener.PROPERTY_DISCONNECTED) == 0) {
			this.controller.connected((boolean) evt.getNewValue());
		}
	}
}
