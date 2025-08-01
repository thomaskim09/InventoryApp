package com.inventory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert; // Import the Alert class
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryController {

    @FXML
    private TableView<Item> tableView;
    @FXML
    private TableColumn<Item, Integer> idColumn;
    @FXML
    private TableColumn<Item, String> nameColumn;
    @FXML
    private TableColumn<Item, Integer> quantityColumn;
    @FXML
    private TableColumn<Item, Double> priceColumn;

    @FXML
    private TextField nameInput;
    @FXML
    private TextField quantityInput;
    @FXML
    private TextField priceInput;

    private ObservableList<Item> itemList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        loadData();
    }

    private void loadData() {
        itemList.clear();
        String sql = "SELECT * FROM inventory";

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                itemList.add(new Item(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        tableView.setItems(itemList);
    }

    @FXML
    private void handleAddItem() {
        String name = nameInput.getText();
        if (name.isEmpty()) {
            showAlert("Validation Error", "Name field cannot be empty.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityInput.getText());
            double price = Double.parseDouble(priceInput.getText());

            String sql = "INSERT INTO inventory(name, quantity, price) VALUES(?,?,?)";

            try (Connection conn = Database.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, price);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            loadData();
            clearFields();

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for quantity and price.");
        }
    }

    @FXML
    private void handleUpdateItem() {
        Item selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select an item in the table to update.");
            return;
        }

        String name = nameInput.getText();
        if (name.isEmpty()) {
            showAlert("Validation Error", "Name field cannot be empty.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityInput.getText());
            double price = Double.parseDouble(priceInput.getText());

            String sql = "UPDATE inventory SET name = ?, quantity = ?, price = ? WHERE id = ?";

            try (Connection conn = Database.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, name);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, price);
                pstmt.setInt(4, selectedItem.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            loadData();
            clearFields();

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for quantity and price.");
        }
    }

    @FXML
    private void handleDeleteItem() {
        Item selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String sql = "DELETE FROM inventory WHERE id = ?";

            try (Connection conn = Database.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selectedItem.getId());
                pstmt.executeUpdate();

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            loadData();
        } else {
            showAlert("No Selection", "Please select an item in the table to delete.");
        }
    }

    private void clearFields() {
        nameInput.clear();
        quantityInput.clear();
        priceInput.clear();
    }

    // New helper method for showing alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}