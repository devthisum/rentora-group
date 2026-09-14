package com.rentora.controller.maintenance;

import com.rentora.model.User;
import com.rentora.service.BookingService;
import com.rentora.service.MaintenanceService;
import com.rentora.service.VehicleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * The maintenance dashboard — shared by admin and maintenance staff:
 *  - upcoming/overdue returns ("return in ___ days")
 *  - vehicles currently being checked or under maintenance (maintenance
 *    staff only can act on these — inspection findings, estimated-days
 *    timeline, and cost are their call, not admin's)
 *  - the full vehicle stock, so its status (Available/Checking/Maintenance)
 *    can be quick-changed by *either* role — this is a separate, lighter
 *    permission (canManageStatus) than record editing (canEdit)
 *
 * POST actions (?action=):
 *   markReturned   -> opens the CHECKING record for a booking that just came back
 *   completeCheck  -> records the inspection result, incl. estimated days (see MaintenanceService)
 *   markResolved   -> repairs finished, vehicle back in service
 *   updateDetails  -> edits the estimated-days / notes / cost on an open record
 */
@WebServlet("/maintenance/dashboard")
public class MaintenanceDashboardServlet extends HttpServlet {

    private final VehicleService vehicleService = new VehicleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MaintenanceService maintenanceService =
                (MaintenanceService) req.getServletContext().getAttribute("maintenanceService");
        BookingService bookingService =
                (BookingService) req.getServletContext().getAttribute("bookingService");
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        try {
            req.setAttribute("activeMaintenance", maintenanceService.getActive());
            req.setAttribute("upcomingReturns", bookingService.getActiveOrderedByReturn());
            req.setAttribute("allVehicles", vehicleService.getAll());
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load the maintenance board.");
        }
        String role = user != null ? user.getRoleName() : null;
        // Record editing (inspection results, estimated days, cost, resolving) is
        // maintenance staff's call only — admin gets a view-only board for these.
        req.setAttribute("canEdit", "MAINTENANCE".equalsIgnoreCase(role));
        // Changing a vehicle's plain status (Available/Checking/Maintenance) is a
        // lighter action both roles can do, from the "All Vehicles" table below.
        req.setAttribute("canManageStatus", "MAINTENANCE".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role));
        req.getRequestDispatcher("/WEB-INF/views/maintenance/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        if (!"MAINTENANCE".equalsIgnoreCase(user.getRoleName())) {
            req.getSession().setAttribute("errorMessage",
                    "Only maintenance staff can update maintenance records — admins have view-only access here.");
            resp.sendRedirect(req.getContextPath() + "/maintenance/dashboard");
            return;
        }

        MaintenanceService maintenanceService =
                (MaintenanceService) req.getServletContext().getAttribute("maintenanceService");

        String action = req.getParameter("action");
        try {
            switch (action == null ? "" : action) {
                case "markReturned" -> {
                    long bookingId = Long.parseLong(req.getParameter("bookingId"));
                    maintenanceService.markReturned(bookingId);
                    req.getSession().setAttribute("successMessage", "Vehicle marked as returned — now under checking.");
                }
                case "completeCheck" -> {
                    long maintenanceId = Long.parseLong(req.getParameter("maintenanceId"));
                    boolean problemFound = "true".equalsIgnoreCase(req.getParameter("problemFound"));
                    String notes = req.getParameter("notes");
                    Integer estimatedDays = null;
                    String estDaysParam = req.getParameter("estimatedDays");
                    if (estDaysParam != null && !estDaysParam.isBlank()) estimatedDays = Integer.parseInt(estDaysParam);
                    BigDecimal repairCost = null;
                    String costParam = req.getParameter("repairCost");
                    if (costParam != null && !costParam.isBlank()) repairCost = new BigDecimal(costParam);
                    boolean customerAtFault = "true".equalsIgnoreCase(req.getParameter("customerAtFault"));

                    maintenanceService.completeCheck(maintenanceId, user.getUserId(), problemFound, notes,
                            estimatedDays, repairCost, customerAtFault);
                    req.getSession().setAttribute("successMessage",
                            problemFound ? "Vehicle marked under maintenance." : "No problem found — vehicle is available again.");
                }
                case "markResolved" -> {
                    long maintenanceId = Long.parseLong(req.getParameter("maintenanceId"));
                    maintenanceService.markResolved(maintenanceId);
                    req.getSession().setAttribute("successMessage", "Maintenance resolved — vehicle is available again.");
                }
                case "updateDetails" -> {
                    long maintenanceId = Long.parseLong(req.getParameter("maintenanceId"));
                    Integer estimatedDays = null;
                    String estDaysParam = req.getParameter("estimatedDays");
                    if (estDaysParam != null && !estDaysParam.isBlank()) estimatedDays = Integer.parseInt(estDaysParam);
                    String notes = req.getParameter("notes");
                    BigDecimal repairCost = null;
                    String costParam = req.getParameter("repairCost");
                    if (costParam != null && !costParam.isBlank()) repairCost = new BigDecimal(costParam);
                    boolean customerAtFault = "true".equalsIgnoreCase(req.getParameter("customerAtFault"));

                    maintenanceService.updateInProgress(maintenanceId, estimatedDays, notes, repairCost, customerAtFault);
                    req.getSession().setAttribute("successMessage", "Maintenance details updated.");
                }
                default -> req.getSession().setAttribute("errorMessage", "Unknown action.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("errorMessage",
                    e.getMessage() != null ? e.getMessage() : "Could not complete that action.");
        }
        resp.sendRedirect(req.getContextPath() + "/maintenance/dashboard");
    }
}
