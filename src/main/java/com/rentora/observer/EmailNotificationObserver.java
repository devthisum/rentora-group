package com.rentora.observer;

/**
 * Sends an email via the configured mail provider (JavaMail/SMTP).
 * Implementation stubbed here — plug in real SMTP config in production.
 */
public class EmailNotificationObserver implements NotificationObserver {

    @Override
    public void update(NotificationEvent event) {
        // TODO: integrate JavaMail / SES / SendGrid here.
        System.out.println("[EMAIL] To user " + event.getRecipientUserId()
                + " | " + event.getTitle() + " - " + event.getMessage());
    }
}
