package com.rentora.observer;

import com.rentora.dao.impl.NotificationDAOImpl;
import com.rentora.model.Notification;

/** Persists the notification to the database so it shows in the user's bell/inbox. */
public class InAppNotificationObserver implements NotificationObserver {

    private final NotificationDAOImpl notificationDAO = new NotificationDAOImpl();

    @Override
    public void update(NotificationEvent event) {
        try {
            Notification notification = new Notification(
                    event.getRecipientUserId(), event.getTitle(), event.getMessage());
            notificationDAO.create(notification);
        } catch (Exception e) {
            // Logged, never allowed to break the primary booking/payment transaction
            System.err.println("InAppNotificationObserver failed: " + e.getMessage());
        }
    }
}
