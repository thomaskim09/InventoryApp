package com.inventory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController {

    @FXML
    private Label totalItemsLabel;
    @FXML
    private Label totalStockLabel;
    @FXML
    private Label totalValueLabel;
    @FXML
    private ListView<String> lowStockListView;

    private ObservableList<String> lowStockItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        lowStockListView.setItems(lowStockItems);
        loadDashboardData();
    }

    private void loadDashboardData() {
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

        // Update the labels
        totalItemsLabel.setText(String.valueOf(totalItems));
        totalStockLabel.setText(String.valueOf(totalStock));

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        totalValueLabel.setText(currencyFormatter.format(totalValue));
    }
}
