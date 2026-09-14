package com.rentora.dao.interfaces;

import com.rentora.model.SavedPaymentMethod;
import java.util.List;
import java.util.Optional;

public interface SavedPaymentMethodDAO {
    long create(SavedPaymentMethod method) throws Exception;
    Optional<SavedPaymentMethod> findById(long id) throws Exception;
    List<SavedPaymentMethod> findByUser(long userId) throws Exception;
    boolean update(SavedPaymentMethod method) throws Exception;
    boolean delete(long id) throws Exception;
    /** Clears the default flag on every one of this user's saved methods (used before setting a new default). */
    boolean clearDefault(long userId) throws Exception;
    boolean setDefault(long id) throws Exception;
}
