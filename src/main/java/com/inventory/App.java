package com.inventory;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Create database and tables on startup
        Database.createNewDatabase();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Dashboard Tab
        Tab dashboardTab = new Tab("Dashboard");
        DashboardView dashboardView = new DashboardView();
        dashboardTab.setContent(dashboardView.getView());

        // Inventory Tab
        Tab inventoryTab = new Tab("Inventory");
        InventoryView inventoryView = new InventoryView();
        inventoryTab.setContent(inventoryView.getView());

        // Suppliers Tab
        Tab suppliersTab = new Tab("Suppliers");
        SuppliersView suppliersView = new SuppliersView();
        suppliersTab.setContent(suppliersView.getView());

        tabPane.getTabs().addAll(dashboardTab, inventoryTab, suppliersTab);

        // Refresh dashboard when it's selected
        dashboardTab.setOnSelectionChanged(e -> {
            if (dashboardTab.isSelected()) {
                dashboardView.loadDashboardData();
            }
        });

        Scene scene = new Scene(tabPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("App.css").toExternalForm());

        stage.setTitle("Inventory Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}