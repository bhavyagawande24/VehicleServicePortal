package com.vehicleportal.controller;

import com.vehicleportal.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
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

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
if (name == null || name.trim().isEmpty() ||
    email == null || email.trim().isEmpty() ||
    phone == null || phone.trim().isEmpty() ||
    password == null || password.isEmpty()) {

    response.setContentType("text/html");
    response.getWriter().println(
        "<h2>All fields are required.</h2>" +
        "<a href='register.html'>Go Back</a>"
    );
    return;
}

if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

    response.setContentType("text/html");
    response.getWriter().println(
        "<h2>Please enter a valid email address.</h2>" +
        "<a href='register.html'>Go Back</a>"
    );
    return;
}

if (!phone.matches("\\d{10}")) {

    response.setContentType("text/html");
    response.getWriter().println(
        "<h2>Phone number must contain exactly 10 digits.</h2>" +
        "<a href='register.html'>Go Back</a>"
    );
    return;
}

if (password.length() < 6) {

    response.setContentType("text/html");
    response.getWriter().println(
        "<h2>Password must be at least 6 characters.</h2>" +
        "<a href='register.html'>Go Back</a>"
    );
    return;
}
        String sql = "INSERT INTO users (name, email, phone, password) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
statement.setString(4, hashPassword(password));
            statement.executeUpdate();

            response.sendRedirect("registration-success.html");

} catch (java.sql.SQLIntegrityConstraintViolationException e) {

    response.setContentType("text/html");

    response.getWriter().println(
        "<h2>Email already registered!</h2>" +
        "<p>Please use a different email address.</p>" +
        "<a href='register.html'>Go Back</a>"
    );

} catch (Exception e) {

    e.printStackTrace();

    response.setContentType("text/html");

    response.getWriter().println(
        "<h2>Registration failed!</h2>" +
        "<a href='register.html'>Go Back</a>"
    );
}    }
}
