package com.rentora.observer;

/** Observer Pattern: implementers react to a NotificationEvent (in-app, email, SMS, etc.). */
public interface NotificationObserver {
    void update(NotificationEvent event);
}
