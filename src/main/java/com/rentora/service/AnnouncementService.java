package com.rentora.service;

import com.rentora.dao.impl.AnnouncementDAOImpl;
import com.rentora.dao.interfaces.AnnouncementDAO;
import com.rentora.exception.ValidationException;
import com.rentora.model.Announcement;
import com.rentora.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

public class AnnouncementService {

    private final AnnouncementDAO announcementDAO = new AnnouncementDAOImpl();

    public long create(long adminId, String title, String message, String priority, String category, LocalDate expiryDate) throws Exception {
        if (!ValidationUtil.isNotBlank(title) || !ValidationUtil.isNotBlank(message)) {
            throw new ValidationException("Title and message are required.");
        }
        Announcement a = new Announcement();
        a.setTitle(title);
        a.setMessage(message);
        a.setPriority(priority == null || priority.isBlank() ? "NORMAL" : priority);
        a.setCategory(category);
        a.setPostedBy(adminId);
        a.setExpiryDate(expiryDate);
        return announcementDAO.create(a);
    }

    public void update(long announcementId, String title, String message, String priority, String category, LocalDate expiryDate) throws Exception {
        Announcement a = new Announcement();
        a.setAnnouncementId(announcementId);
        a.setTitle(title);
        a.setMessage(message);
        a.setPriority(priority);
        a.setCategory(category);
        a.setExpiryDate(expiryDate);
        announcementDAO.update(a);
    }

    public void delete(long announcementId) throws Exception {
        announcementDAO.delete(announcementId);
    }

    public List<Announcement> getAllForAdmin() throws Exception {
        return announcementDAO.findAllForAdmin();
    }

    /** Active (non-expired) announcements, each flagged with whether this user has read it. */
    public List<Announcement> getActiveForUser(long userId) throws Exception {
        List<Announcement> announcements = announcementDAO.findAllActive();
        for (Announcement a : announcements) {
            a.setReadByCurrentUser(announcementDAO.isReadByUser(userId, a.getAnnouncementId()));
        }
        return announcements;
    }

    public void markRead(long userId, long announcementId) throws Exception {
        announcementDAO.markRead(userId, announcementId);
    }

    /** Marks every currently-active announcement as read for this user (e.g. on visiting the board). */
    public void markAllRead(long userId) throws Exception {
        for (Announcement a : announcementDAO.findAllActive()) {
            announcementDAO.markRead(userId, a.getAnnouncementId());
        }
    }

    public int getUnreadCount(long userId) throws Exception {
        return announcementDAO.countUnreadForUser(userId);
    }
}
