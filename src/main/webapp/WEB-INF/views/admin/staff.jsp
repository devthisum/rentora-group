<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem;">
    <h2 class="fw-bold mb-1" data-aos="fade-up">Staff</h2>
    <p class="text-soft mb-4">Create logins for Maintenance staff (vehicle inspection &amp; repair) or Booking staff (front-desk pickup/return).</p>

    <c:if test="${not empty sessionScope.successMessage}">
      <div class="alert alert-success">${sessionScope.successMessage}</div>
      <c:remove var="successMessage" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
      <div class="alert alert-danger">${sessionScope.errorMessage}</div>
      <c:remove var="errorMessage" scope="session" />
    </c:if>
    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <div class="row g-4">
      <div class="col-lg-5">
        <div class="glass-card p-4" data-aos="fade-up">
          <h5 class="fw-bold mb-3">Add Staff Account</h5>
          <form method="post" action="${pageContext.request.contextPath}/admin/staff">
            <input type="hidden" name="formAction" value="create">
            <div class="mb-3">
              <label class="form-label small">Staff Role</label>
              <select name="role" class="form-select form-select-glass" required>
                <option value="" selected disabled>-- Select a role --</option>
                <option value="MAINTENANCE">Maintenance — vehicle inspection &amp; repair</option>
                <option value="BOOKING">Booking Staff — front-desk pickup/return</option>
              </select>
            </div>
            <div class="mb-3">
              <label class="form-label small">Full Name</label>
              <input type="text" name="fullName" class="form-control form-control-glass" required>
            </div>
            <div class="mb-3">
              <label class="form-label small">Email</label>
              <input type="email" name="email" class="form-control form-control-glass" required>
            </div>
            <div class="mb-3">
              <label class="form-label small">Phone</label>
              <input type="text" name="phone" placeholder="0771234567" class="form-control form-control-glass" required>
            </div>
            <div class="mb-3">
              <label class="form-label small">Temporary Password</label>
              <input type="text" name="password" class="form-control form-control-glass" required>
              <small class="text-soft">Share this with them directly — they log in at /login like anyone else.</small>
            </div>
            <button type="submit" class="btn btn-gradient w-100 py-2">Create Staff Login</button>
          </form>
        </div>
      </div>

      <div class="col-lg-7">
        <h6 class="fw-bold mb-2" data-aos="fade-up">Maintenance Staff</h6>
        <div class="glass-card p-3 mb-4" data-aos="fade-up">
          <table class="table table-glass align-middle mb-0">
            <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th></th></tr></thead>
            <tbody>
              <c:forEach var="s" items="${maintenanceStaffList}">
                <tr>
                  <td>${s.fullName}</td><td>${s.email}</td><td>${s.phone}</td>
                  <td class="text-end">
                    <form method="post" action="${pageContext.request.contextPath}/admin/staff"
                          onsubmit="return confirm('Delete ${s.fullName}\'s account? This can\'t be undone.');">
                      <input type="hidden" name="formAction" value="delete">
                      <input type="hidden" name="userId" value="${s.userId}">
                      <button type="submit" class="btn btn-sm btn-outline-glass text-danger"><i class="fa-solid fa-trash"></i></button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
              <c:if test="${empty maintenanceStaffList}">
                <tr><td colspan="4" class="text-secondary text-center py-4">No maintenance staff accounts yet.</td></tr>
              </c:if>
            </tbody>
          </table>
        </div>

        <h6 class="fw-bold mb-2" data-aos="fade-up">Booking Staff</h6>
        <div class="glass-card p-3" data-aos="fade-up">
          <table class="table table-glass align-middle mb-0">
            <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th></th></tr></thead>
            <tbody>
              <c:forEach var="s" items="${bookingStaffList}">
                <tr>
                  <td>${s.fullName}</td><td>${s.email}</td><td>${s.phone}</td>
                  <td class="text-end">
                    <form method="post" action="${pageContext.request.contextPath}/admin/staff"
                          onsubmit="return confirm('Delete ${s.fullName}\'s account? This can\'t be undone.');">
                      <input type="hidden" name="formAction" value="delete">
                      <input type="hidden" name="userId" value="${s.userId}">
                      <button type="submit" class="btn btn-sm btn-outline-glass text-danger"><i class="fa-solid fa-trash"></i></button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
              <c:if test="${empty bookingStaffList}">
                <tr><td colspan="4" class="text-secondary text-center py-4">No booking staff accounts yet.</td></tr>
              </c:if>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
