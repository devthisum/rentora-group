package com.rentora.dao.interfaces;

import com.rentora.model.InquiryMessage;
import com.rentora.model.InquiryThread;
import java.util.List;
import java.util.Optional;

public interface InquiryDAO {
    long createThread(InquiryThread thread) throws Exception;
    Optional<InquiryThread> findExistingThread(long vehicleId, long renterId) throws Exception;
    Optional<InquiryThread> findThreadById(long threadId) throws Exception;
    List<InquiryThread> findThreadsByRenter(long renterId) throws Exception;
    /** Every open conversation across all renters — the shop's support inbox (any admin can view/reply). */
    List<InquiryThread> findAllThreads() throws Exception;

    long addMessage(InquiryMessage message) throws Exception;
    List<InquiryMessage> findMessagesByThread(long threadId) throws Exception;
    boolean markMessagesRead(long threadId, long readerId) throws Exception;
    boolean hasUnreadMessages(long threadId, long forUserId) throws Exception;
}
