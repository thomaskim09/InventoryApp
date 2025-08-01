package com.inventory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Create database and tables on startup
        Database.createNewDatabase();
        Database.createInventoryTable();
        Database.createSuppliersTable(); // Create the new suppliers table

        // Load the new main FXML layout
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main.fxml"));
        Parent root = fxmlLoader.load();

        // Create the scene and add the stylesheet
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("App.css").toExternalForm());

        stage.setTitle("Inventory Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
