<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem;">
    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <c:if test="${not empty thread}">
      <div class="glass-card p-4 mb-4">
        <h4 class="fw-bold mb-1">${thread.vehicleBrand} ${thread.vehicleModel}</h4>
        <p class="text-secondary small mb-0">
          Conversation between <strong>${thread.renterName}</strong> (renter) and Rentora Support
        </p>
      </div>

      <div class="glass-card p-4 mb-4" style="max-height:500px; overflow-y:auto;">
        <c:forEach var="m" items="${messages}">
          <div class="mb-3 p-3" style="border-radius:12px; background:${m.senderId == sessionScope.user.userId ? 'rgba(10,102,194,0.08)' : 'var(--bg2)'};">
            <div class="d-flex justify-content-between">
              <strong style="font-family:'Manrope',sans-serif; font-size:0.9rem;">${m.senderName}</strong>
              <span class="text-secondary" style="font-size:0.78rem;">${m.createdAt}</span>
            </div>
            <p class="mb-0 mt-1">${m.message}</p>
          </div>
        </c:forEach>
        <c:if test="${empty messages}">
          <p class="text-secondary mb-0">No messages yet.</p>
        </c:if>
      </div>

      <div class="glass-card p-4">
        <form method="post" action="${pageContext.request.contextPath}/inquiry/reply">
          <input type="hidden" name="threadId" value="${thread.threadId}">
          <div class="mb-3">
            <textarea name="message" rows="3" class="form-control form-control-glass" placeholder="Type your reply..." required></textarea>
          </div>
          <button type="submit" class="btn btn-gradient">Send Reply</button>
        </form>
      </div>
    </c:if>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
