package com.vehicleportal.controller;

import com.vehicleportal.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String token =
                request.getParameter("token");

        String password =
                request.getParameter("password");

        String confirmPassword =
                request.getParameter("confirmPassword");

        /*
         * Basic validation.
         */
        if (token == null ||
            token.trim().isEmpty()) {

            showMessage(
                    response,
                    "Invalid Reset Link",
                    "This password reset link is invalid."
            );

            return;
        }

        if (password == null ||
            confirmPassword == null ||
            password.isEmpty() ||
            confirmPassword.isEmpty()) {

            showMessage(
                    response,
                    "Password Required",
                    "Please enter your new password."
            );

            return;
        }

        if (!password.equals(confirmPassword)) {

            showMessage(
                    response,
                    "Passwords Don't Match",
                    "The two passwords you entered do not match."
            );

            return;
        }

        if (password.length() < 6) {

            showMessage(
                    response,
                    "Password Too Short",
                    "Your password must contain at least 6 characters."
            );

            return;
        }

        try (Connection connection =
                     DBConnection.getConnection()) {

            String tokenHash =
                    hashToken(token);

            /*
             * Find a valid, unused, non-expired token.
             */
            String tokenSQL =
                    "SELECT id, user_id, expires_at " +
                    "FROM password_reset_tokens " +
                    "WHERE token_hash = ? " +
                    "AND used = FALSE " +
                    "AND expires_at > NOW()";

            int tokenId = -1;
            int userId = -1;

            try (PreparedStatement statement =
                         connection.prepareStatement(tokenSQL)) {

                statement.setString(1, tokenHash);

                try (ResultSet result =
                             statement.executeQuery()) {

                    if (result.next()) {

                        tokenId =
                                result.getInt("id");

                        userId =
                                result.getInt("user_id");
                    }
                }
            }

            /*
             * Token is invalid, expired, or already used.
             */
            if (tokenId == -1) {

                showMessage(
                        response,
                        "Reset Link Expired",
                        "This password reset link is invalid, " +
                        "expired, or has already been used."
                );

                return;
            }

            /*
             * Hash the new password.
             */
            String passwordHash =
                    hashPassword(password);

            /*
             * Update the user's password.
             */
            String updatePasswordSQL =
                    "UPDATE users " +
                    "SET password = ? " +
                    "WHERE id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 updatePasswordSQL)) {

                statement.setString(1, passwordHash);
                statement.setInt(2, userId);

                statement.executeUpdate();
            }

            /*
             * Mark the reset token as used.
             */
            String markUsedSQL =
                    "UPDATE password_reset_tokens " +
                    "SET used = TRUE " +
                    "WHERE id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 markUsedSQL)) {

                statement.setInt(1, tokenId);

                statement.executeUpdate();
            }

            showMessage(
                    response,
                    "Password Reset Successful",
                    "Your password has been changed successfully. " +
                    "You can now log in using your new password."
            );

        } catch (Exception e) {

            e.printStackTrace();

            showMessage(
                    response,
                    "Something Went Wrong",
                    "We couldn't reset your password right now. " +
                    "Please try again later."
            );
        }
    }

    private String hashPassword(String password)
            throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                digest.digest(
                    password.getBytes(
                        StandardCharsets.UTF_8
                    )
                );

        StringBuilder hex =
                new StringBuilder();

        for (byte b : hash) {

            String value =
                    Integer.toHexString(
                        b & 0xff
                    );

            if (value.length() == 1) {
                hex.append('0');
            }

            hex.append(value);
        }

        return hex.toString();
    }

    private String hashToken(String token)
            throws Exception {

        return hashPassword(token);
    }

    private void showMessage(
            HttpServletResponse response,
            String title,
            String message)
            throws IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String page = """
<!DOCTYPE html>
<html lang="en">

<head>

<meta charset="UTF-8">

<meta name="viewport"
      content="width=device-width, initial-scale=1.0">

<title>Password Reset</title>

<style>

* {
    box-sizing: border-box;
}

body {
    margin: 0;
    min-height: 100vh;

    display: flex;
    align-items: center;
    justify-content: center;

    padding: 20px;

    font-family: Arial, Helvetica, sans-serif;

    background: linear-gradient(
        135deg,
        #eef4ff,
        #dbeafe
    );
}

.card {
    width: 100%;
    max-width: 470px;

    background: white;

    padding: 40px 35px;

    border-radius: 18px;

    text-align: center;

    box-shadow:
        0 12px 35px
        rgba(30, 58, 138, 0.15);
}

.icon {
    font-size: 52px;
    margin-bottom: 15px;
}

h1 {
    margin: 0 0 15px;

    color: #1e3a8a;

    font-size: 28px;
}

p {
    color: #64748b;

    line-height: 1.6;

    margin-bottom: 25px;
}

a {
    display: block;

    padding: 14px;

    background: #2563eb;

    color: white;

    border-radius: 9px;

    text-decoration: none;

    font-weight: bold;
}

a:hover {
    background: #1d4ed8;
}

</style>

</head>

<body>

<div class="card">

<div class="icon">🔐</div>

<h1>__TITLE__</h1>

<p>__MESSAGE__</p>

<a href="login.html">
Back to Login
</a>

</div>

</body>

</html>
""";

        page = page.replace(
                "__TITLE__",
                escapeHtml(title)
        );

        page = page.replace(
                "__MESSAGE__",
                escapeHtml(message)
        );

        response.getWriter().println(page);
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
