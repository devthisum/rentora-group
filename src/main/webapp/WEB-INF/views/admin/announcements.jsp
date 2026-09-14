<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem;">
    <h2 class="fw-bold mb-4" data-aos="fade-up">Manage Announcements</h2>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <div class="glass-card p-4 mb-4" data-aos="fade-up">
      <h5 class="fw-bold mb-3">Post a New Announcement</h5>
      <form method="post" action="${pageContext.request.contextPath}/admin/announcements" class="row g-3">
        <input type="hidden" name="action" value="create">
        <div class="col-md-6">
          <label class="form-label small">Title</label>
          <input type="text" name="title" class="form-control form-control-glass" required>
        </div>
        <div class="col-md-3">
          <label class="form-label small">Priority</label>
          <select name="priority" class="form-select form-select-glass">
            <option value="LOW">Low</option>
            <option value="NORMAL" selected>Normal</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label small">Category (optional)</label>
          <input type="text" name="category" placeholder="e.g. Maintenance" class="form-control form-control-glass">
        </div>
        <div class="col-md-6">
          <label class="form-label small">Expiry Date (optional)</label>
          <input type="date" name="expiryDate" class="form-control form-control-glass">
        </div>
        <div class="col-12">
          <label class="form-label small">Message</label>
          <textarea name="message" rows="3" class="form-control form-control-glass" required></textarea>
        </div>
        <div class="col-12">
          <button type="submit" class="btn btn-gradient">Post Announcement</button>
        </div>
      </form>
    </div>

    <div class="glass-card p-3" data-aos="fade-up" data-aos-delay="100">
      <table class="table table-glass align-middle mb-0">
        <thead><tr><th>Title</th><th>Priority</th><th>Category</th><th>Expires</th><th>Posted</th><th>Action</th></tr></thead>
        <tbody>
          <c:forEach var="a" items="${announcements}">
            <tr>
              <td>${a.title}</td>
              <td><span class="badge badge-${a.priority == 'URGENT' || a.priority == 'HIGH' ? 'rejected' : 'active'} text-uppercase">${a.priority}</span></td>
              <td>${a.category}</td>
              <td>${not empty a.expiryDate ? a.expiryDate : 'Never'}</td>
              <td>${a.createdAt}</td>
              <td>
                <form method="post" action="${pageContext.request.contextPath}/admin/announcements" class="d-inline"
                      onsubmit="return confirm('Delete this announcement?');">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="announcementId" value="${a.announcementId}">
                  <button class="btn btn-sm btn-outline-glass"><i class="fa-solid fa-trash"></i></button>
                </form>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty announcements}">
            <tr><td colspan="6" class="text-secondary text-center py-4">No announcements posted yet.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
