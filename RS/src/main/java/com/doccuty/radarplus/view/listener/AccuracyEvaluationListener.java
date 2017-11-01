package com.doccuty.radarplus.view.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.doccuty.radarplus.model.Geoposition;
import com.doccuty.radarplus.model.Setting;
import com.doccuty.radarplus.view.controller.AccuracyEvaluationController;

public class AccuracyEvaluationListener implements PropertyChangeListener {

	AccuracyEvaluationController controller;
	
	public AccuracyEvaluationListener(AccuracyEvaluationController controller) {
		this.controller = controller;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(Setting.PROPERTY_UPDATE_USER_POSITION)) {
			this.controller.updateUserPosition((Geoposition) evt.getNewValue());
		}
	}

}
