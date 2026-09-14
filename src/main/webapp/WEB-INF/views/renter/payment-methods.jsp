<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section style="max-width: 720px; margin: 0 auto; padding: 7rem 1rem 4rem;">
    <h2 class="fw-bold mb-1" data-aos="fade-up">Payment Methods</h2>
    <p class="text-soft mb-4">Saved for faster checkout. This is a demo payment flow — only the last 4 digits and expiry are ever stored, never a full card number.</p>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
    <c:if test="${not empty sessionScope.successMessage}">
      <div class="alert alert-success">${sessionScope.successMessage}</div>
      <c:remove var="successMessage" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
      <div class="alert alert-danger">${sessionScope.errorMessage}</div>
      <c:remove var="errorMessage" scope="session" />
    </c:if>

    <div class="glass-card p-3 mb-4" data-aos="fade-up">
      <c:forEach var="m" items="${methods}">
        <div class="d-flex justify-content-between align-items-center py-2" style="border-bottom:1px solid var(--border-soft, #EEF1F6);">
          <div class="d-flex align-items-center gap-3">
            <i class="fa-solid ${m.type == 'CARD' ? 'fa-credit-card' : 'fa-wallet'} fa-lg" style="color: var(--accent);"></i>
            <div>
              <div class="fw-bold">
                ${m.label}
                <c:if test="${m['default']}"><span class="badge badge-available text-uppercase ms-1">Default</span></c:if>
              </div>
              <div class="text-secondary small">
                <c:choose>
                  <c:when test="${m.type == 'CARD'}">${m.maskedNumber} &nbsp;·&nbsp; Expires ${m.expiry}</c:when>
                  <c:otherwise>Wallet</c:otherwise>
                </c:choose>
              </div>
            </div>
          </div>
          <div class="d-flex gap-2">
            <c:if test="${!m['default']}">
              <form method="post" action="${pageContext.request.contextPath}/renter/payment-methods/default">
                <input type="hidden" name="paymentMethodId" value="${m.paymentMethodId}">
                <button class="btn btn-sm btn-outline-glass">Set Default</button>
              </form>
            </c:if>
            <a href="${pageContext.request.contextPath}/renter/payment-methods/edit?id=${m.paymentMethodId}" class="btn btn-sm btn-outline-glass">
              <i class="fa-solid fa-pen"></i>
            </a>
            <form method="post" action="${pageContext.request.contextPath}/renter/payment-methods/delete"
                  onsubmit="return confirm('Remove this payment method?');">
              <input type="hidden" name="paymentMethodId" value="${m.paymentMethodId}">
              <button class="btn btn-sm btn-outline-glass text-danger"><i class="fa-solid fa-trash"></i></button>
            </form>
          </div>
        </div>
      </c:forEach>
      <c:if test="${empty methods}">
        <p class="text-secondary text-center py-3 mb-0">No saved payment methods yet.</p>
      </c:if>
    </div>

    <div class="glass-card p-4" data-aos="fade-up">
      <h5 class="fw-bold mb-3">Add Payment Method</h5>
      <form method="post" action="${pageContext.request.contextPath}/renter/payment-methods">
        <div class="mb-3">
          <label class="form-label small">Type</label>
          <div class="d-flex gap-2">
            <input type="radio" class="btn-check" name="type" id="typeCard" value="CARD" checked>
            <label class="btn btn-outline-glass flex-fill" for="typeCard"><i class="fa-solid fa-credit-card me-1"></i>Card</label>

            <input type="radio" class="btn-check" name="type" id="typeWallet" value="WALLET">
            <label class="btn btn-outline-glass flex-fill" for="typeWallet"><i class="fa-solid fa-wallet me-1"></i>Wallet</label>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label small">Label</label>
          <input type="text" name="label" placeholder="e.g. Personal Visa" class="form-control form-control-glass" required>
        </div>

        <div id="cardFields">
          <div class="mb-3">
            <label class="form-label small">Card Number</label>
            <input type="text" name="cardNumber" placeholder="4242 4242 4242 4242" maxlength="19" class="form-control form-control-glass">
          </div>
          <div class="mb-3">
            <label class="form-label small">Expiry (MM/YY)</label>
            <input type="text" name="expiry" placeholder="MM/YY" maxlength="5" class="form-control form-control-glass">
          </div>
        </div>

        <div class="form-check mb-3">
          <input class="form-check-input" type="checkbox" name="makeDefault" value="true" id="makeDefault">
          <label class="form-check-label small" for="makeDefault">Set as default</label>
        </div>

        <button type="submit" class="btn btn-gradient w-100 py-2">Save Payment Method</button>
      </form>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />

  <script>
    document.getElementById('typeCard').addEventListener('change', toggle);
    document.getElementById('typeWallet').addEventListener('change', toggle);
    function toggle() {
      document.getElementById('cardFields').style.display = document.getElementById('typeCard').checked ? 'block' : 'none';
    }
  </script>
</body>
</html>
