package com.vehicleportal.controller;

import com.vehicleportal.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) {

            response.setContentType("text/html");

            response.getWriter().println(
                "<h2>All fields are required!</h2>" +
                "<a href='login.html'>Try Again</a>"
            );

            return;
        }

        email = email.trim();

        String sql =
                "SELECT id, name, role FROM users " +
                "WHERE email = ? AND password = ?";

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, hashPassword(password));

            try (ResultSet result = statement.executeQuery()) {

if (!result.next()) {

    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    response.getWriter().println("""
<!DOCTYPE html>
<html lang="en">

<head>

<meta charset="UTF-8">

<meta name="viewport"
      content="width=device-width, initial-scale=1.0">

<title>Login Failed - Vehicle Service Portal</title>

<style>

* {
    box-sizing: border-box;
}

body {
    margin: 0;
    min-height: 100vh;

    display: flex;
    justify-content: center;
    align-items: center;

    padding: 20px;

    font-family: Arial, Helvetica, sans-serif;

    background: linear-gradient(
        135deg,
        #eef4ff,
        #dbeafe
    );
}

.error-card {
    width: 100%;
    max-width: 430px;

    background: white;

    padding: 40px 35px;

    border-radius: 18px;

    text-align: center;

    box-shadow:
        0 12px 35px
        rgba(30, 58, 138, 0.15);
}

.error-icon {
    width: 70px;
    height: 70px;

    margin: 0 auto 20px;

    display: flex;
    align-items: center;
    justify-content: center;

    border-radius: 50%;

    background: #fee2e2;

    color: #dc2626;

    font-size: 36px;
}

h1 {
    margin: 0 0 12px;

    color: #1e3a8a;

    font-size: 28px;
}

.message {
    margin: 0 auto 25px;

    color: #64748b;

    font-size: 16px;

    line-height: 1.6;
}

.try-again {
    display: block;

    padding: 14px;

    background: #2563eb;

    color: white;

    border-radius: 9px;

    text-decoration: none;

    font-weight: bold;

    font-size: 16px;

    transition: 0.2s;
}

.try-again:hover {
    background: #1d4ed8;

    transform: translateY(-1px);
}

.register {
    margin-top: 18px;

    color: #64748b;

    font-size: 14px;
}

.register a {
    color: #2563eb;

    font-weight: bold;

    text-decoration: none;
}

.register a:hover {
    text-decoration: underline;
}

</style>

</head>

<body>

<div class="error-card">

    <div class="error-icon">
        !
    </div>

    <h1>Login Unsuccessful</h1>

    <p class="message">
        The email or password you entered is incorrect.
        Please check your details and try again.
    </p>

    <a href="login.html"
       class="try-again">
        Try Again
    </a>

    <div class="register">

        Don't have an account?

        <a href="register.html">
            Create an account
        </a>

    </div>

</div>

</body>

</html>
""");

    return;
}
                int userId = result.getInt("id");
                String name = result.getString("name");
                String role = result.getString("role");

                HttpSession session = request.getSession();

                session.setAttribute("userId", userId);
                session.setAttribute("userName", name);
                session.setAttribute("userRole", role);

                String loginSQL =
                        "INSERT INTO login_history " +
                        "(user_id) VALUES (?)";

                try (PreparedStatement loginStatement =
                             connection.prepareStatement(loginSQL)) {

                    loginStatement.setInt(1, userId);
                    loginStatement.executeUpdate();
                }

                if ("ADMIN".equalsIgnoreCase(role)) {

                    session.setAttribute("adminId", userId);

                    response.sendRedirect("admin-dashboard");
                    return;
                }

                showDashboard(response, name);
            }

} catch (Exception e) {

    e.printStackTrace();

    response.setContentType("text/html");

    response.getWriter().println(
        "<h2>Login Error</h2>" +
        "<pre>" +
        e.toString() +
        "</pre>" +
        "<a href='login.html'>Try Again</a>"
    );
}    }

    private void showDashboard(HttpServletResponse response,
                               String name)
            throws IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String html = """
<!DOCTYPE html>
<html lang="en">

<head>

<meta charset="UTF-8">

<meta name="viewport"
      content="width=device-width, initial-scale=1.0">

<title>Vehicle Service Portal</title>

<style>

* {
    box-sizing: border-box;
}

body {
    margin: 0;
    padding: 35px 20px;
    font-family: Arial, Helvetica, sans-serif;
    background: #eef4ff;
    color: #1e293b;
}

.dashboard {
    max-width: 850px;
    margin: 0 auto;
}

.header {
    background: linear-gradient(135deg, #1e3a8a, #2563eb);
    color: white;
    padding: 40px;
    border-radius: 18px;
    margin-bottom: 25px;
    box-shadow: 0 10px 30px rgba(30, 58, 138, 0.20);
}

.header h1 {
    margin: 0 0 8px;
    font-size: 40px;
}

.header h2 {
    margin: 0;
    font-size: 22px;
    font-weight: normal;
}

.welcome {
    background: white;
    padding: 30px;
    border-radius: 15px;
    box-shadow: 0 5px 20px rgba(30, 58, 138, 0.09);
    margin-bottom: 25px;
}

.welcome h2 {
    margin-top: 0;
    color: #1e3a8a;
}

.welcome p {
    color: #475569;
    font-size: 17px;
    line-height: 1.6;
}

.actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
}

.action-card {
    background: white;
    padding: 30px;
    border-radius: 15px;
    box-shadow: 0 5px 20px rgba(30, 58, 138, 0.09);
    text-align: center;
}

.action-card:last-child {
    grid-column: 1 / 3;
    width: 50%;
    justify-self: center;
}

.icon {
    font-size: 45px;
    margin-bottom: 12px;
}

.action-card h3 {
    color: #1e3a8a;
    margin: 10px 0;
}

.action-card p {
    color: #64748b;
    min-height: 45px;
}

.action-button {
    display: block;
    padding: 14px 18px;
    margin-top: 20px;
    background: #2563eb;
    color: white;
    border-radius: 9px;
    text-decoration: none;
    font-weight: bold;
    transition: 0.2s;
}

.action-button:hover {
    background: #1d4ed8;
    transform: translateY(-2px);
}

.logout {
    display: block;
    width: 180px;
    margin: 30px auto 0;
    padding: 13px 20px;
    background: #dc2626;
    color: white;
    text-align: center;
    border-radius: 9px;
    text-decoration: none;
    font-weight: bold;
}

.logout:hover {
    background: #b91c1c;
}

.footer {
    text-align: center;
    margin-top: 25px;
    color: #64748b;
    font-size: 14px;
}

@media (max-width: 650px) {

    body {
        padding: 20px 10px;
    }

    .header {
        padding: 28px 22px;
    }

    .header h1 {
        font-size: 30px;
    }

    .actions {
        grid-template-columns: 1fr;
    }

    .action-card:last-child {
        grid-column: auto;
        width: 100%;
    }
}

</style>

</head>

<body>

<div class="dashboard">

<div class="header">

<h1>🚗 Vehicle Service Portal</h1>

<h2>Dashboard</h2>

</div>

<div class="welcome">

<h2>Welcome, %s! 👋</h2>

<p>
Your vehicle service booking portal is ready.
Manage your vehicles and service bookings
from the options below.
</p>

</div>

<div class="actions">

<div class="action-card">

<div class="icon">🚗</div>

<h3>Add Vehicle</h3>

<p>
Register your vehicle in the service portal.
</p>

<a href="add-vehicle.html"
   class="action-button">
Add Vehicle
</a>

</div>

<div class="action-card">

<div class="icon">🔧</div>

<h3>Book Vehicle Service</h3>

<p>
Schedule a service for your registered vehicle.
</p>

<a href="book-service-page"
   class="action-button">
Book Vehicle Service
</a>

</div>

<div class="action-card">

<div class="icon">📋</div>

<h3>My Service Bookings</h3>

<p>
View your bookings and their current status.
</p>

<a href="my-bookings"
   class="action-button">
My Service Bookings
</a>

</div>

</div>

<a href="logout"
   class="logout">
Logout
</a>

<div class="footer">
Vehicle Service Portal
</div>

</div>

</body>

</html>
""";

html = html.replace("%s", escapeHtml(name));

response.getWriter().println(html);    }

    private String hashPassword(String password)
            throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                digest.digest(
                    password.getBytes(StandardCharsets.UTF_8)
                );

        StringBuilder hex =
                new StringBuilder();

        for (byte b : hash) {

            String value =
                    Integer.toHexString(0xff & b);

            if (value.length() == 1) {
                hex.append('0');
            }

            hex.append(value);
        }

        return hex.toString();
    }

    private String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
