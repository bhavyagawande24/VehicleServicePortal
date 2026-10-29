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

        int userId =
                (Integer) session.getAttribute("userId");

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        StringBuilder vehicleOptions = new StringBuilder();

        String sql =
                "SELECT vehicle_id, vehicle_number, vehicle_type, model " +
                "FROM vehicles WHERE user_id = ?";

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet result = statement.executeQuery();

            while (result.next()) {

                int vehicleId =
                        result.getInt("vehicle_id");

                String vehicleNumber =
                        result.getString("vehicle_number");

                String vehicleType =
                        result.getString("vehicle_type");

                String model =
                        result.getString("model");

                vehicleOptions.append(
                    "<option value='" + vehicleId + "'>"
                    + vehicleNumber + " - "
                    + vehicleType + " - "
                    + model
                    + "</option>"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().println(
                    "<h2>Unable to load vehicles.</h2>"
            );

            return;
        }

        response.getWriter().println(
            "<!DOCTYPE html>"
            + "<html>"
            + "<head>"
            + "<meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
            + "<title>Book Vehicle Service</title>"
            + "<link rel='stylesheet' href='css/autolux-booking.css'>"
            + "</head>"

            + "<body>"

            + "<div class='container'>"

            + "<div class='form-box'>"

            + "<h1>AUTOLUX</h1>"
            + "<h2>Book a Service</h2>"

            + "<form action='book-service' method='post'>"

            + "<label>Select Vehicle</label>"

            + "<select name='vehicleId' required>"

            + "<option value=''>Select Your Vehicle</option>"

            + vehicleOptions.toString()

            + "</select>"

            + "<label>Service Type</label>"

            + "<select name='serviceType' required>"

            + "<option value=''>Select Service</option>"
            + "<option value='Oil Change'>Oil Change</option>"
            + "<option value='General Service'>General Service</option>"
            + "<option value='Brake Service'>Brake Service</option>"
            + "<option value='Engine Service'>Engine Service</option>"
            + "<option value='AC Service'>AC Service</option>"
            + "<option value='Full Service'>Full Service</option>"

            + "</select>"

            + "<div style='margin:18px 0;padding:16px;background:#1c1e19;color:#f4f4f0;border:1px solid #c6ff00;border-radius:10px'>"
            + "<strong style='color:#c6ff00'>Estimated Service Cost</strong>"
            + "<h2 id='estimatedCost' style='margin:10px 0;color:#c6ff00'>Select a service</h2>"
            + "<small>Indicative estimate only. Final charges may vary after inspection.</small>"
            + "</div>"
            + "<script>"
            + "const servicePrices={'Oil Change':1499,'General Service':2499,'Brake Service':1999,'Engine Service':4999,'AC Service':1799,'Full Service':3999};"
            + "const serviceSelect=document.querySelector('select[name=serviceType]');"
            + "serviceSelect.addEventListener('change',function(){"
            + "const price=servicePrices[this.value];"
            + "document.getElementById('estimatedCost').textContent=price?'₹'+price.toLocaleString('en-IN'):'Select a service';"
            + "});"
            + "</script>"

            + "<label>Booking Date</label>"

            + "<input type='date' name='bookingDate' min='" + java.time.LocalDate.now() + "' required>"

            + "<button type='submit'>Book Service</button>"

            + "</form>"

            + "<p>"
            + "<a href='dashboard.html'>Back to Dashboard</a>"
            + "</p>"

            + "</div>"
            + "</div>"

            + "</body>"
            + "</html>"
        );
    }
}
