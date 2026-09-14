package com.rentora.controller.admin;

import com.rentora.exception.ValidationException;
import com.rentora.model.Vehicle;
import com.rentora.service.VehicleService;
import com.rentora.util.ImageUploadUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

/** Admin edits a vehicle's details directly — no approval workflow. */
@WebServlet("/admin/vehicles/edit")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024) // 10MB per photo
public class AdminEditVehicleServlet extends HttpServlet {

    private final VehicleService vehicleService = new VehicleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long id = Long.parseLong(req.getParameter("id"));
            Optional<Vehicle> vehicle = vehicleService.getById(id);
            if (vehicle.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Vehicle not found.");
                return;
            }
            req.setAttribute("vehicle", vehicle.get());
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load this vehicle.");
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/edit-vehicle.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Vehicle changes = new Vehicle();
            changes.setVehicleId(Long.parseLong(req.getParameter("vehicleId")));
            changes.setBrand(req.getParameter("brand"));
            changes.setModel(req.getParameter("model"));
            changes.setYear(Integer.parseInt(req.getParameter("year")));
            String seatsParam = req.getParameter("seats");
            if (seatsParam != null && !seatsParam.isBlank()) changes.setSeats(Integer.parseInt(seatsParam));
            changes.setTransmission(req.getParameter("transmission"));
            changes.setFuelType(req.getParameter("fuelType"));
            changes.setPricePerDay(new BigDecimal(req.getParameter("pricePerDay")));
            changes.setDescription(req.getParameter("description"));

            String doorsParam = req.getParameter("doors");
            if (doorsParam != null && !doorsParam.isBlank()) changes.setDoors(Integer.parseInt(doorsParam));
            changes.setAirConditioner(req.getParameter("airConditioner"));
            String mileageParam = req.getParameter("mileage");
            if (mileageParam != null && !mileageParam.isBlank()) changes.setMileage(Integer.parseInt(mileageParam));
            changes.setFeatures(req.getParameter("features"));

            // A newly chosen photo replaces the current one; otherwise fall back to the
            // (possibly unchanged) Image URL field so the existing photo isn't wiped out.
            Part filePart = req.getPart("imageFile");
            String uploadedUrl = ImageUploadUtil.saveIfPresent(filePart, getServletContext(), req.getContextPath(), "vehicles");
            changes.setImageUrl(uploadedUrl != null ? uploadedUrl : req.getParameter("imageUrl"));

            vehicleService.updateVehicle(changes);
            req.getSession().setAttribute("successMessage", "Vehicle updated.");
            resp.sendRedirect(req.getContextPath() + "/admin/vehicles");

        } catch (ValidationException ve) {
            req.setAttribute("errorMessage", ve.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/admin/edit-vehicle.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not update this vehicle.");
            req.getRequestDispatcher("/WEB-INF/views/admin/edit-vehicle.jsp").forward(req, resp);
        }
    }
}
