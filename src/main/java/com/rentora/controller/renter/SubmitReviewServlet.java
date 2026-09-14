package com.rentora.controller.renter;

import com.rentora.model.User;
import com.rentora.service.ReviewService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/renter/review")
public class SubmitReviewServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User renter = (User) session.getAttribute("user");
        String action = req.getParameter("action"); // "edit" or absent (= submit new)
        try {
            int rating = Integer.parseInt(req.getParameter("rating"));
            String comment = req.getParameter("comment");

            if ("edit".equals(action)) {
                long reviewId = Long.parseLong(req.getParameter("reviewId"));
                reviewService.updateReview(reviewId, renter.getUserId(), rating, comment);
                resp.sendRedirect(req.getContextPath() + "/renter/dashboard?reviewUpdated=1");
            } else {
                long bookingId = Long.parseLong(req.getParameter("bookingId"));
                reviewService.submitReview(renter.getUserId(), bookingId, rating, comment);
                resp.sendRedirect(req.getContextPath() + "/renter/dashboard?reviewed=1");
            }

        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/renter/dashboard?error=" +
                    java.net.URLEncoder.encode(e.getMessage() == null ? "Review failed" : e.getMessage(), "UTF-8"));
        }
    }
}
