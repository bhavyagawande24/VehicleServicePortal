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

@WebServlet("/book-service")
public class BookingServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.sendRedirect("login.html");
            return;
        }

        int userId =
                (Integer) session.getAttribute("userId");

        String vehicleIdParam =
                request.getParameter("vehicleId");

        if (vehicleIdParam == null ||
            vehicleIdParam.trim().isEmpty()) {

            response.setContentType("text/html");

            response.getWriter().println(
                "<h2>Please select a vehicle.</h2>" +
                "<a href='book-service-page'>Go Back</a>"
            );

            return;
        }

        int vehicleId;

        try {
            vehicleId = Integer.parseInt(vehicleIdParam);
        } catch (NumberFormatException e) {

            response.setContentType("text/html");

            response.getWriter().println(
                "<h2>Invalid vehicle selected.</h2>" +
                "<a href='book-service-page'>Go Back</a>"
            );

            return;
        }

        String serviceType =
                request.getParameter("serviceType");

        String bookingDate =
                request.getParameter("bookingDate");

        if (serviceType == null ||
            serviceType.trim().isEmpty() ||
            bookingDate == null ||
            bookingDate.trim().isEmpty()) {

            response.setContentType("text/html");

            response.getWriter().println(
                "<h2>Service type and booking date are required.</h2>" +
                "<a href='book-service-page'>Go Back</a>"
            );

            return;
        }

        try {

            java.time.LocalDate selectedDate =
                    java.time.LocalDate.parse(bookingDate);

            if (selectedDate.isBefore(
                    java.time.LocalDate.now())) {

                response.setContentType("text/html");

                response.getWriter().println(
                    "<h2>Booking date cannot be in the past.</h2>" +
                    "<a href='book-service-page'>Go Back</a>"
                );

                return;
            }

        } catch (java.time.format.DateTimeParseException e) {

            response.setContentType("text/html");

            response.getWriter().println(
                "<h2>Invalid booking date.</h2>" +
                "<a href='book-service-page'>Go Back</a>"
            );

            return;
        }

        try (Connection connection =
                     DBConnection.getConnection()) {

            String vehicleCheckSql =
                    "SELECT vehicle_id FROM vehicles " +
                    "WHERE vehicle_id = ? AND user_id = ?";

            try (PreparedStatement vehicleCheck =
                         connection.prepareStatement(
                                 vehicleCheckSql)) {

                vehicleCheck.setInt(1, vehicleId);
                vehicleCheck.setInt(2, userId);

                try (ResultSet result =
                             vehicleCheck.executeQuery()) {

                    if (!result.next()) {

                        response.setContentType("text/html");

                        response.getWriter().println(
                            "<h2>Invalid vehicle selection.</h2>" +
                            "<p>This vehicle does not belong " +
                            "to your account.</p>" +
                            "<a href='book-service-page'>" +
                            "Go Back</a>"
                        );

                        return;
                    }
                }
            }

            String sql =
                    "INSERT INTO bookings " +
                    "(user_id, vehicle_id, service_type, " +
                    "booking_date) " +
                    "VALUES (?, ?, ?, ?)";

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, userId);
                statement.setInt(2, vehicleId);
                statement.setString(3, serviceType);
                statement.setString(4, bookingDate);

                statement.executeUpdate();

                response.sendRedirect(
                        "booking-success.html");
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType("text/html");

            response.getWriter().println(
                "<h2>Booking failed!</h2>" +
                "<a href='book-service-page'>Go Back</a>"
            );
        }
    }
}
