<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="rt" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <style>
    .browse-wrap { max-width: 1180px; margin: 0 auto; padding: 7rem 1rem 4rem; }

    /* Sort + compare (kept inline so this page never depends on a stale cached stylesheet) */
    .sort-select { min-width: 190px; padding-top: .45rem; padding-bottom: .45rem; font-size: .88rem; }
    .compare-toggle { position: relative; display: inline-flex; cursor: pointer; margin-top: .75rem; user-select: none; }
    .compare-toggle input { position: absolute; opacity: 0; width: 1px; height: 1px; pointer-events: none; }
    .compare-toggle span { display: inline-flex; align-items: center; gap: .4rem; padding: .38rem .95rem; border: 1.5px solid var(--line2); border-radius: 999px; background: var(--bg); color: var(--ink3); font-size: .8rem; font-weight: 600; transition: all .2s ease; }
    .compare-toggle:hover span { border-color: var(--accent); color: var(--accent); }
    .compare-toggle .ct-on { display: none; }
    .compare-toggle input:checked ~ .ct-off { display: none; }
    .compare-toggle input:checked ~ .ct-on { display: inline-flex; background: var(--accent); border-color: var(--accent); color: #fff; }
    .compare-toggle input:focus-visible ~ span { outline: 2px solid var(--accent); outline-offset: 2px; }

    .compare-bar { position: fixed; left: 50%; transform: translateX(-50%); bottom: 1rem; width: min(980px, calc(100% - 2rem)); z-index: 1040; padding: .7rem .9rem; background: var(--bg); border: 1px solid var(--line2); border-radius: 18px; box-shadow: 0 12px 40px rgba(0,0,0,.2); }
    .compare-bar[hidden] { display: none; }
    .compare-bar-inner { display: flex; align-items: center; gap: .75rem; flex-wrap: wrap; }
    .compare-bar-items { display: flex; gap: .5rem; flex-wrap: wrap; flex: 1 1 auto; min-width: 0; }
    .compare-chip { display: inline-flex; align-items: center; gap: .15rem; padding: .3rem .35rem .3rem .8rem; border-radius: 999px; background: var(--accent-l); border: 1px solid var(--accent); color: var(--ink); font-size: .82rem; font-weight: 600; }
    .compare-chip button { border: 0; background: transparent; color: var(--ink3); font-size: 1.15rem; line-height: 1; padding: 0 .4rem; cursor: pointer; }
    .compare-chip button:hover { color: var(--red); }
    .compare-bar-hint { font-size: .8rem; color: var(--ink3); white-space: nowrap; }
    .compare-bar .btn.disabled { opacity: .5; pointer-events: none; }
    body:has(.compare-bar:not([hidden])) { padding-bottom: 96px; }
    [data-theme="dark"] .compare-chip { background: rgba(252,125,20,.16); }
  </style>

  <div class="browse-wrap">
    <h2 class="fw-bold text-center mb-1" data-aos="fade-up">Explore Our Fleet</h2>
    <p class="text-center text-secondary mb-4" data-aos="fade-up">Browse our fleet and find the perfect ride for your trip</p>

    <c:if test="${param.compare == 'few'}">
      <div class="alert alert-info" role="alert">Pick at least two vehicles to compare. Tick &ldquo;Compare&rdquo; on the vehicles you like.</div>
    </c:if>

    <!-- ============ CATEGORY PILLS ============ -->
    <div class="vehicle-pill-row" data-aos="fade-up">
      <a href="${pageContext.request.contextPath}/vehicles" class="filter-btn filter-btn-solid ${empty param.category && param.deals != '1' ? 'active' : ''}">All Vehicles</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Car" class="filter-btn filter-btn-solid ${param.category == 'Car' ? 'active' : ''}"><i class="fa-solid fa-car me-1"></i>Cars</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=SUV" class="filter-btn filter-btn-solid ${param.category == 'SUV' ? 'active' : ''}"><i class="fa-solid fa-car-side me-1"></i>SUVs</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Luxury" class="filter-btn filter-btn-solid ${param.category == 'Luxury' ? 'active' : ''}"><i class="fa-solid fa-gem me-1"></i>Luxury</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Sports" class="filter-btn filter-btn-solid ${param.category == 'Sports' ? 'active' : ''}"><i class="fa-solid fa-gauge-high me-1"></i>Sports</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Van" class="filter-btn filter-btn-solid ${param.category == 'Van' ? 'active' : ''}"><i class="fa-solid fa-van-shuttle me-1"></i>Vans</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Bus" class="filter-btn filter-btn-solid ${param.category == 'Bus' ? 'active' : ''}"><i class="fa-solid fa-bus me-1"></i>Buses</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Motorcycle" class="filter-btn filter-btn-solid ${param.category == 'Motorcycle' ? 'active' : ''}"><i class="fa-solid fa-motorcycle me-1"></i>Motorcycles</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=ThreeWheeler" class="filter-btn filter-btn-solid ${param.category == 'ThreeWheeler' ? 'active' : ''}"><i class="fa-solid fa-taxi me-1"></i>Three Wheelers</a>
      <a href="${pageContext.request.contextPath}/vehicles?category=Electric+Vehicle" class="filter-btn filter-btn-solid ${param.category == 'Electric Vehicle' ? 'active' : ''}"><i class="fa-solid fa-bolt me-1"></i>Electric</a>
      <a href="${pageContext.request.contextPath}/vehicles?deals=1" class="filter-btn filter-btn-solid filter-btn-deals ${param.deals == '1' ? 'active' : ''}"><i class="fa-solid fa-tags me-1"></i>Deals</a>
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
      <div class="d-flex align-items-center gap-2 flex-wrap">
        <label for="sortSelect" class="small text-secondary fw-semibold mb-0">Sort by</label>
        <select id="sortSelect" class="form-select form-select-glass sort-select" data-current="${param.sort}" aria-label="Sort vehicles">
          <option value="">Recommended</option>
          <option value="price_asc" ${param.sort == 'price_asc' ? 'selected' : ''}>Price: low to high</option>
          <option value="price_desc" ${param.sort == 'price_desc' ? 'selected' : ''}>Price: high to low</option>
          <option value="rating" ${param.sort == 'rating' ? 'selected' : ''}>Top rated</option>
          <option value="discount" ${param.sort == 'discount' ? 'selected' : ''}>Biggest discount</option>
          <option value="newest" ${param.sort == 'newest' ? 'selected' : ''}>Newest model</option>
        </select>
      <div class="fleet-view-toggle" id="fleetViewToggle">
        <button type="button" data-view="list" class="active"><i class="fa-solid fa-list"></i>List</button>
        <button type="button" data-view="grid"><i class="fa-solid fa-grip"></i>Grid</button>
      </div>
      </div>
    </div>

    <div id="fleetResults" aria-live="polite">
    <c:choose>
      <c:when test="${not empty errorMessage && empty vehicles}">
        <div class="error-state glass-card" role="alert">
          <i class="fa-solid fa-triangle-exclamation"></i>
          <h5>We couldn't load the fleet</h5>
          <p>${errorMessage} This is usually temporary &mdash; please try again in a moment.</p>
          <div class="d-flex gap-2 justify-content-center flex-wrap mt-3">
            <button type="button" class="btn btn-gradient" onclick="window.location.reload()"><i class="fa-solid fa-rotate-right me-1"></i>Try again</button>
            <a href="${pageContext.request.contextPath}/" class="btn btn-outline-glass">Back to home</a>
          </div>
        </div>
      </c:when>
      <c:when test="${empty vehicles}">
        <div class="empty-state glass-card" data-aos="fade-up">
          <i class="fa-solid fa-car-side"></i>
          <h5>${param.deals == '1' ? 'No deals right now' : 'No vehicles match your search'}</h5>
          <p>${param.deals == '1' ? 'New promotions show up here as soon as they go live. Check back soon.' : 'Try widening your price range, picking a different vehicle type, or clearing your filters.'}</p>
          <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-outline-glass mt-3">Clear Filters</a>
        </div>
      </c:when>
      <c:otherwise>
        <div class="fleet-list" id="fleetList">
          <c:forEach var="v" items="${vehicles}" varStatus="loop">
            <div class="fleet-row ${loop.index % 2 == 1 ? 'fleet-row-alt' : ''} ${v.hasPromotion ? 'deal' : ''}" data-aos="fade-up" data-aos-delay="${loop.index % 4 * 75}">
              <div class="fleet-row-media ${v.hasPromotion ? 'has-promo' : ''}">
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
                <rt:promoRibbon vehicle="${v}" />
              </div>
              <div class="fleet-row-info">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
                  <div>
                    <h4 class="fleet-row-title">${v.brand} ${v.model}</h4>
                    <c:if test="${v.hasPromotion}"><div class="small fw-semibold" style="color:var(--accent);"><i class="fa-solid fa-tag me-1"></i>${v.promotionTitle}</div></c:if>
                    <div class="rating-stars mt-1">
                      <c:forEach begin="1" end="5" var="s">
                        <i class="fa-solid fa-star${s <= v.averageRating ? '' : ' dim'}"></i>
                      </c:forEach>
                      <span class="text-soft" style="font-size:.72rem;margin-left:3px;">(${v.averageRating})</span>
                    </div>
                  </div>
                  <rt:promoPrice vehicle="${v}" align="end" />
                </div>
                <div class="fleet-row-specs">
                  <span><i class="fa-solid fa-gear"></i>${v.transmission}</span>
                  <span><i class="fa-solid fa-gas-pump"></i>${v.fuelType}</span>
                  <span><i class="fa-solid fa-user"></i>${v.seats} seats</span>
                  <c:if test="${not empty v.mileage}"><span><i class="fa-solid fa-road"></i>${v.mileage} km</span></c:if>
                </div>
                <label class="compare-toggle"><input type="checkbox" class="compare-check" data-id="${v.vehicleId}" data-name="${v.brand} ${v.model}" aria-label="Add ${v.brand} ${v.model} to comparison"><span class="ct-off"><i class="fa-solid fa-plus"></i>Compare</span><span class="ct-on"><i class="fa-solid fa-check"></i>Added</span></label>
                <div class="d-flex gap-2 mt-2">
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
  </div>

  <!-- Compare bar: appears once a vehicle is ticked; picks survive filtering/sorting (sessionStorage) -->
  <div class="compare-bar" id="compareBar" hidden>
    <div class="compare-bar-inner">
      <div class="compare-bar-items" id="compareItems"></div>
      <span class="compare-bar-hint" id="compareHint"></span>
      <button type="button" class="btn btn-sm btn-outline-glass" id="compareClear">Clear</button>
      <a href="#" class="btn btn-gradient btn-sm disabled" id="compareGo" aria-disabled="true"><i class="fa-solid fa-code-compare me-1"></i>Compare</a>
    </div>
  </div>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
  <script>window.RENTORA_CTX = '${pageContext.request.contextPath}';</script>
  <script src="${pageContext.request.contextPath}/assets/js/compare.js?v=${assetVersion}"></script>
</body>
</html>
