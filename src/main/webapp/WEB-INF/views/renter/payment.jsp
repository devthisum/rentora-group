<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section style="max-width: 560px; margin: 0 auto; padding: 7rem 1rem 4rem;">
    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
    <c:if test="${not empty param.error}">
      <div class="alert alert-danger">${param.error == '1' ? 'Payment could not be processed. Please try again.' : param.error}</div>
    </c:if>
    <c:if test="${not empty param.updated}"><div class="alert alert-success">Booking dates updated.</div></c:if>

    <c:if test="${not empty booking}">
      <!-- ============ COUNTDOWN ============ -->
      <div class="glass-card p-3 mb-4 text-center" data-aos="fade-up" id="countdownCard">
        <span class="text-secondary small">Complete payment within</span>
        <div id="countdownTimer" class="fw-bold" style="font-size:1.6rem; color: var(--accent);">10:00</div>
      </div>

      <div class="glass-card p-3 mb-4" data-aos="fade-up">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <div class="text-soft small">${booking.vehicleBrand} ${booking.vehicleModel} &bull; ${booking.startDate} &rarr; ${booking.endDate}</div>
            <div class="fw-bold" style="font-size:1.1rem; color: var(--accent);">Rs. ${booking.totalAmount}</div>
          </div>
          <a href="${pageContext.request.contextPath}/renter/booking-summary?bookingId=${booking.bookingId}" class="btn btn-sm btn-outline-glass">
            <i class="fa-solid fa-arrow-left me-1"></i>Back to Summary
          </a>
        </div>
      </div>

      <form method="post" action="${pageContext.request.contextPath}/renter/payment" id="paymentForm">
        <input type="hidden" name="bookingId" value="${booking.bookingId}">

        <!-- ============ CUSTOMER DETAILS (required before payment) ============ -->
        <div class="glass-card p-4 mb-4" data-aos="fade-up">
          <h5 class="fw-bold mb-3"><i class="fa-solid fa-id-card me-2" style="color: var(--accent);"></i>Your Details</h5>
          <p class="text-soft small mb-3">We need this on file before you can pick up a vehicle — saved to your account, so you won't need to re-enter it next time.</p>

          <div class="mb-3">
            <label class="form-label small">Street Address</label>
            <input type="text" name="addressStreet" value="${renterProfile.addressStreet}" class="form-control form-control-glass" placeholder="123 Galle Road" required>
          </div>
          <div class="d-flex gap-3">
            <div class="mb-3 flex-fill">
              <label class="form-label small">City</label>
              <input type="text" name="addressCity" value="${renterProfile.addressCity}" class="form-control form-control-glass" placeholder="Negombo" required>
            </div>
            <div class="mb-3 flex-fill">
              <label class="form-label small">Postal Code</label>
              <input type="text" name="addressPostalCode" value="${renterProfile.addressPostalCode}" class="form-control form-control-glass" placeholder="11500" required>
            </div>
          </div>
          <div class="mb-1">
            <label class="form-label small">Driving License Number</label>
            <input type="text" name="drivingLicenseNumber" value="${renterProfile.drivingLicenseNumber}" class="form-control form-control-glass" placeholder="B1234567" required>
          </div>
        </div>

        <!-- ============ PAYMENT ============ -->
        <div class="glass-card p-4" data-aos="fade-up">
          <h5 class="fw-bold mb-3"><i class="fa-solid fa-lock me-2" style="color: var(--accent);"></i>Complete Payment</h5>

          <div class="mb-3">
            <label class="form-label small">Payment Method</label>
            <div class="d-flex gap-2">
              <input type="radio" class="btn-check" name="paymentMethod" id="payCard" value="CARD" checked>
              <label class="btn btn-outline-glass flex-fill" for="payCard"><i class="fa-solid fa-credit-card me-1"></i>Card</label>

              <input type="radio" class="btn-check" name="paymentMethod" id="payWallet" value="WALLET">
              <label class="btn btn-outline-glass flex-fill" for="payWallet"><i class="fa-solid fa-wallet me-1"></i>Wallet</label>
            </div>
          </div>

          <c:if test="${not empty savedMethods}">
            <div class="mb-3">
              <label class="form-label small">Use a saved method</label>
              <select id="savedMethodSelect" class="form-select form-select-glass">
                <option value="">Enter details manually</option>
                <c:forEach var="m" items="${savedMethods}">
                  <option value="${m.type}" data-masked="${m.maskedNumber}" data-expiry="${m.expiry}">
                    ${m.label} ${m.type == 'CARD' ? m.maskedNumber : ''}${m['default'] ? ' (Default)' : ''}
                  </option>
                </c:forEach>
              </select>
            </div>
          </c:if>

          <div id="cardFields">
            <div class="mb-3">
              <label class="form-label small">Card Number</label>
              <input type="text" id="cardNumberInput" class="form-control form-control-glass" placeholder="4242 4242 4242 4242" maxlength="19">
            </div>
            <div class="d-flex gap-3">
              <div class="mb-3 flex-fill">
                <label class="form-label small">Expiry</label>
                <input type="text" id="expiryInput" class="form-control form-control-glass" placeholder="MM/YY" maxlength="5">
              </div>
              <div class="mb-3 flex-fill">
                <label class="form-label small">CVV</label>
                <input type="text" class="form-control form-control-glass" placeholder="123" maxlength="4">
              </div>
            </div>

          </div>

          <div id="walletFields" style="display:none;">
            <div class="mb-3">
              <label class="form-label small">Wallet PIN</label>
              <input type="password" class="form-control form-control-glass" placeholder="Enter your 4-digit PIN" maxlength="4">
            </div>
          </div>

          <p class="text-secondary small mb-3">
            <i class="fa-solid fa-circle-info me-1"></i>
            This is a simulated payment for demonstration purposes — no real charge occurs and card details above are never transmitted or stored.
          </p>

          <button type="submit" class="btn btn-gradient w-100 py-2">
            <i class="fa-solid fa-lock me-2"></i>Pay Rs. ${booking.totalAmount} &amp; Confirm Booking
          </button>
        </div>
      </form>
    </c:if>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script>
    document.getElementById('payCard').addEventListener('change', toggleFields);
    document.getElementById('payWallet').addEventListener('change', toggleFields);
    function toggleFields() {
      document.getElementById('cardFields').style.display = document.getElementById('payCard').checked ? 'block' : 'none';
      document.getElementById('walletFields').style.display = document.getElementById('payWallet').checked ? 'block' : 'none';
    }

    var savedSelect = document.getElementById('savedMethodSelect');
    if (savedSelect) {
      savedSelect.addEventListener('change', function() {
        var opt = savedSelect.selectedOptions[0];
        if (!opt.value) return; // "Enter details manually"
        if (opt.value === 'WALLET') {
          document.getElementById('payWallet').checked = true;
        } else {
          document.getElementById('payCard').checked = true;
          var numEl = document.getElementById('cardNumberInput');
          var expEl = document.getElementById('expiryInput');
          if (numEl) numEl.value = opt.dataset.masked || '';
          if (expEl) expEl.value = opt.dataset.expiry || '';
        }
        toggleFields();
      });
    }

    // ---- 10-minute payment countdown ----
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
          document.getElementById('paymentForm').querySelector('button[type=submit]').disabled = true;
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
