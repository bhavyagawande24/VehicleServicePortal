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

@WebServlet("/admin-dashboard")
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("adminId") == null) {

            response.sendRedirect("admin-login.html");
            return;
        }

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        StringBuilder html = new StringBuilder();

        // AUTOLUX LIVE DATABASE STATS
        int totalBookings = 0, pendingBookings = 0, confirmedBookings = 0;
        int completedBookings = 0, totalUsers = 0;
        try (Connection statsConnection = DBConnection.getConnection();
             PreparedStatement stats = statsConnection.prepareStatement(
                "SELECT " +
                "(SELECT COUNT(*) FROM bookings), " +
                "(SELECT COUNT(*) FROM bookings WHERE status='Pending'), " +
                "(SELECT COUNT(*) FROM bookings WHERE status IN ('Confirmed','Approved','In Progress')), " +
                "(SELECT COUNT(*) FROM bookings WHERE status='Completed'), " +
                "(SELECT COUNT(*) FROM users WHERE role='USER')")) {
            try (ResultSet rs = stats.executeQuery()) {
                if (rs.next()) {
                    totalBookings = rs.getInt(1);
                    pendingBookings = rs.getInt(2);
                    confirmedBookings = rs.getInt(3);
                    completedBookings = rs.getInt(4);
                    totalUsers = rs.getInt(5);
                }
            }
        } catch (Exception statsError) {
            getServletContext().log("Could not load admin summary statistics", statsError);
        }


        html.append("""
<!DOCTYPE html>
<html lang="en">

<head>

<meta charset="UTF-8">

<meta name="viewport"
      content="width=device-width, initial-scale=1.0">

<title>Admin Dashboard - AUTOLUX</title>

<style>

* {
    box-sizing: border-box;
}

body {
    margin: 0;
    padding: 30px 20px;
    font-family: Arial, Helvetica, sans-serif;
    background: #eef4ff;
    color: #1e293b;
}

.dashboard {
    max-width: 1200px;
    margin: auto;
}

.header {
    background: linear-gradient(135deg, #1e3a8a, #2563eb);
    color: white;
    padding: 32px;
    border-radius: 16px;
    margin-bottom: 25px;
    box-shadow: 0 8px 25px rgba(30, 58, 138, 0.20);
}

.header h1 {
    margin: 0 0 8px;
    font-size: 34px;
}

.header p {
    margin: 0;
    font-size: 17px;
    opacity: 0.95;
}

.section {
    background: white;
    padding: 25px;
    margin-bottom: 25px;
    border-radius: 15px;
    box-shadow: 0 5px 20px rgba(30, 58, 138, 0.09);
    overflow-x: auto;
}

.section h2 {
    margin-top: 0;
    margin-bottom: 20px;
    color: #1e3a8a;
    font-size: 24px;
}

table {
    width: 100%;
    border-collapse: collapse;
    min-width: 650px;
}

th,
td {
    padding: 13px;
    border-bottom: 1px solid #e2e8f0;
    text-align: left;
}

th {
    background: #1e3a8a;
    color: white;
}

tr:nth-child(even) {
    background: #f8fafc;
}

tr:hover {
    background: #eff6ff;
}

select {
    padding: 9px 12px;
    border: 1px solid #cbd5e1;
    border-radius: 7px;
    background: white;
    color: #1e293b;
    font-size: 14px;
}

select:focus {
    border-color: #2563eb;
    outline: none;
    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

button {
    padding: 9px 14px;
    margin-left: 5px;
    border: none;
    border-radius: 7px;
    background: #2563eb;
    color: white;
    cursor: pointer;
    font-weight: bold;
}

button:hover {
    background: #1d4ed8;
}

.logout {
    display: inline-block;
    padding: 13px 22px;
    background: #dc2626;
    color: white;
    text-decoration: none;
    border-radius: 9px;
    font-weight: bold;
    margin-bottom: 20px;
}

.logout:hover {
    background: #b91c1c;
}

.status-pending {
    color: #b45309;
    font-weight: bold;
}

.status-approved {
    color: #2563eb;
    font-weight: bold;
}

.status-completed {
    color: #15803d;
    font-weight: bold;
}

.status-cancelled {
    color: #dc2626;
    font-weight: bold;
}

.footer {
    text-align: center;
    color: #64748b;
    padding: 10px;
    font-size: 14px;
}

@media (max-width: 600px) {

    body {
        padding: 15px 10px;
    }

    .header {
        padding: 24px;
    }

    .header h1 {
        font-size: 28px;
    }

    .section {
        padding: 18px;
    }

}

</style>

</head>

<body>

<div class="section" id="autolux-admin-summary">
<h2>AUTOLUX LIVE DATABASE STATS</h2>
<div style="display:flex;flex-wrap:wrap;gap:14px">
<div style="padding:18px;border-radius:10px;background:#1c1e19;color:#c6ff00;min-width:130px">TOTAL BOOKINGS<br><strong>" + totalBookings + "</strong></div>
<div style="padding:18px;border-radius:10px;background:#1c1e19;color:#c6ff00;min-width:130px">PENDING<br><strong>" + pendingBookings + "</strong></div>
<div style="padding:18px;border-radius:10px;background:#1c1e19;color:#c6ff00;min-width:130px">CONFIRMED / IN PROGRESS<br><strong>" + confirmedBookings + "</strong></div>
<div style="padding:18px;border-radius:10px;background:#1c1e19;color:#c6ff00;min-width:130px">COMPLETED<br><strong>" + completedBookings + "</strong></div>
<div style="padding:18px;border-radius:10px;background:#1c1e19;color:#c6ff00;min-width:130px">REGISTERED USERS<br><strong>" + totalUsers + "</strong></div>
</div></div>
<div class="dashboard">

<div class="header">

<h1>🚗 AUTOLUX</h1>

<p>Administrator Dashboard</p>

</div>
""");

        try (Connection connection =
                     DBConnection.getConnection()) {

            // ==========================
            // REGISTERED USERS
            // ==========================

            html.append("""
<div class="section">

<h2>👤 Registered Users</h2>

<table>

<tr>
<th>ID</th>
<th>Name</th>
<th>Email</th>
<th>Phone</th>
<th>Role</th>
<th>Created At</th>
</tr>
""");

            String usersSQL =
                    "SELECT id, name, email, phone, role, created_at " +
                    "FROM users ORDER BY id";

            try (PreparedStatement statement =
                         connection.prepareStatement(usersSQL);
                 ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    html.append("<tr>");

                    html.append("<td>")
                        .append(result.getInt("id"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getString("name"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getString("email"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getString("phone"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getString("role"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getTimestamp("created_at"))
                        .append("</td>");

                    html.append("</tr>");
                }
            }

            html.append("""
</table>

</div>
""");

            // ==========================
            // REGISTERED VEHICLES
            // ==========================

            html.append("""
<div class="section">

<h2>🚗 Registered Vehicles</h2>

<table>

<tr>
<th>Vehicle ID</th>
<th>User ID</th>
<th>Vehicle Number</th>
<th>Type</th>
<th>Model</th>
</tr>
""");

            String vehicleSQL =
                    "SELECT vehicle_id, user_id, vehicle_number, " +
                    "vehicle_type, model FROM vehicles " +
                    "ORDER BY vehicle_id";

            try (PreparedStatement statement =
                         connection.prepareStatement(vehicleSQL);
                 ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    html.append("<tr>");

                    html.append("<td>")
                        .append(result.getInt("vehicle_id"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getInt("user_id"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getString("vehicle_number"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getString("vehicle_type"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getString("model"))
                        .append("</td>");

                    html.append("</tr>");
                }
            }

            html.append("""
</table>

</div>
""");

            // ==========================
            // SERVICE BOOKINGS
            // ==========================

            html.append("""
<div class="section">

<h2>🔧 Service Bookings</h2>

<table>

<tr>
<th>Booking ID</th>
<th>User ID</th>
<th>Vehicle ID</th>
<th>Service</th>
<th>Booking Date</th>
<th>Status</th>
</tr>
""");

            String bookingSQL =
                    "SELECT booking_id, user_id, vehicle_id, " +
                    "service_type, booking_date, status " +
                    "FROM bookings ORDER BY booking_id";

            try (PreparedStatement statement =
                         connection.prepareStatement(bookingSQL);
                 ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    int bookingId =
                            result.getInt("booking_id");

                    String currentStatus =
                            result.getString("status");

                    String statusClass = "";

                    if ("Pending".equals(currentStatus)) {
                        statusClass = "status-pending";
                    } else if ("Approved".equals(currentStatus)) {
                        statusClass = "status-approved";
                    } else if ("Completed".equals(currentStatus)) {
                        statusClass = "status-completed";
                    } else if ("Cancelled".equals(currentStatus)) {
                        statusClass = "status-cancelled";
                    }

                    html.append("<tr>");

                    html.append("<td>")
                        .append(bookingId)
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getInt("user_id"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getInt("vehicle_id"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getString("service_type"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getDate("booking_date"))
                        .append("</td>");

                    html.append("<td>");

                    html.append(
                        "<form action='update-booking-status' " +
                        "method='post'>"
                    );

                    html.append(
                        "<input type='hidden' " +
                        "name='bookingId' value='"
                    )
                    .append(bookingId)
                    .append("'>");

                    html.append(
                        "<select name='status' " +
                        "class='"
                    )
                    .append(statusClass)
                    .append("'>"
                    );

                    html.append("<option value='Pending' ")
                        .append(
                            "Pending".equals(currentStatus)
                            ? "selected" : ""
                        )
                        .append(">Pending</option>");

                    html.append("<option value='Approved' ")
                        .append(
                            "Approved".equals(currentStatus)
                            ? "selected" : ""
                        )
                        .append(">Approved</option>");

                    html.append("<option value='Completed' ")
                        .append(
                            "Completed".equals(currentStatus)
                            ? "selected" : ""
                        )
                        .append(">Completed</option>");

                    html.append("<option value='Cancelled' ")
                        .append(
                            "Cancelled".equals(currentStatus)
                            ? "selected" : ""
                        )
                        .append(">Cancelled</option>");

                    html.append("</select>");

                    html.append(
                        "<button type='submit'>Update</button>"
                    );

                    html.append("</form>");

                    html.append("</td>");

                    html.append("</tr>");
                }
            }

            html.append("""
</table>

</div>
""");

            // ==========================
            // LOGIN HISTORY
            // ==========================

            html.append("""
<div class="section">

<h2>🔐 Login History</h2>

<table>

<tr>
<th>Login ID</th>
<th>User ID</th>
<th>Login Time</th>
</tr>
""");

            String loginSQL =
                    "SELECT login_id, user_id, login_time " +
                    "FROM login_history " +
                    "ORDER BY login_id DESC";

            try (PreparedStatement statement =
                         connection.prepareStatement(loginSQL);
                 ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    html.append("<tr>");

                    html.append("<td>")
                        .append(result.getInt("login_id"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getInt("user_id"))
                        .append("</td>");

                    html.append("<td>")
                        .append(result.getTimestamp("login_time"))
                        .append("</td>");

                    html.append("</tr>");
                }
            }

            html.append("""
</table>

</div>

<a href="logout" class="logout">
Logout
</a>

<div class="footer">
AUTOLUX • Admin Panel
</div>

</div>

</body>

</html>
""");

            response.getWriter().println(html);

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().println(
                "<h2>Unable to load Admin Dashboard.</h2>"
            );
        }
    }
}
