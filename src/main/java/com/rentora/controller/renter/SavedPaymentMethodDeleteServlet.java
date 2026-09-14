package com.rentora.controller.renter;

import com.rentora.model.User;
import com.rentora.service.SavedPaymentMethodService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/renter/payment-methods/delete")
public class SavedPaymentMethodDeleteServlet extends HttpServlet {

    private final SavedPaymentMethodService service = new SavedPaymentMethodService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");
        try {
            long id = Long.parseLong(req.getParameter("paymentMethodId"));
            service.delete(id, user.getUserId());
            session.setAttribute("successMessage", "Payment method removed.");
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Could not remove this payment method.");
        }
        resp.sendRedirect(req.getContextPath() + "/renter/payment-methods");
    }
}
