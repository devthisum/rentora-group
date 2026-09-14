package com.rentora.dao.interfaces;

import com.rentora.model.Announcement;
import java.util.List;
import java.util.Optional;

public interface AnnouncementDAO {
    long create(Announcement announcement) throws Exception;
    Optional<Announcement> findById(long announcementId) throws Exception;
    List<Announcement> findAllActive() throws Exception; // not expired
    List<Announcement> findAllForAdmin() throws Exception; // including expired, for management
    boolean update(Announcement announcement) throws Exception;
    boolean delete(long announcementId) throws Exception;

    boolean markRead(long userId, long announcementId) throws Exception;
    boolean isReadByUser(long userId, long announcementId) throws Exception;
    int countUnreadForUser(long userId) throws Exception;
}
