<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem;">
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
      <h2 class="fw-bold mb-0" data-aos="fade-up">Shop Vehicle Stock</h2>
      <a href="${pageContext.request.contextPath}/admin/vehicles/add" class="btn btn-gradient"><i class="fa-solid fa-plus me-1"></i>Add Vehicle</a>
    </div>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
    <c:if test="${not empty sessionScope.successMessage}">
      <div class="alert alert-success">${sessionScope.successMessage}</div>
      <c:remove var="successMessage" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
      <div class="alert alert-danger">${sessionScope.errorMessage}</div>
      <c:remove var="errorMessage" scope="session" />
    </c:if>

    <div class="vehicle-view-tabs mb-3" data-aos="fade-up">
      <a href="${pageContext.request.contextPath}/admin/vehicles?view=active" class="vehicle-view-tab ${view == 'active' ? 'active' : ''}">Active <span class="count">${activeCount}</span></a>
      <a href="${pageContext.request.contextPath}/admin/vehicles?view=archived" class="vehicle-view-tab ${view == 'archived' ? 'active' : ''}">Archived <span class="count">${archivedCount}</span></a>
      <a href="${pageContext.request.contextPath}/admin/vehicles?view=all" class="vehicle-view-tab ${view == 'all' ? 'active' : ''}">All</a>
    </div>

    <div class="glass-card p-3" data-aos="fade-up">
      <table class="table table-glass align-middle mb-0">
        <thead>
          <tr>
            <th>Vehicle</th><th>Number</th><th>Category</th><th>Price/day</th><th>Status</th><th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="v" items="${vehicles}">
            <tr>
              <td>
                ${v.brand} ${v.model} (${v.year})
                <c:if test="${v.archived}"><span class="badge badge-archived text-uppercase ms-1">Archived</span></c:if>
              </td>
              <td>${v.vehicleNumber}</td>
              <td>${v.categoryName}</td>
              <td>Rs. ${v.pricePerDay}</td>
              <td>
                <c:if test="${v.archived}"><span class="text-soft small">Hidden from customers</span></c:if>
                <c:if test="${!v.archived}">
                <form method="post" action="${pageContext.request.contextPath}/admin/vehicles/status" class="status-inline-form">
                  <input type="hidden" name="vehicleId" value="${v.vehicleId}">
                  <select name="status" class="form-select form-select-sm status-inline-select status-${v.displayStatus.toLowerCase()}" onchange="handleStatusChange(this)">
                    <option value="AVAILABLE" ${v.displayStatus == 'AVAILABLE' ? 'selected' : ''}>Available</option>
                    <option value="CHECKING" ${v.displayStatus == 'CHECKING' ? 'selected' : ''}>Checking</option>
                    <option value="MAINTENANCE" ${v.displayStatus == 'MAINTENANCE' ? 'selected' : ''}>Maintenance</option>
                    <c:if test="${v.displayStatus == 'BOOKED'}">
                      <option value="BOOKED" selected disabled>Booked (today)</option>
                    </c:if>
                  </select>
                </form>
                </c:if>
              </td>
              <td>
                <a href="${pageContext.request.contextPath}/admin/vehicles/edit?id=${v.vehicleId}" class="btn btn-sm btn-outline-glass">Edit</a>
                <c:choose>
                  <c:when test="${v.archived}">
                    <form method="post" action="${pageContext.request.contextPath}/admin/vehicles/archive" class="d-inline"
                          onsubmit="return confirm('Restore this vehicle? Customers will be able to see and book it again.');">
                      <input type="hidden" name="action" value="restore">
                      <input type="hidden" name="vehicleId" value="${v.vehicleId}">
                      <button class="btn btn-sm btn-outline-glass"><i class="fa-solid fa-rotate-left me-1"></i>Restore</button>
                    </form>
                  </c:when>
                  <c:otherwise>
                    <form method="post" action="${pageContext.request.contextPath}/admin/vehicles/archive" class="d-inline"
                          onsubmit="return confirm('Archive this vehicle? It will be hidden from customers but all its booking history is kept. You can restore it later.');">
                      <input type="hidden" name="action" value="archive">
                      <input type="hidden" name="vehicleId" value="${v.vehicleId}">
                      <button class="btn btn-sm btn-outline-glass"><i class="fa-solid fa-box-archive me-1"></i>Archive</button>
                    </form>
                  </c:otherwise>
                </c:choose>
                <form method="post" action="${pageContext.request.contextPath}/admin/vehicles/delete" class="d-inline"
                      onsubmit="return confirm('Permanently delete this vehicle? This only works for vehicles with no booking history — otherwise use Archive.');">
                  <input type="hidden" name="vehicleId" value="${v.vehicleId}">
                  <button class="btn btn-sm btn-outline-glass text-danger">Delete</button>
                </form>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty vehicles}">
            <tr><td colspan="6" class="text-secondary text-center py-4">
              <c:choose>
                <c:when test="${view == 'archived'}">No archived vehicles.</c:when>
                <c:otherwise>No vehicles here yet. Add your first one!</c:otherwise>
              </c:choose>
            </td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </section>

  <script>
    // Setting a vehicle to Checking/Maintenance opens a real maintenance
    // record (visible on the Maintenance dashboard) with no estimate yet —
    // it's maintenance staff who set the actual estimated-days timeline,
    // via "Complete Inspection" or "Edit Duration / Cost" on that record,
    // not whoever flips this status dropdown.
    document.addEventListener('DOMContentLoaded', function () {
      document.querySelectorAll('.status-inline-select').forEach(function (sel) {
        sel.dataset.original = sel.value; // remember the pre-change value so we can revert on cancel
      });
    });

    function handleStatusChange(select) {
      const form = select.form;
      const original = select.dataset.original;

      if (select.value === 'MAINTENANCE') {
        if (!confirm('Send this vehicle to Maintenance? It will appear on the Maintenance dashboard for a technician to set the repair estimate.')) {
          select.value = original; return;
        }
      } else if (select.value === 'CHECKING') {
        if (!confirm('Send this vehicle to Checking? It will appear on the Maintenance dashboard for inspection.')) {
          select.value = original; return;
        }
      } else if (select.value === 'AVAILABLE' && (original === 'CHECKING' || original === 'MAINTENANCE')) {
        if (!confirm('Mark this vehicle Available again? Any open maintenance record for it will be resolved.')) {
          select.value = original; return;
        }
      }
      select.dataset.original = select.value;
      form.submit();
    }
  </script>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
