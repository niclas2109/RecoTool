package com.doccuty.radarplus.view.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.doccuty.radarplus.MainApp;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.network.RecoToolMqttServer;
import com.doccuty.radarplus.recommender.Recommender;
import com.doccuty.radarplus.view.listener.RootListener;
import com.fasterxml.jackson.core.JsonProcessingException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RootController implements Initializable {

	@FXML
	MenuItem mi_new;

	@FXML
	MenuItem mi_trainWalkingSpeed;

	@FXML
	MenuItem mi_save;

	@FXML
	MenuItem mi_openControlPanel;

	@FXML
	MenuItem mi_openTrainingScreen;

	@FXML
	MenuItem mi_openRegisterScreen;

	@FXML
	MenuItem mi_startNewEvaluation;

	@FXML
	MenuItem mi_openSystemPromptController;

	@FXML
	MenuItem mi_navigateToNextDestination;

	@FXML
	MenuItem mi_openImpactEvaluationScreen;

	@FXML
	MenuItem mi_saveScoresToFile;

	@FXML
	MenuItem mi_quitApplication;

	@FXML
	ImageView iv_loading;

	RecoTool app;

	private BorderPane rootLayout;
	private Scene scene;

	private Stage stage;
	private Stage secondStage;

	private ResourceBundle bundle;

	RootListener listener;

	public RootController() {
		this.listener = new RootListener(this);
		this.secondStage = new Stage();
		this.secondStage.initModality(Modality.APPLICATION_MODAL);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.bundle = resources;
	}

	public void init() {

		if (this.app == null)
			return;

		this.mi_save.setDisable(true);
		this.mi_trainWalkingSpeed.setDisable(true);

		this.app.addPropertyChangeListener(RecoTool.PROPERTY_CURRENT_USER, listener);
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_SAVED, listener);
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_UPDATED, listener);
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_RECOMMENDATIONS, listener);

		this.app.getMQTTClient().addPropertyChangeListener(RecoToolMqttServer.PROPERTY_TOPIC_UPDATE, listener);

		this.setCurrentUser(this.app.getCurrentUser());
	}

	public void setCurrentUser(User value) {
		boolean disable = (value == null || value.getId() == 0);

		this.mi_save.setDisable(disable);

		this.mi_openControlPanel.setDisable(disable);
		this.mi_openTrainingScreen.setDisable(disable);

		this.mi_trainWalkingSpeed.setDisable(disable);
	}

	/**
	 * Open SettingsScreen to configure RecoTool
	 * 
	 * @param ev
	 * @return
	 */

	@FXML
	public SettingsController openSettings(ActionEvent ev) {

		SettingsController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/SettingsScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));

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

	/**
	 * Open UserProfileController Screen to open an existing user profile
	 * 
	 * @param ev
	 * @return
	 */
	@FXML
	public OpenUserProfileController openUserProfile(ActionEvent ev) {

		OpenUserProfileController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/OpenUserProfileScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));

			AnchorPane personOverview = (AnchorPane) loader.load();
			Scene s = new Scene(personOverview);

			this.secondStage.setScene(s);

			controller = (OpenUserProfileController) loader.getController();
			controller.withApp(this.app).setStage(this.secondStage);

		} catch (IOException e) {
			e.printStackTrace();
		}

		this.secondStage.showAndWait();
		this.secondStage.toFront();

		return controller;
	}

	/**
	 * Open WalkingSpeedTrainerController to train a user's walking speed into the
	 * system
	 * 
	 * @param ev
	 * @return
	 */
	@FXML
	public WalkingSpeedTrainerController openWalkingSpeedTrainerScreen(ActionEvent ev) {

		WalkingSpeedTrainerController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/WalkingSpeedTrainerScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));

			AnchorPane personOverview = (AnchorPane) loader.load();
			Scene s = new Scene(personOverview);

			this.secondStage.setScene(s);

			controller = (WalkingSpeedTrainerController) loader.getController();
			controller.withStudyApp(this.app).withStage(this.secondStage).init();

		} catch (IOException e) {
			e.printStackTrace();
		}

		this.secondStage.showAndWait();
		this.secondStage.toFront();

		return controller;
	}

	/**
	 * Navigation to next destination which is defined in settings
	 * 
	 * @param ev
	 */
	@FXML
	public void navigateToNextDestination(ActionEvent ev) {
		this.app.startNavigationToLastDestination();
	}

	/**
	 * Opens ImpactEvaluationController
	 * 
	 * @param ev
	 * @return
	 */
	@FXML
	public ImpactEvaluationController openImpactEvaluationScreen(ActionEvent ev) {

		this.startNewEvaluation(null);
		
		ImpactEvaluationController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/ImpactEvaluationScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));

			AnchorPane personOverview = (AnchorPane) loader.load();
			Scene s = new Scene(personOverview);

			this.secondStage.setScene(s);

			controller = (ImpactEvaluationController) loader.getController();
			controller.withApp(this.app).withStage(this.secondStage).init();

		} catch (IOException e) {
			e.printStackTrace();
		}

		this.secondStage.showAndWait();
		this.secondStage.toFront();

		return controller;
	}

	/**
	 * Saves current scores of listed items in WoZ-Controller Screen
	 * 
	 * @param ev
	 */
	@FXML
	public void saveCurrentScores(ActionEvent ev) {
		this.app.getResultTracker().writeToCSV(this.app.getRecommender().getRecommendations(),
				this.app.getCurrentUser(), this.app.getSetting());
	}

	/**
	 * Opens SavedItemScoresController which provides a list of all saved item
	 * scores
	 * 
	 * @param ev
	 * @return
	 */
	@FXML
	public SavedItemScoresController openResultFileScreen(ActionEvent ev) {
		SavedItemScoresController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/SavedItemScores.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));

			AnchorPane personOverview = (AnchorPane) loader.load();
			Scene s = new Scene(personOverview);

			this.secondStage.setScene(s);

			controller = (SavedItemScoresController) loader.getController();
			controller.withApp(this.app).withStage(this.secondStage).init();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.secondStage.showAndWait();

		return controller;
	}

	/**
	 * Opens AboutScreen to gather more information about RecoTool
	 * 
	 * @param ev
	 * @return
	 */
	@FXML
	public AboutController openAboutScreen(ActionEvent ev) {

		AboutController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/AboutScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));

			AnchorPane personOverview = (AnchorPane) loader.load();
			Scene s = new Scene(personOverview);

			this.secondStage.setScene(s);

			controller = (AboutController) loader.getController();
			controller.withApp(this.app).withStage(this.secondStage).init();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.secondStage.showAndWait();

		return controller;
	}

	/**
	 * Opens SystemPromptController to send Messages to DataGlasses Client via MQTT
	 * 
	 * @param ev
	 * @return
	 */
	@FXML
	public SystemPromptController openSystemPromptController(ActionEvent ev) {

		SystemPromptController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/SystemPromptScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));

			AnchorPane personOverview = (AnchorPane) loader.load();
			Scene s = new Scene(personOverview);

			this.secondStage.setScene(s);

			controller = (SystemPromptController) loader.getController();
			controller.withApp(this.app).withStage(this.secondStage);

		} catch (IOException e) {
			e.printStackTrace();
		}

		this.secondStage.showAndWait();

		return controller;
	}

	/**
	 * Start a new Evaluation and keep/discard current setting
	 * 
	 * @param ev
	 */
	@FXML
	public void startNewEvaluation(ActionEvent ev) {
		try {
			if (!this.app.getEvaluationRunning() || this.stopEvaluation()) {
				this.app.withNavigationDestination(null).withCurrentItem(null).prepareEvaluation(false);

				if (this.app.getRecommender().getMode().equals(Recommender.PROPERTY_EFFICIENCY_MODE)) {
					this.app.getRecommender().setMode(Recommender.PROPERTY_ABIDANCE_MODE);
					this.app.switchedRecommendationMode(Recommender.PROPERTY_EFFICIENCY_MODE,
							Recommender.PROPERTY_ABIDANCE_MODE);
				}
			}
		} catch (MqttException | JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void quitApplication(ActionEvent ev) {
		System.exit(0);
	}

	/**
	 * Reset everything
	 * 
	 * @param ev
	 */
	@FXML
	public void startNew(ActionEvent ev) {
		this.app.getRecommender().resetRecommendations();
		this.app.withCurrentItem(null).withCurrentUser(new User()).getSetting().getUsedItem().clear();
		this.showRegisterScreen();
	}

	/**
	 * Save current user
	 * 
	 * @param ev
	 */
	@FXML
	public void save(ActionEvent ev) {

		// No user selected
		if (this.app.getCurrentUser() == null || this.app.getCurrentUser().getFirstname() == null) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Kein Benutzer");
			alert.setHeaderText(null);
			alert.setContentText("Es wurde kein Benutzer gewählt!");
			alert.showAndWait();
			return;
		}

		// Evaluation running
		if (this.app.getEvaluationRunning()) {

			String title = bundle.getString("evaluationRunning");
			String msg = bundle.getString("saveUserMessage");

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(msg);

			Optional<ButtonType> option = alert.showAndWait();

			if (option.get() == ButtonType.OK) {
				this.app.saveUser();
			}

			return;
		}

		this.app.saveUser();
	}

	@FXML
	public void openRegisterScreen(ActionEvent ev) {
		this.showRegisterScreen();
	}

	@FXML
	public void openTrainingScreen(ActionEvent ev) {
		this.showTrainingScreen();
	}

	@FXML
	public void openControlPanel(ActionEvent ev) {
		this.showControllerScreen();
	}

	// ===================================================

	public RecoTool getStudyApp() {
		return this.app;
	}

	public void setStudyApp(RecoTool value) {
		this.app = value;
	}

	public RootController withStudyApp(RecoTool value) {
		this.setStudyApp(value);
		return this;
	}

	// ===================================================

	/**
	 * Shows the registration screen inside the root layout.
	 */
	public RegisterController showRegisterScreen() {

		RegisterController controller = null;

		if (this.app.getEvaluationRunning() && !this.stopEvaluation()) {
			return null;
		}

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/RegisterScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));

			AnchorPane personOverview = (AnchorPane) loader.load();

			// Set person overview into the center of root layout.
			rootLayout.setCenter(personOverview);

			controller = (RegisterController) loader.getController();
			controller.withStudyApp(this.app).withParent(this).setListeners();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return controller;
	}

	/**
	 * Shows the training screen inside the root layout.
	 */

	public TrainingController showTrainingScreen() {

		if (this.app.getCurrentUser().getId() <= 0) {
			this.noUserAlert();
			return null;
		}

		if (this.app.getEvaluationRunning() && !this.stopEvaluation()) {
			return null;
		}

		TrainingController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/TrainingScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));
			AnchorPane personOverview = (AnchorPane) loader.load();

			// Set person overview into the center of root layout.
			rootLayout.setCenter(personOverview);

			controller = (TrainingController) loader.getController();
			controller.withStudyApp(this.app).withParent(this).setListeners();

			this.app.getTrainer().nextPair();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return controller;
	}

	/**
	 * Shows information screen after user initialization
	 */

	public InfoController showInfoScreen() {

		InfoController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/InfoScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));
			AnchorPane personOverview = (AnchorPane) loader.load();

			// Set person overview into the center of root layout.
			rootLayout.setCenter(personOverview);

			controller = (InfoController) loader.getController();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return controller;
	}

	/**
	 * Shows the training screen inside the root layout.
	 */

	public WoZController showControllerScreen() {

		if (this.app.getCurrentUser().getId() <= 0) {
			this.noUserAlert();
			return null;
		}

		if (this.app.getStartTrafficJunction() == null || this.app.getEndTrafficJunction() == null
				|| this.app.getStartTrafficJunction().getId() == 0 || this.app.getEndTrafficJunction().getId() == 0) {
			this.noTrafficJunctionAlert();
			return null;
		}

		if (this.app.getEvaluationRunning() && !this.stopEvaluation()) {
			return null;
		}

		WoZController controller = null;

		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/ControllerScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(MainApp.LOCALES_FILE_PATH, MainApp.getSystemLocale()));

			AnchorPane personOverview = (AnchorPane) loader.load();

			// Set person overview into the center of root layout.
			rootLayout.setCenter(personOverview);

			controller = (WoZController) loader.getController();
			controller.withStudyApp(this.app).withParent(this).init();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return controller;
	}

	private void noUserAlert() {

		String title = bundle.getString("noUser");
		String message = bundle.getString("noUserMessage");

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}

	private void noTrafficJunctionAlert() {

		String title = bundle.getString("noTrafficJunction");
		String message = bundle.getString("noTrafficJunctionMessage");

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}

	public void setEvaluationRunning(boolean value) {
		this.mi_startNewEvaluation.setDisable(!value);
		this.mi_openSystemPromptController.setDisable(!value);
		this.mi_navigateToNextDestination.setDisable(!value);
		this.mi_openImpactEvaluationScreen.setDisable(!value);
		this.mi_saveScoresToFile.setDisable(!value);
	}

	public boolean stopEvaluation() {

		String title = bundle.getString("evaluationRunning");
		String message = bundle.getString("evaluationCancelMessage");

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		ButtonType cancelButton = new ButtonType("Setting beibehalten", ButtonData.NO);

		alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL, cancelButton);

		Optional<ButtonType> option = alert.showAndWait();

		if (option.get() == ButtonType.OK) {
			this.app.stopEvaluation(true);
			return true;
		} else if (option.get() == cancelButton) {
			this.app.stopEvaluation(false);
			return true;
		}

		return false;
	}

	// ================================================

	public BorderPane getRootLayout() {
		return this.rootLayout;
	}

	public void setRootLayout(BorderPane value) {
		this.rootLayout = value;
	}

	public RootController withRootLayout(BorderPane value) {
		this.setRootLayout(value);
		return this;
	}

	// ================================================

	public Scene getScene() {
		return this.scene;
	}

	public void setScene(Scene value) {
		this.scene = value;
	}

	public RootController withScene(Scene value) {
		this.setScene(value);
		return this;
	}

	// ================================================

	public Stage getStage() {
		return this.stage;
	}

	public RootController setStage(Stage value) {
		this.stage = value;
		return this;
	}

	public RootController withStage(Stage value) {
		this.setStage(value);
		return this;
	}
}
