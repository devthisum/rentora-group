<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="text-center" style="max-width: 480px; margin: 0 auto; padding: 8rem 1rem 5rem;">
    <div class="glass-card p-5" data-aos="fade-up">
      <div style="width:80px;height:80px;border-radius:50%;background:rgba(34,197,94,0.12);
                  display:flex;align-items:center;justify-content:center;margin:0 auto 1.5rem;">
        <i class="fa-solid fa-check fa-2x" style="color:#22C55E;"></i>
      </div>
      <h3 class="fw-bold mb-2">Booking Confirmed!</h3>
      <p class="text-secondary mb-4">
        Your payment was successful and your booking is now confirmed — no further approval needed.
      </p>

      <c:if test="${not empty booking}">
        <div class="text-start glass-card p-3 mb-4" style="background: var(--bg2);">
          <div class="d-flex justify-content-between mb-1">
            <span class="text-secondary small">Vehicle</span>
            <strong class="small">${booking.vehicleBrand} ${booking.vehicleModel}</strong>
          </div>
          <div class="d-flex justify-content-between mb-1">
            <span class="text-secondary small">Dates</span>
            <strong class="small">${booking.startDate} &rarr; ${booking.endDate}</strong>
          </div>
          <c:if test="${not empty payment}">
            <div class="d-flex justify-content-between">
              <span class="text-secondary small">Transaction Ref</span>
              <strong class="small">${payment.transactionRef}</strong>
            </div>
          </c:if>
        </div>
      </c:if>

      <a href="${pageContext.request.contextPath}/renter/dashboard" class="btn btn-gradient">
        View My Bookings
      </a>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
