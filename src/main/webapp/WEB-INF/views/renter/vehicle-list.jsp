<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <style>
    .browse-wrap { max-width: 1180px; margin: 0 auto; padding: 7rem 1rem 4rem; }
  </style>

  <div class="browse-wrap">
    <h2 class="fw-bold text-center mb-1" data-aos="fade-up">Explore Our Fleet</h2>
    <p class="text-center text-secondary mb-4" data-aos="fade-up">Browse our fleet and find the perfect ride for your trip</p>

    <c:if test="${not empty errorMessage}">
      <div class="alert alert-danger">${errorMessage}</div>
    </c:if>

    <!-- ============ CATEGORY PILLS ============ -->
    <div class="vehicle-pill-row" data-aos="fade-up">
      <a href="${pageContext.request.contextPath}/vehicles" class="filter-btn filter-btn-solid ${empty param.category ? 'active' : ''}">All Vehicles</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Car" class="filter-btn filter-btn-solid ${param.category == 'Car' ? 'active' : ''}"><i class="fa-solid fa-car me-1"></i>Cars</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=SUV" class="filter-btn filter-btn-solid ${param.category == 'SUV' ? 'active' : ''}"><i class="fa-solid fa-car-side me-1"></i>SUVs</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Luxury" class="filter-btn filter-btn-solid ${param.category == 'Luxury' ? 'active' : ''}"><i class="fa-solid fa-gem me-1"></i>Luxury</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Sports" class="filter-btn filter-btn-solid ${param.category == 'Sports' ? 'active' : ''}"><i class="fa-solid fa-gauge-high me-1"></i>Sports</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Van" class="filter-btn filter-btn-solid ${param.category == 'Van' ? 'active' : ''}"><i class="fa-solid fa-van-shuttle me-1"></i>Vans</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Bus" class="filter-btn filter-btn-solid ${param.category == 'Bus' ? 'active' : ''}"><i class="fa-solid fa-bus me-1"></i>Buses</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Motorcycle" class="filter-btn filter-btn-solid ${param.category == 'Motorcycle' ? 'active' : ''}"><i class="fa-solid fa-motorcycle me-1"></i>Motorcycles</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=ThreeWheeler" class="filter-btn filter-btn-solid ${param.category == 'ThreeWheeler' ? 'active' : ''}"><i class="fa-solid fa-taxi me-1"></i>Three Wheelers</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Electric+Vehicle" class="filter-btn filter-btn-solid ${param.category == 'Electric Vehicle' ? 'active' : ''}"><i class="fa-solid fa-bolt me-1"></i>Electric</a>
      <button type="button" class="filter-btn filter-btn-outline" data-bs-toggle="collapse" data-bs-target="#moreFilters">
        <i class="fa-solid fa-sliders me-1"></i>More Filters
      </button>
    </div>

    <!-- ============ MORE FILTERS (search + price + transmission) ============ -->
    <div class="collapse ${not empty param.minPrice || not empty param.maxPrice || not empty param.transmission || not empty param.q ? 'show' : ''}" id="moreFilters">
      <form method="get" action="${pageContext.request.contextPath}/vehicles" class="glass-card p-3 more-filters-bar" data-aos="fade-up">
        <input type="hidden" name="category" value="${param.category}">
        <div class="search-box-wrap flex-grow-1">
          <i class="fa-solid fa-magnifying-glass search-box-icon"></i>
          <input type="text" name="q" value="${param.q}" placeholder="Search by brand or model..." class="form-control form-control-glass search-box-input">
        </div>
        <input type="number" name="minPrice" value="${param.minPrice}" placeholder="Min Price/day" class="form-control form-control-glass">
        <input type="number" name="maxPrice" value="${param.maxPrice}" placeholder="Max Price/day" class="form-control form-control-glass">
        <select name="transmission" class="form-select form-select-glass">
          <option value="">Any Transmission</option>
          <option value="MANUAL" ${param.transmission == 'MANUAL' ? 'selected' : ''}>Manual</option>
          <option value="AUTOMATIC" ${param.transmission == 'AUTOMATIC' ? 'selected' : ''}>Automatic</option>
        </select>
        <button type="submit" class="btn btn-gradient"><i class="fa-solid fa-filter me-1"></i>Apply</button>
      </form>
    </div>

    <div class="fleet-toolbar mt-4 mb-3" data-aos="fade-up">
      <p class="text-secondary small fw-semibold mb-0">
        <c:choose>
          <c:when test="${empty vehicles}">0 vehicles found</c:when>
          <c:otherwise>${fn:length(vehicles)} vehicle${fn:length(vehicles) == 1 ? '' : 's'} found</c:otherwise>
        </c:choose>
      </p>
      <div class="fleet-view-toggle" id="fleetViewToggle">
        <button type="button" data-view="list" class="active"><i class="fa-solid fa-list"></i>List</button>
        <button type="button" data-view="grid"><i class="fa-solid fa-grip"></i>Grid</button>
      </div>
    </div>

    <c:choose>
      <c:when test="${empty vehicles}">
        <div class="empty-state glass-card" data-aos="fade-up">
          <i class="fa-solid fa-car-side"></i>
          <h5>No vehicles match your search</h5>
          <p>Try widening your price range, picking a different vehicle type, or clearing your filters.</p>
          <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-outline-glass mt-3">Clear Filters</a>
        </div>
      </c:when>
      <c:otherwise>
        <div class="fleet-list" id="fleetList">
          <c:forEach var="v" items="${vehicles}" varStatus="loop">
            <div class="fleet-row ${loop.index % 2 == 1 ? 'fleet-row-alt' : ''}" data-aos="fade-up" data-aos-delay="${loop.index % 4 * 75}">
              <div class="fleet-row-media">
                <c:if test="${v.displayStatus != 'AVAILABLE'}">
                  <span class="badge badge-${v.displayStatus.toLowerCase()} text-uppercase vehicle-card-v2-badge">${v.displayStatus}</span>
                </c:if>
                <c:if test="${sessionScope.user.roleName == 'RENTER'}">
                  <button type="button" class="vehicle-heart wishlist-toggle-btn" data-vehicle-id="${v.vehicleId}"
                          data-favorited="${favoritedIds.contains(v.vehicleId)}" style="border:none;">
                    <i class="fa-${favoritedIds.contains(v.vehicleId) ? 'solid' : 'regular'} fa-heart"
                       style="color:${favoritedIds.contains(v.vehicleId) ? '#EF4444' : ''};"></i>
                  </button>
                </c:if>
                <img src="${not empty v.imageUrl ? v.imageUrl : 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=600'}" alt="${v.brand} ${v.model}">
              </div>
              <div class="fleet-row-info">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
                  <div>
                    <h4 class="fleet-row-title">${v.brand} ${v.model}</h4>
                    <div class="rating-stars mt-1">
                      <c:forEach begin="1" end="5" var="s">
                        <i class="fa-solid fa-star${s <= v.averageRating ? '' : ' dim'}"></i>
                      </c:forEach>
                      <span class="text-soft" style="font-size:.72rem;margin-left:3px;">(${v.averageRating})</span>
                    </div>
                  </div>
                  <div class="vehicle-card-v2-price fs-5">Rs. ${v.pricePerDay}<small>per day</small></div>
                </div>
                <div class="fleet-row-specs">
                  <span><i class="fa-solid fa-gear"></i>${v.transmission}</span>
                  <span><i class="fa-solid fa-gas-pump"></i>${v.fuelType}</span>
                  <span><i class="fa-solid fa-user"></i>${v.seats} seats</span>
                  <c:if test="${not empty v.mileage}"><span><i class="fa-solid fa-road"></i>${v.mileage} km</span></c:if>
                </div>
                <div class="d-flex gap-2 mt-3">
                  <a href="${pageContext.request.contextPath}/vehicle-details?id=${v.vehicleId}" class="btn btn-outline-glass flex-fill">Details</a>
                  <a href="${pageContext.request.contextPath}/vehicle-details?id=${v.vehicleId}" class="btn btn-gradient flex-fill">Book Now</a>
                </div>
              </div>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
