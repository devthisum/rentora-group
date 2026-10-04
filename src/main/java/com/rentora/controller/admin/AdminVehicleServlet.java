package com.rentora.controller.admin;

import com.rentora.exception.ValidationException;
import com.rentora.model.User;
import com.rentora.model.Vehicle;
import com.rentora.builder.VehicleBuilder;
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
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;

/** Lists the shop's full vehicle stock (GET) and adds a new vehicle to it (POST). Admin only. */
@WebServlet("/admin/vehicles")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 60 * 1024 * 1024) // 10MB per photo
public class AdminVehicleServlet extends HttpServlet {

    private final VehicleService vehicleService = new VehicleService();
    private final VehicleImageService imageService = new VehicleImageService();
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            java.util.List<com.rentora.model.Vehicle> all = vehicleService.getAll();
            String view = req.getParameter("view");
            if (!"archived".equals(view) && !"all".equals(view)) view = "active";
            final String selected = view;
            long archivedCount = all.stream().filter(com.rentora.model.Vehicle::isArchived).count();
            req.setAttribute("vehicles", all.stream().filter(v ->
                    "all".equals(selected) || ("archived".equals(selected) == v.isArchived())).toList());
            req.setAttribute("view", selected);
            req.setAttribute("archivedCount", archivedCount);
            req.setAttribute("activeCount", all.size() - archivedCount);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load vehicles.");
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/vehicle-list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User admin = (User) session.getAttribute("user");

        try {
            // Builder pattern: assemble the vehicle step by step; each step converts and checks its own value,
            // and the Factory supplies category defaults for anything left blank.
            VehicleBuilder builder = VehicleBuilder.forNewVehicle(req.getParameter("categoryName"))
                    .categoryId(req.getParameter("categoryId"))
                    .vehicleNumber(req.getParameter("vehicleNumber"))
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

            // A chosen gallery photo takes priority over a pasted URL.
            Part filePart = req.getPart("imageFile");
            String uploadedUrl = ImageUploadUtil.saveIfPresent(filePart, getServletContext(), req.getContextPath(), "vehicles");
            Vehicle vehicle = builder.imageUrl(uploadedUrl != null ? uploadedUrl : req.getParameter("imageUrl")).build();

            // Collect gallery photos first so a too-long list is rejected BEFORE the vehicle is created.
            java.util.List<String> extras = new java.util.ArrayList<>(
                    ImageUploadUtil.saveAll(req.getParts(), "galleryFiles", getServletContext(), req.getContextPath(), "vehicles"));
            extras.addAll(VehicleImageService.parseUrls(req.getParameter("extraImageUrls")));
            if (extras.size() + 1 > VehicleImageService.MAX_PHOTOS_PER_VEHICLE) {
                throw new ValidationException("A vehicle can have at most " + VehicleImageService.MAX_PHOTOS_PER_VEHICLE
                        + " photos (including the cover).");
            }

            long vehicleId = vehicleService.addVehicle(vehicle, admin.getUserId());

            // Gallery: cover becomes the primary row, then the extra photos.
            imageService.syncPrimary(vehicleId, vehicle.getImageUrl());
            imageService.addExtraImages(vehicleId, extras);
            auditLogService.record(admin, "VEHICLE_CREATED", "vehicle", vehicleId,
                    vehicle.getBrand() + " " + vehicle.getModel() + " (" + vehicle.getVehicleNumber() + ") added to stock");
            req.getSession().setAttribute("successMessage", "Vehicle added to the shop's stock.");
            resp.sendRedirect(req.getContextPath() + "/admin/vehicles");

        } catch (ValidationException ve) {
            req.setAttribute("errorMessage", ve.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/admin/add-vehicle.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not add this vehicle. Please check the details and try again.");
            req.getRequestDispatcher("/WEB-INF/views/admin/add-vehicle.jsp").forward(req, resp);
        }
    }
}
