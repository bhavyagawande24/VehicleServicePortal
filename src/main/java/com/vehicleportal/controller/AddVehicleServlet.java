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

@WebServlet("/add-vehicle")
public class AddVehicleServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // User must be logged in
        if (session == null ||
            session.getAttribute("userId") == null) {

            response.sendRedirect("login.html");
            return;
        }

        int userId =
                (Integer) session.getAttribute("userId");

        String vehicleNumber =
                request.getParameter("vehicleNumber");

        String vehicleType =
                request.getParameter("vehicleType");

        String model =
                request.getParameter("model");
if (vehicleNumber == null || vehicleNumber.trim().isEmpty() ||
    vehicleType == null || vehicleType.trim().isEmpty() ||
    model == null || model.trim().isEmpty()) {

    response.setContentType("text/html");

    response.getWriter().println(
        "<h2>All vehicle fields are required.</h2>" +
        "<a href='add-vehicle.html'>Go Back</a>"
    );

    return;
}

vehicleNumber = vehicleNumber.trim().toUpperCase();
vehicleType = vehicleType.trim();
model = model.trim();

if (!vehicleNumber.matches("^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$")) {

    response.setContentType("text/html");

    response.getWriter().println(
        "<h2>Invalid vehicle number.</h2>" +
        "<p>Example: MH12AB1234</p>" +
        "<a href='add-vehicle.html'>Go Back</a>"
    );

    return;
}
        String sql =
                "INSERT INTO vehicles " +
                "(user_id, vehicle_number, vehicle_type, model) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, vehicleNumber);
            statement.setString(3, vehicleType);
            statement.setString(4, model);

            statement.executeUpdate();

            response.sendRedirect(
                    "vehicle-success.html");

} catch (java.sql.SQLIntegrityConstraintViolationException e) {

    response.setContentType("text/html");

    response.getWriter().println(
        "<h2>Vehicle already registered!</h2>" +
        "<p>This vehicle number is already in the system.</p>" +
        "<a href='add-vehicle.html'>Go Back</a>"
    );

} catch (Exception e) {

    e.printStackTrace();

    response.setContentType("text/html");

    response.getWriter().println(
        "<h2>Vehicle registration failed!</h2>" +
        "<a href='add-vehicle.html'>Go Back</a>"
    );
    }
}
}
