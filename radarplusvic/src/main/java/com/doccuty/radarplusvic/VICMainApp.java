package com.doccuty.radarplusvic;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import com.doccuty.radarplusvic.model.VICPromptListener;
import com.doccuty.radarplusvic.view.controller.PromptController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * (c) by Niclas Kannengießer
 */

public class VICMainApp extends Application {

	public static final String LOCALES_FILE_PATH = "locales.promptlistener";

	private Stage primaryStage;
	private BorderPane rootLayout;

	PromptController promptController;

	static VICPromptListener app;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("VIC PromptListener");
		this.primaryStage.setMaximized(true);
		this.primaryStage.setOnCloseRequest(e -> quitEvent());

		app = new VICPromptListener();

		initRootLayout();

		app.init();
	}

	private void quitEvent() {
		VICMainApp.app.disconnectMQTT();
		System.exit(0);
	}

	/**
	 * Initializes the root layout.
	 */
	public void initRootLayout() {
		try {

			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("fxml/PromptScreen.fxml"));
			loader.setResources(ResourceBundle.getBundle(VICMainApp.LOCALES_FILE_PATH, VICMainApp.getSystemLocale()));
			
			rootLayout = (BorderPane) loader.load();

			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();

			promptController = (PromptController) loader.getController();
			promptController.withScene(scene).withStage(primaryStage).withApp(VICMainApp.app).init();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the main stage.
	 * 
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
	 * Get system language
	 * 
	 * @return
	 */
	public static Locale getSystemLocale() {
		String country = System.getProperty("user.country");
		return new Locale(String.format("%s", country.toLowerCase(), country.toUpperCase()));
	}
}
