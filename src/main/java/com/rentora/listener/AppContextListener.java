package com.rentora.listener;

import com.rentora.observer.EmailNotificationObserver;
import com.rentora.observer.InAppNotificationObserver;
import com.rentora.observer.NotificationSubject;
import com.rentora.service.BookingService;
import com.rentora.service.InquiryService;
import com.rentora.service.MaintenanceService;
import com.rentora.service.PaymentService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wires up the Observer Pattern subject once at application startup and
 * shares single service instances (with observers attached) across all
 * servlets via the ServletContext. Also starts the background job that
 * auto-cancels unpaid bookings after their 10-minute payment window.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        NotificationSubject subject = new NotificationSubject();
        subject.subscribe(new InAppNotificationObserver());
        subject.subscribe(new EmailNotificationObserver());

        BookingService bookingService = new BookingService(subject);
        InquiryService inquiryService = new InquiryService(subject);
        PaymentService paymentService = new PaymentService(subject);
        MaintenanceService maintenanceService = new MaintenanceService(subject);

        sce.getServletContext().setAttribute("notificationSubject", subject);
        sce.getServletContext().setAttribute("bookingService", bookingService);
        sce.getServletContext().setAttribute("inquiryService", inquiryService);
        sce.getServletContext().setAttribute("paymentService", paymentService);
        sce.getServletContext().setAttribute("maintenanceService", maintenanceService);

        // Every 60s, cancel any booking that's been sitting AWAITING_PAYMENT
        // for more than 10 minutes, freeing those dates back up for others.
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rentora-booking-expiry");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                bookingService.cancelExpiredBookings();
            } catch (Exception e) {
                System.err.println("Booking expiry job failed: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);

        System.out.println("Rentora application context initialized: Observer notification pipeline ready.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) scheduler.shutdownNow();
        com.rentora.util.DBConnectionManager.getInstance().shutdown();
    }
}
