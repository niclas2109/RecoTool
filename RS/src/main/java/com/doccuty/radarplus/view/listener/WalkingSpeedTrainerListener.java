package com.doccuty.radarplus.view.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.view.controller.WalkingSpeedTrainerController;

public class WalkingSpeedTrainerListener implements PropertyChangeListener {

	WalkingSpeedTrainerController controller;
	
	public WalkingSpeedTrainerListener(WalkingSpeedTrainerController controller) {
		this.controller = controller;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(User.PROPERTY_CURRENT_WALKING_SPEED)) {
			this.controller.setCurrentWalkingSpeed((long) evt.getNewValue());
		}
	}

}
