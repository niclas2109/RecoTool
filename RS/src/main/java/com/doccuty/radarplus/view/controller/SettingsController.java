package com.doccuty.radarplus.view.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.jboss.logging.Logger;

import com.doccuty.radarplus.model.SystemPrompt;
import com.doccuty.radarplus.model.TrafficJunction;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.network.RecoToolMqttServer;
import com.doccuty.radarplus.persistence.TrafficJunctionDAO;
import com.doccuty.radarplus.view.listener.SettingsListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class SettingsController implements Initializable {

	private final static Logger LOG = Logger.getLogger(SettingsController.class);

	@FXML
	TextField tf_brokerIP;

	@FXML
	TextField tf_brokerPort;

	@FXML
	CheckBox cb_networkActivated;

	@FXML
	CheckBox cb_randomizeGeoposition;

	@FXML
	CheckBox cb_useGeocoordinates;

	@FXML
	CheckBox cb_serendipityEnabled;

	@FXML
	CheckBox cb_weightingEnabled;

	@FXML
	ImageView iv_connectionCave;

	@FXML
	ImageView iv_connectionDataGlasses;

	@FXML
	TextField tf_maxNumOfItems;

	@FXML
	TextField tf_maxNumOfProductivityItems;

	@FXML
	ChoiceBox<String> cb_evaluationDuration;

	@FXML
	TextField tf_timeMaximizer;

	@FXML
	HBox hbox_systemPrompt;

	@FXML
	Label lbl_systemPromptMessage;

	@FXML
	ChoiceBox<TrafficJunction> cb_startStation;

	@FXML
	ChoiceBox<TrafficJunction> cb_endStation;

	@FXML
	TextField tf_nextConnectionPosition;

	@FXML
	TextField tf_startPositionLng;

	@FXML
	TextField tf_startPositionLat;

	@FXML
	TextField tf_endPositionLng;

	@FXML
	TextField tf_endPositionLat;

	@FXML
	ChoiceBox<String> cb_startHour;

	@FXML
	ChoiceBox<String> cb_startMinute;

	@FXML
	CheckBox cb_realtimeUpdateAccuracyEvaluationMap;

	@FXML
	TextField tf_walkingSpeedTrainingPositionLat;

	@FXML
	TextField tf_walkingSpeedTrainingPositionLng;

	@FXML
	CheckBox cb_useAverageUsageDuration;

	@FXML
	TextField tf_numberOfItemsToUse;

	@FXML
	Label lbl_evaluationFilesDirectory;

	@FXML
	CheckBox cb_delayEnable;

	@FXML
	TextField tf_delayDuration;

	ObservableList<TrafficJunction> trafficJunctions;

	private Stage stage;
	private SettingsListener listener;

	private RecoTool app;

	public SettingsController() {

		listener = new SettingsListener(this);
		trafficJunctions = FXCollections.observableArrayList();

		TrafficJunctionDAO trafficJunctionDao = new TrafficJunctionDAO();
		trafficJunctions.addAll(trafficJunctionDao.findAllOrderByNameAsc());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.cb_startStation.setItems(this.trafficJunctions);
		this.cb_startStation.setConverter(new StringConverter<TrafficJunction>() {
			@Override
			public String toString(TrafficJunction tJ) {
				return tJ.getName();
			}

			@Override
			public TrafficJunction fromString(String string) {
				return null;
			}
		});

		this.cb_endStation.setItems(this.trafficJunctions);
		this.cb_endStation.setConverter(new StringConverter<TrafficJunction>() {
			@Override
			public String toString(TrafficJunction tJ) {
				return tJ.getName();
			}

			@Override
			public TrafficJunction fromString(String string) {
				return null;
			}
		});

		for (int i = 5; i < 80; i += 5)
			this.cb_evaluationDuration.getItems().add((i < 10) ? "0" + i : "" + i);

		for (int i = 0; i < 25; i++)
			this.cb_startHour.getItems().add((i < 10) ? "0" + i : "" + i);

		for (int i = 0; i < 61; i++)
			this.cb_startMinute.getItems().add((i < 10) ? "0" + i : "" + i);
	}

	public void init() {

		try {
			// Set evaluation settings
			this.cb_startHour.getSelectionModel().select(this.app.getStartTime().get(Calendar.HOUR_OF_DAY));
			this.cb_startMinute.getSelectionModel().select(this.app.getStartTime().get(Calendar.MINUTE));

			try {
				this.cb_evaluationDuration.getSelectionModel()
						.select((int) (this.app.getEvaluationDuration().toMinutes() / 5 - 1));
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.tf_timeMaximizer.setText(
					"" + Math.round(this.app.getRecommender().getContextBasedPostFilter().getTimeMaximizer() / 60000));

			// Set start and end station
			this.cb_startStation.getSelectionModel()
					.select(this.getIndexOfTraffixJunction(this.app.getStartTrafficJunction()));
			this.cb_endStation.getSelectionModel()
					.select(this.getIndexOfTraffixJunction(this.app.getEndTrafficJunction()));

			this.tf_startPositionLat.setText(this.app.getStartPosition().getLatitude() + "");
			this.tf_startPositionLng.setText(this.app.getStartPosition().getLongitude() + "");

			this.tf_endPositionLat.setText(this.app.getEndPosition().getLatitude() + "");
			this.tf_endPositionLng.setText(this.app.getEndPosition().getLongitude() + "");

			this.tf_nextConnectionPosition.setText(this.app.getNextConnectionPositionIdentifier());

			// Set item settings
			this.tf_maxNumOfItems.setText(this.app.getMaxNumOfItems() + "");
			this.tf_maxNumOfProductivityItems.setText(this.app.getMaxNumOfProductivityItems() + "");

			this.cb_randomizeGeoposition.setSelected(this.app.getRandomizeItemGeoposition());
			this.cb_useGeocoordinates.setSelected(this.app.getUseGeocoordinates());

			this.cb_serendipityEnabled.setSelected(this.app.getRecommender().getSerendipityEnabled());

			this.cb_weightingEnabled.setSelected(this.app.getRecommender().getWeightingEnabled());

			this.cb_realtimeUpdateAccuracyEvaluationMap
					.setSelected(this.app.getRealtimeUserPositionUpdateAccuracyEvaluationMap());

			this.tf_walkingSpeedTrainingPositionLat.setText(this.app.getWalkingTrainingPosition().getLatitude() + "");
			this.tf_walkingSpeedTrainingPositionLng.setText(this.app.getWalkingTrainingPosition().getLongitude() + "");

			this.cb_useAverageUsageDuration.setSelected(this.app.getMaxNumOfItemsToUse() == 0);

			this.tf_numberOfItemsToUse.setText(this.app.getMaxNumOfItemsToUse() + "");
			this.updateCheckBoxUsageAvUsageDuration(null);

			this.tf_delayDuration.setText(this.app.getDelayDuration().toMinutes() + "");
			this.updateCheckBoxDelayDuration(null);

			if (this.app == null)
				return;

			// Set MQTT settings

			if (this.app.getMQTTClient().isConnected()) {
				this.cb_networkActivated.setSelected(true);
				this.updateNetworkConnection();
			}

			if (this.app.getMQTTClient() != null) {
				this.tf_brokerIP.setText(this.app.getMQTTClient().getBrokerURI());
				this.tf_brokerPort.setText(this.app.getMQTTClient().getBrokerPort() + "");
			}

			this.lbl_evaluationFilesDirectory.setText(RecoTool.prefs.get("evaluationFilesDirectory", "/"));

			// Set listener
			this.app.getMQTTClient().addPropertyChangeListener(RecoToolMqttServer.PROPERTY_TOPIC_UPDATE, listener);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void toggleNetworkActivated(MouseEvent ev) {
		this.toggleNetworkConnection();
		this.updateNetworkConnection();
	}

	@FXML
	public void validateNumberInput(KeyEvent ev) {
		if (!((TextField) ev.getTarget()).getText().matches("[0-9]*")) {
			((TextField) ev.getTarget()).setText("0");
		}
	}

	@FXML
	public void saveEvaluationSettings(MouseEvent ev) {

		Duration d = Duration
				.ofMinutes(Integer.parseInt(this.cb_evaluationDuration.getSelectionModel().getSelectedItem()));
		this.app.setEvaluationDuration(d);

		this.app.getRecommender().getContextBasedPostFilter()
				.setTimeMaximizer(Double.parseDouble(this.tf_timeMaximizer.getText()) * 60000);

		Calendar sT = Calendar.getInstance();
		sT.set(Calendar.HOUR_OF_DAY, this.cb_startHour.getSelectionModel().getSelectedIndex());
		sT.set(Calendar.MINUTE, this.cb_startMinute.getSelectionModel().getSelectedIndex());

		int maxNumOfItems = 0;

		if (!tf_numberOfItemsToUse.isDisabled() && !tf_numberOfItemsToUse.getText().equals(""))
			maxNumOfItems = Integer.parseInt(tf_numberOfItemsToUse.getText());

		this.app.setStartTime(sT);

		this.app.getStartPosition().withLongitude(Double.parseDouble(this.tf_startPositionLng.getText()))
				.withLatitude(Double.parseDouble(this.tf_startPositionLat.getText()));

		this.app.getEndPosition().withLongitude(Double.parseDouble(this.tf_endPositionLng.getText()))
				.withLatitude(Double.parseDouble(this.tf_endPositionLat.getText()));

		this.app.getWalkingTrainingPosition()
				.withLongitude(Double.parseDouble(this.tf_walkingSpeedTrainingPositionLng.getText()))
				.withLatitude(Double.parseDouble(this.tf_walkingSpeedTrainingPositionLat.getText()));

		this.app.withStartTrafficJunction(this.cb_startStation.getSelectionModel().getSelectedItem())
				.withEndTrafficJunction(this.cb_endStation.getSelectionModel().getSelectedItem())
				.withNextConnectionPosition(this.tf_nextConnectionPosition.getText())
				.withMaxNumOfItems(Integer.parseInt(this.tf_maxNumOfItems.getText()))
				.withMaxNumOfProductivityItems(Integer.parseInt(this.tf_maxNumOfProductivityItems.getText()))
				.withRandomizeItemGeoposition(this.cb_randomizeGeoposition.isSelected())
				.withUseGeocoordinates(this.cb_useGeocoordinates.isSelected())
				.withRealtimeUserPositionUpdateAccuracyEvaluationMap(
						this.cb_realtimeUpdateAccuracyEvaluationMap.isSelected())
				.withMaxNumOfItemsToUse(maxNumOfItems)
				.withDelayDuration(Duration.ofMinutes(Long.parseLong(this.tf_delayDuration.getText())));

		this.app.getRecommender().withSerendipityEnabled(this.cb_serendipityEnabled.isSelected())
				.withWeightingEnabled(this.cb_weightingEnabled.isSelected());

		try {
			this.updateSettings();

			this.stage.hide();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@FXML
	public void saveNetworkSettings(MouseEvent ev) {

		try {

			// reconnect to mqtt broker, if connection data was changed
			if (!this.tf_brokerIP.getText().equals(this.app.getMQTTClient().getBrokerURI())
					|| Integer.parseInt(this.tf_brokerPort.getText()) != this.app.getMQTTClient().getBrokerPort()) {

				if (this.app.getMQTTClient().isConnected())
					this.app.getMQTTClient().getClient().disconnect();

				this.app.getMQTTClient().withBrokerURI(this.tf_brokerIP.getText())
						.withBrokerPort((this.tf_brokerPort.getText().length() > 0)
								? Integer.parseInt(this.tf_brokerPort.getText())
								: 0)
						.connect();
			}

			this.updateSettings();

			this.stage.hide();

		} catch (IOException | MqttException e) {
			e.printStackTrace();
			this.showSystemprompt(
					new SystemPrompt(SystemPrompt.SYSTEM_PROMPT_MODE_ERROR, "Can not connect to broker!"));
		}

	}

	private void updateSettings() throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		// Simulation settings
		RecoTool.prefs.putLong("startTime", this.app.getStartTime().getTimeInMillis());

		RecoTool.prefs.putLong("evaluationDuration", this.app.getEvaluationDuration().toMinutes());

		RecoTool.prefs.putDouble("timeMaximizer",
				this.app.getRecommender().getContextBasedPostFilter().getTimeMaximizer());

		RecoTool.prefs.put("startTrafficJunction", mapper.writeValueAsString(this.app.getStartTrafficJunction()));
		RecoTool.prefs.put("endTrafficJunction", mapper.writeValueAsString(this.app.getEndTrafficJunction()));

		RecoTool.prefs.put("nextConnectionPositionIdentifier", this.tf_nextConnectionPosition.getText());

		RecoTool.prefs.put("startPosition", mapper.writeValueAsString(this.app.getStartPosition()));
		RecoTool.prefs.put("endPosition", mapper.writeValueAsString(this.app.getEndPosition()));

		// Item settings
		RecoTool.prefs.putInt("maxNumOfItems", this.app.getMaxNumOfItems());
		RecoTool.prefs.putInt("maxNumOfProductivityItems", this.app.getMaxNumOfProductivityItems());

		RecoTool.prefs.putBoolean("useGeocoordinates", this.app.getUseGeocoordinates());

		RecoTool.prefs.putBoolean("randomizeItemGeoposition", this.app.getRandomizeItemGeoposition());
		RecoTool.prefs.putBoolean("serendipityEnabled", this.app.getRecommender().getSerendipityEnabled());
		RecoTool.prefs.putBoolean("weightingEnabled", this.app.getRecommender().getWeightingEnabled());

		// Network settings
		RecoTool.prefs.put("brokerIP", this.app.getMQTTClient().getBrokerURI());
		RecoTool.prefs.putInt("brokerPort", this.app.getMQTTClient().getBrokerPort());

		// Other setting
		RecoTool.prefs.putBoolean("realtimeUserPositionUpdateAccuracyEvaluationMap",
				this.app.getRealtimeUserPositionUpdateAccuracyEvaluationMap());

		RecoTool.prefs.put("walkingTrainingPosition", mapper.writeValueAsString(this.app.getWalkingTrainingPosition()));

		RecoTool.prefs.putInt("maxNumOfItemsToUse", this.app.getMaxNumOfItemsToUse());

		RecoTool.prefs.put("evaluationFilesDirectory", this.lbl_evaluationFilesDirectory.getText());

		RecoTool.prefs.putLong("delayDuration", this.app.getDelayDuration().toMinutes());
	}

	@FXML
	public void updateCheckBoxUsageAvUsageDuration(KeyEvent ev) {
		boolean active = this.tf_numberOfItemsToUse.getText().compareTo("") != 0 && Integer.parseInt(this.tf_numberOfItemsToUse.getText()) <= 0;
		this.cb_useAverageUsageDuration.setSelected(active);

		setMaxNumberOfItemsToZero(null);
	}

	@FXML
	public void setMaxNumberOfItemsToZero(MouseEvent ev) {
		if (this.cb_useAverageUsageDuration.isSelected()) {
			this.tf_numberOfItemsToUse.setDisable(true);
			this.tf_numberOfItemsToUse.setText("0");
		} else {
			this.tf_numberOfItemsToUse.setDisable(false);
		}
	}

	@FXML
	public void updateCheckBoxDelayDuration(KeyEvent ev) {
		boolean active = this.tf_delayDuration.getText().compareTo("") != 0 && Long.parseLong(this.tf_delayDuration.getText()) <= 0;
		this.cb_delayEnable.setSelected(active);

		this.setDelayDuration(null);
	}

	@FXML
	public void setDelayDuration(MouseEvent ev) {
		if (this.cb_delayEnable.isSelected()) {
			this.tf_delayDuration.setDisable(true);
			this.tf_delayDuration.setText("0");
		} else {
			this.tf_delayDuration.setDisable(false);
		}
	}

	@FXML
	public void chooseDirectory(MouseEvent ev) {

		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);

		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(stage);

		if (selectedDirectory == null) {
			lbl_evaluationFilesDirectory.setText("No Directory selected");
		} else {
			lbl_evaluationFilesDirectory.setText(selectedDirectory.getAbsolutePath());
		}

	}

	@FXML
	public void close(MouseEvent ev) {
		this.stage.hide();
	}

	public void toggleNetworkConnection() {
		try {
			if (this.app.getMQTTClient().isConnected()) {
				this.app.getMQTTClient().getClient().disconnect();
			} else {
				this.app.initializeMQTT();
			}
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void updateNetworkConnection() {
		try {
			Image connected = new Image(
					getClass().getClassLoader().getResource("icons/icons8-Wi-Fi Filled-50.png").openStream());
			Image disconnected = new Image(
					getClass().getClassLoader().getResource("icons/icons8-Wi-Fi off-50.png").openStream());

			if (!this.app.getMQTTClient().isConnected()) {
				iv_connectionCave.setImage(disconnected);
				iv_connectionDataGlasses.setImage(disconnected);
			} else {
				if (this.app.getMQTTClient().getSubscribedTopic().contains(RecoToolMqttServer.PROPERTY_CAVE_TOPIC))
					iv_connectionCave.setImage(connected);
				else
					iv_connectionCave.setImage(disconnected);

				if (this.app.getMQTTClient().getSubscribedTopic()
						.contains(RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC))
					iv_connectionDataGlasses.setImage(connected);
				else
					iv_connectionDataGlasses.setImage(disconnected);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showSystemprompt(SystemPrompt alert) {
		this.hbox_systemPrompt.setVisible(true);
		this.lbl_systemPromptMessage.setText(alert.getMessage());
	}

	private int getIndexOfTraffixJunction(TrafficJunction value) {

		if (value == null)
			return 0;

		List<TrafficJunction> trafficJunctions = this.trafficJunctions.filtered(jT -> jT.getId() == value.getId());
		int idx = (!trafficJunctions.isEmpty()) ? this.trafficJunctions.indexOf(trafficJunctions.get(0)) : 0;

		if (idx > 0)
			return idx;

		return 0;
	}

	// ======================================

	public void setStage(Stage value) {
		this.stage = value;
	}

	public SettingsController withStage(Stage value) {
		this.setStage(value);
		return this;
	}

	// ======================================

	public void setApp(RecoTool value) {
		this.app = value;
	}

	public SettingsController withApp(RecoTool value) {
		this.setApp(value);
		return this;
	}

}
