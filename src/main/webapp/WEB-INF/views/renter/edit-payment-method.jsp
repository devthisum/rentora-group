<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section style="max-width: 480px; margin: 0 auto; padding: 7rem 1rem 4rem;">
    <h2 class="fw-bold mb-4" data-aos="fade-up">Edit Payment Method</h2>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <c:if test="${not empty method}">
      <div class="glass-card p-4" data-aos="fade-up">
        <form method="post" action="${pageContext.request.contextPath}/renter/payment-methods/edit">
          <input type="hidden" name="paymentMethodId" value="${method.paymentMethodId}">
          <input type="hidden" name="type" value="${method.type}">

          <div class="mb-3">
            <label class="form-label small">Label</label>
            <input type="text" name="label" value="${method.label}" class="form-control form-control-glass" required>
          </div>

          <c:if test="${method.type == 'CARD'}">
            <div class="mb-3">
              <label class="form-label small">Card Number</label>
              <input type="text" name="cardNumber" placeholder="Currently ${method.maskedNumber} — leave blank to keep it"
                     maxlength="19" class="form-control form-control-glass">
              <small class="text-soft">Only fill this in if you want to replace the saved card number.</small>
            </div>
            <div class="mb-3">
              <label class="form-label small">Expiry (MM/YY)</label>
              <input type="text" name="expiry" value="${method.expiry}" maxlength="5" class="form-control form-control-glass" required>
            </div>
          </c:if>

          <div class="d-flex gap-2 mt-2">
            <button type="submit" class="btn btn-gradient flex-grow-1 py-2">Save Changes</button>
            <a href="${pageContext.request.contextPath}/renter/payment-methods" class="btn btn-outline-glass py-2">Cancel</a>
          </div>
        </form>
      </div>
    </c:if>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
