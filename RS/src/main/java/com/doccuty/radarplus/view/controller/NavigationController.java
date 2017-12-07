package com.doccuty.radarplus.view.controller;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import com.doccuty.radarplus.model.Geoposition;
import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.view.listener.NavigationListener;
import com.fasterxml.jackson.core.JsonProcessingException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

/**
 * This controller manages navigation through maps in CAVE.
 * All functions are connected to respective *.fxml file.
 * 
 * @author Niclas Kannengießer
 *
 */

public class NavigationController implements Initializable {

	@FXML
	Button btn_startNavigation;

	@FXML
	Button btn_cancelNavigation;
	
	@FXML
	Button btn_startEvaluation;

	@FXML
	Button btn_systemPrompt;

	@FXML
	TextField tf_longitude;

	@FXML
	TextField tf_latitude;

	@FXML
	Label lbl_time;

	@FXML
	Label lbl_username;

	@FXML
	TableView<Geoposition> tv_lastDestinations;

	@FXML
	TableColumn<Geoposition, Long> tc_lastDestinationID;

	@FXML
	TableColumn<Geoposition, Double> tc_lastDestinationLatitude;

	@FXML
	TableColumn<Geoposition, Double> tc_lastDestinationLongitude;

	ObservableList<Geoposition> destinationData = FXCollections.observableArrayList();

	private RecoTool app;

	private RootController parent;

	private ResourceBundle bundle;

	NavigationListener listener;

	public NavigationController() {
		this.listener = new NavigationListener(this);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.bundle = resources;

		this.tv_lastDestinations.setItems(this.destinationData);
		this.tc_lastDestinationID.setCellValueFactory(new PropertyValueFactory<Geoposition, Long>("id"));
		this.tc_lastDestinationLongitude
				.setCellValueFactory(new PropertyValueFactory<Geoposition, Double>("longitude"));
		this.tc_lastDestinationLatitude.setCellValueFactory(new PropertyValueFactory<Geoposition, Double>("latitude"));
	}

	// Initialize controller screen with app data
	public void init() {
		if (this.app == null)
			return;

		this.lbl_username.setText(this.app.getCurrentUser().getFirstname() + " "
				+ this.app.getCurrentUser().getLastname() + " (" + this.app.getCurrentUser().getAge() + ")");

		this.updateTime();
	}

	public void setListeners() {
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_EVALUATION_DURATION, listener);
	}

	@FXML
	public void sendSystemPrompt(MouseEvent ev) {
		this.parent.openSystemPromptController(null);
	}

	/**
	 * Start a new navigation to
	 * @param ev
	 */
	
	@FXML
	public void startNavigation(MouseEvent ev) {

		if (this.tf_latitude.getText().length() == 0 || this.tf_longitude.getText().length() == 0)
			return;

		Geoposition destination = new Geoposition().withLatitude(Double.parseDouble(this.tf_latitude.getText()))
				.withLongitude(Double.parseDouble(this.tf_longitude.getText())).withId(this.destinationData.size() + 1);

		Item item = new Item().withName("destination").withGeoposition(destination);

		try {
			this.app.startNavigation(item);

			this.destinationData.add(0, item.getGeoposition());
			this.btn_cancelNavigation.setDisable(false);
		} catch (JSONException | JsonProcessingException | MqttException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void cancelNavigation(MouseEvent ev) {
		try {
			this.app.cancelNavigation();
			this.btn_cancelNavigation.setDisable(true);
		} catch (JSONException | JsonProcessingException | MqttException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void startEvaluation(MouseEvent ev) {
		if (!this.app.getEvaluationRunning()) {
			this.app.startClock();
			this.btn_startEvaluation.getStyleClass().remove("button-start");
		} else {
			this.app.stopEvaluation(true);
			this.btn_startEvaluation.getStyleClass().add("button-start");
		}
	}

	public void updateTime() {

		if (this.app == null)
			return;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				DateFormat df = new SimpleDateFormat("HH:mm:ss");
				df.setTimeZone(TimeZone.getDefault());

				String time = df.format(app.getSetting().getCurrentTime());

				lbl_time.setText(time);

			}
		});

	}

	// =============================================

	public RecoTool getApp() {
		return this.app;
	}

	public void setApp(RecoTool value) {
		this.app = value;
	}

	public NavigationController withApp(RecoTool value) {
		this.setApp(value);
		return this;
	}

	// =============================================

	public RootController getParent() {
		return this.parent;
	}

	public void setParent(RootController value) {
		this.parent = value;
	}

	public NavigationController withParent(RootController value) {
		this.setParent(value);
		return this;
	}

}
