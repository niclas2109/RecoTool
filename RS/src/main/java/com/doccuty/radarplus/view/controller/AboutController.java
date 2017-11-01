package com.doccuty.radarplus.view.controller;

import java.lang.reflect.Field;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.network.RecoToolMqttServer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AboutController {

	@FXML
	TableView<String> tv_mqttCommands;

	@FXML
	TableColumn<String, String> tc_command;

	ObservableList<String> mqttCommandData = FXCollections.observableArrayList();

	private Stage stage;
	private RecoTool app;


	public void initialize() {
		this.tv_mqttCommands.setItems(mqttCommandData);
	}

	public void init() {
		
		for (Field field : RecoToolMqttServer.class.getFields()) {
			if (field.getType().equals(String.class) && field.getName().contains("MQTT_CMD")) {
				try {
					this.mqttCommandData.add((String) field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		tc_command.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TableColumn.CellDataFeatures<String, String> p) {
						return new SimpleStringProperty(p.getValue());
					}
				});
	}

	@FXML
	protected void close(MouseEvent ev) {
		this.stage.hide();
	}
	
	
	// =====================================

	public RecoTool getApp() {
		return this.app;
	}

	public void setApp(RecoTool value) {
		this.app = value;
	}

	public AboutController withApp(RecoTool value) {
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

	public AboutController withStage(Stage value) {
		this.setStage(value);
		return this;
	}

}
