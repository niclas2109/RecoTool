package com.doccuty.radarplus.view.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jboss.logging.Logger;

import com.doccuty.radarplus.network.RecoToolMqttServer;
import com.doccuty.radarplus.view.controller.SettingsController;

public class SettingsListener implements PropertyChangeListener {

	private final static Logger LOG = Logger.getLogger(SettingsListener.class);

	SettingsController controller;
	
	public SettingsListener(SettingsController controller) {
		this.controller = controller;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(RecoToolMqttServer.PROPERTY_TOPIC_UPDATE)) {
			this.controller.updateNetworkConnection();
		} else if(evt.getPropertyName().compareTo(RecoToolMqttServer.PROPERTY_TOPIC_UPDATE) == 0) {
			LOG.info(RecoToolMqttServer.PROPERTY_TOPIC_UPDATE);
			this.controller.updateNetworkConnection();
		}
	}

}
