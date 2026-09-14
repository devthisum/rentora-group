package com.rentora.service;

import com.rentora.dao.impl.InquiryDAOImpl;
import com.rentora.dao.impl.UserDAOImpl;
import com.rentora.dao.impl.VehicleDAOImpl;
import com.rentora.dao.interfaces.InquiryDAO;
import com.rentora.dao.interfaces.UserDAO;
import com.rentora.dao.interfaces.VehicleDAO;
import com.rentora.exception.ValidationException;
import com.rentora.model.InquiryMessage;
import com.rentora.model.InquiryThread;
import com.rentora.model.User;
import com.rentora.model.Vehicle;
import com.rentora.observer.NotificationEvent;
import com.rentora.observer.NotificationSubject;
import com.rentora.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for the shop's support inbox: renters ask questions about a
 * vehicle, and any admin/staff member can view and reply — there's no
 * per-vehicle owner to route messages to.
 */
public class InquiryService {

    private final InquiryDAO inquiryDAO = new InquiryDAOImpl();
    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final NotificationSubject notificationSubject;

    public InquiryService(NotificationSubject notificationSubject) {
        this.notificationSubject = notificationSubject;
    }

    /** Starts a new inquiry, or if the renter already has an open thread about this
     *  vehicle, appends the message to that existing thread instead of duplicating it. */
    public long startOrContinueInquiry(long renterId, long vehicleId, String message) throws Exception {
        if (!ValidationUtil.isNotBlank(message)) {
            throw new ValidationException("Please write a message.");
        }
        Optional<Vehicle> maybeVehicle = vehicleDAO.findById(vehicleId);
        if (maybeVehicle.isEmpty()) {
            throw new ValidationException("Vehicle not found.");
        }
        Vehicle vehicle = maybeVehicle.get();

        Optional<InquiryThread> existing = inquiryDAO.findExistingThread(vehicleId, renterId);
        long threadId;
        if (existing.isPresent()) {
            threadId = existing.get().getThreadId();
        } else {
            InquiryThread thread = new InquiryThread();
            thread.setVehicleId(vehicleId);
            thread.setRenterId(renterId);
            threadId = inquiryDAO.createThread(thread);
        }

        InquiryMessage msg = new InquiryMessage();
        msg.setThreadId(threadId);
        msg.setSenderId(renterId);
        msg.setMessage(message);
        inquiryDAO.addMessage(msg);

        for (User admin : userDAO.findAllByRole("ADMIN")) {
            notificationSubject.notifyAll(new NotificationEvent(
                    admin.getUserId(),
                    "New Inquiry",
                    "New question about " + vehicle.getBrand() + " " + vehicle.getModel() + "."));
        }

        return threadId;
    }

    /** The renter who opened the thread, or any admin, may reply. */
    public void reply(long threadId, long senderId, boolean senderIsAdmin, String message) throws Exception {
        if (!ValidationUtil.isNotBlank(message)) {
            throw new ValidationException("Please write a message.");
        }
        InquiryThread thread = requireParticipant(threadId, senderId, senderIsAdmin);

        InquiryMessage msg = new InquiryMessage();
        msg.setThreadId(threadId);
        msg.setSenderId(senderId);
        msg.setMessage(message);
        inquiryDAO.addMessage(msg);

        if (senderIsAdmin) {
            // Admin replying -> notify the renter
            notificationSubject.notifyAll(new NotificationEvent(
                    thread.getRenterId(), "New Message",
                    "New reply about " + thread.getVehicleBrand() + " " + thread.getVehicleModel() + "."));
        } else {
            // Renter replying -> notify every admin
            for (User admin : userDAO.findAllByRole("ADMIN")) {
                notificationSubject.notifyAll(new NotificationEvent(
                        admin.getUserId(), "New Message",
                        "New reply about " + thread.getVehicleBrand() + " " + thread.getVehicleModel() + "."));
            }
        }
    }

    /** Loads a thread, verifying the requester is a participant (renter who owns it, or any admin), and marks it read for them. */
    public InquiryThread getThreadDetail(long threadId, long requestingUserId, boolean requesterIsAdmin) throws Exception {
        InquiryThread thread = requireParticipant(threadId, requestingUserId, requesterIsAdmin);
        inquiryDAO.markMessagesRead(threadId, requestingUserId);
        return thread;
    }

    private InquiryThread requireParticipant(long threadId, long userId, boolean isAdmin) throws Exception {
        Optional<InquiryThread> maybeThread = inquiryDAO.findThreadById(threadId);
        if (maybeThread.isEmpty()) {
            throw new ValidationException("Conversation not found.");
        }
        InquiryThread thread = maybeThread.get();
        if (!isAdmin && thread.getRenterId() != userId) {
            throw new ValidationException("You don't have permission to view this conversation.");
        }
        return thread;
    }

    public List<InquiryMessage> getMessages(long threadId) throws Exception {
        return inquiryDAO.findMessagesByThread(threadId);
    }

    public List<InquiryThread> getThreadsForRenter(long renterId) throws Exception {
        List<InquiryThread> threads = inquiryDAO.findThreadsByRenter(renterId);
        for (InquiryThread t : threads) {
            t.setHasUnread(inquiryDAO.hasUnreadMessages(t.getThreadId(), renterId));
        }
        return threads;
    }

    /** Every open conversation in the shop's support inbox — visible to all admins. */
    public List<InquiryThread> getAllThreads() throws Exception {
        return inquiryDAO.findAllThreads();
    }
}
