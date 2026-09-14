<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem;">
    <h2 class="fw-bold mb-4" data-aos="fade-up">My Bookings</h2>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
    <c:if test="${not empty param.reviewed}"><div class="alert alert-success">Thanks — your review has been submitted!</div></c:if>
    <c:if test="${not empty param.reviewUpdated}"><div class="alert alert-success">Your review has been updated.</div></c:if>
    <c:if test="${not empty param.error}"><div class="alert alert-danger">${param.error}</div></c:if>
    <c:if test="${not empty sessionScope.successMessage}">
      <div class="alert alert-success">${sessionScope.successMessage}</div>
      <c:remove var="successMessage" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
      <div class="alert alert-danger">${sessionScope.errorMessage}</div>
      <c:remove var="errorMessage" scope="session" />
    </c:if>

    <div class="glass-card p-3" data-aos="fade-up">
      <table class="table table-glass align-middle mb-0">
        <thead>
          <tr><th>Vehicle</th><th>Dates</th><th>Total</th><th>Status</th><th>Action</th></tr>
        </thead>
        <tbody>
          <c:forEach var="b" items="${bookings}">
            <tr>
              <td>${b.vehicleBrand} ${b.vehicleModel}</td>
              <td>${b.startDate} &rarr; ${b.endDate}</td>
              <td>
                Rs. ${b.totalAmount}
                <c:if test="${b.lateFee > 0}">
                  <div class="text-danger small fw-semibold mt-1">
                    <i class="fa-solid fa-triangle-exclamation me-1"></i>+Rs. ${b.lateFee} late fee
                  </div>
                  <div class="text-soft small">Total with late fee: Rs. ${b.totalAmount + b.lateFee}</div>
                </c:if>
              </td>
              <td>
                <span class="badge badge-${b.status.toLowerCase()} text-uppercase">${b.status}</span>
              </td>
              <td>
                <c:if test="${b.status == 'AWAITING_PAYMENT'}">
                  <a href="${pageContext.request.contextPath}/renter/booking-summary?bookingId=${b.bookingId}" class="btn btn-sm btn-gradient me-1">
                    Pay Now
                  </a>
                  <form method="post" action="${pageContext.request.contextPath}/renter/booking/cancel" class="d-inline"
                        onsubmit="return confirm('Cancel this booking?');">
                    <input type="hidden" name="bookingId" value="${b.bookingId}">
                    <button type="submit" class="btn btn-sm btn-outline-glass text-danger">Cancel</button>
                  </form>
                </c:if>
                <c:if test="${b.status == 'COMPLETED'}">
                  <c:choose>
                    <c:when test="${not empty reviewsByBooking[b.bookingId]}">
                      <button class="btn btn-sm btn-outline-glass" data-bs-toggle="modal" data-bs-target="#editReviewModal${b.bookingId}">
                        <i class="fa-solid fa-pen me-1"></i>Edit Review
                      </button>
                    </c:when>
                    <c:otherwise>
                      <button class="btn btn-sm btn-outline-glass" data-bs-toggle="modal" data-bs-target="#reviewModal${b.bookingId}">
                        Leave a Review
                      </button>
                    </c:otherwise>
                  </c:choose>
                </c:if>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty bookings}">
            <tr><td colspan="5" class="text-secondary text-center py-4">You haven't made any bookings yet.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>

    <!-- Review modals live OUTSIDE the glass-card on purpose: a glass-card has
         backdrop-filter + overflow:hidden, both of which trap/clip a Bootstrap
         modal's position:fixed if it's nested inside one. -->
    <c:forEach var="b" items="${bookings}">
      <c:if test="${b.status == 'COMPLETED'}">
        <c:set var="existingReview" value="${reviewsByBooking[b.bookingId]}" />

        <c:if test="${empty existingReview}">
          <div class="modal fade" id="reviewModal${b.bookingId}" tabindex="-1">
            <div class="modal-dialog">
              <div class="modal-content">
                <form method="post" action="${pageContext.request.contextPath}/renter/review">
                  <input type="hidden" name="bookingId" value="${b.bookingId}">
                  <div class="modal-header">
                    <h5 class="modal-title">Review ${b.vehicleBrand} ${b.vehicleModel}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                  </div>
                  <div class="modal-body">
                    <label class="form-label small">Rating</label>
                    <select name="rating" class="form-select form-select-glass mb-3" required>
                      <option value="5">5 — Excellent</option>
                      <option value="4">4 — Good</option>
                      <option value="3">3 — Average</option>
                      <option value="2">2 — Poor</option>
                      <option value="1">1 — Terrible</option>
                    </select>
                    <label class="form-label small">Comment</label>
                    <textarea name="comment" rows="3" class="form-control form-control-glass" placeholder="Share your experience..."></textarea>
                  </div>
                  <div class="modal-footer">
                    <button type="submit" class="btn btn-gradient">Submit Review</button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </c:if>

        <c:if test="${not empty existingReview}">
          <div class="modal fade" id="editReviewModal${b.bookingId}" tabindex="-1">
            <div class="modal-dialog">
              <div class="modal-content">
                <form method="post" action="${pageContext.request.contextPath}/renter/review">
                  <input type="hidden" name="action" value="edit">
                  <input type="hidden" name="reviewId" value="${existingReview.reviewId}">
                  <div class="modal-header">
                    <h5 class="modal-title">Edit Review — ${b.vehicleBrand} ${b.vehicleModel}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                  </div>
                  <div class="modal-body">
                    <label class="form-label small">Rating</label>
                    <select name="rating" class="form-select form-select-glass mb-3" required>
                      <option value="5" ${existingReview.rating == 5 ? 'selected' : ''}>5 — Excellent</option>
                      <option value="4" ${existingReview.rating == 4 ? 'selected' : ''}>4 — Good</option>
                      <option value="3" ${existingReview.rating == 3 ? 'selected' : ''}>3 — Average</option>
                      <option value="2" ${existingReview.rating == 2 ? 'selected' : ''}>2 — Poor</option>
                      <option value="1" ${existingReview.rating == 1 ? 'selected' : ''}>1 — Terrible</option>
                    </select>
                    <label class="form-label small">Comment</label>
                    <textarea name="comment" rows="3" class="form-control form-control-glass">${existingReview.comment}</textarea>
                  </div>
                  <div class="modal-footer">
                    <button type="submit" class="btn btn-gradient">Save Changes</button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </c:if>
      </c:if>
    </c:forEach>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
