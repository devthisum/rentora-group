<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem;">
    <h2 class="fw-bold mb-4" data-aos="fade-up">My Conversations</h2>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <div class="glass-card p-3" data-aos="fade-up" data-aos-delay="100">
      <c:forEach var="t" items="${threads}" varStatus="loop">
        <a href="${pageContext.request.contextPath}/inquiry/thread?id=${t.threadId}"
           class="d-flex justify-content-between align-items-center p-3 text-decoration-none"
           style="border-bottom:1px solid var(--border-soft, #EEF1F6); color:inherit;"
           data-aos="fade-up" data-aos-delay="${loop.index % 5 * 60}">
          <div>
            <strong style="font-family:'Manrope',sans-serif;">${t.vehicleBrand} ${t.vehicleModel}</strong>
            <div class="text-secondary small">with Rentora Support</div>
          </div>
          <c:if test="${t.hasUnread}"><span class="badge badge-pending">NEW</span></c:if>
        </a>
      </c:forEach>
      <c:if test="${empty threads}">
        <p class="text-secondary text-center py-4 mb-0">No conversations yet. Ask a question from any vehicle's details page to get started.</p>
      </c:if>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
