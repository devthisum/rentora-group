package com.rentora.controller.renter;

import com.rentora.exception.ValidationException;
import com.rentora.model.SavedPaymentMethod;
import com.rentora.model.User;
import com.rentora.service.SavedPaymentMethodService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

/** Edits a saved payment method's label (and, for cards, expiry / replacement number). */
@WebServlet("/renter/payment-methods/edit")
public class SavedPaymentMethodEditServlet extends HttpServlet {

    private final SavedPaymentMethodService service = new SavedPaymentMethodService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession(false).getAttribute("user");
        try {
            long id = Long.parseLong(req.getParameter("id"));
            Optional<SavedPaymentMethod> method = service.getByUser(user.getUserId()).stream()
                    .filter(m -> m.getPaymentMethodId() == id).findFirst();
            if (method.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Payment method not found.");
                return;
            }
            req.setAttribute("method", method.get());
            if (req.getParameter("error") != null) {
                String err = req.getParameter("error");
                req.setAttribute("errorMessage", "1".equals(err) ? "Could not update this payment method." : err);
            }
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load this payment method.");
        }
        req.getRequestDispatcher("/WEB-INF/views/renter/edit-payment-method.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");
        long id = Long.parseLong(req.getParameter("paymentMethodId"));
        try {
            String type = req.getParameter("type");
            if ("WALLET".equals(type)) {
                service.updateWallet(id, user.getUserId(), req.getParameter("label"));
            } else {
                service.updateCard(id, user.getUserId(), req.getParameter("label"),
                        req.getParameter("cardNumber"), req.getParameter("expiry"));
            }
            session.setAttribute("successMessage", "Payment method updated.");
            resp.sendRedirect(req.getContextPath() + "/renter/payment-methods");
        } catch (ValidationException ve) {
            resp.sendRedirect(req.getContextPath() + "/renter/payment-methods/edit?id=" + id +
                    "&error=" + java.net.URLEncoder.encode(ve.getMessage(), "UTF-8"));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/renter/payment-methods/edit?id=" + id + "&error=1");
        }
    }
}
