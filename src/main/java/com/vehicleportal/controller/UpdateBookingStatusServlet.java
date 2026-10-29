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

@WebServlet("/update-booking-status")
public class UpdateBookingStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("adminId") == null) {

            response.sendRedirect("admin-login.html");
            return;
        }

        String bookingId = request.getParameter("bookingId");
        String status = request.getParameter("status");

        if (!"Pending".equals(status) &&
            !"Approved".equals(status) &&
            !"Confirmed".equals(status) &&
            !"In Progress".equals(status) &&
            !"Completed".equals(status) &&
            !"Cancelled".equals(status)) {

            response.sendRedirect("admin-dashboard");
            return;
        }

        String sql =
                "UPDATE bookings SET status = ? " +
                "WHERE booking_id = ?";

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, Integer.parseInt(bookingId));

            statement.executeUpdate();

            response.sendRedirect("admin-dashboard");

        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType("text/html");

            response.getWriter().println(
                "<h2>Unable to update booking status.</h2>" +
                "<a href='admin-dashboard'>Back to Admin Dashboard</a>"
            );
        }
    }
}
