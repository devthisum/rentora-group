package com.rentora.controller;

import com.google.gson.Gson;
import com.rentora.model.Booking;
import com.rentora.model.MaintenanceRecord;
import com.rentora.service.BookingService;
import com.rentora.service.MaintenanceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Public JSON endpoint feeding the availability calendar on a vehicle's
 * booking page: which dates are already booked (green) and which are
 * blocked off as a maintenance buffer around a checkout (red).
 *
 * Buffer sizing:
 *  - Before a booking is actually returned: a default ±2 day buffer around
 *    its checkout date, in case of an early/late return.
 *  - Once returned and a maintenance record is open: the buffer becomes
 *    forward-only, sized by staff's estimated_days (defaults to 2 until they
 *    set one) — shrinks below 2 days, extends beyond 2 days, live as staff
 *    edits it on the Maintenance Dashboard.
 *  - Once maintenance is resolved (OK/RESOLVED), no buffer — vehicle is
 *    fully available again.
 */
@WebServlet("/vehicle-calendar")
public class VehicleCalendarServlet extends HttpServlet {

    private static final int DEFAULT_BUFFER_DAYS = 2;
    private final Gson gson = new Gson();

    private record DateRange(String start, String end) { }
    private record CalendarResponse(List<DateRange> booked, List<DateRange> maintenance) { }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        BookingService bookingService = (BookingService) req.getServletContext().getAttribute("bookingService");
        MaintenanceService maintenanceService = (MaintenanceService) req.getServletContext().getAttribute("maintenanceService");

        List<DateRange> booked = new ArrayList<>();
        List<DateRange> maintenance = new ArrayList<>();

        try {
            long vehicleId = Long.parseLong(req.getParameter("vehicleId"));
            List<Booking> activeBookings = bookingService.getActiveByVehicle(vehicleId);

            for (Booking b : activeBookings) {
                if (!"RETURNED".equals(b.getStatus())) {
                    booked.add(new DateRange(b.getStartDate().toString(), b.getEndDate().toString()));
                }

                Optional<MaintenanceRecord> maybeRecord = maintenanceService.getByBookingId(b.getBookingId());
                LocalDate checkout = b.getEndDate();

                if (maybeRecord.isPresent()) {
                    MaintenanceRecord record = maybeRecord.get();
                    String stage = record.getStage();
                    if ("CHECKING".equals(stage) || "UNDER_MAINTENANCE".equals(stage) || "AWAITING_CUSTOMER_PAYMENT".equals(stage)) {
                        int days = (record.getEstimatedDays() != null && record.getEstimatedDays() > 0)
                                ? record.getEstimatedDays() : DEFAULT_BUFFER_DAYS;
                        maintenance.add(new DateRange(checkout.toString(), checkout.plusDays(days).toString()));
                    }
                    // OK / RESOLVED -> no buffer, vehicle's fully free again.
                } else {
                    // Not yet returned — default symmetric buffer around the expected checkout.
                    maintenance.add(new DateRange(
                            checkout.minusDays(DEFAULT_BUFFER_DAYS).toString(),
                            checkout.plusDays(DEFAULT_BUFFER_DAYS).toString()));
                }
            }
        } catch (Exception e) {
            // Malformed/missing vehicleId — just return empty ranges rather than an error page.
        }

        resp.getWriter().write(gson.toJson(new CalendarResponse(booked, maintenance)));
    }
}
