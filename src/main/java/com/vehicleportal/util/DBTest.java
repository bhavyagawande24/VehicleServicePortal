package com.vehicleportal.util;

import java.sql.Connection;

public class DBTest {

    public static void main(String[] args) {

        try {
            Connection connection = DBConnection.getConnection();

            System.out.println("================================");
            System.out.println("DATABASE CONNECTION SUCCESSFUL!");
            System.out.println("================================");

            connection.close();

        } catch (Exception e) {

            System.out.println("DATABASE CONNECTION FAILED!");
            e.printStackTrace();
        }
    }
}
