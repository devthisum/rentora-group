package com.rentora.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer Pattern (Subject).
 * Services fire events here (e.g. "booking accepted") and every
 * registered observer (in-app notifier, email notifier, ...) reacts
 * independently, without the booking logic knowing about delivery channels.
 */
public class NotificationSubject {

    private final List<NotificationObserver> observers = new ArrayList<>();

    public void subscribe(NotificationObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyAll(NotificationEvent event) {
        for (NotificationObserver observer : observers) {
            observer.update(event);
        }
    }
}
