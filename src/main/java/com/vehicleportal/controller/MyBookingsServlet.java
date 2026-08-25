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

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.sendRedirect("login.html");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        StringBuilder html = new StringBuilder();

        html.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport"
                      content="width=device-width, initial-scale=1.0">

                <title>My Bookings - Vehicle Service Portal</title>

                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background: #f2f4f7;
                        margin: 0;
                        padding: 30px;
                    }

                    .container {
                        max-width: 1100px;
                        margin: auto;
                    }

                    .box {
                        background: white;
                        padding: 30px;
                        border-radius: 12px;
                        box-shadow: 0 4px 15px rgba(0,0,0,0.1);
                    }

                    h1 {
                        text-align: center;
                    }

                    h2 {
                        margin-top: 30px;
                    }

                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-top: 20px;
                    }

                    th, td {
                        padding: 14px;
                        border: 1px solid #ddd;
                        text-align: left;
                    }

                    th {
                        background: #222;
                        color: white;
                    }

                    tr:nth-child(even) {
                        background: #f7f7f7;
                    }

                    .back {
                        display: inline-block;
                        margin-top: 25px;
                        text-decoration: none;
                        font-weight: bold;
                    }
                </style>

            </head>

            <body>

            <div class="container">

            <div class="box">

                <h1>Vehicle Service Portal</h1>

                <h2>My Service Bookings</h2>

                <table>

                <tr>
                    <th>Booking ID</th>
                    <th>Vehicle</th>
                    <th>Service</th>
                    <th>Booking Date</th>
                    <th>Status</th>
                </tr>
            """);

        String sql =
                "SELECT b.booking_id, " +
                "v.vehicle_number, " +
                "b.service_type, " +
                "b.booking_date, " +
                "b.status " +
                "FROM bookings b " +
                "JOIN vehicles v ON b.vehicle_id = v.vehicle_id " +
                "WHERE b.user_id = ? " +
                "ORDER BY b.booking_id DESC";

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet result = statement.executeQuery();

            boolean found = false;

            while (result.next()) {

                found = true;

                html.append("<tr>");

                html.append("<td>")
                    .append(result.getInt("booking_id"))
                    .append("</td>");

                html.append("<td>")
                    .append(result.getString("vehicle_number"))
                    .append("</td>");

                html.append("<td>")
                    .append(result.getString("service_type"))
                    .append("</td>");

                html.append("<td>")
                    .append(result.getDate("booking_date"))
                    .append("</td>");

                html.append("<td>")
                    .append(result.getString("status"))
                    .append("</td>");

                html.append("</tr>");
            }

            if (!found) {

                html.append("""
                    <tr>
                        <td colspan="5">
                            No bookings found.
                        </td>
                    </tr>
                    """);
            }

        } catch (Exception e) {

            e.printStackTrace();

            html.append("""
                <tr>
                    <td colspan="5">
                        Unable to load bookings.
                    </td>
                </tr>
                """);
        }

        html.append("""
                </table>

                <a class="back"
                   href="dashboard.html">
                    ← Back to Dashboard
                </a>

            </div>

            </div>

            </body>
            </html>
            """);

        response.getWriter().println(html);
    }
}
