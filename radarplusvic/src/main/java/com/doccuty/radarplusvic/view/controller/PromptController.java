package com.doccuty.radarplusvic.view.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import org.jboss.logging.Logger;

import com.doccuty.radarplusvic.VICMainApp;
import com.doccuty.radarplusvic.model.SystemPrompt;
import com.doccuty.radarplusvic.model.VICPromptListener;
import com.doccuty.radarplusvic.view.listener.PromptListener;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * (c) by Niclas Kannengießer
 */

public class PromptController implements Initializable {

	private static final Logger LOG = Logger.getLogger(PromptController.class);

	VICPromptListener app;

	Scene scene;
	private Stage stage;
	private Stage secondStage;

	@FXML
	MenuItem mi_connect;

	@FXML
	Label lbl_connectionStatus;

	@FXML
	Label lbl_currentPrompt;

	@FXML
	TableView<SystemPrompt> tv_prompts;

	@FXML
	TableColumn<SystemPrompt, String> tc_promptMessage;

	@FXML
	TableColumn<SystemPrompt, String> tc_promptType;

	ObservableList<SystemPrompt> shownPrompts = FXCollections.observableArrayList();

	private PromptListener listener;

	private ResourceBundle bundle;

	public PromptController() {
		this.listener = new PromptListener(this);

		this.secondStage = new Stage();
		this.secondStage.initModality(Modality.APPLICATION_MODAL);
	}

	public void init() {

		tc_promptMessage.setCellValueFactory(new PropertyValueFactory<SystemPrompt, String>("message"));
		tc_promptType.setCellValueFactory(new PropertyValueFactory<SystemPrompt, String>("mode"));

		if (this.app == null)
			return;

		this.app.addPropertyChangeListener(VICPromptListener.PROPERTY_NEW_SYSTEM_PROMPT, listener);
		this.app.addPropertyChangeListener(VICPromptListener.PROPERTY_CONNECTED, listener);
		this.app.addPropertyChangeListener(VICPromptListener.PROPERTY_DISCONNECTED, listener);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.tv_prompts.setItems(shownPrompts);
		this.bundle = resources;
	}

	@FXML
	public void connectToBroker(ActionEvent ev) {
		if (!this.app.getMQTTClient().isConnected() && this.app.connectToMQTTBroker())
			mi_connect.setText(this.bundle.getString("disconnect"));
		else if (this.app.getMQTTClient().isConnected() && this.app.disconnectMQTT())
			mi_connect.setText(this.bundle.getString("connect"));
	}

	public void connected(boolean status) {

		boolean connected = this.app.getMQTTClient() != null && this.app.getMQTTClient().isConnected();

		String s = (connected) ? this.bundle.getString("connected").replace("%uri%",
				this.app.getMQTTClient().getClient().getServerURI()) : this.bundle.getString("notconnected");

		this.lbl_connectionStatus.setText(s);
	}

	@FXML
	public SettingsController openSettings(ActionEvent ev) {

		SettingsController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/SettingsScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(VICMainApp.LOCALES_FILE_PATH, VICMainApp.getSystemLocale()));

			AnchorPane personOverview = (AnchorPane) loader.load();
			Scene s = new Scene(personOverview);

			this.secondStage.setScene(s);

			controller = (SettingsController) loader.getController();
			controller.withApp(this.app).withStage(this.secondStage).init();

		} catch (IOException e) {
			e.printStackTrace();
		}

		this.secondStage.showAndWait();

		return controller;
	}

	@FXML
	public void quitApplication(ActionEvent ev) {
		System.exit(0);
	}

	// =================================================

	public void addNewSystemPrompt(SystemPrompt newPrompt) {
		this.shownPrompts.add(0, newPrompt);
		showNextPrompt(newPrompt);
	}

	public void showNextPrompt(SystemPrompt prompt) {

		String msg;

		if (prompt == null) {
			msg = "-";
		} else {
			msg = prompt.getMessage();
		}

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				lbl_currentPrompt.setText(msg);
			}
		});

	}

	@FXML
	public void clearSystemPromptList(MouseEvent ev) {
		this.shownPrompts.clear();
		this.app.getNewPrompts().clear();
		this.lbl_currentPrompt.setText("-");
	}

	// =================================================

	public Scene getScene() {
		return this.scene;
	}

	public void setScene(Scene value) {
		this.scene = value;
	}

	public PromptController withScene(Scene value) {
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

	public PromptController withStage(Stage value) {
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

	public PromptController withApp(VICPromptListener value) {
		this.setApp(value);
		return this;
	}
}
