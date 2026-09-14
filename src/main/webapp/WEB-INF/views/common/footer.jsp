<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:if test="${sessionScope.user.roleName != 'ADMIN' && sessionScope.user.roleName != 'MAINTENANCE' && sessionScope.user.roleName != 'BOOKING'}">
<footer class="footer-glass pb-4">
  <div class="container">
    <div class="row g-4 pb-4">
      <div class="col-lg-4">
        <a class="navbar-brand mb-3 d-inline-flex" href="${pageContext.request.contextPath}/" style="color:#fff !important;">
          <span class="dot"></span>RENTORA
        </a>
        <p style="font-size:0.9rem;max-width:320px;">Vehicle rentals for every journey — browse our stock and book your ride in minutes.</p>
        <div class="d-flex gap-2 mt-3">
          <a href="#" class="social-icon"><i class="fa-brands fa-facebook-f"></i></a>
          <a href="#" class="social-icon"><i class="fa-brands fa-instagram"></i></a>
          <a href="#" class="social-icon"><i class="fa-brands fa-x-twitter"></i></a>
          <a href="#" class="social-icon"><i class="fa-brands fa-linkedin-in"></i></a>
        </div>
      </div>
      <div class="col-6 col-lg-2">
        <h6>Quick Links</h6>
        <div class="d-flex flex-column">
          <a href="${pageContext.request.contextPath}/">Home</a>
          <a href="${pageContext.request.contextPath}/vehicles">Vehicles</a>
          <a href="${pageContext.request.contextPath}/announcements">Announcements</a>
          <a href="${pageContext.request.contextPath}/login">Login</a>
        </div>
      </div>
      <div class="col-6 col-lg-2">
        <h6>Company</h6>
        <div class="d-flex flex-column">
          <a href="#">Careers</a>
          <a href="#">Blog</a>
          <a href="#">Partners</a>
          <a href="#">Terms</a>
        </div>
      </div>
      <div class="col-lg-4">
        <h6>Stay in the loop</h6>
        <p style="font-size:0.88rem;">Get new vehicle drops and exclusive offers straight to your inbox.</p>
        <div class="d-flex mb-3">
          <input type="email" class="form-control newsletter-input" placeholder="Your email">
          <button class="btn btn-gradient" style="border-radius:0 100px 100px 0;">Join</button>
        </div>
        <div style="font-size:0.85rem;"><i class="fa-solid fa-location-dot me-2" style="color:var(--accent);"></i>Negombo, Western Province, Sri Lanka</div>
        <div style="font-size:0.85rem;" class="mt-2"><i class="fa-solid fa-phone me-2" style="color:var(--accent);"></i>+94 76 123 4567</div>
        <div style="font-size:0.85rem;" class="mt-2"><i class="fa-solid fa-clock me-2" style="color:var(--accent);"></i>Mon–Sun, 7:00 AM – 10:00 PM</div>
      </div>
    </div>
    <div class="footer-bottom d-flex flex-wrap justify-content-between">
      <span>&copy; 2026 RENTORA. All rights reserved.</span>
      <span>Designed &amp; built for drivers everywhere.</span>
    </div>
  </div>
</footer>

<a href="https://wa.me/94761234567" target="_blank" class="whatsapp-float"><i class="fa-brands fa-whatsapp"></i></a>
<button type="button" class="back-to-top" id="backToTop"><i class="fa-solid fa-arrow-up"></i></button>
</c:if>

<!-- Bootstrap Bundle (incl. Popper) -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<!-- GSAP -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/gsap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/ScrollTrigger.min.js"></script>
<!-- AOS -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/aos/2.3.4/aos.js"></script>
<!-- Chart.js -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>

<!-- Rentora scripts -->
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
