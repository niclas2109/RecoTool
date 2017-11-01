package com.doccuty.radarplus.view.controller;

import com.doccuty.radarplus.model.User;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class InfoController {
	
	@FXML
	Text txt_infoText;
	
	public InfoController() {

	}
	
	public void personalizeText(User user) {
		
		if(user == null || user.getFirstname() == null || user.getLastname() == null)
			return;
		
		String s = txt_infoText.getText();
		s = s.replaceAll("%firstname%", user.getFirstname());
		s = s.replaceAll("%lastname%", user.getLastname());
		
		txt_infoText.setText(s);
	}
	
}
