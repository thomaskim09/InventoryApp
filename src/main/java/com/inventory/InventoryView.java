package com.inventory;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryView {

    private final TableView<Item> tableView = new TableView<>();
    private final TextField nameInput = new TextField();
    private final TextField quantityInput = new TextField();
    private final TextField priceInput = new TextField();
    private final TextField searchInput = new TextField();

    private final Button newButton = new Button("Clear");
    private final Button saveButton = new Button("Save");
    private final Button deleteButton = new Button("Delete");

    private final ObservableList<Item> itemList = FXCollections.observableArrayList();
    private FilteredList<Item> filteredData;

    public VBox getView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("root");

        // Search Bar
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchInput.setPromptText("Enter item name...");
        HBox.setHgrow(searchInput, Priority.ALWAYS);
        searchBox.getChildren().addAll(new Label("Search by Name:"), searchInput);

        // Table
        setupTable();

        // Details Pane
        VBox detailsPane = createDetailsPane();

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(tableView, detailsPane);
        splitPane.setDividerPositions(0.6);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        root.getChildren().addAll(searchBox, splitPane);

        loadData();
        setupFiltering();
        setupSelectionListener();
        setupButtonListeners();
        updateButtonStates();

        return root;
    }

    private void setupTable() {
        TableColumn<Item, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(75);

        TableColumn<Item, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);

        TableColumn<Item, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(100);

        TableColumn<Item, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setPrefWidth(100);

        tableView.getColumns().addAll(idColumn, nameColumn, quantityColumn, priceColumn);
    }

    private VBox createDetailsPane() {
        VBox detailsBox = new VBox(20);
        detailsBox.setPadding(new Insets(10));
        detailsBox.getStyleClass().add("details-pane");

        Label detailsTitle = new Label("Item Details");
        detailsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        nameInput.setPromptText("Name");
        quantityInput.setPromptText("Quantity");
        priceInput.setPromptText("Price");

        grid.add(new Label("Name"), 0, 0);
        grid.add(nameInput, 1, 0);
        grid.add(new Label("Quantity"), 0, 1);
        grid.add(quantityInput, 1, 1);
        grid.add(new Label("Price"), 0, 2);
        grid.add(priceInput, 1, 2);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(newButton, saveButton, deleteButton);

        detailsBox.getChildren().addAll(detailsTitle, grid, buttonBox);
        return detailsBox;
    }

    private void setupFiltering() {
        filteredData = new FilteredList<>(itemList, p -> true);
        searchInput.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(item -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return item.getName().toLowerCase().contains(lowerCaseFilter);
            });
        });
        tableView.setItems(filteredData);
    }

    private void setupSelectionListener() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateItemDetails(newSelection);
                    }
                    updateButtonStates();
                });
    }

    private void setupButtonListeners() {
        newButton.setOnAction(e -> handleNewItem());
        saveButton.setOnAction(e -> handleSaveItem());
        deleteButton.setOnAction(e -> handleDeleteItem());

        // Add listeners to text fields to update button states
        nameInput.textProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
        quantityInput.textProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
        priceInput.textProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
    }

    private void updateButtonStates() {
        boolean hasText = !nameInput.getText().trim().isEmpty() ||
                !quantityInput.getText().trim().isEmpty() ||
                !priceInput.getText().trim().isEmpty();
        boolean isItemSelected = tableView.getSelectionModel().getSelectedItem() != null;

        newButton.setDisable(!hasText);
        deleteButton.setDisable(!isItemSelected);
        saveButton.setDisable(nameInput.getText().trim().isEmpty());
    }

    private void populateItemDetails(Item item) {
        nameInput.setText(item.getName());
        quantityInput.setText(String.valueOf(item.getQuantity()));
        priceInput.setText(String.valueOf(item.getPrice()));
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
    }

    private void handleNewItem() {
        clearFields();
        tableView.getSelectionModel().clearSelection();
        nameInput.requestFocus();
    }

    private void handleSaveItem() {
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
        updateButtonStates();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}