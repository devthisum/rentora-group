<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem;">
    <h2 class="fw-bold mb-4"><i class="fa-solid fa-bullhorn me-2" style="color: var(--accent);"></i>Announcements</h2>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <div class="row g-3">
      <c:forEach var="a" items="${announcements}">
        <div class="col-12">
          <div class="glass-card p-4"
               style="border-left: 4px solid ${a.priority == 'URGENT' ? '#EF4444' : a.priority == 'HIGH' ? '#F59E0B' : 'var(--accent)'};">
            <div class="d-flex justify-content-between align-items-start">
              <h5 class="fw-bold mb-1">${a.title}</h5>
              <span class="badge badge-${a.priority == 'URGENT' || a.priority == 'HIGH' ? 'rejected' : 'active'} text-uppercase">${a.priority}</span>
            </div>
            <p class="text-secondary mb-2">${a.message}</p>
            <div class="text-secondary small">
              Posted by ${a.postedByName} &bull; ${a.createdAt}
              <c:if test="${not empty a.category}"> &bull; ${a.category}</c:if>
              <c:if test="${not empty a.expiryDate}"> &bull; Expires ${a.expiryDate}</c:if>
            </div>
          </div>
        </div>
      </c:forEach>
      <c:if test="${empty announcements}">
        <div class="col-12">
          <div class="glass-card p-5 text-center">
            <p class="text-secondary mb-0">No announcements right now. Check back later.</p>
          </div>
        </div>
      </c:if>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
