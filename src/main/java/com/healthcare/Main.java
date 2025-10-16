package com.healthcare;

import java.io.IOException;

import com.healthcare.controller.LoginController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class for the Healthcare JavaFX Application
 * Simple Hello World application
 */
public class Main extends Application {
    
    @Override
    public void init() throws Exception {
        super.init();
        System.out.println("Initializing application...");
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting Healthcare JavaFX Application...");
            
            // Load the login FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            
            // Set up the login controller
            LoginController controller = fxmlLoader.getController();
            controller.setPrimaryStage(primaryStage);
            
            // Configure the primary stage
            primaryStage.setTitle("Healthcare Management System - Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
            
            // Show the stage
            primaryStage.show();
            
            System.out.println("Application started successfully");
            
        } catch (IOException e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Launching Healthcare JavaFX Application...");
        launch(args);
    }
}