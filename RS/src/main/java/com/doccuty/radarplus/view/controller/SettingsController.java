package com.doccuty.radarplus.view.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.paho.client.mqttv3.MqttException;
import com.doccuty.radarplus.model.SystemPrompt;
import com.doccuty.radarplus.model.TrafficJunction;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.network.RecoToolMqttServer;
import com.doccuty.radarplus.persistence.TrafficJunctionDAO;
import com.doccuty.radarplus.view.listener.SettingsListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class SettingsController implements Initializable {

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

		ObjectMapper mapper = new ObjectMapper();
		File from = new File(getClass().getClassLoader().getResource("settings/appSettings.json").getPath());

		JsonNode json = null;

		try {
			json = mapper.readTree(from);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Set evaluation settings
		Calendar startTime = Calendar.getInstance();
		if (json.has("startTime")) {
			startTime.setTimeInMillis(json.get("startTime").asLong());
		}

		this.cb_startHour.getSelectionModel().select(startTime.get(Calendar.HOUR_OF_DAY));
		this.cb_startMinute.getSelectionModel().select(startTime.get(Calendar.MINUTE));

		int evaluationDuration = json.get("evaluationDuration").asInt();

		try {
			this.cb_evaluationDuration.getSelectionModel().select(evaluationDuration / 5 - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		double timeMaximizer = json.get("timeMaximizer").asDouble();
		this.tf_timeMaximizer.setText("" + Math.round(timeMaximizer / 60000));

		// Set start and end station
		TrafficJunction trafficJunction = null;

		if (json.has("startTrafficJunction")) {
			try {
				trafficJunction = mapper.treeToValue(json.get("startTrafficJunction"), TrafficJunction.class);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		this.cb_startStation.getSelectionModel().select(this.getIndexOfTraffixJunction(trafficJunction));

		if (json.has("endTrafficJunction")) {
			try {
				trafficJunction = mapper.treeToValue(json.get("endTrafficJunction"), TrafficJunction.class);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		this.cb_endStation.getSelectionModel().select(this.getIndexOfTraffixJunction(trafficJunction));

		this.tf_startPositionLat.setText(json.get("startPosition").get("latitude").asText());
		this.tf_startPositionLng.setText(json.get("startPosition").get("longitude").asText());

		this.tf_endPositionLat.setText(json.get("endPosition").get("latitude").asText());
		this.tf_endPositionLng.setText(json.get("endPosition").get("longitude").asText());

		this.tf_nextConnectionPosition.setText(json.get("nextConnectionPosition").asText());

		// Set item settings
		this.tf_maxNumOfItems.setText(json.get("maxNumOfItems").asText());
		this.tf_maxNumOfProductivityItems.setText(json.get("maxNumOfProductivityItems").asText());

		this.cb_randomizeGeoposition.setSelected(json.get("randomizeItemGeoposition").asBoolean());
		this.cb_useGeocoordinates.setSelected(json.get("useGeocoordinates").asBoolean());

		if (json.has("serendipityEnabled"))
			this.cb_serendipityEnabled.setSelected(json.get("serendipityEnabled").asBoolean());

		if (json.has("weightingEnabled"))
			this.cb_weightingEnabled.setSelected(json.get("weightingEnabled").asBoolean());

		if (json.has("realtimeUserPositionUpdateAccuracyEvaluationMap"))
			this.cb_realtimeUpdateAccuracyEvaluationMap
					.setSelected(json.get("realtimeUserPositionUpdateAccuracyEvaluationMap").asBoolean());

		if (json.has("walkingTrainingPosition")) {
			this.tf_walkingSpeedTrainingPositionLat
					.setText(json.get("walkingTrainingPosition").get("latitude").asText());
			this.tf_walkingSpeedTrainingPositionLng
					.setText(json.get("walkingTrainingPosition").get("longitude").asText());
		}

		if (json.has("useAverageUsageDuration")) {
			cb_useAverageUsageDuration.setSelected(json.get("useAverageUsageDuration").asBoolean());
		}

		if (json.has("maxNumOfItemsToUse")) {
			tf_numberOfItemsToUse.setText(json.get("maxNumOfItemsToUse").asText());
		}

		if (this.app == null)
			return;

		// Set MQTT settings

		if (this.app.getMQTTClient().isConnected()) {
			this.cb_networkActivated.setSelected(true);
			this.updateNetworkConnection();
		}

		try {

			String brokerIP = json.get("brokerIP").asText();
			String brokerPort = json.get("brokerPort").asText();

			if (brokerIP != null) {
				this.tf_brokerIP.setText(brokerIP);
				this.tf_brokerPort.setText(brokerPort);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set listener
		this.app.getMQTTClient().addPropertyChangeListener(RecoToolMqttServer.PROPERTY_TOPIC_UPDATE, listener);
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
				.withMaxNumOfItemsToUse(Integer.parseInt(tf_numberOfItemsToUse.getText()));

		this.app.getRecommender().withSerendipityEnabled(this.cb_serendipityEnabled.isSelected())
				.withWeightingEnabled(this.cb_weightingEnabled.isSelected());

		try {
			this.writeJsonSettings();

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
					|| !this.tf_brokerPort.getText().equals(this.app.getMQTTClient().getBrokerPort())) {

				if (this.app.getMQTTClient().isConnected())
					this.app.getMQTTClient().getClient().disconnect();

				this.app.getMQTTClient().withBrokerURI(this.tf_brokerIP.getText())
						.withBrokerPort(this.tf_brokerPort.getText());
				this.app.getMQTTClient().connect();
			}

			this.writeJsonSettings();

			this.stage.hide();

		} catch (IOException | MqttException e) {
			e.printStackTrace();
			this.showSystemprompt(
					new SystemPrompt(SystemPrompt.SYSTEM_PROMPT_MODE_ERROR, "Can not connect to broker!"));
		}

	}

	private void writeJsonSettings() throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		File f = new File(getClass().getClassLoader().getResource("settings/appSettings.json").getPath());

		JsonNode json = mapper.createObjectNode();
		((ObjectNode) json).put("version", this.app.getVersion());
		((ObjectNode) json).put("randomizeItemGeoposition", this.app.getRandomizeItemGeoposition());
		((ObjectNode) json).put("useGeocoordinates", this.app.getUseGeocoordinates());
		((ObjectNode) json).put("serendipityEnabled", this.app.getRecommender().getSerendipityEnabled());
		((ObjectNode) json).put("weightingEnabled", this.app.getRecommender().getWeightingEnabled());
		((ObjectNode) json).put("maxNumOfItems", this.app.getMaxNumOfItems());
		((ObjectNode) json).put("maxNumOfProductivityItems", this.app.getMaxNumOfProductivityItems());
		((ObjectNode) json).put("evaluationDuration", this.app.getEvaluationDuration().toMinutes());
		((ObjectNode) json).put("timeMaximizer",
				this.app.getRecommender().getContextBasedPostFilter().getTimeMaximizer());
		((ObjectNode) json).set("startTrafficJunction",
				mapper.readTree(mapper.writeValueAsString(this.app.getStartTrafficJunction())));
		((ObjectNode) json).set("endTrafficJunction",
				mapper.readTree(mapper.writeValueAsString(this.app.getEndTrafficJunction())));
		((ObjectNode) json).put("startTime", this.app.getStartTime().getTimeInMillis());

		((ObjectNode) json).set("startPosition",
				mapper.readTree(mapper.writeValueAsString(this.app.getStartPosition())));

		((ObjectNode) json).set("endPosition", mapper.readTree(mapper.writeValueAsString(this.app.getEndPosition())));

		((ObjectNode) json).set("nextConnectionPosition",
				mapper.readTree(mapper.writeValueAsString(this.app.getNextConnectionPosition())));

		((ObjectNode) json).put("realtimeUserPositionUpdateAccuracyEvaluationMap",
				this.app.getRealtimeUserPositionUpdateAccuracyEvaluationMap());

		((ObjectNode) json).set("walkingTrainingPosition",
				mapper.readTree(mapper.writeValueAsString(this.app.getWalkingTrainingPosition())));

		((ObjectNode) json).set("maxNumOfItemsToUse",
				mapper.readTree(mapper.writeValueAsString(this.app.getMaxNumOfItemsToUse())));

		((ObjectNode) json).put("networkEnabled", this.cb_networkActivated.isSelected());
		((ObjectNode) json).put("brokerIP", this.tf_brokerIP.getText());
		((ObjectNode) json).put("brokerPort", this.tf_brokerPort.getText());

		mapper.writeValue(f, json);
	}

	@FXML
	public void updateCheckBoxUsageAvUsageDuration(KeyEvent ev) {
		boolean active = Integer.parseInt(this.tf_numberOfItemsToUse.getText()) <= 0;
		this.cb_useAverageUsageDuration.setSelected(active);

		setMaxNumberOfItemsToZero(null);
	}

	@FXML
	public void setMaxNumberOfItemsToZero(MouseEvent ev) {
		if(this.cb_useAverageUsageDuration.isSelected()) {
			this.tf_numberOfItemsToUse.setDisable(true);
			this.tf_numberOfItemsToUse.setText("0");
		} else {
			this.tf_numberOfItemsToUse.setDisable(false);
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
