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

@WebServlet("/book-service-page")
public class BookServicePageServlet extends HttpServlet {

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

        StringBuilder vehicleOptions = new StringBuilder();
        boolean hasVehicles = false;

        String sql = "SELECT vehicle_id, vehicle_number, vehicle_type, model " +
                     "FROM vehicles WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    hasVehicles = true;
                    int vehicleId = result.getInt("vehicle_id");

                    vehicleOptions.append("<option value=\"")
                        .append(vehicleId)
                        .append("\">")
                        .append(escapeHtml(result.getString("vehicle_number")))
                        .append(" - ")
                        .append(escapeHtml(result.getString("vehicle_type")))
                        .append(" - ")
                        .append(escapeHtml(result.getString("model")))
                        .append("</option>");
                }
            }

        } catch (Exception e) {
            getServletContext().log("Unable to load customer vehicles.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               "Unable to load your vehicles. Please try again later.");
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Book a Service | AUTOLUX</title>
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
                        <a href="%s/my-bookings"><span class="ap-nav-icon">▤</span>My Bookings</a>
                        <a href="%s/book-service-page" class="active"><span class="ap-nav-icon">＋</span>Book a Service</a>
                        <a href="%s/add-vehicle.html"><span class="ap-nav-icon">▣</span>Add Your Vehicle</a>
                        <a href="%s/features/garage"><span class="ap-nav-icon">◇</span>My Garage</a>
                        <a href="%s/logout" class="logout"><span class="ap-nav-icon">↗</span>Logout</a>
                    </nav>
                    <div class="ap-sidebar-bottom">
                        <div class="ap-sidebar-note">
                            <strong>CARE THAT KEEPS YOU MOVING.</strong>
                            <p>Choose your vehicle and service, then select a date that works for you.</p>
                        </div>
                    </div>
                </aside>

                <main class="ap-main">
                    <div class="ap-topbar">
                        <div class="ap-breadcrumb">Customer Portal <span>/ Book a Service</span></div>
                        <div class="ap-status"><span class="ap-dot"></span> CUSTOMER AREA</div>
                    </div>
                    <header class="ap-heading">
                        <div class="ap-eyebrow">MAINTENANCE · 03</div>
                        <h1>Book a Service</h1>
                        <p>Choose your vehicle, select the service you need, and pick your preferred date.</p>
                    </header>

                    <section class="ap-card">
            """.formatted(context, context, context, context, context, context, context, context));

        if (!hasVehicles) {
            html.append("""
                <div class="ap-no-vehicles">
                    You haven't added a vehicle yet. Add your vehicle first to continue with a service booking.
                    <a class="ap-back" href="%s/add-vehicle.html"> Add a Vehicle →</a>
                </div>
                """.formatted(context));
        }

        html.append("""
                        <form action="%s/book-service" method="post">
                            <div class="ap-form-grid">
                                <div class="ap-field full">
                                    <label for="vehicleId">Select Vehicle</label>
                                    <select id="vehicleId" name="vehicleId" required>
                                        <option value="">Select Your Vehicle</option>
                                        %s
                                    </select>
                                </div>

                                <div class="ap-field full">
                                    <label for="serviceType">Service Type</label>
                                    <select id="serviceType" name="serviceType" required>
                                        <option value="">Select Service</option>
                                        <option value="Oil Change">Oil Change</option>
                                        <option value="General Service">General Service</option>
                                        <option value="Brake Service">Brake Service</option>
                                        <option value="Engine Service">Engine Service</option>
                                        <option value="AC Service">AC Service</option>
                                        <option value="Full Service">Full Service</option>
                                    </select>
                                </div>
                            </div>

                            <div class="ap-cost">
                                <div class="ap-cost-label">Estimated Service Cost</div>
                                <div class="ap-cost-value" id="estimatedCost">Select a service</div>
                                <small>Indicative estimate only. Final charges may vary after inspection.</small>
                            </div>

                            <div class="ap-field">
                                <label for="bookingDate">Booking Date</label>
                                <input id="bookingDate" type="date" name="bookingDate" min="%s" required>
                            </div>

                            <div class="ap-form-actions">
                                <button class="ap-primary" type="submit" %s>Book Service <span>→</span></button>
                                <a class="ap-back" href="%s/dashboard.html">← Back to Dashboard</a>
                            </div>
                        </form>
                    </section>
                </main>
            </div>

            <script>
                const servicePrices = {
                    'Oil Change': 1499,
                    'General Service': 2499,
                    'Brake Service': 1999,
                    'Engine Service': 4999,
                    'AC Service': 1799,
                    'Full Service': 3999
                };

                const serviceSelect = document.getElementById('serviceType');
                const estimatedCost = document.getElementById('estimatedCost');

                serviceSelect.addEventListener('change', function () {
                    const price = servicePrices[this.value];
                    estimatedCost.textContent = price
                        ? '₹' + price.toLocaleString('en-IN')
                        : 'Select a service';
                });
            </script>
            </body>
            </html>
            """.formatted(
                context,
                vehicleOptions.toString(),
                java.time.LocalDate.now(),
                hasVehicles ? "" : "disabled",
                context
            ));

        response.getWriter().write(html.toString());
    }
}
