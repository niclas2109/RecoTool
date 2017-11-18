package com.doccuty.radarplus.view.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import com.doccuty.radarplus.model.SystemPrompt;
import com.doccuty.radarplus.MainApp;
import com.doccuty.radarplus.model.RecoTool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class SystemPromptController implements Initializable {

	private Stage stage;

	private RecoTool app;

	private String alertType;

	@FXML
	ChoiceBox<SystemPrompt> cb_systemMessage;

	@FXML
	TextField tf_systemMessage;

	@FXML
	RadioButton rbtn_errorPrompt;

	@FXML
	RadioButton rbtn_infoPrompt;

	@FXML
	RadioButton rbtn_efficiencyPrompt;

	SystemPrompt currentPrompt;

	@Override
	public void initialize(URL url, ResourceBundle rb) {

		this.cb_systemMessage.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
				currentPrompt = (SystemPrompt) cb_systemMessage.getItems().get((Integer) number2);

				if (currentPrompt.getMode() != null
						&& currentPrompt.getMode().compareTo(SystemPrompt.SYSTEM_PROMPT_MODE_ERROR) == 0) {
					rbtn_errorPrompt.setSelected(true);
					alertType = SystemPrompt.SYSTEM_PROMPT_MODE_ERROR;
				} else if (currentPrompt.getMode() != null
						&& currentPrompt.getMode().compareTo(SystemPrompt.SYSTEM_PROMPT_MODE_EFFICIENCY) == 0) {
					rbtn_efficiencyPrompt.setSelected(true);
					alertType = SystemPrompt.SYSTEM_PROMPT_MODE_EFFICIENCY;
				} else {
					rbtn_infoPrompt.setSelected(true);
					alertType = SystemPrompt.SYSTEM_PROMPT_MODE_INFO;
				}

				if ((Integer) number2 == 0) {
					tf_systemMessage.setText("");
				} else {

					tf_systemMessage.setText(currentPrompt.getMessage().replace("%duration%",
							"" + Duration.ofMillis(app.getSetting().getTimeToDeparture()).toMinutes()));

					if (app.getCurrentItem() != null) {
						String s = currentPrompt.getMessage().replace("%item%", app.getCurrentItem().getName());
						s = s.replace("%min%", app.getCurrentItem().getEstimatedUsageDuration().toMinutes() + "");

						tf_systemMessage.setText(s);
					}
				}

			}
		});

		this.cb_systemMessage.setConverter(new StringConverter<SystemPrompt>() {
			@Override
			public String toString(SystemPrompt sP) {
				return sP.getMessage();
			}

			@Override
			public SystemPrompt fromString(String string) {
				return null;
			}
		});

		ObjectMapper mapper = new ObjectMapper();

		try {
			InputStream from = this.getClass().getResourceAsStream(MainApp.SYSTEM_PROMPTS_JSON_FILE);

			if (from == null)
				throw new FileNotFoundException("File " + MainApp.SYSTEM_PROMPTS_JSON_FILE + " not found!");

			for (SystemPrompt prompt : mapper.readValue(from, SystemPrompt[].class)) {
				this.cb_systemMessage.getItems().add(prompt);
			}

			this.cb_systemMessage.getSelectionModel().select(0);

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (rbtn_errorPrompt.isSelected())
			this.alertType = SystemPrompt.SYSTEM_PROMPT_MODE_ERROR;
		else if (rbtn_efficiencyPrompt.isSelected())
			this.alertType = SystemPrompt.SYSTEM_PROMPT_MODE_EFFICIENCY;
		else
			this.alertType = SystemPrompt.SYSTEM_PROMPT_MODE_INFO;


		this.tf_systemMessage.requestFocus();
	}

	@FXML
	public void changePromptType(ActionEvent ev) {
		if (rbtn_errorPrompt.isSelected())
			this.alertType = SystemPrompt.SYSTEM_PROMPT_MODE_ERROR;
		else if (rbtn_efficiencyPrompt.isSelected())
			this.alertType = SystemPrompt.SYSTEM_PROMPT_MODE_EFFICIENCY;
		else
			this.alertType = SystemPrompt.SYSTEM_PROMPT_MODE_INFO;
	}

	@FXML
	public void clearSystemPromptQueue(MouseEvent ev) {
		try {
			this.app.clearSystemPromptQueue();
		} catch (JSONException | JsonProcessingException | MqttException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void send(MouseEvent ev) {
		try {
			this.app.sendSystemPrompt(currentPrompt.withMessage(tf_systemMessage.getText()).withMode(this.alertType));
			this.stage.hide();
		} catch (JSONException | JsonProcessingException | MqttException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void close(MouseEvent ev) {
		this.stage.hide();
	}

	// =====================================

	public RecoTool getApp() {
		return this.app;
	}

	public void setApp(RecoTool value) {
		this.app = value;
	}

	public SystemPromptController withApp(RecoTool value) {
		this.setApp(value);
		return this;
	}

	// =====================================

	public Stage getStage() {
		return this.stage;
	}

	public void setStage(Stage value) {
		this.stage = value;
	}

	public SystemPromptController withStage(Stage value) {
		this.setStage(value);
		return this;
	}

}
