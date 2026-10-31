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
* { box-sizing: border-box; }

body {
    margin: 0;
    padding: 30px 20px;
    font-family: Arial, Helvetica, sans-serif;
    background: #080a08;
    color: #f5f7f8;
}

.dashboard {
    max-width: 1200px;
    margin: auto;
}

.header {
    background: linear-gradient(135deg, #111411, #202820);
    color: #f5f7f8;
    padding: 32px;
    border: 1px solid #46513b;
    border-radius: 18px;
    box-shadow: 0 10px 35px rgba(0,0,0,.3);
}

.header h1 { color: #c6ff00; margin-top: 0; }
.header p { color: #a8b0b4; }

.section {
    background: #111411;
    color: #f5f7f8;
    border: 1px solid #303b40;
    border-radius: 16px;
    padding: 24px;
    margin: 22px auto;
    max-width: 1200px;
    overflow-x: auto;
}

.section h2 { color: #c6ff00; margin-top: 0; }

#autolux-admin-summary > div {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
}

.stat-card {
    flex: 1 1 150px;
    padding: 20px;
    border-radius: 12px;
    background: #1c241b;
    border: 1px solid #46513b;
    color: #c6ff00;
    min-width: 130px;
    font-size: 13px;
    font-weight: bold;
}

.stat-card strong {
    display: block;
    margin-top: 10px;
    font-size: 30px;
    color: #f5f7f8;
}

table {
    width: 100%;
    min-width: 650px;
    border-collapse: collapse;
    background: #111411;
    color: #f5f7f8;
}

th {
    background: #202820;
    color: #c6ff00;
    text-align: left;
    padding: 14px;
    border-bottom: 1px solid #46513b;
}

td {
    padding: 13px;
    border-bottom: 1px solid #303b40;
    color: #d9dfda;
}

tr:hover td { background: #1b211d; }

button {
    background: #c6ff00;
    color: #080a08;
    border: none;
    border-radius: 8px;
    padding: 10px 16px;
    font-weight: bold;
    cursor: pointer;
}

button:hover { background: #dcff70; }

select, input {
    background: #1b211d;
    color: #f5f7f8;
    border: 1px solid #46513b;
    border-radius: 7px;
    padding: 9px;
}

select:focus, input:focus {
    outline: 1px solid #c6ff00;
}

.status-pending { color: #ffd166; }
.status-approved { color: #c6ff00; }
.status-completed { color: #65d68e; }
.status-cancelled { color: #ff7777; }

a { color: #c6ff00; }
a:hover { color: #dcff70; }

.logout {
    display: inline-block;
    margin: 20px 0;
    padding: 11px 20px;
    background: #c6ff00;
    color: #080a08;
    border-radius: 8px;
    font-weight: bold;
    text-decoration: none;
}

.logout:hover { background: #dcff70; }

.footer {
    text-align: center;
    color: #929b95;
    padding: 18px;
    font-size: 14px;
}

@media (max-width: 600px) {
    body { padding: 15px 10px; }
    .header { padding: 24px; }
    .header h1 { font-size: 28px; }
    .section { padding: 16px; }
}
</style>

</head>

<body>

<div class="section" id="autolux-admin-summary">
<h2>AUTOLUX LIVE DATABASE STATS</h2>
<div style="display:flex;flex-wrap:wrap;gap:14px">
<div style="padding:18px;border-radius:10px;background:#1c1e19;color:#c6ff00;min-width:130px">TOTAL BOOKINGS<br><strong>__TOTAL_BOOKINGS__</strong></div>
<div style="padding:18px;border-radius:10px;background:#1c1e19;color:#c6ff00;min-width:130px">PENDING<br><strong>__PENDING_BOOKINGS__</strong></div>
<div style="padding:18px;border-radius:10px;background:#1c1e19;color:#c6ff00;min-width:130px">CONFIRMED / IN PROGRESS<br><strong>__CONFIRMED_BOOKINGS__</strong></div>
<div style="padding:18px;border-radius:10px;background:#1c1e19;color:#c6ff00;min-width:130px">COMPLETED<br><strong>__COMPLETED_BOOKINGS__</strong></div>
<div style="padding:18px;border-radius:10px;background:#1c1e19;color:#c6ff00;min-width:130px">REGISTERED USERS<br><strong>__TOTAL_USERS__</strong></div>
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

            String dashboardHtml = html.toString()
                    .replace("__TOTAL_BOOKINGS__", String.valueOf(totalBookings))
                    .replace("__PENDING_BOOKINGS__", String.valueOf(pendingBookings))
                    .replace("__CONFIRMED_BOOKINGS__", String.valueOf(confirmedBookings))
                    .replace("__COMPLETED_BOOKINGS__", String.valueOf(completedBookings))
                    .replace("__TOTAL_USERS__", String.valueOf(totalUsers));

            response.getWriter().println(dashboardHtml);

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().println(
                "<h2>Unable to load Admin Dashboard.</h2>"
            );
        }
    }
}
