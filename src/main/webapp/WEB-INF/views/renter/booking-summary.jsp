<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section style="max-width: 640px; margin: 0 auto; padding: 7rem 1rem 4rem;">
    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
    <c:if test="${not empty param.error}">
      <div class="alert alert-danger">${param.error == '1' ? 'Could not update those dates. Please try again.' : param.error}</div>
    </c:if>
    <c:if test="${not empty param.updated}"><div class="alert alert-success">Booking dates updated.</div></c:if>

    <c:if test="${not empty booking}">
      <!-- ============ COUNTDOWN ============ -->
      <div class="glass-card p-3 mb-4 text-center" data-aos="fade-up" id="countdownCard">
        <span class="text-secondary small">Complete payment within</span>
        <div id="countdownTimer" class="fw-bold" style="font-size:1.6rem; color: var(--accent);">10:00</div>
      </div>

      <h2 class="fw-bold mb-1" data-aos="fade-up">Review Your Booking</h2>
      <p class="text-soft mb-4" data-aos="fade-up">Check everything below, then continue to payment.</p>

      <!-- ============ VEHICLE ============ -->
      <div class="glass-card p-4 mb-4" data-aos="fade-up">
        <div class="row g-3 align-items-center">
          <c:if test="${not empty vehicle}">
            <div class="col-4">
              <img src="${not empty vehicle.imageUrl ? vehicle.imageUrl : 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=600'}"
                   alt="${vehicle.brand} ${vehicle.model}" style="width:100%; height:90px; object-fit:cover; border-radius:var(--r);">
            </div>
          </c:if>
          <div class="col">
            <h5 class="fw-bold mb-1">${booking.vehicleBrand} ${booking.vehicleModel}</h5>
            <c:if test="${not empty vehicle}">
              <div class="d-flex gap-2 flex-wrap">
                <span class="feature-pill"><i class="fa-solid fa-gear me-1"></i>${vehicle.transmission}</span>
                <span class="feature-pill"><i class="fa-solid fa-gas-pump me-1"></i>${vehicle.fuelType}</span>
                <span class="feature-pill"><i class="fa-solid fa-users me-1"></i>${vehicle.seats} Seats</span>
              </div>
            </c:if>
          </div>
        </div>
      </div>

      <!-- ============ RENTER ============ -->
      <c:if test="${not empty renter}">
        <div class="glass-card p-4 mb-4" data-aos="fade-up">
          <h6 class="fw-bold mb-3"><i class="fa-solid fa-user me-2" style="color: var(--accent);"></i>Renter Details</h6>
          <div class="d-flex justify-content-between mb-2"><span class="text-soft">Name</span><strong>${renter.fullName}</strong></div>
          <div class="d-flex justify-content-between mb-2"><span class="text-soft">Email</span><strong>${renter.email}</strong></div>
          <div class="d-flex justify-content-between"><span class="text-soft">Phone</span><strong>${renter.phone}</strong></div>
        </div>
      </c:if>

      <!-- ============ BOOKING SUMMARY ============ -->
      <div class="glass-card p-4 mb-4" data-aos="fade-up">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h6 class="fw-bold mb-0"><i class="fa-solid fa-receipt me-2" style="color: var(--accent);"></i>Booking Summary</h6>
          <button type="button" class="btn btn-sm btn-outline-glass" data-bs-toggle="collapse" data-bs-target="#editDatesForm">
            <i class="fa-solid fa-pen me-1"></i>Edit Dates
          </button>
        </div>
        <div class="d-flex justify-content-between mb-2">
          <span class="text-soft">Pickup Date</span><strong>${booking.startDate}</strong>
        </div>
        <div class="d-flex justify-content-between mb-2">
          <span class="text-soft">Return Date</span><strong>${booking.endDate}</strong>
        </div>
        <div class="d-flex justify-content-between mb-2">
          <span class="text-soft">Duration</span><strong>${numDays} day${numDays == 1 ? '' : 's'}</strong>
        </div>
        <c:if test="${not empty vehicle}">
          <div class="d-flex justify-content-between mb-2">
            <span class="text-soft">Rate</span><strong>Rs. ${vehicle.pricePerDay} / day</strong>
          </div>
        </c:if>
        <hr>
        <div class="d-flex justify-content-between">
          <span class="fw-bold">Total Due</span>
          <span class="fw-bold" style="font-size:1.3rem; color: var(--accent);">Rs. ${booking.totalAmount}</span>
        </div>

        <!-- Edit dates (collapsed by default) -->
        <div class="collapse mt-3" id="editDatesForm">
          <hr>
          <form method="post" action="${pageContext.request.contextPath}/renter/booking/update-dates">
            <input type="hidden" name="bookingId" value="${booking.bookingId}">
            <input type="hidden" name="redirectTo" value="booking-summary">
            <div class="d-flex gap-2 mb-2">
              <div class="flex-fill">
                <label class="form-label small">Pickup Date</label>
                <input type="date" name="startDate" value="${booking.startDate}" class="form-control form-control-glass" required>
              </div>
              <div class="flex-fill">
                <label class="form-label small">Return Date</label>
                <input type="date" name="endDate" value="${booking.endDate}" class="form-control form-control-glass" required>
              </div>
            </div>
            <div class="mb-2">
              <label class="form-label small">Recalculate with</label>
              <select name="paymentMethod" class="form-select form-select-glass">
                <option value="CARD">Card (incl. 5% service fee)</option>
                <option value="WALLET">Wallet (2% loyalty discount)</option>
              </select>
            </div>
            <button type="submit" class="btn btn-outline-glass w-100">Update Booking</button>
          </form>
        </div>
      </div>

      <a href="${pageContext.request.contextPath}/renter/payment?bookingId=${booking.bookingId}" class="btn btn-gradient w-100 py-2" data-aos="fade-up">
        Continue to Payment <i class="fa-solid fa-arrow-right ms-1"></i>
      </a>

      <form method="post" action="${pageContext.request.contextPath}/renter/booking/cancel" class="mt-2"
            onsubmit="return confirm('Cancel this booking? This cannot be undone.');" data-aos="fade-up">
        <input type="hidden" name="bookingId" value="${booking.bookingId}">
        <button type="submit" class="btn btn-outline-glass text-danger w-100 py-2">
          <i class="fa-solid fa-xmark me-1"></i>Cancel Booking
        </button>
      </form>
    </c:if>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />

  <script>
    // ---- 10-minute payment countdown (shared with the payment page — see payment.jsp) ----
    <c:if test="${not empty booking}">
    (function() {
      var createdAt = new Date("${booking.createdAt}");
      var deadline = new Date(createdAt.getTime() + 10 * 60 * 1000);
      var timerEl = document.getElementById('countdownTimer');
      var cardEl = document.getElementById('countdownCard');

      function tick() {
        var msLeft = deadline - new Date();
        if (msLeft <= 0) {
          timerEl.textContent = "Expired";
          cardEl.classList.add('border-danger');
          clearInterval(interval);
          setTimeout(function() {
            alert("This booking's 10-minute payment window has expired. Please book again.");
            window.location.href = "${pageContext.request.contextPath}/renter/dashboard";
          }, 800);
          return;
        }
        var totalSec = Math.floor(msLeft / 1000);
        var min = Math.floor(totalSec / 60);
        var sec = totalSec % 60;
        timerEl.textContent = min + ":" + (sec < 10 ? "0" : "") + sec;
        if (totalSec <= 60) timerEl.style.color = "#EF4444";
      }
      var interval = setInterval(tick, 1000);
      tick();
    })();
    </c:if>
  </script>
</body>
</html>
