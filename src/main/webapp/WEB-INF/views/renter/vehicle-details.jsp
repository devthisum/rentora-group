<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <style>
    .details-layout {
      display: grid;
      grid-template-columns: 1fr 380px;
      gap: 1.5rem;
      align-items: start;
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 1rem 4rem;
      padding-top: 7rem;
    }
    @media (max-width: 900px) {
      .details-layout { grid-template-columns: 1fr; }
    }
  </style>

  <div class="details-layout">
    <c:if test="${not empty errorMessage}">
      <div class="alert alert-danger" style="grid-column: 1 / -1;">${errorMessage}</div>
    </c:if>

    <!-- ============ LEFT: PHOTOS, INFO, REVIEWS ============ -->
    <div>
      <div class="glass-card p-3" data-aos="fade-up">
        <div style="position:relative;">
          <img id="mainVehicleImage" src="${not empty vehicle.imageUrl ? vehicle.imageUrl : 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=900'}"
               class="w-100 rounded-3 mb-3" style="height:380px;object-fit:cover;" alt="${vehicle.brand}">
          <c:if test="${sessionScope.user.roleName == 'RENTER'}">
            <button class="vehicle-heart wishlist-toggle-btn" data-vehicle-id="${vehicle.vehicleId}"
                    data-favorited="${isFavorited}" style="border:none;">
              <i class="fa-${isFavorited ? 'solid' : 'regular'} fa-heart" style="color:${isFavorited ? '#EF4444' : ''};"></i>
            </button>
          </c:if>
        </div>

        <c:if test="${fn:length(galleryImages) > 1}">
          <div class="d-flex gap-2 mb-3" style="overflow-x:auto;">
            <c:forEach var="img" items="${galleryImages}">
              <img src="${img}" onclick="document.getElementById('mainVehicleImage').src=this.src"
                   style="width:70px;height:56px;object-fit:cover;border-radius:8px;cursor:pointer;flex-shrink:0;opacity:0.8;"
                   onmouseover="this.style.opacity=1" onmouseout="this.style.opacity=0.8">
            </c:forEach>
          </div>
        </c:if>

        <h2 class="fw-bold">
          ${vehicle.brand} ${vehicle.model} (${vehicle.year})
          <c:if test="${vehicle.displayStatus != 'AVAILABLE'}">
            <span class="badge badge-${vehicle.displayStatus.toLowerCase()} text-uppercase align-middle ms-2">${vehicle.displayStatus}</span>
          </c:if>
        </h2>
        <p class="text-secondary">${vehicle.description}</p>
        <div class="d-flex justify-content-around text-center mt-4">
          <div><i class="fa-solid fa-users mb-1"></i><br>${vehicle.seats} Seats</div>
          <div><i class="fa-solid fa-gears mb-1"></i><br>${vehicle.transmission}</div>
          <div><i class="fa-solid fa-gas-pump mb-1"></i><br>${vehicle.fuelType}</div>
          <div><i class="fa-solid fa-star text-warning mb-1"></i><br>${vehicle.averageRating}</div>
        </div>
      </div>

      <!-- Technical Specification + Car Equipment -->
      <div class="glass-card p-4 mt-4" data-aos="fade-up">
        <h5 class="fw-bold mb-3">Technical Specification</h5>
        <div class="tech-spec-grid">
          <div class="tech-spec-box">
            <i class="fa-solid fa-gear"></i>
            <div><span class="tech-spec-label">Gear Box</span><span class="tech-spec-value">${vehicle.transmission}</span></div>
          </div>
          <div class="tech-spec-box">
            <i class="fa-solid fa-gas-pump"></i>
            <div><span class="tech-spec-label">Fuel</span><span class="tech-spec-value">${vehicle.fuelType}</span></div>
          </div>
          <div class="tech-spec-box">
            <i class="fa-solid fa-door-open"></i>
            <div><span class="tech-spec-label">Doors</span><span class="tech-spec-value">${not empty vehicle.doors ? vehicle.doors : '—'}</span></div>
          </div>
          <div class="tech-spec-box">
            <i class="fa-solid fa-snowflake"></i>
            <div><span class="tech-spec-label">Air Conditioner</span><span class="tech-spec-value">${vehicle.airConditioner == 'NO' ? 'No' : 'Yes'}</span></div>
          </div>
          <div class="tech-spec-box">
            <i class="fa-solid fa-user"></i>
            <div><span class="tech-spec-label">Seats</span><span class="tech-spec-value">${vehicle.seats}</span></div>
          </div>
          <div class="tech-spec-box">
            <i class="fa-solid fa-road"></i>
            <div><span class="tech-spec-label">Distance</span><span class="tech-spec-value">
              <c:choose>
                <c:when test="${not empty vehicle.mileage}">${vehicle.mileage} km</c:when>
                <c:otherwise>—</c:otherwise>
              </c:choose>
            </span></div>
          </div>
        </div>

        <a href="#booking-box" class="btn btn-gradient w-100 mt-4 py-2 rent-scroll-btn">Rent a car</a>

        <c:if test="${not empty vehicle.featureList}">
          <h5 class="fw-bold mt-4 mb-3">Car Equipment</h5>
          <div class="equipment-list">
            <c:forEach var="feat" items="${vehicle.featureList}">
              <span class="equipment-item"><i class="fa-solid fa-check"></i>${feat}</span>
            </c:forEach>
          </div>
        </c:if>
      </div>

      <!-- Reviews -->
      <div class="glass-card p-4 mt-4" data-aos="fade-up">
        <h5 class="fw-bold mb-3">Renter Reviews</h5>
        <c:choose>
          <c:when test="${empty reviews}">
            <p class="text-secondary mb-0">No reviews yet for this vehicle.</p>
          </c:when>
          <c:otherwise>
            <c:forEach var="rev" items="${reviews}">
              <div class="mb-3 pb-3" style="border-bottom:1px solid var(--border-soft, #EEF1F6);">
                <div class="d-flex justify-content-between">
                  <strong>${rev.renterName}</strong>
                  <span class="text-warning">
                    <c:forEach begin="1" end="${rev.rating}"><i class="fa-solid fa-star"></i></c:forEach>
                  </span>
                </div>
                <p class="text-secondary small mb-0">${rev.comment}</p>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>

      <c:if test="${sessionScope.user.roleName == 'RENTER'}">
        <div class="glass-card p-4 mt-4" data-aos="fade-up">
          <h5 class="fw-bold mb-3"><i class="fa-solid fa-message me-2" style="color: var(--accent);"></i>Ask a Question</h5>
          <form method="post" action="${pageContext.request.contextPath}/renter/inquiry/send">
            <input type="hidden" name="vehicleId" value="${vehicle.vehicleId}">
            <textarea name="message" rows="3" class="form-control form-control-glass mb-3"
                      placeholder="e.g. Is this vehicle available for a week in August?" required></textarea>
            <button type="submit" class="btn btn-outline-glass w-100">Send Message</button>
          </form>
        </div>
      </c:if>
    </div>

    <!-- ============ RIGHT: BOOKING BOX ============ -->
    <div>
      <div class="glass-card p-4" id="booking-box" data-aos="fade-up" style="position:sticky; top:6.5rem; scroll-margin-top:6.5rem;">
        <c:choose>
          <c:when test="${vehicle.hasPromotion}">
            <div class="badge badge-active text-uppercase mb-2 d-inline-block"><i class="fa-solid fa-tag me-1"></i>${vehicle.promotionTitle}</div>
            <h4 class="fw-bold mb-1">
              Rs. ${vehicle.discountedPrice} <span class="text-secondary fs-6">/ day</span>
              <span class="text-secondary fs-6" style="text-decoration:line-through; opacity:.6;">Rs. ${vehicle.pricePerDay}</span>
            </h4>
          </c:when>
          <c:otherwise>
            <h4 class="fw-bold mb-1">Rs. ${vehicle.pricePerDay} <span class="text-secondary fs-6">/ day</span></h4>
          </c:otherwise>
        </c:choose>

        <div class="details-trust-badges mb-3">
          <span><i class="fa-solid fa-shield-halved"></i>Fully Insured</span>
          <span><i class="fa-solid fa-rotate-left"></i>Free Cancellation</span>
          <span><i class="fa-solid fa-headset"></i>24/7 Support</span>
        </div>

        <c:choose>
          <c:when test="${vehicle.status == 'CHECKING' || vehicle.status == 'MAINTENANCE'}">
            <div class="text-center py-3">
              <span class="badge badge-${vehicle.status.toLowerCase()} text-uppercase mb-2 d-inline-block">${vehicle.status}</span>
              <p class="text-secondary mb-0">
                This vehicle is currently
                ${vehicle.status == 'CHECKING' ? 'being inspected after its last rental' : 'in for maintenance'}
                and can't be booked right now. Check back soon, or browse similar vehicles in the meantime.
              </p>
            </div>
          </c:when>
          <c:when test="${not empty sessionScope.user && sessionScope.user.roleName == 'RENTER'}">
            <form method="post" action="${pageContext.request.contextPath}/renter/book" id="booking-form"
                  data-price-per-day="${vehicle.hasPromotion ? vehicle.discountedPrice : vehicle.pricePerDay}" data-vehicle-id="${vehicle.vehicleId}">
              <input type="hidden" name="vehicleId" value="${vehicle.vehicleId}">
              <input type="hidden" id="startDate" name="startDate" required>
              <input type="hidden" id="endDate" name="endDate" required>

              <div class="mb-2">
                <label class="form-label small">Pickup &amp; Return Dates</label>
                <input type="text" id="dateRangePicker" class="form-control form-control-glass" placeholder="Select dates..." readonly>
              </div>
              <div class="d-flex gap-3 mb-3 small">
                <span><span class="calendar-dot" style="background:#22C55E;"></span> Booked</span>
                <span><span class="calendar-dot" style="background:#EF4444;"></span> Maintenance buffer</span>
              </div>

              <p class="text-soft small mb-3"><i class="fa-solid fa-shop me-1"></i>Pickup &amp; return at our shop.</p>
              <div class="mb-3">
                <label class="form-label small">Payment Method</label>
                <select name="paymentMethod" class="form-select form-select-glass">
                  <option value="CARD">Card (incl. 5% service fee)</option>
                  <option value="WALLET">Wallet (2% loyalty discount)</option>
                </select>
              </div>
              <p id="priceEstimate" class="text-info small mb-3"></p>
              <button type="submit" class="btn btn-gradient w-100 py-2">Request to Book</button>
            </form>
          </c:when>
          <c:when test="${empty sessionScope.user}">
            <p class="text-secondary">Please <a href="${pageContext.request.contextPath}/login" class="text-info">log in</a> as a renter to book this vehicle.</p>
          </c:when>
          <c:otherwise>
            <p class="text-secondary">Only renter accounts can book vehicles.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>

  <!-- ============ YOU MIGHT ALSO LIKE ============ -->
  <c:if test="${not empty relatedVehicles}">
    <section class="section-pad pt-0" style="max-width:1200px;margin:0 auto;padding-left:1rem;padding-right:1rem;">
      <h4 class="fw-bold mb-4" data-aos="fade-up">You Might Also Like</h4>
      <div class="related-vehicles-grid">
        <c:forEach var="rv" items="${relatedVehicles}" varStatus="rs">
          <a href="${pageContext.request.contextPath}/vehicle-details?id=${rv.vehicleId}" class="glass-card vehicle-card related-vehicle-card" data-aos="fade-up" data-aos-delay="${rs.index * 75}">
            <div class="vehicle-img">
              <img src="${not empty rv.imageUrl ? rv.imageUrl : 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=600'}" alt="${rv.brand} ${rv.model}">
            </div>
            <div class="p-3">
              <h6 class="mb-1" style="font-family:var(--font-serif);font-weight:700;">${rv.brand} ${rv.model}</h6>
              <div class="rating-stars mb-2">
                <c:forEach begin="1" end="5" var="s">
                  <i class="fa-solid fa-star${s <= rv.averageRating ? '' : ' dim'}"></i>
                </c:forEach>
              </div>
              <c:choose>
                <c:when test="${rv.hasPromotion}">
                  <div class="price-pill"><span class="amount">Rs. ${rv.discountedPrice}</span><span class="unit">per day</span></div>
                  <div class="badge badge-active text-uppercase mt-1" style="font-size:.65rem;"><i class="fa-solid fa-tag me-1"></i>Deal</div>
                </c:when>
                <c:otherwise>
                  <div class="price-pill"><span class="amount">Rs. ${rv.pricePerDay}</span><span class="unit">per day</span></div>
                </c:otherwise>
              </c:choose>
            </div>
          </a>
        </c:forEach>
      </div>
    </section>
  </c:if>

  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
  <style>
    .calendar-dot { display:inline-block; width:10px; height:10px; border-radius:50%; margin-right:4px; vertical-align:middle; }
    .flatpickr-day.rentora-booked { background:#22C55E22 !important; color:#16A34A !important; }
    .flatpickr-day.rentora-maintenance { background:#EF444422 !important; color:#DC2626 !important; }
    .flatpickr-day.rentora-booked, .flatpickr-day.rentora-maintenance { pointer-events:none; }
  </style>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />

  <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
  <c:if test="${sessionScope.user.roleName == 'RENTER'}">
    <script>
      (function() {
        var bookingForm = document.getElementById('booking-form');
        if (!bookingForm) return; // vehicle isn't bookable right now (CHECKING/MAINTENANCE) — no calendar needed
        var vehicleId = bookingForm.dataset.vehicleId;
        var bookedRanges = [];
        var maintenanceRanges = [];

        function toDates(range) {
          var dates = [];
          var d = new Date(range.start + "T00:00:00");
          var end = new Date(range.end + "T00:00:00");
          while (d <= end) { dates.push(new Date(d)); d.setDate(d.getDate() + 1); }
          return dates;
        }

        fetch("${pageContext.request.contextPath}/vehicle-calendar?vehicleId=" + vehicleId)
          .then(r => r.json())
          .then(data => {
            bookedRanges = data.booked || [];
            maintenanceRanges = data.maintenance || [];
            initCalendar();
          })
          .catch(() => initCalendar());

        function initCalendar() {
          var disable = [];
          bookedRanges.forEach(r => disable.push({ from: r.start, to: r.end }));
          maintenanceRanges.forEach(r => disable.push({ from: r.start, to: r.end }));

          flatpickr("#dateRangePicker", {
            mode: "range",
            minDate: "today",
            dateFormat: "Y-m-d",
            disable: disable,
            onDayCreate: function(dObj, dStr, fp, dayElem) {
              var iso = dayElem.dateObj.toISOString().slice(0, 10);
              if (bookedRanges.some(r => iso >= r.start && iso <= r.end)) {
                dayElem.classList.add('rentora-booked');
                dayElem.title = "Already booked";
              } else if (maintenanceRanges.some(r => iso >= r.start && iso <= r.end)) {
                dayElem.classList.add('rentora-maintenance');
                dayElem.title = "Reserved for maintenance";
              }
            },
            onChange: function(selectedDates) {
              if (selectedDates.length === 2) {
                var fmt = d => d.toISOString().slice(0, 10);
                document.getElementById('startDate').value = fmt(selectedDates[0]);
                document.getElementById('endDate').value = fmt(selectedDates[1]);
                document.getElementById('startDate').dispatchEvent(new Event('change'));
                document.getElementById('endDate').dispatchEvent(new Event('change'));
              }
            }
          });
        }
      })();
    </script>
  </c:if>
</body>
</html>
