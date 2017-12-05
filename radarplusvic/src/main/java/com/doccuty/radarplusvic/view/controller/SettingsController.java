package com.doccuty.radarplusvic.view.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.doccuty.radarplusvic.model.VICPromptListener;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SettingsController implements Initializable {

	@FXML
	HBox hbox_systemPrompt;

	@FXML
	Label lbl_systemPrompt;

	@FXML
	TextField tf_mqttBrokerURI;

	@FXML
	TextField tf_mqttBrokerPort;

	private ResourceBundle bundle;
	
	VICPromptListener app;

	private Scene scene;
	private Stage stage;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		this.bundle = resources;
		
		this.hbox_systemPrompt.managedProperty().bind(this.hbox_systemPrompt.visibleProperty());
	}

	public void init() {

		if (this.app == null)
			return;

		this.tf_mqttBrokerURI.setText(this.app.getMQTTClient().getBrokerURI());
		this.tf_mqttBrokerPort.setText(this.app.getMQTTClient().getBrokerPort() + "");
	}

	@FXML
	public void save(MouseEvent ev) {

		if (this.tf_mqttBrokerURI.getText().compareTo("") == 0) {
			this.tf_mqttBrokerURI.requestFocus();
			return;
		}

		if (this.tf_mqttBrokerPort.getText().compareTo("") == 0) {
			this.tf_mqttBrokerPort.requestFocus();
			return;
		}

		if (this.app.getMQTTClient().isConnected()) {
			this.app.disconnectMQTT();
		}

		this.app.getMQTTClient().withBrokerURI(this.tf_mqttBrokerURI.getText())
				.withBrokerPort(Integer.parseInt(this.tf_mqttBrokerPort.getText()));
		if (this.app.connectToMQTTBroker()) {

			VICPromptListener.prefs.put("mqttBrokerURI", this.tf_mqttBrokerURI.getText());
			VICPromptListener.prefs.putInt("mqttBrokerPort", Integer.parseInt(this.tf_mqttBrokerPort.getText()));

			this.stage.hide();
		} else {
			lbl_systemPrompt.setText(this.bundle.getString("cannotConnect"));
			hbox_systemPrompt.setVisible(true);
		}

	}

	@FXML
	public void cancel(MouseEvent ev) {
		this.stage.hide();
	}

	// =================================================

	public Scene getScene() {
		return this.scene;
	}

	public void setScene(Scene value) {
		this.scene = value;
	}

	public SettingsController withScene(Scene value) {
		this.scene = value;
		return this;
	}

	// =================================================

	public Stage getStage() {
		return this.stage;
	}

	public void setStage(Stage value) {
		this.stage = value;
	}

	public SettingsController withStage(Stage value) {
		this.setStage(value);
		return this;
	}

	// =================================================

	public VICPromptListener getApp() {
		return this.app;
	}

	public void setApp(VICPromptListener value) {
		this.app = value;
	}

	public SettingsController withApp(VICPromptListener value) {
		this.setApp(value);
		return this;
	}
}
