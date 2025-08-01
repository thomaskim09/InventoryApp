package com.inventory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
    @FXML
    private TextField searchInput;

    private ObservableList<Item> itemList = FXCollections.observableArrayList();
    private FilteredList<Item> filteredData;

    @FXML
    public void initialize() {
        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        loadData();

        // Setup filtered list for search functionality
        filteredData = new FilteredList<>(itemList, p -> true);
        searchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal));
        tableView.setItems(filteredData);

        // Add listener for table selection
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateItemDetails(newSelection);
                    }
                });
    }

    private void filterData(String searchText) {
        filteredData.setPredicate(item -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            return item.getName().toLowerCase().contains(lowerCaseFilter);
        });
    }

    private void populateItemDetails(Item item) {
        nameInput.setText(item.getName());
        quantityInput.setText(String.valueOf(item.getQuantity()));
        priceInput.setText(String.valueOf(item.getPrice()));
    }

    private void loadData() {
        int selectedId = -1;
        if (tableView.getSelectionModel().getSelectedItem() != null) {
            selectedId = tableView.getSelectionModel().getSelectedItem().getId();
        }

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

        // Re-select the previously selected item
        int finalSelectedId = selectedId;
        tableView.getItems().stream()
                .filter(item -> item.getId() == finalSelectedId)
                .findFirst()
                .ifPresent(item -> tableView.getSelectionModel().select(item));
    }

    @FXML
    private void handleNewItem() {
        clearFields();
        tableView.getSelectionModel().clearSelection();
        nameInput.requestFocus();
    }

    @FXML
    private void handleSaveItem() {
        // If an item is selected, it's an update. Otherwise, it's an addition.
        Item selectedItem = tableView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            handleAddItem();
        } else {
            handleUpdateItem(selectedItem);
        }
    }

    private void handleAddItem() {
        if (!validateInput())
            return;

        String sql = "INSERT INTO inventory(name, quantity, price) VALUES(?,?,?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nameInput.getText());
            pstmt.setInt(2, Integer.parseInt(quantityInput.getText()));
            pstmt.setDouble(3, Double.parseDouble(priceInput.getText()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        loadData();
        clearFields();
    }

    private void handleUpdateItem(Item selectedItem) {
        if (!validateInput())
            return;

        String sql = "UPDATE inventory SET name = ?, quantity = ?, price = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nameInput.getText());
            pstmt.setInt(2, Integer.parseInt(quantityInput.getText()));
            pstmt.setDouble(3, Double.parseDouble(priceInput.getText()));
            pstmt.setInt(4, selectedItem.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        loadData();
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
            clearFields();
        } else {
            showAlert("No Selection", "Please select an item in the table to delete.");
        }
    }

    private boolean validateInput() {
        String name = nameInput.getText();
        if (name == null || name.isEmpty()) {
            showAlert("Validation Error", "Name field cannot be empty.");
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityInput.getText());
            double price = Double.parseDouble(priceInput.getText());
            if (quantity < 0 || price < 0) {
                showAlert("Validation Error", "Quantity and price cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for quantity and price.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        nameInput.clear();
        quantityInput.clear();
        priceInput.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
