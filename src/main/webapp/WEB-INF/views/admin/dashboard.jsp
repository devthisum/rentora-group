<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container admin-dash" style="padding-top: 7rem; padding-bottom: 4rem;">

    <!-- ============ HEADER ============ -->
    <div class="admin-dash-header mb-4" data-aos="fade-up">
      <div>
        <div class="eyebrow mb-2">Admin</div>
        <h2 class="fw-bold mb-1">Welcome back, ${adminName}</h2>
        <p class="text-soft mb-0">Here's what's happening at RENTORA today.</p>
      </div>
      <div class="d-flex gap-2 flex-wrap">
        <a href="${pageContext.request.contextPath}/admin/vehicles/add" class="btn btn-gradient"><i class="fa-solid fa-plus me-1"></i>Add Vehicle</a>
        <a href="${pageContext.request.contextPath}/admin/vehicles" class="btn btn-outline-glass"><i class="fa-solid fa-car me-1"></i>All Vehicles</a>
        <a href="${pageContext.request.contextPath}/admin/staff" class="btn btn-outline-glass"><i class="fa-solid fa-users me-1"></i>Staff</a>
        <a href="${pageContext.request.contextPath}/maintenance/dashboard" class="btn btn-outline-glass"><i class="fa-solid fa-screwdriver-wrench me-1"></i>Maintenance</a>
      </div>
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

    <!-- ============ STAT CARDS ============ -->
    <div class="row g-3 mb-4">
      <div class="col-6 col-lg-3">
        <div class="admin-stat-card" data-aos="fade-up">
          <div class="admin-stat-icon admin-stat-icon-green"><i class="fa-solid fa-car-side"></i></div>
          <div class="admin-stat-value">${availableCount}</div>
          <div class="admin-stat-label">Available Vehicles</div>
        </div>
      </div>
      <div class="col-6 col-lg-3">
        <div class="admin-stat-card" data-aos="fade-up" data-aos-delay="25">
          <div class="admin-stat-icon admin-stat-icon-blue"><i class="fa-solid fa-key"></i></div>
          <div class="admin-stat-value">${bookedCount}</div>
          <div class="admin-stat-label">Currently Booked</div>
        </div>
      </div>
      <div class="col-6 col-lg-3">
        <div class="admin-stat-card" data-aos="fade-up" data-aos-delay="50">
          <div class="admin-stat-icon admin-stat-icon-amber"><i class="fa-solid fa-screwdriver-wrench"></i></div>
          <div class="admin-stat-value">${maintenanceCount}</div>
          <div class="admin-stat-label">In Checking / Maintenance</div>
        </div>
      </div>
      <div class="col-6 col-lg-3">
        <div class="admin-stat-card" data-aos="fade-up" data-aos-delay="75">
          <div class="admin-stat-icon admin-stat-icon-green"><i class="fa-solid fa-warehouse"></i></div>
          <div class="admin-stat-value">${totalVehicles}</div>
          <div class="admin-stat-label">Total Fleet Size</div>
        </div>
      </div>
      <div class="col-6 col-lg-6">
        <div class="admin-stat-card admin-stat-card-wide" data-aos="fade-up" data-aos-delay="100">
          <div class="admin-stat-icon admin-stat-icon-green"><i class="fa-solid fa-sack-dollar"></i></div>
          <div>
            <div class="admin-stat-value">Rs. ${totalRevenue}</div>
            <div class="admin-stat-label">Total Revenue</div>
          </div>
        </div>
      </div>
      <div class="col-6 col-lg-6">
        <div class="admin-stat-card admin-stat-card-wide" data-aos="fade-up" data-aos-delay="125">
          <div class="admin-stat-icon admin-stat-icon-red"><i class="fa-solid fa-triangle-exclamation"></i></div>
          <div>
            <div class="admin-stat-value">Rs. ${lateFeesCollected}</div>
            <div class="admin-stat-label">Late Fees Collected</div>
          </div>
        </div>
      </div>
    </div>

    <!-- ============ ANALYTICS ============ -->
    <div class="row g-3 mb-4">
      <div class="col-lg-7">
        <div class="admin-chart-card" data-aos="fade-up">
          <h6 class="fw-bold mb-3">Revenue — Last 6 Months</h6>
          <canvas id="revenueChart" height="220"></canvas>
        </div>
      </div>
      <div class="col-lg-5">
        <div class="admin-chart-card" data-aos="fade-up" data-aos-delay="25">
          <h6 class="fw-bold mb-3">Bookings by Status</h6>
          <canvas id="statusChart" height="220"></canvas>
        </div>
      </div>
    </div>

    <div class="row g-3 mb-4">
      <div class="col-lg-12">
        <div class="admin-chart-card" data-aos="fade-up">
          <h6 class="fw-bold mb-3">Most Popular Vehicles</h6>
          <c:choose>
            <c:when test="${empty topVehicles}">
              <p class="text-soft mb-0">No bookings yet — popular vehicles will show up here once you do.</p>
            </c:when>
            <c:otherwise>
              <c:forEach var="entry" items="${topVehicles}" varStatus="rs">
                <div class="popular-vehicle-row">
                  <span class="popular-vehicle-rank">#${rs.index + 1}</span>
                  <span class="popular-vehicle-name">${entry.key}</span>
                  <div class="popular-vehicle-bar-track">
                    <div class="popular-vehicle-bar" style="width:${(entry.value / topVehicles[0].value) * 100}%;"></div>
                  </div>
                  <span class="popular-vehicle-count">${entry.value} booking${entry.value == 1 ? '' : 's'}</span>
                </div>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>

    <!-- ============ VEHICLES DUE BACK / MAINTENANCE ============ -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h5 class="fw-bold mb-0" data-aos="fade-up">Vehicles Due Back / Under Maintenance</h5>
      <a href="${pageContext.request.contextPath}/maintenance/dashboard" class="small">Open Maintenance Board &rarr;</a>
    </div>
    <div class="admin-table-card mb-4" data-aos="fade-up">
      <table class="table table-glass align-middle mb-0">
        <thead><tr><th>Vehicle</th><th>Stage</th><th>Details</th></tr></thead>
        <tbody>
          <c:forEach var="m" items="${activeMaintenance}">
            <tr>
              <td>${m.vehicleBrand} ${m.vehicleModel} (${m.vehicleNumber})</td>
              <td><span class="badge badge-${m.stage.toLowerCase()} text-uppercase">${m.stage}</span></td>
              <td>
                <c:if test="${m.stage == 'CHECKING'}">Awaiting inspection</c:if>
                <c:if test="${m.stage == 'UNDER_MAINTENANCE'}">Under maintenance${not empty m.estimatedDays ? ' — '.concat(m.estimatedDays).concat(' day(s)') : ''}</c:if>
                <c:if test="${m.stage == 'AWAITING_CUSTOMER_PAYMENT'}">Awaiting customer payment (Rs. ${m.totalCharge})</c:if>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty activeMaintenance}">
            <tr><td colspan="3" class="text-secondary text-center py-4">Nothing needs attention right now.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>

    <!-- ============ ALL BOOKINGS ============ -->
    <h5 class="fw-bold mb-3" data-aos="fade-up">All Bookings</h5>
    <div class="admin-table-card" data-aos="fade-up">
      <table class="table table-glass align-middle mb-0">
        <thead><tr><th>Renter</th><th>Vehicle</th><th>Dates</th><th>Total</th><th>Late Fee</th><th>Status</th><th></th></tr></thead>
        <tbody>
          <c:forEach var="b" items="${allBookings}">
            <tr>
              <td>${b.renterName}</td>
              <td>${b.vehicleBrand} ${b.vehicleModel}</td>
              <td>${b.startDate} &rarr; ${b.endDate}</td>
              <td>Rs. ${b.totalAmount}</td>
              <td>
                <c:choose>
                  <c:when test="${b.lateFee > 0}"><span class="text-danger fw-semibold">Rs. ${b.lateFee}</span></c:when>
                  <c:otherwise><span class="text-soft small">—</span></c:otherwise>
                </c:choose>
              </td>
              <td><span class="badge badge-${b.status.toLowerCase()} text-uppercase">${b.status}</span></td>
              <td>
                <a href="${pageContext.request.contextPath}/booking/booking-details?id=${b.bookingId}" target="_blank" class="btn btn-sm btn-outline-glass">
                  <i class="fa-solid fa-eye"></i>
                </a>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty allBookings}">
            <tr><td colspan="7" class="text-secondary text-center py-4">No bookings yet.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />

  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
  <script>
    (function () {
      var accentColor = getComputedStyle(document.documentElement).getPropertyValue('--accent').trim() || '#FC7D14';
      Chart.defaults.font.family = "'Outfit', sans-serif";
      Chart.defaults.color = '#8a8f98';

      // ---- Revenue trend (last 6 months) ----
      var revenueCtx = document.getElementById('revenueChart');
      if (revenueCtx) {
        new Chart(revenueCtx, {
          type: 'bar',
          data: {
            labels: [<c:forEach var="m" items="${monthLabels}" varStatus="s">'${m}'${s.last ? '' : ','}</c:forEach>],
            datasets: [{
              label: 'Revenue (Rs.)',
              data: [<c:forEach var="r" items="${monthRevenue}" varStatus="s">${r}${s.last ? '' : ','}</c:forEach>],
              backgroundColor: accentColor,
              borderRadius: 8,
              maxBarThickness: 46
            }]
          },
          options: {
            plugins: { legend: { display: false } },
            scales: {
              y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,.05)' } },
              x: { grid: { display: false } }
            }
          }
        });
      }

      // ---- Booking status donut ----
      var statusCtx = document.getElementById('statusChart');
      if (statusCtx) {
        var statusColors = {
          AWAITING_PAYMENT: '#d97706', CONFIRMED: '#FC7D14', ONGOING: '#1a56db',
          RETURNED: '#8a8f98', COMPLETED: '#0d9e6e', CANCELLED: '#dc2626'
        };
        var labels = [<c:forEach var="e" items="${statusCounts}" varStatus="s">'${e.key}'${s.last ? '' : ','}</c:forEach>];
        var data = [<c:forEach var="e" items="${statusCounts}" varStatus="s">${e.value}${s.last ? '' : ','}</c:forEach>];
        new Chart(statusCtx, {
          type: 'doughnut',
          data: {
            labels: labels,
            datasets: [{
              data: data,
              backgroundColor: labels.map(function (l) { return statusColors[l] || '#c9ccd3'; }),
              borderWidth: 2,
              borderColor: '#fff'
            }]
          },
          options: {
            plugins: { legend: { position: 'bottom', labels: { boxWidth: 10, padding: 12, font: { size: 11 } } } },
            cutout: '65%'
          }
        });
      }
    })();
  </script>
</body>
</html>
