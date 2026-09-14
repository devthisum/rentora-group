package com.rentora.dao.interfaces;

import com.rentora.model.Notification;
import java.util.List;

public interface NotificationDAO {
    long create(Notification notification) throws Exception;
    List<Notification> findByUser(long userId) throws Exception;
    boolean markAsRead(long notificationId) throws Exception;
}
