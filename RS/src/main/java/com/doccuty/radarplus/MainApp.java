package com.doccuty.radarplus;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.view.controller.RootController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {
	
	private Stage primaryStage;
    private BorderPane rootLayout;
    
    public static RecoTool app;

    RootController rootController;

    
    public static void main(String[] args) {
        launch(args);
    }    
    
    @Override
    public void start(Stage primaryStage) {
    	
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("RecoTool");
        this.primaryStage.setMaximized(true);
        this.primaryStage.setOnCloseRequest(e -> quitEvent());

        initRootLayout();

        app = new RecoTool();
        app.init();
        
        rootController.withStudyApp(app).showRegisterScreen();
        rootController.init();
    }

    private void quitEvent() {
    		MainApp.app.disconnectMQTT();
    		System.exit(0);
    }
    
    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
      
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("fxml/RootLayout.fxml"));
            loader.setResources(ResourceBundle.getBundle("rectool", getSystemLocale()));
            
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            
            rootController = (RootController) loader.getController();
            rootController.withRootLayout(rootLayout).withScene(scene).withStage(primaryStage);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
       
    /**
     * Get system language
     * @return
     */ 
    public static Locale getSystemLocale(){
    	   String country = System.getProperty("user.country"); 
    	   return new Locale(String.format("%s", country.toLowerCase(),country.toUpperCase()));
    	}
}
