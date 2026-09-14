package com.rentora.controller.admin;

import com.rentora.exception.ValidationException;
import com.rentora.model.User;
import com.rentora.model.Vehicle;
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
@MultipartConfig(maxFileSize = 10 * 1024 * 1024) // 10MB per photo
public class AdminVehicleServlet extends HttpServlet {

    private final VehicleService vehicleService = new VehicleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("vehicles", vehicleService.getAll());
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
            Vehicle vehicle = new Vehicle();
            vehicle.setCategoryId(Integer.parseInt(req.getParameter("categoryId")));
            vehicle.setCategoryName(req.getParameter("categoryName"));
            vehicle.setVehicleNumber(req.getParameter("vehicleNumber"));
            vehicle.setBrand(req.getParameter("brand"));
            vehicle.setModel(req.getParameter("model"));
            vehicle.setYear(Integer.parseInt(req.getParameter("year")));
            String seatsParam = req.getParameter("seats");
            if (seatsParam != null && !seatsParam.isBlank()) vehicle.setSeats(Integer.parseInt(seatsParam));
            vehicle.setTransmission(req.getParameter("transmission"));
            vehicle.setFuelType(req.getParameter("fuelType"));
            vehicle.setPricePerDay(new BigDecimal(req.getParameter("pricePerDay")));
            vehicle.setDescription(req.getParameter("description"));

            String doorsParam = req.getParameter("doors");
            if (doorsParam != null && !doorsParam.isBlank()) vehicle.setDoors(Integer.parseInt(doorsParam));
            vehicle.setAirConditioner(req.getParameter("airConditioner"));
            String mileageParam = req.getParameter("mileage");
            if (mileageParam != null && !mileageParam.isBlank()) vehicle.setMileage(Integer.parseInt(mileageParam));
            vehicle.setFeatures(req.getParameter("features"));

            // A chosen gallery photo takes priority over a pasted URL.
            Part filePart = req.getPart("imageFile");
            String uploadedUrl = ImageUploadUtil.saveIfPresent(filePart, getServletContext(), req.getContextPath(), "vehicles");
            vehicle.setImageUrl(uploadedUrl != null ? uploadedUrl : req.getParameter("imageUrl"));

            vehicleService.addVehicle(vehicle, admin.getUserId());
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
