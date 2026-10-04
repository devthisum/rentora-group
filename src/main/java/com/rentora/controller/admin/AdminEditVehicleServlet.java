package com.rentora.controller.admin;

import com.rentora.exception.ValidationException;
import com.rentora.model.Vehicle;
import com.rentora.builder.VehicleBuilder;
import com.rentora.model.User;
import com.rentora.service.AuditLogService;
import com.rentora.service.VehicleImageService;
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
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 60 * 1024 * 1024) // 10MB per photo
public class AdminEditVehicleServlet extends HttpServlet {

    private final VehicleService vehicleService = new VehicleService();
    private final VehicleImageService imageService = new VehicleImageService();
    private final AuditLogService auditLogService = new AuditLogService();

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
            req.setAttribute("galleryRows", imageService.getGalleryRows(id));
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load this vehicle.");
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/edit-vehicle.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Builder pattern (same builder as Add Vehicle, started from the existing vehicle's id)
            VehicleBuilder builder = VehicleBuilder.forExistingVehicle(req.getParameter("vehicleId"))
                    .brand(req.getParameter("brand"))
                    .model(req.getParameter("model"))
                    .year(req.getParameter("year"))
                    .seats(req.getParameter("seats"))
                    .transmission(req.getParameter("transmission"))
                    .fuelType(req.getParameter("fuelType"))
                    .pricePerDay(req.getParameter("pricePerDay"))
                    .description(req.getParameter("description"))
                    .doors(req.getParameter("doors"))
                    .airConditioner(req.getParameter("airConditioner"))
                    .mileage(req.getParameter("mileage"))
                    .features(req.getParameter("features"));

            // A newly chosen photo replaces the current one; otherwise fall back to the
            // (possibly unchanged) Image URL field so the existing photo isn't wiped out.
            Part filePart = req.getPart("imageFile");
            String uploadedUrl = ImageUploadUtil.saveIfPresent(filePart, getServletContext(), req.getContextPath(), "vehicles");
            Vehicle changes = builder.imageUrl(uploadedUrl != null ? uploadedUrl : req.getParameter("imageUrl")).build();

            vehicleService.updateVehicle(changes);

            // Gallery: keep the primary row in step with the cover, drop ticked photos, add new ones.
            long vid = changes.getVehicleId();
            imageService.syncPrimary(vid, changes.getImageUrl());
            imageService.removeImages(vid, req.getParameterValues("removeImageIds"));
            java.util.List<String> extras = new java.util.ArrayList<>(
                    ImageUploadUtil.saveAll(req.getParts(), "galleryFiles", getServletContext(), req.getContextPath(), "vehicles"));
            extras.addAll(VehicleImageService.parseUrls(req.getParameter("extraImageUrls")));
            imageService.addExtraImages(vid, extras);
            auditLogService.record((User) req.getSession().getAttribute("user"), "VEHICLE_UPDATED", "vehicle", vid,
                    changes.getBrand() + " " + changes.getModel() + " details updated");
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
