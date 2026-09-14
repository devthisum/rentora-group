package com.rentora.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Announcement {
    private long announcementId;
    private String title;
    private String message;
    private String priority; // LOW, NORMAL, HIGH, URGENT
    private String category;
    private long postedBy;
    private LocalDate expiryDate;
    private LocalDateTime createdAt;

    // Joined / computed display fields
    private String postedByName;
    private boolean readByCurrentUser;

    public long getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(long announcementId) { this.announcementId = announcementId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getPostedBy() { return postedBy; }
    public void setPostedBy(long postedBy) { this.postedBy = postedBy; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }

    public boolean isReadByCurrentUser() { return readByCurrentUser; }
    public void setReadByCurrentUser(boolean readByCurrentUser) { this.readByCurrentUser = readByCurrentUser; }
}
