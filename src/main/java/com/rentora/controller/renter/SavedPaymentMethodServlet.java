package com.rentora.controller.renter;

import com.rentora.exception.ValidationException;
import com.rentora.model.User;
import com.rentora.service.SavedPaymentMethodService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Lists a renter's saved payment methods (GET) and adds a new one (POST). */
@WebServlet("/renter/payment-methods")
public class SavedPaymentMethodServlet extends HttpServlet {

    private final SavedPaymentMethodService service = new SavedPaymentMethodService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession(false).getAttribute("user");
        try {
            req.setAttribute("methods", service.getByUser(user.getUserId()));
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Could not load your payment methods.");
        }
        req.getRequestDispatcher("/WEB-INF/views/renter/payment-methods.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");
        String type = req.getParameter("type"); // CARD or WALLET
        boolean makeDefault = "true".equals(req.getParameter("makeDefault"));

        try {
            if ("WALLET".equals(type)) {
                service.addWallet(user.getUserId(), req.getParameter("label"), makeDefault);
            } else {
                service.addCard(user.getUserId(), req.getParameter("label"),
                        req.getParameter("cardNumber"), req.getParameter("expiry"), makeDefault);
            }
            session.setAttribute("successMessage", "Payment method saved.");
        } catch (ValidationException ve) {
            session.setAttribute("errorMessage", ve.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Could not save this payment method.");
        }
        resp.sendRedirect(req.getContextPath() + "/renter/payment-methods");
    }
}
