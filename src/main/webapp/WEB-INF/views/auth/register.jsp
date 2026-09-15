<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <div class="auth-page">
    <div class="auth-left" data-aos="fade-right">
      <img class="auth-bg-img" src="https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=1200&auto=format&fit=crop" alt="">
      <div class="auth-left-content">
        <div class="eyebrow mb-3" style="background:rgba(255,255,255,.12); color:#fff;" data-aos="fade-up" data-aos-delay="100">Join Rentora</div>
        <h2 data-aos="fade-up" data-aos-delay="200">Rent a ride,<br>your way.</h2>
        <p data-aos="fade-up" data-aos-delay="300">Browse our vehicle stock and book in minutes.</p>
      </div>
    </div>

    <div class="auth-right">
      <div class="auth-right-inner" data-aos="fade-left">
        <a href="${pageContext.request.contextPath}/" class="navbar-brand mb-4 d-inline-flex"><span class="dot"></span><span class="brand-text">RENTORA</span></a>

        <h3 class="mb-1 fw-bold" style="font-family:var(--font-serif);">Create Your Account</h3>
        <p class="text-soft mb-4">Join Rentora to browse and book vehicles</p>

        <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

        <form method="post" action="${pageContext.request.contextPath}/register" id="registerForm">
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label small">First Name</label>
              <input type="text" name="firstName" value="${firstName}" class="form-control form-control-glass" required>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label small">Last Name</label>
              <input type="text" name="lastName" value="${lastName}" class="form-control form-control-glass" required>
            </div>
          </div>
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label small">Email Address</label>
              <input type="email" name="email" value="${email}" class="form-control form-control-glass" required>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label small">Phone Number</label>
              <input type="text" name="phone" value="${phone}" placeholder="0771234567" class="form-control form-control-glass" required>
            </div>
          </div>

          <div class="mb-1 mt-3">
            <label class="form-label small">Password</label>
            <input type="password" id="password" name="password" class="form-control form-control-glass" required>
            <small id="password-hint" class="text-soft">Min 8 chars, upper, lower, digit, symbol</small>
          </div>

          <button type="submit" class="btn btn-gradient w-100 py-2 mt-4">Create Account</button>
        </form>

        <p class="text-center text-soft mt-4 mb-0">
          Already have an account?
          <a href="${pageContext.request.contextPath}/login" style="color:var(--accent); font-weight:600;">Log in</a>
        </p>
      </div>
    </div>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/aos/2.3.4/aos.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>
