<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem;">
    <h2 class="fw-bold mb-1" data-aos="fade-up">Booking Desk</h2>
    <p class="text-soft mb-4">Confirm pickups and returns — a vehicle only shows as Booked once you confirm the customer has actually taken it.</p>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
    <c:if test="${not empty sessionScope.successMessage}">
      <div class="alert alert-success">${sessionScope.successMessage}</div>
      <c:remove var="successMessage" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
      <div class="alert alert-danger">${sessionScope.errorMessage}</div>
      <c:remove var="errorMessage" scope="session" />
    </c:if>
    <c:if test="${!canEdit}">
      <div class="alert alert-info"><i class="fa-solid fa-eye me-2"></i>View-only — only booking staff can confirm pickups, no-shows, and returns.</div>
    </c:if>

    <!-- ============ TODAY'S PICKUPS ============ -->
    <h5 class="fw-bold mb-3" data-aos="fade-up">Today's Pickups</h5>
    <div class="glass-card p-3 mb-5" data-aos="fade-up">
      <c:choose>
        <c:when test="${empty todaysPickups}">
          <div class="empty-state">
            <i class="fa-solid fa-calendar-check"></i>
            <h5>Nothing due for pickup</h5>
            <p>Paid bookings whose pickup date has arrived will show up here.</p>
          </div>
        </c:when>
        <c:otherwise>
          <table class="table table-glass align-middle mb-0">
            <thead>
              <tr><th>Vehicle</th><th>Renter</th><th>Pickup Date</th><th>Status</th><th>Action</th></tr>
            </thead>
            <tbody>
              <c:forEach var="b" items="${todaysPickups}">
                <tr>
                  <td>${b.vehicleBrand} ${b.vehicleModel} <span class="text-soft">(${b.vehicleNumber})</span></td>
                  <td>${b.renterName}</td>
                  <td>
                    ${b.startDate}
                  </td>
                  <td><span class="badge badge-confirmed text-uppercase">Awaiting Pickup</span></td>
                  <td>
                    <a href="${pageContext.request.contextPath}/booking/booking-details?id=${b.bookingId}" target="_blank" class="btn btn-sm btn-outline-glass me-1">
                      <i class="fa-solid fa-eye me-1"></i>See Details
                    </a>
                    <c:if test="${canEdit}">
                      <form method="post" action="${pageContext.request.contextPath}/booking/dashboard" class="d-inline"
                            onsubmit="return confirm('Confirm this customer has taken the vehicle?');">
                        <input type="hidden" name="action" value="confirmPickup">
                        <input type="hidden" name="bookingId" value="${b.bookingId}">
                        <button class="btn btn-sm btn-gradient me-1">Confirm Pickup</button>
                      </form>
                      <form method="post" action="${pageContext.request.contextPath}/booking/dashboard" class="d-inline"
                            onsubmit="return confirm('Mark this as a no-show? The booking will be cancelled and the dates freed up.');">
                        <input type="hidden" name="action" value="markNoShow">
                        <input type="hidden" name="bookingId" value="${b.bookingId}">
                        <button class="btn btn-sm btn-outline-glass text-danger">No-Show</button>
                      </form>
                    </c:if>
                    <c:if test="${!canEdit}"><span class="text-soft small">—</span></c:if>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </c:otherwise>
      </c:choose>
    </div>

    <!-- ============ CURRENTLY OUT — CONFIRM RETURN ============ -->
    <h5 class="fw-bold mb-3" data-aos="fade-up">Currently Out — Due Back</h5>
    <div class="glass-card p-3" data-aos="fade-up">
      <c:choose>
        <c:when test="${empty currentlyOut}">
          <div class="empty-state">
            <i class="fa-solid fa-car-side"></i>
            <h5>Nothing out right now</h5>
            <p>Vehicles picked up by a customer will show up here until they're returned.</p>
          </div>
        </c:when>
        <c:otherwise>
          <table class="table table-glass align-middle mb-0">
            <thead>
              <tr><th>Vehicle</th><th>Renter</th><th>Due Back</th><th>Late Fee</th><th>Action</th></tr>
            </thead>
            <tbody>
              <c:forEach var="b" items="${currentlyOut}">
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
                  <td>
                    <c:choose>
                      <c:when test="${b.daysLateSoFar > 0}">
                        <span class="text-danger fw-semibold">Rs. ${b.projectedLateFee}</span>
                        <div class="text-soft small">${b.daysLateSoFar} day(s) late so far</div>
                      </c:when>
                      <c:otherwise><span class="text-soft small">—</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <a href="${pageContext.request.contextPath}/booking/booking-details?id=${b.bookingId}" target="_blank" class="btn btn-sm btn-outline-glass me-1">
                      <i class="fa-solid fa-eye me-1"></i>See Details
                    </a>
                    <c:if test="${canEdit}">
                      <form method="post" action="${pageContext.request.contextPath}/booking/dashboard" class="d-inline"
                            onsubmit="return confirm('Confirm this vehicle has been physically returned?${b.daysLateSoFar > 0 ? ' A late fee of Rs. '.concat(b.projectedLateFee).concat(' will be recorded.') : ''}');">
                        <input type="hidden" name="action" value="confirmReturn">
                        <input type="hidden" name="bookingId" value="${b.bookingId}">
                        <button class="btn btn-sm btn-gradient">Confirm Return</button>
                      </form>
                    </c:if>
                    <c:if test="${!canEdit}"><span class="text-soft small">—</span></c:if>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </c:otherwise>
      </c:choose>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
