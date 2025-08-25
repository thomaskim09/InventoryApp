package com.inventory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SuppliersView {

    private final TableView<Supplier> tableView = new TableView<>();
    private final TextField nameInput = new TextField();
    private final TextField contactInput = new TextField();
    private final TextField emailInput = new TextField();
    private final TextField phoneInput = new TextField();

    private final Button newButton = new Button("Clear");
    private final Button saveButton = new Button("Save");
    private final Button deleteButton = new Button("Delete");

    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    public VBox getView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("root");

        // Table
        setupTable();

        // Details Pane
        VBox detailsPane = createDetailsPane();

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(tableView, detailsPane);
        splitPane.setDividerPositions(0.6);

        root.getChildren().add(splitPane);

        loadData();
        setupSelectionListener();
        setupButtonListeners();
        updateButtonStates();

        return root;
    }

    private void setupTable() {
        TableColumn<Supplier, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Supplier, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Supplier, String> contactColumn = new TableColumn<>("Contact Person");
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));

        TableColumn<Supplier, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Supplier, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        tableView.getColumns().addAll(idColumn, nameColumn, contactColumn, emailColumn, phoneColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setItems(supplierList);
    }

    private VBox createDetailsPane() {
        VBox detailsBox = new VBox(20);
        detailsBox.setPadding(new Insets(10));
        detailsBox.getStyleClass().add("details-pane");

        Label detailsTitle = new Label("Supplier Details");
        detailsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        nameInput.setPromptText("Supplier Name");
        contactInput.setPromptText("Contact Person");
        emailInput.setPromptText("Email Address");
        phoneInput.setPromptText("Phone Number");

        grid.add(new Label("Name"), 0, 0);
        grid.add(nameInput, 1, 0);
        grid.add(new Label("Contact"), 0, 1);
        grid.add(contactInput, 1, 1);
        grid.add(new Label("Email"), 0, 2);
        grid.add(emailInput, 1, 2);
        grid.add(new Label("Phone"), 0, 3);
        grid.add(phoneInput, 1, 3);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(newButton, saveButton, deleteButton);

        detailsBox.getChildren().addAll(detailsTitle, grid, buttonBox);
        return detailsBox;
    }

    private void setupSelectionListener() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateSupplierDetails(newSelection);
                    }
                    updateButtonStates();
                });
    }

    private void setupButtonListeners() {
        newButton.setOnAction(e -> handleNewSupplier());
        saveButton.setOnAction(e -> handleSaveSupplier());
        deleteButton.setOnAction(e -> handleDeleteSupplier());

        // Add listeners to text fields to update button states
        nameInput.textProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
        contactInput.textProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
        emailInput.textProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
        phoneInput.textProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
    }

    private void updateButtonStates() {
        boolean hasText = !nameInput.getText().trim().isEmpty() ||
                !contactInput.getText().trim().isEmpty() ||
                !emailInput.getText().trim().isEmpty() ||
                !phoneInput.getText().trim().isEmpty();
        boolean isItemSelected = tableView.getSelectionModel().getSelectedItem() != null;

        newButton.setDisable(!hasText);
        deleteButton.setDisable(!isItemSelected);
        saveButton.setDisable(nameInput.getText().trim().isEmpty());
    }

    private void loadData() {
        supplierList.clear();
        String sql = "SELECT * FROM suppliers";

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                supplierList.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact_person"),
                        rs.getString("email"),
                        rs.getString("phone")));
            }
        } catch (SQLException e) {
            System.out.println("Database Error on loading suppliers: " + e.getMessage());
        }
    }

    private void populateSupplierDetails(Supplier supplier) {
        nameInput.setText(supplier.getName());
        contactInput.setText(supplier.getContactPerson());
        emailInput.setText(supplier.getEmail());
        phoneInput.setText(supplier.getPhone());
    }

    private void handleNewSupplier() {
        clearFields();
        tableView.getSelectionModel().clearSelection();
        nameInput.requestFocus();
    }

    private void handleSaveSupplier() {
        Supplier selectedSupplier = tableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier == null) {
            addSupplier();
        } else {
            updateSupplier(selectedSupplier);
        }
    }

    private void addSupplier() {
        if (!validateInput())
            return;

        String sql = "INSERT INTO suppliers(name, contact_person, email, phone) VALUES(?,?,?,?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nameInput.getText());
            pstmt.setString(2, contactInput.getText());
            pstmt.setString(3, emailInput.getText());
            pstmt.setString(4, phoneInput.getText());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Database Error on adding supplier: " + e.getMessage());
        }
        loadData();
        clearFields();
    }

    private void updateSupplier(Supplier supplier) {
        if (!validateInput())
            return;

        String sql = "UPDATE suppliers SET name = ?, contact_person = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nameInput.getText());
            pstmt.setString(2, contactInput.getText());
            pstmt.setString(3, emailInput.getText());
            pstmt.setString(4, phoneInput.getText());
            pstmt.setInt(5, supplier.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Database Error on updating supplier: " + e.getMessage());
        }
        loadData();
    }

    private void handleDeleteSupplier() {
        Supplier selectedSupplier = tableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier != null) {
            String sql = "DELETE FROM suppliers WHERE id = ?";
            try (Connection conn = Database.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedSupplier.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Database Error on deleting supplier: " + e.getMessage());
            }
            loadData();
            clearFields();
        } else {
            showAlert("No Selection", "Please select a supplier in the table to delete.");
        }
    }

    private boolean validateInput() {
        String name = nameInput.getText();
        if (name == null || name.trim().isEmpty()) {
            showAlert("Validation Error", "Supplier name cannot be empty.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        nameInput.clear();
        contactInput.clear();
        emailInput.clear();
        phoneInput.clear();
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