package com.doccuty.radarplus.view.controller;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.view.listener.WalkingSpeedTrainerListener;
import com.fasterxml.jackson.core.JsonProcessingException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class WalkingSpeedTrainerController {

	@FXML
	Label lbl_maximum;

	@FXML
	TextField tf_maximum;

	@FXML
	Button btn_measureMax;

	@FXML
	TextField tf_average;

	@FXML
	Label lbl_average;

	@FXML
	Button btn_measureAvg;

	@FXML
	TextField tf_minumum;

	@FXML
	Label lbl_minimum;

	@FXML
	Button btn_measureMin;

	private WalkingSpeedTrainerListener listener;

	private boolean measuringMin;
	private boolean measuringAvg;
	private boolean measuringMax;

	private RecoTool studyApp;
	private Stage stage;

	private RootController parentController;

	public WalkingSpeedTrainerController() {
		this.measuringMin = false;
		this.measuringAvg = false;
		this.measuringMax = false;

		this.listener = new WalkingSpeedTrainerListener(this);
	}

	public void init() {
		if (this.studyApp == null)
			return;

		this.lbl_minimum.setText("" + this.studyApp.getCurrentUser().getMinWalkingSpeed());
		this.lbl_average.setText("" + this.studyApp.getCurrentUser().getAvgWalkingSpeed());
		this.lbl_maximum.setText("" + this.studyApp.getCurrentUser().getMaxWalkingSpeed());

		this.studyApp.getCurrentUser().addPropertyChangeListener(User.PROPERTY_CURRENT_WALKING_SPEED, listener);
	}

	@FXML
	public void close(MouseEvent ev) {
		this.stage.hide();
	}

	@FXML
	protected void apply(MouseEvent ev) {
		this.studyApp.getCurrentUser().setMinWalkingSpeed(Long.parseLong(this.lbl_minimum.getText()));
		this.studyApp.getCurrentUser().setAvgWalkingSpeed(Long.parseLong(this.lbl_average.getText()));
		this.studyApp.getCurrentUser().setMaxWalkingSpeed(Long.parseLong(this.lbl_maximum.getText()));

		this.stage.hide();
	}

	@FXML
	protected void save(MouseEvent ev) {
		this.studyApp.getCurrentUser().setMinWalkingSpeed(Long.parseLong(this.lbl_minimum.getText()));
		this.studyApp.getCurrentUser().setAvgWalkingSpeed(Long.parseLong(this.lbl_average.getText()));
		this.studyApp.getCurrentUser().setMaxWalkingSpeed(Long.parseLong(this.lbl_maximum.getText()));

		this.studyApp.saveUser();

		this.stage.hide();
	}

	@FXML
	protected void measureMinWalkingSpeed(ActionEvent ev) {

		this.measuringMin = !this.measuringMin;

		if (this.measuringMin) {
			this.studyApp.getWalkingSpeedCalculator().clear();
			this.btn_measureMin.getStyleClass().add("active");
			return;
		}

		this.btn_measureMin.getStyleClass().remove("active");
		this.studyApp.getCurrentUser().setMinWalkingSpeed(Long.parseLong(this.lbl_minimum.getText()));
	}

	@FXML
	protected void measureAvgWalkingSpeed(ActionEvent ev) {

		this.measuringAvg = !this.measuringAvg;

		if (this.measuringAvg) {
			this.studyApp.getWalkingSpeedCalculator().clear();
			this.btn_measureAvg.getStyleClass().add("active");
			return;
		}

		this.btn_measureAvg.getStyleClass().remove("active");
		this.studyApp.getCurrentUser().setAvgWalkingSpeed(Long.parseLong(this.lbl_average.getText()));
	}

	@FXML
	protected void measureMaxWalkingSpeed(ActionEvent ev) {

		this.measuringMax = !this.measuringMax;

		if (this.measuringMax) {
			this.studyApp.getWalkingSpeedCalculator().clear();
			this.btn_measureMax.getStyleClass().add("active");
			return;
		}

		this.btn_measureMax.getStyleClass().remove("active");
		this.studyApp.getCurrentUser().setMaxWalkingSpeed(Long.parseLong(this.lbl_maximum.getText()));
	}

	public void setCurrentWalkingSpeed(long newValue) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (measuringMin)
					lbl_minimum.setText("" + studyApp.getCurrentUser().getCurrentWalkingSpeed());

				if (measuringAvg)
					lbl_average.setText("" + studyApp.getCurrentUser().getCurrentWalkingSpeed());

				if (measuringMax)
					lbl_maximum.setText("" + studyApp.getCurrentUser().getCurrentWalkingSpeed());
			}
		});
	}

	@FXML
	public void sendWalkingTrainingPosition() {
		try {
			this.studyApp.sendWalkingSpeedTrainingPosition();
		} catch (JSONException | JsonProcessingException | MqttException e) {
			e.printStackTrace();
		}
	}

	// ===================================================

	public RecoTool getStudyApp() {
		return this.studyApp;
	}

	public void setStudyApp(RecoTool value) {
		this.studyApp = value;
	}

	public WalkingSpeedTrainerController withStudyApp(RecoTool value) {
		this.setStudyApp(value);
		return this;
	}

	// ===================================================

	public RootController setParent() {
		return this.parentController;
	}

	public void setParent(RootController value) {
		this.parentController = value;
	}

	public WalkingSpeedTrainerController withParent(RootController value) {
		setParent(value);
		return this;
	}

	// ======================================

	public Stage getStage() {
		return this.stage;
	}

	public void setStage(Stage value) {
		this.stage = value;
	}

	public WalkingSpeedTrainerController withStage(Stage value) {
		this.setStage(value);
		return this;
	}
}
