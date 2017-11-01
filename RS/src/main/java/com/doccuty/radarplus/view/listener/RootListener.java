package com.doccuty.radarplus.view.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jboss.logging.Logger;

import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.network.RecoToolMqttServer;
import com.doccuty.radarplus.view.controller.RootController;

public class RootListener implements PropertyChangeListener {

	private final static Logger LOG = Logger.getLogger(RootListener.class);
	
	private RootController controller;
	
	public RootListener(RootController rootController) {
		this.controller = rootController;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_CURRENT_USER) == 0) {
			this.controller.setCurrentUser((User) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_SAVED) == 0) {
			LOG.info(RecoTool.PROPERTY_SAVED);
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_UPDATED) == 0) {
			LOG.info(RecoTool.PROPERTY_UPDATED);
		} else if(evt.getPropertyName().compareTo(RecoToolMqttServer.PROPERTY_TOPIC_UPDATE) == 0) {
			LOG.info(RecoToolMqttServer.PROPERTY_TOPIC_UPDATE);
		}

	}

}
