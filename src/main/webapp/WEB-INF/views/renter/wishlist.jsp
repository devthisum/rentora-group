<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <style>
    .wishlist-layout { max-width: 1200px; margin: 0 auto; padding: 0 1rem 4rem; padding-top: 7rem; }
    .wishlist-filters { display: grid; grid-template-columns: 1fr 1fr auto; gap: .75rem; align-items: end; margin-bottom: 1.5rem; }
    @media (max-width: 700px) { .wishlist-filters { grid-template-columns: 1fr; } }
    .wishlist-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
    @media (max-width: 950px) { .wishlist-grid { grid-template-columns: repeat(2, 1fr); } }
    @media (max-width: 650px) { .wishlist-grid { grid-template-columns: 1fr; } }
  </style>

  <div class="wishlist-layout">
    <h2 class="fw-bold mb-4" data-aos="fade-up">My Saved Vehicles</h2>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <form method="get" action="${pageContext.request.contextPath}/renter/wishlist" class="glass-card p-3 wishlist-filters" data-aos="fade-up">
      <div>
        <label class="form-label small">Search saved vehicles</label>
        <input type="text" name="q" value="${keyword}" placeholder="e.g. Toyota" class="form-control form-control-glass">
      </div>
      <div>
        <label class="form-label small">Sort by</label>
        <select name="sort" class="form-select form-select-glass">
          <option value="" ${empty sortBy ? 'selected' : ''}>Recently Added</option>
          <option value="price_asc" ${sortBy == 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
          <option value="price_desc" ${sortBy == 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
          <option value="rating" ${sortBy == 'rating' ? 'selected' : ''}>Highest Rated</option>
        </select>
      </div>
      <div>
        <button type="submit" class="btn btn-gradient">Apply</button>
      </div>
    </form>

    <c:choose>
      <c:when test="${empty vehicles}">
        <div class="glass-card p-5 text-center" data-aos="fade-up">
          <i class="fa-regular fa-heart fa-2x mb-3" style="color: var(--accent);"></i>
          <h5>Nothing saved yet</h5>
          <p class="text-secondary mb-0">Tap the heart icon on any vehicle to save it here.</p>
        </div>
      </c:when>
      <c:otherwise>
        <div class="wishlist-grid">
          <c:forEach var="v" items="${vehicles}">
            <div class="glass-card vehicle-card" data-aos="fade-up">
              <div class="vehicle-img" style="position:relative;">
                <img src="${not empty v.imageUrl ? v.imageUrl : 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=600'}" alt="${v.brand} ${v.model}">
                <c:if test="${v.displayStatus != 'AVAILABLE'}">
                  <span class="badge badge-${v.displayStatus.toLowerCase()} text-uppercase" style="position:absolute; top:10px; left:10px;">${v.displayStatus}</span>
                </c:if>
                <button class="vehicle-heart wishlist-toggle-btn" data-vehicle-id="${v.vehicleId}"
                        data-favorited="true" data-remove-on-unfavorite="true" style="border:none;">
                  <i class="fa-solid fa-heart" style="color:#EF4444;"></i>
                </button>
              </div>
              <div class="p-3">
                <div class="d-flex justify-content-between align-items-start mb-1">
                  <h5 class="mb-0">${v.brand} ${v.model}</h5>
                  <c:choose>
                    <c:when test="${v.hasPromotion}">
                      <span class="price-tag">Rs. ${v.discountedPrice}/day <span style="text-decoration:line-through; opacity:.6; font-weight:400;">Rs. ${v.pricePerDay}</span></span>
                    </c:when>
                    <c:otherwise>
                      <span class="price-tag">Rs. ${v.pricePerDay}/day</span>
                    </c:otherwise>
                  </c:choose>
                </div>
                <c:if test="${v.hasPromotion}">
                  <span class="badge badge-active text-uppercase mb-2 d-inline-block"><i class="fa-solid fa-tag me-1"></i>${v.promotionTitle}</span>
                </c:if>
                <p class="text-secondary small mb-2">${v.categoryName} &nbsp;|&nbsp; ${v.seats} seats</p>
                <div class="d-flex justify-content-between align-items-center">
                  <span><i class="fa-solid fa-star text-warning me-1"></i>${v.averageRating}</span>
                  <a href="${pageContext.request.contextPath}/vehicle-details?id=${v.vehicleId}" class="btn btn-outline-glass btn-sm">View Details</a>
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
