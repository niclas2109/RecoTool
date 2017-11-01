package com.doccuty.radarplus.view.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.view.controller.RegisterController;
import com.doccuty.radarplus.view.handler.RegisterHandler;

public class RegisterListener implements PropertyChangeListener {

	RegisterHandler handler;
	RegisterController controller;
	
	public RegisterListener(RegisterController controller) {
		handler			= new RegisterHandler();
		
		this.controller	= controller;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().compareTo(User.PROPERTY_USERNAME) == 0) {
			handler.checkForUsernameDuplicates((String) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(User.PROPERTY_EMAIL) == 0) {
			handler.validateEmail((String) evt.getNewValue());
		} else if(evt.getPropertyName().compareTo(RecoTool.PROPERTY_CURRENT_USER) == 0) {
			User user = (User) evt.getNewValue();
			user.addPropertyChangeListener(this);
			controller.fillWithUserData(user);
		}
	}

}
