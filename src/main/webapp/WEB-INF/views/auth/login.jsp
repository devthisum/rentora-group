<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <div class="auth-page">
    <div class="auth-left" data-aos="fade-right">
      <img class="auth-bg-img" src="https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=1200&auto=format&fit=crop" alt="">
      <div class="auth-left-content">
        <div class="eyebrow mb-3" style="background:rgba(255,255,255,.12); color:#fff;" data-aos="fade-up" data-aos-delay="100">Welcome Back</div>
        <h2 data-aos="fade-up" data-aos-delay="200">Your next journey<br>is one login away.</h2>
        <p data-aos="fade-up" data-aos-delay="300">Pick up where you left off — bookings, saved vehicles, and conversations all in one place.</p>
      </div>
    </div>

    <div class="auth-right">
      <div class="auth-right-inner" data-aos="fade-left">
        <a href="${pageContext.request.contextPath}/" class="navbar-brand mb-4 d-inline-flex"><span class="dot"></span>RENTORA</a>

        <h3 class="mb-1 fw-bold" style="font-family:var(--font-serif);">Welcome Back</h3>
        <p class="text-soft mb-4">Log in to your Rentora account</p>

        <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
        <c:if test="${not empty successMessage}"><div class="alert alert-success">${successMessage}</div></c:if>

        <form method="post" action="${pageContext.request.contextPath}/login">
          <div class="mb-3">
            <label class="form-label small">Email Address</label>
            <input type="email" name="email" class="form-control form-control-glass" required>
          </div>
          <div class="mb-4">
            <label class="form-label small">Password</label>
            <input type="password" name="password" class="form-control form-control-glass" required>
          </div>
          <button type="submit" class="btn btn-gradient w-100 py-2">Log In</button>
        </form>

        <p class="text-center text-soft mt-4 mb-0">
          Don't have an account?
          <a href="${pageContext.request.contextPath}/register" style="color:var(--accent); font-weight:600;">Sign up</a>
        </p>
      </div>
    </div>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/aos/2.3.4/aos.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>
