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

@WebServlet("/my-bookings")
public class MyBookingsServlet extends HttpServlet {

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String context = request.getContextPath();

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        StringBuilder html = new StringBuilder();
        html.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>My Service Bookings | AUTOLUX</title>
                <link rel="stylesheet" href="%s/css/autolux-customer-pages.css">
            </head>
            <body class="autolux-page">
            <div class="ap-layout">
                <aside class="ap-sidebar">
                    <a href="%s/dashboard.html" class="ap-brand">
                        <span class="ap-brand-icon">A</span>
                        <span class="ap-brand-name">AUTO<span>LUX</span></span>
                    </a>
                    <div class="ap-nav-label">CUSTOMER MENU</div>
                    <nav class="ap-nav">
                        <a href="%s/dashboard.html"><span class="ap-nav-icon">⌂</span>Dashboard</a>
                        <a href="%s/my-bookings" class="active"><span class="ap-nav-icon">▤</span>My Bookings</a>
                        <a href="%s/book-service-page"><span class="ap-nav-icon">＋</span>Book a Service</a>
                        <a href="%s/add-vehicle.html"><span class="ap-nav-icon">▣</span>Add Your Vehicle</a>
                        <a href="%s/features/garage"><span class="ap-nav-icon">◇</span>My Garage</a>
                        <a href="%s/logout" class="logout"><span class="ap-nav-icon">↗</span>Logout</a>
                    </nav>
                    <div class="ap-sidebar-bottom">
                        <div class="ap-sidebar-note">
                            <strong>EVERY SERVICE, TRACKED.</strong>
                            <p>Review your bookings and keep up with your vehicle's service schedule.</p>
                        </div>
                    </div>
                </aside>

                <main class="ap-main">
                    <div class="ap-topbar">
                        <div class="ap-breadcrumb">Customer Portal <span>/ My Bookings</span></div>
                        <div class="ap-status"><span class="ap-dot"></span> CUSTOMER AREA</div>
                    </div>
                    <header class="ap-heading">
                        <div class="ap-eyebrow">SERVICE HISTORY · 02</div>
                        <h1>My Service Bookings</h1>
                        <p>All your service requests, organised in one place.</p>
                    </header>
                    <section class="ap-card">
                        <div class="ap-table-wrap">
                            <table class="ap-table">
                                <thead>
                                    <tr>
                                        <th>Booking ID</th>
                                        <th>Vehicle</th>
                                        <th>Service</th>
                                        <th>Booking Date</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
            """.formatted(context, context, context, context, context, context, context, context));

        String sql = "SELECT b.booking_id, v.vehicle_number, b.service_type, " +
                     "b.booking_date, b.status " +
                     "FROM bookings b " +
                     "JOIN vehicles v ON b.vehicle_id = v.vehicle_id " +
                     "WHERE b.user_id = ? " +
                     "ORDER BY b.booking_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                boolean found = false;

                while (result.next()) {
                    found = true;
                    String status = escapeHtml(result.getString("status"));

                    html.append("<tr>")
                        .append("<td class=\"ap-id\">#")
                        .append(result.getInt("booking_id"))
                        .append("</td><td>")
                        .append(escapeHtml(result.getString("vehicle_number")))
                        .append("</td><td>")
                        .append(escapeHtml(result.getString("service_type")))
                        .append("</td><td>")
                        .append(escapeHtml(String.valueOf(result.getDate("booking_date"))))
                        .append("</td><td><span class=\"ap-status-pill\">")
                        .append(status)
                        .append("</span></td></tr>");
                }

                if (!found) {
                    html.append("<tr><td class=\"ap-empty\" colspan=\"5\">")
                        .append("No bookings found. Your service bookings will appear here.")
                        .append("</td></tr>");
                }
            }

        } catch (Exception e) {
            getServletContext().log("Unable to load customer bookings.", e);
            html.append("<tr><td class=\"ap-empty\" colspan=\"5\">")
                .append("Unable to load bookings right now. Please try again later.")
                .append("</td></tr>");
        }

        html.append("""
                                </tbody>
                            </table>
                        </div>
                        <div class="ap-form-actions">
                            <a class="ap-primary" href="%s/book-service-page">Book a Service <span>→</span></a>
                            <a class="ap-back" href="%s/dashboard.html">← Back to Dashboard</a>
                        </div>
                    </section>
                </main>
            </div>
            </body>
            </html>
            """.formatted(context, context));

        response.getWriter().write(html.toString());
    }
}
