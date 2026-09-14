<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem;">
    <h2 class="fw-bold mb-1" data-aos="fade-up">Maintenance</h2>
    <p class="text-soft mb-4">Vehicles due back, being checked, or under repair.</p>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
    <c:if test="${not empty sessionScope.successMessage}">
      <div class="alert alert-success">${sessionScope.successMessage}</div>
      <c:remove var="successMessage" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
      <div class="alert alert-danger">${sessionScope.errorMessage}</div>
      <c:remove var="errorMessage" scope="session" />
    </c:if>

    <!-- ============ CURRENTLY RENTED OUT — "Return in ___ days" ============ -->
    <h5 class="fw-bold mb-3" data-aos="fade-up">Currently Rented Out</h5>
    <div class="glass-card p-3 mb-5" data-aos="fade-up">
      <table class="table table-glass align-middle mb-0">
        <thead>
          <tr><th>Vehicle</th><th>Renter</th><th>Due Back</th><th>Status</th><th>Action</th></tr>
        </thead>
        <tbody>
          <c:forEach var="b" items="${upcomingReturns}">
            <tr>
              <td>${b.vehicleBrand} ${b.vehicleModel} <span class="text-soft">(${b.vehicleNumber})</span></td>
              <td>${b.renterName}</td>
              <td>
                ${b.endDate}
                <c:choose>
                  <c:when test="${b.daysUntilReturn > 0}">
                    <span class="badge badge-available ms-1">Return in ${b.daysUntilReturn} day(s)</span>
                  </c:when>
                  <c:otherwise>
                    <span class="badge badge-maintenance ms-1">Overdue</span>
                  </c:otherwise>
                </c:choose>
              </td>
              <td><span class="badge badge-booked text-uppercase">${b.status}</span></td>
              <td>
                <c:if test="${canEdit}">
                  <form method="post" action="${pageContext.request.contextPath}/maintenance/dashboard"
                        onsubmit="return confirm('Mark this vehicle as returned and start the maintenance check?');">
                    <input type="hidden" name="action" value="markReturned">
                    <input type="hidden" name="bookingId" value="${b.bookingId}">
                    <button class="btn btn-sm btn-gradient">Return Vehicle</button>
                  </form>
                </c:if>
                <c:if test="${!canEdit}"><span class="text-soft small">—</span></c:if>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty upcomingReturns}">
            <tr><td colspan="5" class="text-secondary text-center py-4">No vehicles currently out on rent.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>

    <!-- ============ CHECKING / UNDER MAINTENANCE / AWAITING CUSTOMER PAYMENT ============ -->
    <h5 class="fw-bold mb-3" data-aos="fade-up">Checking &amp; Under Maintenance</h5>
    <div class="row g-4">
      <c:if test="${empty activeMaintenance}">
        <div class="col-12">
          <div class="glass-card p-5 text-center">
            <p class="text-soft mb-0">Nothing to check right now — every vehicle in stock is either available or booked.</p>
          </div>
        </div>
      </c:if>

      <c:forEach var="m" items="${activeMaintenance}">
        <div class="col-md-6 col-lg-4">
          <div class="glass-card p-4 h-100">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <h6 class="fw-bold mb-0">${m.vehicleBrand} ${m.vehicleModel}</h6>
              <span class="badge badge-maintenance text-uppercase">${m.stage}</span>
            </div>
            <p class="text-soft small mb-2">Vehicle No: ${m.vehicleNumber}</p>
            <c:if test="${not empty m.renterName}"><p class="text-soft small mb-3">Renter: ${m.renterName} (${m.renterPhone})</p></c:if>

            <c:choose>
              <%-- ---------------- STAGE: CHECKING ---------------- --%>
              <c:when test="${m.stage == 'CHECKING'}">
                <c:if test="${canEdit}">
                  <p class="small mb-3">Return the vehicle checked in above, then record what the inspection found.</p>
                  <button class="btn btn-sm btn-outline-glass w-100 mb-2" type="button"
                          data-bs-toggle="collapse" data-bs-target="#check-${m.maintenanceId}">
                    Complete Inspection
                  </button>
                  <div class="collapse" id="check-${m.maintenanceId}">
                    <form method="post" action="${pageContext.request.contextPath}/maintenance/dashboard" class="mt-2">
                      <input type="hidden" name="action" value="completeCheck">
                      <input type="hidden" name="maintenanceId" value="${m.maintenanceId}">

                      <div class="mb-2">
                        <label class="form-label small">Notes</label>
                        <textarea name="notes" rows="2" class="form-control form-control-glass" placeholder="What did you check?"></textarea>
                      </div>

                      <div class="form-check mb-2">
                        <input class="form-check-input" type="radio" name="problemFound" value="false" id="ok-${m.maintenanceId}" checked
                               onchange="document.getElementById('problem-fields-${m.maintenanceId}').style.display='none';">
                        <label class="form-check-label small" for="ok-${m.maintenanceId}">No problem found — mark available</label>
                      </div>
                      <div class="form-check mb-2">
                        <input class="form-check-input" type="radio" name="problemFound" value="true" id="bad-${m.maintenanceId}"
                               onchange="document.getElementById('problem-fields-${m.maintenanceId}').style.display='block';">
                        <label class="form-check-label small" for="bad-${m.maintenanceId}">Problem found — send to maintenance</label>
                      </div>

                      <div id="problem-fields-${m.maintenanceId}" style="display:none;" class="border-top pt-2 mt-2">
                        <div class="mb-2">
                          <label class="form-label small">Estimated days under maintenance</label>
                          <input type="number" name="estimatedDays" min="1" class="form-control form-control-glass">
                        </div>
                        <div class="mb-2">
                          <label class="form-label small">Repair cost (Rs., optional)</label>
                          <input type="number" step="0.01" name="repairCost" class="form-control form-control-glass">
                        </div>
                        <div class="form-check mb-2">
                          <input class="form-check-input" type="checkbox" name="customerAtFault" value="true" id="fault-${m.maintenanceId}">
                          <label class="form-check-label small" for="fault-${m.maintenanceId}">
                            Customer caused the damage — bill cost + 20% surcharge, due within 24 hrs
                          </label>
                        </div>
                      </div>

                      <button type="submit" class="btn btn-sm btn-gradient w-100 mt-2">Submit Inspection</button>
                    </form>
                  </div>
                </c:if>
                <c:if test="${!canEdit}">
                  <p class="small text-soft mb-0">Awaiting inspection by maintenance staff.</p>
                </c:if>
              </c:when>

              <%-- ---------------- STAGE: UNDER_MAINTENANCE ---------------- --%>
              <c:when test="${m.stage == 'UNDER_MAINTENANCE'}">
                <p class="small mb-1">Under maintenance for <strong>${m.estimatedDays}</strong> day(s).</p>
                <c:if test="${not empty m.notes}"><p class="small text-soft mb-3">${m.notes}</p></c:if>
                <c:if test="${canEdit}">
                  <button class="btn btn-sm btn-outline-glass w-100 mb-2" type="button"
                          data-bs-toggle="collapse" data-bs-target="#edit-${m.maintenanceId}">
                    <i class="fa-solid fa-pen me-1"></i>Edit Duration / Cost
                  </button>
                  <div class="collapse mb-2" id="edit-${m.maintenanceId}">
                    <form method="post" action="${pageContext.request.contextPath}/maintenance/dashboard" class="border-top pt-2">
                      <input type="hidden" name="action" value="updateDetails">
                      <input type="hidden" name="maintenanceId" value="${m.maintenanceId}">
                      <div class="mb-2">
                        <label class="form-label small">Estimated days under maintenance</label>
                        <input type="number" name="estimatedDays" min="1" value="${m.estimatedDays}" class="form-control form-control-glass">
                        <small class="text-soft">Under 2 days shrinks the calendar's blocked window; over 2 days extends it.</small>
                      </div>
                      <div class="mb-2">
                        <label class="form-label small">Notes</label>
                        <textarea name="notes" rows="2" class="form-control form-control-glass">${m.notes}</textarea>
                      </div>
                      <div class="mb-2">
                        <label class="form-label small">Repair cost (Rs., optional)</label>
                        <input type="number" step="0.01" name="repairCost" value="${m.repairCost}" class="form-control form-control-glass">
                      </div>
                      <div class="form-check mb-2">
                        <input class="form-check-input" type="checkbox" name="customerAtFault" value="true"
                               id="edit-fault-${m.maintenanceId}" ${m.customerAtFault ? 'checked' : ''}>
                        <label class="form-check-label small" for="edit-fault-${m.maintenanceId}">
                          Customer caused the damage — bill cost + 20% surcharge, due within 24 hrs
                        </label>
                      </div>
                      <button type="submit" class="btn btn-sm btn-gradient w-100">Save Changes</button>
                    </form>
                  </div>
                  <form method="post" action="${pageContext.request.contextPath}/maintenance/dashboard">
                    <input type="hidden" name="action" value="markResolved">
                    <input type="hidden" name="maintenanceId" value="${m.maintenanceId}">
                    <button class="btn btn-sm btn-gradient w-100">Repairs Done — Mark Available</button>
                  </form>
                </c:if>
              </c:when>

              <%-- ---------------- STAGE: AWAITING_CUSTOMER_PAYMENT ---------------- --%>
              <c:when test="${m.stage == 'AWAITING_CUSTOMER_PAYMENT'}">
                <p class="small mb-1">Under maintenance for <strong>${m.estimatedDays}</strong> day(s). Customer at fault.</p>
                <p class="small mb-1">Repair cost: Rs. ${m.repairCost} + 20% surcharge (Rs. ${m.extraChargeAmount})</p>
                <p class="small fw-bold mb-2">Total due: Rs. ${m.totalCharge}</p>
                <c:choose>
                  <c:when test="${m.chargePaid}">
                    <span class="badge badge-available mb-2">Paid</span>
                  </c:when>
                  <c:when test="${m.paymentOverdue}">
                    <span class="badge badge-maintenance mb-2">Payment overdue (24hr window passed)</span>
                  </c:when>
                  <c:otherwise>
                    <span class="badge badge-booked mb-2">Awaiting payment (due within 24 hrs)</span>
                  </c:otherwise>
                </c:choose>
                <c:if test="${!m.chargePaid && canEdit}">
                  <button class="btn btn-sm btn-outline-glass w-100 mb-2 mt-2" type="button"
                          data-bs-toggle="collapse" data-bs-target="#edit-${m.maintenanceId}">
                    <i class="fa-solid fa-pen me-1"></i>Edit Duration / Cost
                  </button>
                  <div class="collapse mb-2" id="edit-${m.maintenanceId}">
                    <form method="post" action="${pageContext.request.contextPath}/maintenance/dashboard" class="border-top pt-2">
                      <input type="hidden" name="action" value="updateDetails">
                      <input type="hidden" name="maintenanceId" value="${m.maintenanceId}">
                      <div class="mb-2">
                        <label class="form-label small">Estimated days under maintenance</label>
                        <input type="number" name="estimatedDays" min="1" value="${m.estimatedDays}" class="form-control form-control-glass">
                      </div>
                      <div class="mb-2">
                        <label class="form-label small">Notes</label>
                        <textarea name="notes" rows="2" class="form-control form-control-glass">${m.notes}</textarea>
                      </div>
                      <div class="mb-2">
                        <label class="form-label small">Repair cost (Rs.)</label>
                        <input type="number" step="0.01" name="repairCost" value="${m.repairCost}" class="form-control form-control-glass">
                      </div>
                      <div class="form-check mb-2">
                        <input class="form-check-input" type="checkbox" name="customerAtFault" value="true"
                               id="edit-fault-${m.maintenanceId}" checked>
                        <label class="form-check-label small" for="edit-fault-${m.maintenanceId}">Customer at fault (20% surcharge)</label>
                      </div>
                      <button type="submit" class="btn btn-sm btn-gradient w-100">Save Changes</button>
                    </form>
                  </div>
                  <form method="post" action="${pageContext.request.contextPath}/maintenance/dashboard" class="mt-2"
                        onsubmit="return confirm('Confirm the customer has paid this charge?');">
                    <input type="hidden" name="action" value="markResolved">
                    <input type="hidden" name="maintenanceId" value="${m.maintenanceId}">
                    <button class="btn btn-sm btn-outline-glass w-100">Confirm Paid &amp; Mark Available</button>
                  </form>
                </c:if>
              </c:when>
            </c:choose>
          </div>
        </div>
      </c:forEach>
    </div>

    <!-- ============ ALL VEHICLES — UPDATE STATUS ============
         Status-only (Available/Checking/Maintenance). Both admin and
         maintenance staff can use this — vehicle *details* editing is
         admin-only, from /admin/vehicles. -->
    <h5 class="fw-bold mb-3 mt-5" data-aos="fade-up">All Vehicles</h5>
    <div class="glass-card p-3" data-aos="fade-up">
      <table class="table table-glass align-middle mb-0">
        <thead>
          <tr><th>Vehicle</th><th>Number</th><th>Category</th><th>Status</th></tr>
        </thead>
        <tbody>
          <c:forEach var="v" items="${allVehicles}">
            <tr>
              <td>${v.brand} ${v.model} (${v.year})</td>
              <td>${v.vehicleNumber}</td>
              <td>${v.categoryName}</td>
              <td>
                <c:choose>
                  <c:when test="${canManageStatus}">
                    <form method="post" action="${pageContext.request.contextPath}/maintenance/vehicles/status" class="status-inline-form">
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
                  </c:when>
                  <c:otherwise>
                    <span class="badge badge-${v.displayStatus.toLowerCase()} text-uppercase">${v.displayStatus}</span>
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty allVehicles}">
            <tr><td colspan="4" class="text-secondary text-center py-4">No vehicles in stock yet.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </section>

  <script>
    // Quick status-only changer (Available/Checking/Maintenance). Setting
    // Checking/Maintenance opens a real maintenance record (visible in the
    // board above) with no estimate yet — maintenance staff sets the actual
    // estimated-days timeline themselves via "Complete Inspection" or
    // "Edit Duration / Cost" on that record, since it's their call to make,
    // not whoever happened to flip the status dropdown.
    document.addEventListener('DOMContentLoaded', function () {
      document.querySelectorAll('.status-inline-select').forEach(function (sel) {
        sel.dataset.original = sel.value; // remember the pre-change value so we can revert on cancel
      });
    });

    function handleStatusChange(select) {
      const form = select.form;
      const original = select.dataset.original;

      if (select.value === 'MAINTENANCE') {
        if (!confirm('Send this vehicle to Maintenance? It will appear in the board above for a technician to set the repair estimate.')) {
          select.value = original; return;
        }
      } else if (select.value === 'CHECKING') {
        if (!confirm('Send this vehicle to Checking? It will appear in the board above for inspection.')) {
          select.value = original; return;
        }
      } else if (select.value === 'AVAILABLE' && (original === 'CHECKING' || original === 'MAINTENANCE')) {
        if (!confirm('Mark this vehicle Available again? Its open maintenance record will be resolved.')) {
          select.value = original; return;
        }
      }
      select.dataset.original = select.value;
      form.submit();
    }
  </script>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
