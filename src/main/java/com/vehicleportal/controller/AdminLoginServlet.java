package com.vehicleportal.controller;

import com.vehicleportal.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/admin-login")
public class AdminLoginServlet extends HttpServlet {
private String hashPassword(String password) throws Exception {

    java.security.MessageDigest md =
            java.security.MessageDigest.getInstance("SHA-256");

    byte[] hash = md.digest(
            password.getBytes(java.nio.charset.StandardCharsets.UTF_8)
    );

    StringBuilder hex = new StringBuilder();

    for (byte b : hash) {
        hex.append(String.format("%02x", b));
    }

    return hex.toString();
}
    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String sql =
                "SELECT id, name, role FROM users " +
                "WHERE email = ? AND password = ? AND role = 'ADMIN'";

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email);
statement.setString(2, hashPassword(password));
            ResultSet result = statement.executeQuery();

            if (result.next()) {

                HttpSession session =
                        request.getSession();

                session.setAttribute(
                        "adminId",
                        result.getInt("id")
                );

                session.setAttribute(
                        "adminName",
                        result.getString("name")
                );

                session.setAttribute(
                        "adminRole",
                        result.getString("role")
                );

                response.sendRedirect("admin-dashboard");

            } else {

                response.setContentType("text/html");

                response.getWriter().println(
                    "<h2>Invalid Admin Login</h2>" +
                    "<a href='admin-login.html'>Try Again</a>"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType("text/html");

            response.getWriter().println(
                "<h2>Admin login failed.</h2>" +
                "<p>Please check the server.</p>"
            );
        }
    }
}
