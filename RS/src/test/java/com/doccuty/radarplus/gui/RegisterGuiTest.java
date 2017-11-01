package com.doccuty.radarplus.gui;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ResourceBundle;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.categories.TestFX;

import com.doccuty.radarplus.MainApp;
import com.doccuty.radarplus.model.SystemPrompt;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.view.controller.RootController;
import com.fasterxml.jackson.core.JsonProcessingException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

@Category(TestFX.class)
public class RegisterGuiTest extends GuiTest {

	RecoTool app;
	RootController rootController;
	
	@Override
    protected Parent getRootNode() {
		BorderPane rootLayout = null;
        
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("fxml/RootLayout.fxml"));	
            loader.setResources(ResourceBundle.getBundle("rectool", MainApp.getSystemLocale()));

            rootLayout = (BorderPane) loader.load();

            rootController = (RootController) loader.getController();
            rootController.withRootLayout(rootLayout);
            
            this.app = new RecoTool();
            this.app.init();
            app.getTrainer();
            
            rootController.withStudyApp(app).showRegisterScreen();
            rootController.init();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
				
        return rootLayout;
    }
	
	@Test
	public void registrationTest() {

		TextField tf_firstname = find("#tf_firstname");
		TextField tf_lastname = find("#tf_lastname");
		TextField tf_email = find("#tf_email");

		RadioButton rbtn_male = find("#rbtn_male");
		
		Button btn_register = find("#btn_register");

		click(tf_firstname).type("Max");
		click(tf_lastname).type("Mustermann");
		click(rbtn_male);
		click(tf_email).type("asda-asd.de");

		assertEquals("Compare firstname input and firstname of user", tf_firstname.getText(), app.getCurrentUser().getFirstname());
		assertEquals("Compare lastname input and lastname of user", tf_lastname.getText(), app.getCurrentUser().getLastname());

		click(btn_register);
	}
	
	@Test
	public void errorAlertTest() {
		try {
			this.app.getMQTTClient().connect();
			
			if(!this.app.getMQTTClient().isConnected())
				return;
				
			this.app.sendSystemPrompt(new SystemPrompt(SystemPrompt.SYSTEM_PROMPT_MODE_ERROR, "Verspätung von 10 min!"));
			this.app.getMQTTClient().disonnect();
		} catch (JSONException | JsonProcessingException | MqttException e) {
			e.printStackTrace();
		}
	}

}
