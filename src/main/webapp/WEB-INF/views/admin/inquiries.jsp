<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem;">
    <h2 class="fw-bold mb-1" data-aos="fade-up">Customer Inquiries</h2>
    <p class="text-soft mb-4">Every conversation renters have started about a vehicle.</p>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <div class="glass-card p-3" data-aos="fade-up">
      <table class="table table-glass align-middle mb-0">
        <thead>
          <tr><th>Renter</th><th>Vehicle</th><th>Status</th><th></th></tr>
        </thead>
        <tbody>
          <c:forEach var="t" items="${threads}">
            <tr>
              <td>${t.renterName}</td>
              <td>${t.vehicleBrand} ${t.vehicleModel}</td>
              <td><span class="badge badge-${t.status.toLowerCase()} text-uppercase">${t.status}</span></td>
              <td>
                <a href="${pageContext.request.contextPath}/inquiry/thread?id=${t.threadId}" class="btn btn-sm btn-outline-glass">Open</a>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty threads}">
            <tr><td colspan="4" class="text-secondary text-center py-4">No inquiries yet.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
