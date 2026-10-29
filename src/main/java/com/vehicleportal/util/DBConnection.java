package com.vehicleportal.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = System.getenv().getOrDefault(
        "DB_URL",
        "jdbc:mysql://" +
        System.getenv().getOrDefault("DB_HOST", "localhost") + ":" +
        System.getenv().getOrDefault("DB_PORT", "3306") + "/" +
        System.getenv().getOrDefault("DB_NAME", "vehicle_service_portal")
    );

    private static final String USER =
        System.getenv().getOrDefault("DB_USER", "root");

    private static final String PASSWORD =
        System.getenv().getOrDefault("DB_PASSWORD", "");

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found!", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
