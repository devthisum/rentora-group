package com.rentora.controller.admin;

import com.rentora.model.Booking;
import com.rentora.model.User;
import com.rentora.model.Vehicle;
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
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final VehicleService vehicleService = new VehicleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BookingService bookingService =
                (BookingService) req.getServletContext().getAttribute("bookingService");
        MaintenanceService maintenanceService =
                (MaintenanceService) req.getServletContext().getAttribute("maintenanceService");
        HttpSession session = req.getSession(false);
        User admin = session != null ? (User) session.getAttribute("user") : null;

        try {
            List<Vehicle> allVehicles = vehicleService.getAll();
            List<Booking> allBookings = bookingService.getAll();

            // Fleet status counts — uses getDisplayStatus() (Available/Booked/Checking/Maintenance),
            // NOT the raw stored status column, since "Booked" is never actually stored — it's
            // computed live from today's ONGOING bookings (see VehicleDAOImpl).
            long availableCount = allVehicles.stream().filter(v -> "AVAILABLE".equals(v.getDisplayStatus())).count();
            long bookedCount = allVehicles.stream().filter(v -> "BOOKED".equals(v.getDisplayStatus())).count();
            long maintenanceCount = allVehicles.stream()
                    .filter(v -> "CHECKING".equals(v.getDisplayStatus()) || "MAINTENANCE".equals(v.getDisplayStatus())).count();

            List<Booking> revenueEligible = allBookings.stream()
                    .filter(b -> !"AWAITING_PAYMENT".equals(b.getStatus()) && !"CANCELLED".equals(b.getStatus()))
                    .collect(Collectors.toList());

            BigDecimal totalRevenue = revenueEligible.stream()
                    .map(Booking::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal lateFeesCollected = revenueEligible.stream()
                    .map(Booking::getLateFee)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ---- Last 6 months of revenue, for the trend chart ----
            List<String> monthLabels = new ArrayList<>();
            List<BigDecimal> monthRevenue = new ArrayList<>();
            YearMonth cursor = YearMonth.now().minusMonths(5);
            for (int i = 0; i < 6; i++) {
                YearMonth month = cursor.plusMonths(i);
                monthLabels.add(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
                BigDecimal sum = revenueEligible.stream()
                        .filter(b -> b.getCreatedAt() != null && YearMonth.from(b.getCreatedAt()).equals(month))
                        .map(Booking::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                monthRevenue.add(sum);
            }

            // ---- Booking status breakdown, for the donut chart ----
            Map<String, Long> statusCounts = allBookings.stream()
                    .collect(Collectors.groupingBy(Booking::getStatus, LinkedHashMap::new, Collectors.counting()));

            // ---- Top 5 most-booked vehicles, for the "popular fleet" list ----
            Map<String, Long> vehicleBookingCounts = allBookings.stream()
                    .filter(b -> !"CANCELLED".equals(b.getStatus()))
                    .collect(Collectors.groupingBy(
                            b -> b.getVehicleBrand() + " " + b.getVehicleModel(),
                            Collectors.counting()));
            List<Map.Entry<String, Long>> topVehicles = vehicleBookingCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            req.setAttribute("adminName", admin != null ? admin.getFullName() : "Admin");
            req.setAttribute("allVehicles", allVehicles);
            req.setAttribute("availableCount", availableCount);
            req.setAttribute("bookedCount", bookedCount);
            req.setAttribute("maintenanceCount", maintenanceCount);
            req.setAttribute("totalVehicles", allVehicles.size());
            req.setAttribute("allBookings", allBookings);
            req.setAttribute("totalRevenue", totalRevenue);
            req.setAttribute("lateFeesCollected", lateFeesCollected);
            req.setAttribute("monthLabels", monthLabels);
            req.setAttribute("monthRevenue", monthRevenue);
            req.setAttribute("statusCounts", statusCounts);
            req.setAttribute("topVehicles", topVehicles);
            req.setAttribute("activeMaintenance", maintenanceService.getActive());
            req.setAttribute("upcomingReturns", bookingService.getActiveOrderedByReturn());
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load admin dashboard data.");
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
