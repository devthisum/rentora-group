<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem; max-width: 900px;">
    <c:choose>
      <c:when test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
      </c:when>
      <c:otherwise>
        <div class="d-flex align-items-center justify-content-between mb-4" data-aos="fade-up">
          <h2 class="fw-bold mb-0">Booking #${booking.bookingId}</h2>
          <span class="badge badge-${booking.status.toLowerCase()} text-uppercase fs-6">${booking.status}</span>
        </div>

        <div class="row g-4">
          <!-- ============ CUSTOMER ============ -->
          <div class="col-md-6">
            <div class="glass-card p-4 h-100" data-aos="fade-up">
              <h5 class="fw-bold mb-3"><i class="fa-solid fa-user me-2" style="color:var(--accent);"></i>Customer</h5>
              <c:choose>
                <c:when test="${not empty renter}">
                  <div class="detail-row"><span>Full Name</span><strong>${renter.fullName}</strong></div>
                  <div class="detail-row"><span>Email</span><strong>${renter.email}</strong></div>
                  <div class="detail-row"><span>Phone</span><strong>${renter.phone}</strong></div>
                  <div class="detail-row"><span>NIC</span><strong>${not empty renter.nicNumber ? renter.nicNumber : '—'}</strong></div>
                  <div class="detail-row"><span>Account Status</span><strong>${renter.status}</strong></div>
                </c:when>
                <c:otherwise><p class="text-soft mb-0">Customer details unavailable.</p></c:otherwise>
              </c:choose>
            </div>
          </div>

          <!-- ============ VEHICLE ============ -->
          <div class="col-md-6">
            <div class="glass-card p-4 h-100" data-aos="fade-up" data-aos-delay="50">
              <h5 class="fw-bold mb-3"><i class="fa-solid fa-car-side me-2" style="color:var(--accent);"></i>Vehicle</h5>
              <c:choose>
                <c:when test="${not empty vehicle}">
                  <div class="detail-row"><span>Vehicle</span><strong>${vehicle.brand} ${vehicle.model} (${vehicle.year})</strong></div>
                  <div class="detail-row"><span>Number</span><strong>${vehicle.vehicleNumber}</strong></div>
                  <div class="detail-row"><span>Category</span><strong>${vehicle.categoryName}</strong></div>
                  <div class="detail-row"><span>Transmission</span><strong>${vehicle.transmission}</strong></div>
                  <div class="detail-row"><span>Fuel Type</span><strong>${vehicle.fuelType}</strong></div>
                  <div class="detail-row"><span>Seats</span><strong>${vehicle.seats}</strong></div>
                  <div class="detail-row"><span>Price / Day</span><strong>Rs. ${vehicle.pricePerDay}</strong></div>
                </c:when>
                <c:otherwise><p class="text-soft mb-0">Vehicle details unavailable.</p></c:otherwise>
              </c:choose>
            </div>
          </div>

          <!-- ============ BOOKING & PRICING ============ -->
          <div class="col-12">
            <div class="glass-card p-4" data-aos="fade-up" data-aos-delay="100">
              <h5 class="fw-bold mb-3"><i class="fa-solid fa-file-invoice-dollar me-2" style="color:var(--accent);"></i>Booking &amp; Pricing</h5>
              <div class="row g-3">
                <div class="col-md-4"><div class="detail-row"><span>Pickup Date</span><strong>${booking.startDate}</strong></div></div>
                <div class="col-md-4"><div class="detail-row"><span>Return Date</span><strong>${booking.endDate}</strong></div></div>
                <div class="col-md-4"><div class="detail-row"><span>Booked On</span><strong>${booking.createdAt}</strong></div></div>
                <div class="col-md-4"><div class="detail-row"><span>Total Amount</span><strong>Rs. ${booking.totalAmount}</strong></div></div>
                <div class="col-md-4">
                  <div class="detail-row">
                    <span>Late Fee</span>
                    <c:choose>
                      <c:when test="${booking.lateFee > 0}"><strong class="text-danger">Rs. ${booking.lateFee}</strong></c:when>
                      <c:otherwise><strong class="text-soft">None</strong></c:otherwise>
                    </c:choose>
                  </div>
                </div>
                <div class="col-md-4">
                  <div class="detail-row"><span>Returned At</span><strong>${not empty booking.returnedAt ? booking.returnedAt : '—'}</strong></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-4" data-aos="fade-up">
          <button type="button" class="btn btn-outline-glass" onclick="window.close();">Close Window</button>
        </div>
      </c:otherwise>
    </c:choose>
  </section>

  <style>
    .detail-row { display: flex; justify-content: space-between; gap: 1rem; padding: 8px 0; border-bottom: 1px solid var(--line2); font-size: .9rem; }
    .detail-row:last-child { border-bottom: none; }
    .detail-row span { color: var(--ink3); }
  </style>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
