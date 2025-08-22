package com.inventory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_HOST = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "inventory_db";
    private static final String DB_URL = DB_HOST + DB_NAME;
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void createNewDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_HOST, USER, PASS);
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        } catch (SQLException e) {
            System.out.println("Error creating database: " + e.getMessage());
        }
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