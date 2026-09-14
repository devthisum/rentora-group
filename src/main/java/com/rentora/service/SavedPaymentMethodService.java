package com.rentora.service;

import com.rentora.dao.impl.SavedPaymentMethodDAOImpl;
import com.rentora.dao.interfaces.SavedPaymentMethodDAO;
import com.rentora.exception.ValidationException;
import com.rentora.model.SavedPaymentMethod;
import com.rentora.util.ValidationUtil;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Lets a renter save a payment method for faster checkout later. Since
 * Rentora's payments are entirely simulated, a card is only ever kept as a
 * masked "**** **** **** 1234" + expiry — the full number and CVV are
 * discarded the moment they're typed, same as the one-off payment form.
 */
public class SavedPaymentMethodService {

    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{2}$");

    private final SavedPaymentMethodDAO dao = new SavedPaymentMethodDAOImpl();

    public long addCard(long userId, String label, String cardNumber, String expiry, boolean makeDefault) throws Exception {
        String digitsOnly = cardNumber == null ? "" : cardNumber.replaceAll("\\s+", "");
        if (digitsOnly.length() < 12 || digitsOnly.length() > 19 || !digitsOnly.matches("\\d+")) {
            throw new ValidationException("Enter a valid card number.");
        }
        if (!EXPIRY_PATTERN.matcher(expiry == null ? "" : expiry.trim()).matches()) {
            throw new ValidationException("Expiry must be in MM/YY format.");
        }
        if (!ValidationUtil.isNotBlank(label)) {
            throw new ValidationException("Give this card a label, e.g. \"Personal Visa\".");
        }

        SavedPaymentMethod method = new SavedPaymentMethod();
        method.setUserId(userId);
        method.setType("CARD");
        method.setLabel(label.trim());
        method.setMaskedNumber(mask(digitsOnly));
        method.setExpiry(expiry.trim());

        return saveWithDefaultHandling(userId, method, makeDefault);
    }

    public long addWallet(long userId, String label, boolean makeDefault) throws Exception {
        if (!ValidationUtil.isNotBlank(label)) {
            throw new ValidationException("Give this wallet a label, e.g. \"My Wallet\".");
        }
        SavedPaymentMethod method = new SavedPaymentMethod();
        method.setUserId(userId);
        method.setType("WALLET");
        method.setLabel(label.trim());

        return saveWithDefaultHandling(userId, method, makeDefault);
    }

    private long saveWithDefaultHandling(long userId, SavedPaymentMethod method, boolean makeDefault) throws Exception {
        boolean hasExisting = !dao.findByUser(userId).isEmpty();
        // The very first saved method becomes the default automatically.
        method.setDefault(makeDefault || !hasExisting);
        if (method.isDefault()) {
            dao.clearDefault(userId);
        }
        return dao.create(method);
    }

    /** Edits a saved card's label/expiry, and optionally replaces the number (re-masked, never stored in full). */
    public void updateCard(long paymentMethodId, long userId, String label, String newCardNumber, String expiry) throws Exception {
        SavedPaymentMethod method = requireOwned(paymentMethodId, userId);
        if (!"CARD".equals(method.getType())) {
            throw new ValidationException("This isn't a card.");
        }
        if (!ValidationUtil.isNotBlank(label)) {
            throw new ValidationException("Give this card a label.");
        }
        if (!EXPIRY_PATTERN.matcher(expiry == null ? "" : expiry.trim()).matches()) {
            throw new ValidationException("Expiry must be in MM/YY format.");
        }

        method.setLabel(label.trim());
        method.setExpiry(expiry.trim());
        if (ValidationUtil.isNotBlank(newCardNumber)) {
            String digitsOnly = newCardNumber.replaceAll("\\s+", "");
            if (digitsOnly.length() < 12 || digitsOnly.length() > 19 || !digitsOnly.matches("\\d+")) {
                throw new ValidationException("Enter a valid card number.");
            }
            method.setMaskedNumber(mask(digitsOnly));
        }
        dao.update(method);
    }

    public void updateWallet(long paymentMethodId, long userId, String label) throws Exception {
        SavedPaymentMethod method = requireOwned(paymentMethodId, userId);
        if (!"WALLET".equals(method.getType())) {
            throw new ValidationException("This isn't a wallet.");
        }
        if (!ValidationUtil.isNotBlank(label)) {
            throw new ValidationException("Give this wallet a label.");
        }
        method.setLabel(label.trim());
        dao.update(method);
    }

    public void delete(long paymentMethodId, long userId) throws Exception {
        requireOwned(paymentMethodId, userId);
        dao.delete(paymentMethodId);
    }

    public void setDefault(long paymentMethodId, long userId) throws Exception {
        requireOwned(paymentMethodId, userId);
        dao.clearDefault(userId);
        dao.setDefault(paymentMethodId);
    }

    public List<SavedPaymentMethod> getByUser(long userId) throws Exception {
        return dao.findByUser(userId);
    }

    private SavedPaymentMethod requireOwned(long paymentMethodId, long userId) throws Exception {
        Optional<SavedPaymentMethod> maybe = dao.findById(paymentMethodId);
        if (maybe.isEmpty() || maybe.get().getUserId() != userId) {
            throw new ValidationException("Payment method not found.");
        }
        return maybe.get();
    }

    private String mask(String digitsOnly) {
        String last4 = digitsOnly.substring(digitsOnly.length() - 4);
        return "**** **** **** " + last4;
    }
}
