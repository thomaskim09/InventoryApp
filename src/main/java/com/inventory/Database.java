package com.inventory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/inventory_db";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void createNewDatabase() {
        // This method is no longer needed for MySQL in the same way,
        // but we can use it to ensure the tables exist.
        createInventoryTable();
        createSuppliersTable();
    }

    public static void createInventoryTable() {
        String sql = "CREATE TABLE IF NOT EXISTS inventory (\n"
                + " id INT PRIMARY KEY AUTO_INCREMENT,\n"
                + " name VARCHAR(255) NOT NULL,\n"
                + " quantity INT NOT NULL,\n"
                + " price DOUBLE NOT NULL\n"
                + ");";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createSuppliersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS suppliers (\n"
                + " id INT PRIMARY KEY AUTO_INCREMENT,\n"
                + " name VARCHAR(255) NOT NULL,\n"
                + " contact_person VARCHAR(255),\n"
                + " email VARCHAR(255),\n"
                + " phone VARCHAR(255)\n"
                + ");";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}