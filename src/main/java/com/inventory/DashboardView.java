package com.inventory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardView {

    private Label totalItemsLabel = new Label("0");
    private Label totalStockLabel = new Label("0");
    private Label totalValueLabel = new Label("$0.00");
    private ListView<String> lowStockListView = new ListView<>();
    private ObservableList<String> lowStockItems = FXCollections.observableArrayList();

    public VBox getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("root");

        Label titleLabel = new Label("Inventory Dashboard");
        titleLabel.setFont(new Font("System Bold", 24));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        grid.add(createMetricCard("Total Unique Items", totalItemsLabel), 0, 0);
        grid.add(createMetricCard("Total Stock Quantity", totalStockLabel), 1, 0);
        grid.add(createMetricCard("Total Inventory Value", totalValueLabel), 2, 0);

        VBox lowStockBox = new VBox(10);
        lowStockBox.getStyleClass().add("card");
        lowStockBox.getChildren().addAll(new Label("Low Stock Items (Less than 10)"), lowStockListView);

        lowStockListView.setItems(lowStockItems);
        loadDashboardData();

        root.getChildren().addAll(titleLabel, grid, lowStockBox);
        return root;
    }

    private VBox createMetricCard(String title, Label metricLabel) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card");
        metricLabel.getStyleClass().add("metric-label");
        card.getChildren().addAll(new Label(title), metricLabel);
        return card;
    }

    public void loadDashboardData() {
        int totalItems = 0;
        int totalStock = 0;
        double totalValue = 0.0;
        lowStockItems.clear();

        String sql = "SELECT name, quantity, price FROM inventory";

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                totalItems++;
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                totalStock += quantity;
                totalValue += quantity * price;

                if (quantity < 10) {
                    lowStockItems.add(rs.getString("name") + " (Qty: " + quantity + ")");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error on loading dashboard data: " + e.getMessage());
        }

        totalItemsLabel.setText(String.valueOf(totalItems));
        totalStockLabel.setText(String.valueOf(totalStock));

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        totalValueLabel.setText(currencyFormatter.format(totalValue));
    }
}