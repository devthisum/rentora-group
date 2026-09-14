<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <!-- ============ HERO ============ -->
  <section class="hero-v3" id="home">
    <div class="container position-relative" style="z-index:4;">
      <div class="row align-items-center gy-5">
        <div class="col-lg-6">
          <div class="eyebrow mb-3">Vehicle Rental, Reimagined</div>
          <h1 class="hero-v3-title mb-3">
            <span id="heroTypewriter" data-lines="Rent the Perfect|Car for Your Next Journey"></span>
          </h1>
          <p class="text-soft fs-5 mb-4" style="max-width:460px;">Affordable, reliable, and fast bookings.</p>

          <form method="get" action="${pageContext.request.contextPath}/vehicles" id="heroSearchForm">
            <input type="hidden" name="category" id="heroCategoryInput" value="">

            <div class="hero-tab-group mb-4">
              <button type="button" class="hero-tab" data-category="Car">Economy</button>
              <button type="button" class="hero-tab active" data-category="Luxury">Luxury</button>
              <button type="button" class="hero-tab" data-category="SUV">SUVs</button>
            </div>

            <div class="hero-search-pill-v3" style="max-width:520px; margin:0;">
              <div class="hero-search-pill-field">
                <select name="brand">
                  <option value="">Select car</option>
                  <c:forEach var="v" items="${featuredVehicles}">
                    <option value="${v.brand}">${v.brand} ${v.model}</option>
                  </c:forEach>
                </select>
              </div>
              <div class="hero-search-pill-field">
                <input type="date" name="startDate" placeholder="Start date">
              </div>
              <div class="hero-search-pill-field">
                <input type="date" name="endDate" placeholder="End date">
              </div>
              <button type="submit" class="hero-search-pill-submit">Search</button>
            </div>
          </form>

          <div class="stats-strip p-3 mt-5" style="max-width:520px;">
            <div class="row text-center">
              <div class="col-4 col-divider">
                <div class="stat-num" data-count="500">0</div>
                <div class="stat-label">Vehicles</div>
              </div>
              <div class="col-4 col-divider">
                <div class="stat-num" data-count="15000">0</div>
                <div class="stat-label">Happy Customers</div>
              </div>
              <div class="col-4">
                <div class="stat-num"><span data-count="4.9" data-decimal="1">0</span>&#9733;</div>
                <div class="stat-label">Average Rating</div>
              </div>
            </div>
          </div>
        </div>

        <div class="col-lg-6">
          <div class="position-relative">
            <div class="hero-img-wrap" style="height:460px;">
              <img class="hero-image" src="https://images.unsplash.com/photo-1617531653332-bd46c24f2068?q=80&w=1200&auto=format&fit=crop" alt="Premium car ready for rent">
            </div>
            <div class="floating-card fc1">
              <div class="d-flex align-items-center gap-2">
                <div class="feature-icon" style="width:40px;height:40px;margin:0;border-radius:12px;font-size:1rem;"><i class="fa-solid fa-shield-halved"></i></div>
                <div>
                  <div style="font-family:var(--font-serif);font-weight:700;font-size:0.9rem;">Fully Insured</div>
                  <div class="text-soft" style="font-size:0.75rem;">Every trip protected</div>
                </div>
              </div>
            </div>
            <div class="floating-card fc2">
              <div class="stars mb-1"><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i></div>
              <div style="font-family:var(--font-serif);font-weight:800;font-size:1.1rem;">4.9<span class="text-soft" style="font-weight:600;font-size:0.75rem;"> / 5.0</span></div>
              <div class="text-soft" style="font-size:0.75rem;">from 8,200 rides</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- ============ ABOUT / TRUST ============ -->
  <section class="section-pad" style="background:var(--bg2);">
    <div class="container">
      <div class="row align-items-center g-4">
        <div class="col-lg-6">
          <div class="about-collage">
            <div class="about-collage-main" data-aos="zoom-in" data-aos-delay="0">
              <img src="https://images.unsplash.com/photo-1617531653332-bd46c24f2068?q=80&w=900&auto=format&fit=crop" alt="Premium car ready for rent">
            </div>
            <div class="about-collage-side">
              <div class="ag-tile" data-aos="zoom-in" data-aos-delay="75">
                <img src="https://images.unsplash.com/photo-1555215695-3004980ad54e?q=80&w=500&auto=format&fit=crop" alt="BMW front view">
              </div>
              <div class="ag-tile" data-aos="zoom-in" data-aos-delay="150">
                <img src="https://images.unsplash.com/photo-1502877338535-766e1452684a?q=80&w=500&auto=format&fit=crop" alt="Car in the forest">
              </div>
            </div>
            <div class="about-badge-overlap" data-aos="fade-up" data-aos-delay="300">
              <div class="about-badge-overlap-icon"><i class="fa-solid fa-trophy"></i></div>
              <div>
                <div class="num">20</div>
                <div class="label">Years of experience</div>
              </div>
            </div>
          </div>
        </div>
        <div class="col-lg-6" data-aos="fade-up" data-aos-delay="100">
          <div class="eyebrow mb-3">About Us</div>
          <h2 class="display-6 fw-bold mb-2">Driving your journey with <span class="gradient-text">reliability and care</span></h2>
          <p class="text-soft" style="max-width:520px;">At RENTORA, we're dedicated to making every drive smooth, secure, and memorable. With a focus on reliable vehicles and exceptional service, we go the extra mile to ensure that you feel confident and cared for at every step of your journey.</p>

          <div class="about-point">
            <div class="about-point-icon"><i class="fa-solid fa-car-side"></i></div>
            <div>
              <h6>Reliable, Well-Maintained Fleet</h6>
              <p>Every vehicle is thoroughly checked, individually cleaned, and ready for you to enjoy a smooth, comfortable ride every time.</p>
            </div>
          </div>
          <div class="about-point">
            <div class="about-point-icon"><i class="fa-solid fa-headset"></i></div>
            <div>
              <h6>24/7 Customer Support</h6>
              <p>Our dedicated support team is here around the clock to assist you with any needs, making sure your journey is seamless from start to finish.</p>
            </div>
          </div>

          <a href="#vehicles" class="btn btn-gradient mt-4">Learn More <i class="fa-solid fa-arrow-right ms-1"></i></a>
        </div>
      </div>
    </div>
  </section>
  <section class="why-us-section">
    <div class="container">
      <div class="row g-4 text-center">
        <div class="col-md-4" data-aos="fade-up">
          <i class="fa-solid fa-location-dot why-us-icon"></i>
          <h5 class="why-us-title">Availability</h5>
          <p class="why-us-text">A fleet ready across our locations, so the vehicle you want is there when you need it.</p>
        </div>
        <div class="col-md-4" data-aos="fade-up" data-aos-delay="100">
          <i class="fa-solid fa-car why-us-icon"></i>
          <h5 class="why-us-title">Comfort</h5>
          <p class="why-us-text">Every vehicle is inspected and cleaned between rentals, so you always ride in comfort.</p>
        </div>
        <div class="col-md-4" data-aos="fade-up" data-aos-delay="200">
          <i class="fa-solid fa-wallet why-us-icon"></i>
          <h5 class="why-us-title">Savings</h5>
          <p class="why-us-text">Transparent daily pricing with no hidden fees, plus loyalty discounts on wallet payments.</p>
        </div>
      </div>
    </div>
  </section>

  <!-- ============ EXPLORE OUR FLEET ============ -->
  <section class="section-pad" id="vehicles">
    <div class="container">
      <div class="row align-items-end mb-5">
        <div class="col-md-7" data-aos="fade-up">
          <div class="eyebrow mb-3">Explore Our Fleet</div>
          <h2 class="display-6 fw-bold">Vehicles picked for your <span class="gradient-text">next trip</span></h2>
        </div>
        <div class="col-md-5 text-md-end" data-aos="fade-up">
          <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-outline-glass mt-3 mt-md-0">View All Vehicles</a>
        </div>
      </div>

      <div class="row g-4">
        <c:choose>
          <c:when test="${empty featuredVehicles}">
            <div class="col-12">
              <div class="glass-card p-5 text-center" data-aos="fade-up">
                <i class="fa-solid fa-car-burst fa-2x mb-3" style="color: var(--accent);"></i>
                <h5>No vehicles available yet</h5>
                <p class="text-soft mb-0">Check back soon — new vehicles are added to our stock regularly.</p>
              </div>
            </div>
          </c:when>
          <c:otherwise>
            <c:forEach var="v" items="${featuredVehicles}" varStatus="loop">
              <div class="col-lg-4 col-md-6" data-aos="fade-up" data-aos-delay="${loop.index % 3 * 100}">
                <div class="vehicle-card">
                  <div class="vehicle-img" style="position:relative;">
                    <img src="${not empty v.imageUrl ? v.imageUrl : 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=800&auto=format&fit=crop'}" alt="${v.brand} ${v.model}">
                    <div class="vehicle-badge">${v.categoryName}</div>
                    <c:if test="${v.displayStatus != 'AVAILABLE'}">
                      <span class="badge badge-${v.displayStatus.toLowerCase()} text-uppercase" style="position:absolute; top:50px; left:14px;">${v.displayStatus}</span>
                    </c:if>
                    <c:if test="${sessionScope.user.roleName == 'RENTER'}">
                      <button class="vehicle-heart wishlist-toggle-btn" data-vehicle-id="${v.vehicleId}"
                              data-favorited="${favoritedIds.contains(v.vehicleId)}" style="border:none;">
                        <i class="fa-${favoritedIds.contains(v.vehicleId) ? 'solid' : 'regular'} fa-heart"
                           style="color:${favoritedIds.contains(v.vehicleId) ? '#EF4444' : ''};"></i>
                      </button>
                    </c:if>
                  </div>
                  <div class="p-4">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                      <h5 class="mb-0" style="font-family:var(--font-serif);font-weight:800;">${v.brand} ${v.model}</h5>
                      <div class="rating-stars">
                        <c:forEach begin="1" end="5" var="s">
                          <i class="fa-solid fa-star${s <= v.averageRating ? '' : ' dim'}"></i>
                        </c:forEach>
                      </div>
                    </div>
                    <div class="d-flex gap-2 mb-3 flex-wrap">
                      <span class="feature-pill"><i class="fa-solid fa-gas-pump me-1"></i>${v.fuelType}</span>
                      <span class="feature-pill"><i class="fa-solid fa-users me-1"></i>${v.seats} Seats</span>
                      <span class="feature-pill"><i class="fa-solid fa-gear me-1"></i>${v.transmission}</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <div class="price-tag">Rs. ${v.pricePerDay}/day</div>
                    </div>
                    <div class="d-flex gap-2">
                      <a href="${pageContext.request.contextPath}/vehicle-details?id=${v.vehicleId}" class="btn btn-outline-glass flex-fill" style="padding:9px 20px;font-size:0.82rem;">View</a>
                      <a href="${pageContext.request.contextPath}/vehicle-details?id=${v.vehicleId}" class="btn btn-gradient flex-fill" style="padding:9px 20px;font-size:0.82rem;">Book</a>
                    </div>
                  </div>
                </div>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </section>

  <!-- ============ CATEGORIES (slideshow) ============ -->
  <section class="section-pad" id="categories" style="background:var(--bg2);">
    <div class="container">
      <div class="text-center mb-5" data-aos="fade-up">
        <div class="eyebrow mb-3 justify-content-center">Browse by Category</div>
        <h2 class="display-6 fw-bold">Choose the <span class="gradient-text">car that suits you</span></h2>
      </div>
      <div class="cat-slider-wrap" data-aos="fade-up">
        <button type="button" class="cat-slider-arrow cat-slider-prev" aria-label="Previous categories">
          <i class="fa-solid fa-chevron-left"></i>
        </button>

        <div class="cat-slider" id="categorySlider">
          <a href="${pageContext.request.contextPath}/vehicles?category=SUV" class="cat-card cat-slide">
            <img src="https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=900&auto=format&fit=crop" alt="SUV">
            <div class="cat-content"><h5>SUVs</h5><span>Space for the journey</span></div>
          </a>
          <a href="${pageContext.request.contextPath}/vehicles?category=Luxury" class="cat-card cat-slide">
            <img src="https://images.unsplash.com/photo-1553440569-bcc63803a83d?q=80&w=900&auto=format&fit=crop" alt="Luxury car">
            <div class="cat-content"><h5>Luxury</h5><span>Arrive in style</span></div>
          </a>
          <a href="${pageContext.request.contextPath}/vehicles?category=Electric+Vehicle" class="cat-card cat-slide">
            <img src="https://images.unsplash.com/photo-1580273916550-e323be2ae537?q=80&w=900&auto=format&fit=crop" alt="Electric car">
            <div class="cat-content"><h5>Electric</h5><span>Zero-emission drives</span></div>
          </a>
          <a href="${pageContext.request.contextPath}/vehicles?category=Motorcycle" class="cat-card cat-slide">
            <img src="https://images.unsplash.com/photo-1558981806-ec527fa84c39?q=80&w=900&auto=format&fit=crop" alt="Motorcycle">
            <div class="cat-content"><h5>Motorcycles</h5><span>Light &amp; quick</span></div>
          </a>
          <a href="${pageContext.request.contextPath}/vehicles?category=Van" class="cat-card cat-slide">
            <img src="https://images.unsplash.com/photo-1571068316344-75bc76f77890?q=80&w=900&auto=format&fit=crop" alt="Van">
            <div class="cat-content"><h5>Vans</h5><span>Built for cargo</span></div>
          </a>
          <a href="${pageContext.request.contextPath}/vehicles?category=Car" class="cat-card cat-slide">
            <img src="https://images.unsplash.com/photo-1503736334956-4c8f8e92946d?q=80&w=900&auto=format&fit=crop" alt="Sedan">
            <div class="cat-content"><h5>Sedans</h5><span>Everyday comfort rides</span></div>
          </a>
        </div>

        <button type="button" class="cat-slider-arrow cat-slider-next" aria-label="Next categories">
          <i class="fa-solid fa-chevron-right"></i>
        </button>
      </div>
    </div>
  </section>

  <!-- ============ HOW IT WORKS ============ -->
  <section class="section-pad">
    <div class="container">
      <div class="text-center mb-5" data-aos="fade-up">
        <div class="eyebrow mb-3 justify-content-center">Simple Process</div>
        <h2 class="display-6 fw-bold">How <span class="gradient-text">RENTORA</span> works</h2>
      </div>
      <div class="row align-items-start text-center">
        <div class="col-md-3 d-flex flex-column align-items-center" data-aos="fade-up">
          <div class="step-num">01</div>
          <h6 class="mt-3" style="font-family:var(--font-serif);font-weight:800;">Search</h6>
          <p class="text-soft" style="font-size:0.88rem;">Tell us your dates, location and the kind of ride you need.</p>
        </div>
        <div class="col-md-2 d-none d-md-block"><div class="step-line"></div></div>
        <div class="col-md-3 d-flex flex-column align-items-center" data-aos="fade-up" data-aos-delay="100">
          <div class="step-num">02</div>
          <h6 class="mt-3" style="font-family:var(--font-serif);font-weight:800;">Book</h6>
          <p class="text-soft" style="font-size:0.88rem;">Compare vehicles, pick your favorite and confirm instantly.</p>
        </div>
        <div class="col-md-2 d-none d-md-block"><div class="step-line"></div></div>
        <div class="col-md-2 d-flex flex-column align-items-center" data-aos="fade-up" data-aos-delay="200">
          <div class="step-num">03</div>
          <h6 class="mt-3" style="font-family:var(--font-serif);font-weight:800;">Drive</h6>
          <p class="text-soft" style="font-size:0.88rem;">Pick up the keys and hit the road on your terms.</p>
        </div>
      </div>
    </div>
  </section>

  <!-- ============ REVIEWS ============ -->
  <section class="section-pad">
    <div class="container">
      <div class="text-center mb-5" data-aos="fade-up">
        <div class="eyebrow mb-3 justify-content-center">Testimonials</div>
        <h2 class="display-6 fw-bold">Loved by <span class="gradient-text">thousands</span> of drivers</h2>
      </div>
      <div class="row g-4">
        <div class="col-lg-4" data-aos="fade-up">
          <div class="review-card">
            <div class="stars mb-3"><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i></div>
            <p class="text-soft" style="font-size:0.94rem;">"Booking took less than three minutes and the SUV was spotless. Rentora is now my only choice for road trips."</p>
            <div class="d-flex align-items-center gap-3 mt-4">
              <img src="https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=150&auto=format&fit=crop" class="review-avatar">
              <div><strong style="font-family:var(--font-serif);font-size:0.9rem;">Amaya Fernando</strong><div class="text-soft" style="font-size:0.78rem;">Colombo, Sri Lanka</div></div>
            </div>
          </div>
        </div>
        <div class="col-lg-4" data-aos="fade-up" data-aos-delay="100">
          <div class="review-card">
            <div class="stars mb-3"><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i></div>
            <p class="text-soft" style="font-size:0.94rem;">"Great selection of well-maintained vehicles and the support team replies fast. My go-to rental shop now."</p>
            <div class="d-flex align-items-center gap-3 mt-4">
              <img src="https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=150&auto=format&fit=crop" class="review-avatar">
              <div><strong style="font-family:var(--font-serif);font-size:0.9rem;">Dilshan Perera</strong><div class="text-soft" style="font-size:0.78rem;">Negombo, Sri Lanka</div></div>
            </div>
          </div>
        </div>
        <div class="col-lg-4" data-aos="fade-up" data-aos-delay="200">
          <div class="review-card">
            <div class="stars mb-3"><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-regular fa-star"></i></div>
            <p class="text-soft" style="font-size:0.94rem;">"Support answered a late-night question in minutes. The whole experience felt premium from start to finish."</p>
            <div class="d-flex align-items-center gap-3 mt-4">
              <img src="https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?q=80&w=150&auto=format&fit=crop" class="review-avatar">
              <div><strong style="font-family:var(--font-serif);font-size:0.9rem;">Nadeesha Silva</strong><div class="text-soft" style="font-size:0.78rem;">Kandy, Sri Lanka</div></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- ============ TRUSTED BRANDS ============ -->
  <section class="brands-strip-section">
    <div class="container">
      <div class="brands-strip" data-aos="fade-up">
        <span class="brand-logo-item">
          <img src="${pageContext.request.contextPath}/assets/images/brands/toyota.png" alt="Toyota" onerror="this.style.display='none'; this.nextElementSibling.style.display='inline';">
          <span class="brand-logo-fallback" style="display:none;">Toyota</span>
        </span>
        <span class="brand-logo-item">
          <img src="${pageContext.request.contextPath}/assets/images/brands/ford.png" alt="Ford" onerror="this.style.display='none'; this.nextElementSibling.style.display='inline';">
          <span class="brand-logo-fallback" style="display:none;">Ford</span>
        </span>
        <span class="brand-logo-item">
          <img src="${pageContext.request.contextPath}/assets/images/brands/mercedes.png" alt="Mercedes-Benz" onerror="this.style.display='none'; this.nextElementSibling.style.display='inline';">
          <span class="brand-logo-fallback" style="display:none;">Mercedes</span>
        </span>
        <span class="brand-logo-item">
          <img src="${pageContext.request.contextPath}/assets/images/brands/jeep.png" alt="Jeep" onerror="this.style.display='none'; this.nextElementSibling.style.display='inline';">
          <span class="brand-logo-fallback" style="display:none;">Jeep</span>
        </span>
        <span class="brand-logo-item">
          <img src="${pageContext.request.contextPath}/assets/images/brands/bmw.png" alt="BMW" onerror="this.style.display='none'; this.nextElementSibling.style.display='inline';">
          <span class="brand-logo-fallback" style="display:none;">BMW</span>
        </span>
        <span class="brand-logo-item">
          <img src="${pageContext.request.contextPath}/assets/images/brands/audi.png" alt="Audi" onerror="this.style.display='none'; this.nextElementSibling.style.display='inline';">
          <span class="brand-logo-fallback" style="display:none;">Audi</span>
        </span>
      </div>
    </div>
  </section>

  <!-- ============ FAQ ============ -->
  <section class="section-pad">
    <div class="container">
      <div class="row">
        <div class="col-lg-4 mb-4 mb-lg-0" data-aos="fade-up">
          <div class="eyebrow mb-3">Got Questions?</div>
          <h2 class="display-6 fw-bold mb-3">Frequently asked <span class="gradient-text">questions</span></h2>
          <p class="text-soft">Can't find what you're looking for? Reach out to our support team any time.</p>
          <a href="#contact" class="btn btn-outline-glass">Contact Support</a>
        </div>
        <div class="col-lg-8" data-aos="fade-up" data-aos-delay="100">
          <div class="accordion accordion-rentora" id="faqAccordion">
            <div class="accordion-item">
              <h2 class="accordion-header">
                <button class="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#faq1">What documents do I need to rent a vehicle?</button>
              </h2>
              <div id="faq1" class="accordion-collapse collapse show" data-bs-parent="#faqAccordion">
                <div class="accordion-body text-soft">A valid driver's license, a government-issued ID, and a payment method on file. Some vehicle categories may require a minimum driving history.</div>
              </div>
            </div>
            <div class="accordion-item">
              <h2 class="accordion-header">
                <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#faq2">How do you keep the fleet in good condition?</button>
              </h2>
              <div id="faq2" class="accordion-collapse collapse" data-bs-parent="#faqAccordion">
                <div class="accordion-body text-soft">Every vehicle is inspected by our team after each rental. Any vehicle that needs repairs is taken off the site until it's back in top condition.</div>
              </div>
            </div>
            <div class="accordion-item">
              <h2 class="accordion-header">
                <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#faq3">Can I cancel or modify a booking?</button>
              </h2>
              <div id="faq3" class="accordion-collapse collapse" data-bs-parent="#faqAccordion">
                <div class="accordion-body text-soft">Yes — you can cancel a booking from your dashboard. Modifying dates currently requires cancelling and rebooking.</div>
              </div>
            </div>
            <div class="accordion-item">
              <h2 class="accordion-header">
                <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#faq4">What happens when I return the vehicle?</button>
              </h2>
              <div id="faq4" class="accordion-collapse collapse" data-bs-parent="#faqAccordion">
                <div class="accordion-body text-soft">Our team checks the vehicle over. If everything's fine, your booking is marked complete right away. If damage is found and it's your responsibility, we'll send you the repair cost to settle within 24 hours.</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- ============ CTA ============ -->
  <section class="pb-5">
    <div class="container">
      <div class="cta-banner text-center" data-aos="fade-up">
        <div class="position-relative" style="z-index:2;">
          <h2 class="display-5 fw-bold mb-3" style="color:#fff;">Ready to hit the road?</h2>
          <p class="mb-4" style="opacity:0.9;max-width:520px;margin:0 auto;">Join thousands of drivers already moving with RENTORA.</p>
          <div class="d-flex justify-content-center gap-3 flex-wrap">
            <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-cta">Rent a Vehicle</a>
            <a href="${pageContext.request.contextPath}/register" class="btn btn-outline-glass" style="border-color:rgba(255,255,255,0.5); color:#fff; background:rgba(255,255,255,0.1);">Create an Account</a>
          </div>
        </div>
      </div>
    </div>
  </section>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
