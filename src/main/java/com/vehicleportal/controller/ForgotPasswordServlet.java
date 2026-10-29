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
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Base64;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private static final String RESET_BASE_URL =
            "http://localhost:8080/AUTOLUX/reset-password.html?token=";

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {

            showMessage(
                    response,
                    "Email Required",
                    "Please enter your registered email address.",
                    false
            );

            return;
        }

        email = email.trim();

        try (Connection connection =
                     DBConnection.getConnection()) {

            int userId = -1;

            String userSQL =
                    "SELECT id FROM users WHERE email = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(userSQL)) {

                statement.setString(1, email);

                try (ResultSet result =
                             statement.executeQuery()) {

                    if (result.next()) {
                        userId = result.getInt("id");
                    }
                }
            }

            /*
             * Always use the same message whether the
             * email exists or not.
             */
            if (userId == -1) {

                showMessage(
                        response,
                        "Check Your Email",
                        "If an account exists for that email, " +
                        "a password reset link has been generated.",
                        true
                );

                return;
            }

            /*
             * Generate a secure random token.
             */
            SecureRandom secureRandom =
                    new SecureRandom();

            byte[] tokenBytes =
                    new byte[32];

            secureRandom.nextBytes(tokenBytes);

            String token =
                    Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(tokenBytes);

            /*
             * Store only the SHA-256 hash of the token.
             */
            String tokenHash =
                    hashToken(token);

            /*
             * Token expires after 15 minutes.
             */
            LocalDateTime expiresAt =
                    LocalDateTime.now()
                            .plusMinutes(15);

            /*
             * Invalidate previous unused tokens.
             */
            String invalidateSQL =
                    "UPDATE password_reset_tokens " +
                    "SET used = TRUE " +
                    "WHERE user_id = ? AND used = FALSE";

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 invalidateSQL)) {

                statement.setInt(1, userId);

                statement.executeUpdate();
            }

            /*
             * Save new token.
             */
            String insertSQL =
                    "INSERT INTO password_reset_tokens " +
                    "(user_id, token_hash, expires_at) " +
                    "VALUES (?, ?, ?)";

            try (PreparedStatement statement =
                         connection.prepareStatement(insertSQL)) {

                statement.setInt(1, userId);
                statement.setString(2, tokenHash);
                statement.setObject(3, expiresAt);

                statement.executeUpdate();
            }

            /*
             * LOCAL DEVELOPMENT ONLY:
             * Show the reset link on screen instead of
             * sending an email.
             */
            String resetLink =
                    RESET_BASE_URL + token;

            showMessageWithLink(
                    response,
                    resetLink
            );

        } catch (Exception e) {

            e.printStackTrace();

            showMessage(
                    response,
                    "Something Went Wrong",
                    "We couldn't process your request right now.",
                    false
            );
        }
    }

    private String hashToken(String token)
            throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                digest.digest(
                    token.getBytes(
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

    private void showMessage(
            HttpServletResponse response,
            String title,
            String message,
            boolean success)
            throws IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String icon = success ? "📧" : "⚠️";

        response.getWriter().println(
                createPage(
                        icon,
                        title,
                        message,
                        null
                )
        );
    }

    private void showMessageWithLink(
            HttpServletResponse response,
            String resetLink)
            throws IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String page =
                createPage(
                        "🔐",
                        "Reset Link Generated",
                        "For local development, your password reset link " +
                        "is shown below. In production, this link would be " +
                        "sent to the user's email.",
                        resetLink
                );

        response.getWriter().println(page);
    }

private String createPage(
        String icon,
        String title,
        String message,
        String resetLink) {

    String linkHTML = "";

    if (resetLink != null) {

        linkHTML =
                "<a href='" +
                resetLink +
                "' class='reset-button'>" +
                "Continue to Reset Password" +
                "</a>";
    }

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

.message {
    color: #64748b;

    line-height: 1.6;

    margin-bottom: 25px;
}

.reset-button {
    display: block;

    padding: 14px;

    background: #2563eb;

    color: white;

    border-radius: 9px;

    text-decoration: none;

    font-weight: bold;

    transition: 0.2s;
}

.reset-button:hover {
    background: #1d4ed8;

    transform: translateY(-1px);
}

.login-link {
    display: block;

    margin-top: 18px;

    color: #64748b;

    text-decoration: none;

    font-size: 14px;
}

.login-link:hover {
    color: #2563eb;
}

</style>

</head>

<body>

<div class="card">

<div class="icon">__ICON__</div>

<h1>__TITLE__</h1>

<p class="message">__MESSAGE__</p>

__LINK__

<a href="login.html"
   class="login-link">
Back to Login
</a>

</div>

</body>

</html>
""";

    page = page.replace(
            "__ICON__",
            escapeHtml(icon)
    );

    page = page.replace(
            "__TITLE__",
            escapeHtml(title)
    );

    page = page.replace(
            "__MESSAGE__",
            escapeHtml(message)
    );

    page = page.replace(
            "__LINK__",
            linkHTML
    );

    return page;
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
