package com.doccuty.radarplus.view.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.view.controller.NavigationController;

public class NavigationListener implements PropertyChangeListener {

	NavigationController controller;

	public NavigationListener(NavigationController controller) {
		this.controller = controller;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_EVALUATION_DURATION) == 0) {
			this.controller.updateTime();
		}
	}

}
