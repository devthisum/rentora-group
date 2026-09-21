<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <div class="auth-page">
    <div class="auth-left" data-aos="fade-right">
      <img class="auth-bg-img" src="https://images.unsplash.com/photo-1633265486064-086b219458ec?q=80&w=1200&auto=format&fit=crop" alt="">
      <div class="auth-left-content">
        <div class="eyebrow mb-3" style="background:rgba(255,255,255,.12); color:#fff;" data-aos="fade-up" data-aos-delay="100">Account Recovery</div>
        <h2 data-aos="fade-up" data-aos-delay="200">Forgot your<br>password?</h2>
        <p data-aos="fade-up" data-aos-delay="300">No problem — we'll email you a one-time code to get back in.</p>
      </div>
    </div>

    <div class="auth-right">
      <div class="auth-right-inner" data-aos="fade-left">
        <a href="${pageContext.request.contextPath}/" class="navbar-brand mb-4 d-inline-flex"><span class="dot"></span><span class="brand-text">RENTORA</span></a>

        <h3 class="mb-1 fw-bold" style="font-family:var(--font-serif);">Reset Your Password</h3>
        <p class="text-soft mb-4">Enter the email on your account and we'll send you a 6-digit code.</p>

        <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

        <form method="post" action="${pageContext.request.contextPath}/forgot-password">
          <div class="mb-4">
            <label class="form-label small">Email Address</label>
            <input type="email" name="email" class="form-control form-control-glass" required autofocus>
          </div>
          <button type="submit" class="btn btn-gradient w-100 py-2">Send Reset Code</button>
        </form>

        <p class="text-center text-soft mt-4 mb-0">
          Remembered your password?
          <a href="${pageContext.request.contextPath}/login" style="color:var(--accent); font-weight:600;">Back to Log In</a>
        </p>
      </div>
    </div>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/aos/2.3.4/aos.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>
