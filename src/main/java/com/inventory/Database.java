package com.inventory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:inventory.db";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createNewDatabase() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                // This message can be removed in production
                // System.out.println("A new database has been created or connection
                // established.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createInventoryTable() {
        String sql = "CREATE TABLE IF NOT EXISTS inventory (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " name text NOT NULL,\n"
                + " quantity integer NOT NULL,\n"
                + " price real NOT NULL\n"
                + ");";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // New method to create the suppliers table
    public static void createSuppliersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS suppliers (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " name text NOT NULL,\n"
                + " contact_person text,\n"
                + " email text,\n"
                + " phone text\n"
                + ");";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
