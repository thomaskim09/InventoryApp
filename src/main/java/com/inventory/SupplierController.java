package com.inventory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class SupplierController {

    // Table and Columns
    @FXML
    private TableView<Supplier> tableView;
    @FXML
    private TableColumn<Supplier, Integer> idColumn;
    @FXML
    private TableColumn<Supplier, String> nameColumn;
    @FXML
    private TableColumn<Supplier, String> contactColumn;
    @FXML
    private TableColumn<Supplier, String> emailColumn;
    @FXML
    private TableColumn<Supplier, String> phoneColumn;

    // Form Fields
    @FXML
    private TextField nameInput;
    @FXML
    private TextField contactInput;
    @FXML
    private TextField emailInput;
    @FXML
    private TextField phoneInput;

    private ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        loadData();
        tableView.setItems(supplierList);

        // Add listener for table selection
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateSupplierDetails(newSelection);
                    }
                });
    }

    private void loadData() {
        int selectedId = -1;
        if (tableView.getSelectionModel().getSelectedItem() != null) {
            selectedId = tableView.getSelectionModel().getSelectedItem().getId();
        }

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

        // Re-select the previously selected item
        int finalSelectedId = selectedId;
        tableView.getItems().stream()
                .filter(supplier -> supplier.getId() == finalSelectedId)
                .findFirst()
                .ifPresent(supplier -> tableView.getSelectionModel().select(supplier));
    }

    private void populateSupplierDetails(Supplier supplier) {
        nameInput.setText(supplier.getName());
        contactInput.setText(supplier.getContactPerson());
        emailInput.setText(supplier.getEmail());
        phoneInput.setText(supplier.getPhone());
    }

    @FXML
    private void handleNewSupplier() {
        clearFields();
        tableView.getSelectionModel().clearSelection();
        nameInput.requestFocus();
    }

    @FXML
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

    @FXML
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
        // Can add more validation here (e.g., email format)
        return true;
    }

    private void clearFields() {
        nameInput.clear();
        contactInput.clear();
        emailInput.clear();
        phoneInput.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
