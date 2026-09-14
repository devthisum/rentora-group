package com.rentora.controller.admin;

import com.rentora.service.MaintenanceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Quick vehicle-status changer, shared by both the admin stock list and the
 * maintenance dashboard's "All Vehicles" table (two URL patterns, one
 * servlet — same logic either way). Routes through MaintenanceService
 * rather than flipping the raw status column directly, so setting
 * Checking/Maintenance opens a real maintenance record visible on the
 * maintenance board, and setting Available resolves that record properly
 * instead of leaving it stuck open.
 *
 * This only ever sets status with no estimate attached — the actual
 * estimated-days timeline is maintenance staff's call, entered by them
 * through "Complete Inspection" / "Edit Duration / Cost" on the resulting
 * record, not through this quick dropdown.
 */
@WebServlet({"/admin/vehicles/status", "/maintenance/vehicles/status"})
public class AdminVehicleStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean fromMaintenance = req.getServletPath().startsWith("/maintenance/");
        String redirectTo = fromMaintenance ? "/maintenance/dashboard" : "/admin/vehicles";

        MaintenanceService maintenanceService =
                (MaintenanceService) req.getServletContext().getAttribute("maintenanceService");

        try {
            long vehicleId = Long.parseLong(req.getParameter("vehicleId"));
            String status = req.getParameter("status");
            Integer estimatedDays = null;
            String estParam = req.getParameter("estimatedDays");
            if (estParam != null && !estParam.isBlank()) estimatedDays = Integer.parseInt(estParam);
            String notes = req.getParameter("notes");

            maintenanceService.setVehicleStatus(vehicleId, status, estimatedDays, notes);

            req.getSession().setAttribute("successMessage",
                    ("CHECKING".equals(status) || "MAINTENANCE".equals(status))
                            ? "Vehicle sent to maintenance — visible on the Maintenance dashboard."
                            : "Vehicle status updated.");
        } catch (Exception e) {
            req.getSession().setAttribute("errorMessage",
                    e.getMessage() != null ? e.getMessage() : "Could not update vehicle status.");
        }
        resp.sendRedirect(req.getContextPath() + redirectTo);
    }
}
