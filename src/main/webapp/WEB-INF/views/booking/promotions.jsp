<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />
  <jsp:include page="/WEB-INF/views/common/admin-sidebar.jsp" />

  <section class="container admin-content" style="padding-top: 7rem; padding-bottom: 4rem;">
    <h2 class="fw-bold mb-4" data-aos="fade-up">Vehicle Promotions</h2>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
    <c:if test="${not empty param.error}"><div class="alert alert-danger">${param.error}</div></c:if>

    <!-- ============ CREATE ============ -->
    <div class="glass-card p-4 mb-4" data-aos="fade-up">
      <h5 class="fw-bold mb-3">Create a New Promotion</h5>
      <p class="text-soft small mb-3">Spot a vehicle that's been sitting idle? Give it a price-drop deal — it shows up for customers immediately.</p>
      <form method="post" action="${pageContext.request.contextPath}/booking/promotions" class="row g-3">
        <input type="hidden" name="action" value="create">

        <div class="col-md-6">
          <label class="form-label small">Vehicle</label>
          <select name="vehicleId" class="form-select form-select-glass" required>
            <option value="" disabled selected>Choose a vehicle...</option>
            <c:forEach var="v" items="${vehicles}">
              <option value="${v.vehicleId}">${v.brand} ${v.model} (${v.vehicleNumber}) — Rs. ${v.pricePerDay}/day</option>
            </c:forEach>
          </select>
        </div>
        <div class="col-md-6">
          <label class="form-label small">Promotion Title</label>
          <input type="text" name="title" class="form-control form-control-glass" placeholder="e.g. Independence Day Sale" required>
        </div>

        <div class="col-md-4">
          <label class="form-label small">Discount Type</label>
          <select name="discountType" class="form-select form-select-glass" required>
            <option value="PERCENTAGE">Percentage (%)</option>
            <option value="FIXED_AMOUNT">Fixed Amount (Rs.)</option>
          </select>
        </div>
        <div class="col-md-4">
          <label class="form-label small">Discount Value</label>
          <input type="number" step="0.01" min="0.01" name="discountValue" class="form-control form-control-glass" placeholder="e.g. 20" required>
        </div>
        <div class="col-md-4">
          <label class="form-label small">&nbsp;</label>
          <div class="form-text mt-2">Percentage max 90%. Fixed amount must be less than the vehicle's daily price.</div>
        </div>

        <div class="col-md-6">
          <label class="form-label small">Start Date</label>
          <input type="date" name="startDate" class="form-control form-control-glass" required>
        </div>
        <div class="col-md-6">
          <label class="form-label small">End Date</label>
          <input type="date" name="endDate" class="form-control form-control-glass" required>
        </div>

        <div class="col-12">
          <label class="form-label small">Description (optional)</label>
          <textarea name="description" rows="2" class="form-control form-control-glass" placeholder="Shown to customers alongside the deal."></textarea>
        </div>

        <div class="col-12">
          <button type="submit" class="btn btn-gradient"><i class="fa-solid fa-tag me-1"></i>Create Promotion</button>
        </div>
      </form>
    </div>

    <!-- ============ LIST / MANAGE ============ -->
    <div class="glass-card p-3" data-aos="fade-up" data-aos-delay="100">
      <table class="table table-glass align-middle mb-0">
        <thead>
          <tr>
            <th>Vehicle</th><th>Title</th><th>Discount</th><th>Dates</th><th>Status</th><th>Created By</th><th>Action</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="p" items="${promotions}">
            <tr>
              <td>${p.vehicleBrand} ${p.vehicleModel}<br><span class="text-soft small">${p.vehicleNumber}</span></td>
              <td>${p.title}</td>
              <td>
                <c:choose>
                  <c:when test="${p.discountType == 'PERCENTAGE'}">${p.discountValue}% off</c:when>
                  <c:otherwise>Rs. ${p.discountValue} off</c:otherwise>
                </c:choose>
              </td>
              <td>${p.startDate} &rarr; ${p.endDate}</td>
              <td>
                <c:choose>
                  <c:when test="${p.currentlyActive}"><span class="badge badge-active text-uppercase">Live</span></c:when>
                  <c:when test="${p.status == 'INACTIVE'}"><span class="badge badge-cancelled text-uppercase">Inactive</span></c:when>
                  <c:otherwise><span class="badge badge-pending text-uppercase">Scheduled/Expired</span></c:otherwise>
                </c:choose>
              </td>
              <td>${p.createdByName}</td>
              <td class="d-flex gap-1">
                <button type="button" class="btn btn-sm btn-outline-glass" data-bs-toggle="modal" data-bs-target="#editPromo${p.promotionId}">
                  <i class="fa-solid fa-pen"></i>
                </button>
                <form method="post" action="${pageContext.request.contextPath}/booking/promotions" class="d-inline"
                      onsubmit="return confirm('Delete this promotion? This can\'t be undone.');">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="promotionId" value="${p.promotionId}">
                  <button class="btn btn-sm btn-outline-glass text-danger"><i class="fa-solid fa-trash"></i></button>
                </form>
              </td>
            </tr>

            <!-- Edit modal for this promotion -->
            <div class="modal fade" id="editPromo${p.promotionId}" tabindex="-1">
              <div class="modal-dialog">
                <div class="modal-content">
                  <form method="post" action="${pageContext.request.contextPath}/booking/promotions">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="promotionId" value="${p.promotionId}">
                    <div class="modal-header">
                      <h5 class="modal-title">Edit Promotion — ${p.vehicleBrand} ${p.vehicleModel}</h5>
                      <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                      <div class="mb-3">
                        <label class="form-label small">Title</label>
                        <input type="text" name="title" class="form-control form-control-glass" value="${p.title}" required>
                      </div>
                      <div class="row g-2 mb-3">
                        <div class="col-6">
                          <label class="form-label small">Discount Type</label>
                          <select name="discountType" class="form-select form-select-glass">
                            <option value="PERCENTAGE" ${p.discountType == 'PERCENTAGE' ? 'selected' : ''}>Percentage (%)</option>
                            <option value="FIXED_AMOUNT" ${p.discountType == 'FIXED_AMOUNT' ? 'selected' : ''}>Fixed Amount (Rs.)</option>
                          </select>
                        </div>
                        <div class="col-6">
                          <label class="form-label small">Discount Value</label>
                          <input type="number" step="0.01" min="0.01" name="discountValue" class="form-control form-control-glass" value="${p.discountValue}" required>
                        </div>
                      </div>
                      <div class="row g-2 mb-3">
                        <div class="col-6">
                          <label class="form-label small">Start Date</label>
                          <input type="date" name="startDate" class="form-control form-control-glass" value="${p.startDate}" required>
                        </div>
                        <div class="col-6">
                          <label class="form-label small">End Date</label>
                          <input type="date" name="endDate" class="form-control form-control-glass" value="${p.endDate}" required>
                        </div>
                      </div>
                      <div class="mb-3">
                        <label class="form-label small">Description</label>
                        <textarea name="description" rows="2" class="form-control form-control-glass">${p.description}</textarea>
                      </div>
                      <div class="mb-1">
                        <label class="form-label small">Status</label>
                        <select name="status" class="form-select form-select-glass">
                          <option value="ACTIVE" ${p.status == 'ACTIVE' ? 'selected' : ''}>Active</option>
                          <option value="INACTIVE" ${p.status == 'INACTIVE' ? 'selected' : ''}>Inactive (turn off without deleting)</option>
                        </select>
                      </div>
                    </div>
                    <div class="modal-footer">
                      <button type="submit" class="btn btn-gradient">Save Changes</button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
          </c:forEach>
          <c:if test="${empty promotions}">
            <tr><td colspan="7" class="text-secondary text-center py-4">No promotions created yet.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
