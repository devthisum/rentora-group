package com.rentora.observer;

/** Immutable payload passed to observers when a notifiable event occurs. */
public class NotificationEvent {
    private final long recipientUserId;
    private final String title;
    private final String message;

    public NotificationEvent(long recipientUserId, String title, String message) {
        this.recipientUserId = recipientUserId;
        this.title = title;
        this.message = message;
    }

    public long getRecipientUserId() { return recipientUserId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
}
