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
        <div class="eyebrow mb-3" style="background:rgba(255,255,255,.12); color:#fff;" data-aos="fade-up" data-aos-delay="100">Check Your Inbox</div>
        <h2 data-aos="fade-up" data-aos-delay="200">Enter your<br>reset code.</h2>
        <p data-aos="fade-up" data-aos-delay="300">We just emailed a 6-digit code to your address. It expires in 10 minutes.</p>
      </div>
    </div>

    <div class="auth-right">
      <div class="auth-right-inner" data-aos="fade-left">
        <a href="${pageContext.request.contextPath}/" class="navbar-brand mb-4 d-inline-flex"><span class="dot"></span><span class="brand-text">RENTORA</span></a>

        <h3 class="mb-1 fw-bold" style="font-family:var(--font-serif);">Enter Code & New Password</h3>
        <p class="text-soft mb-4">Check <strong>${email}</strong> for your code.</p>

        <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

        <form method="post" action="${pageContext.request.contextPath}/reset-password">
          <input type="hidden" name="email" value="${email}">

          <div class="mb-3">
            <label class="form-label small">6-Digit Code</label>
            <input type="text" name="otpCode" class="form-control form-control-glass" maxlength="6" pattern="[0-9]{6}"
                   inputmode="numeric" placeholder="000000" required autofocus
                   style="letter-spacing:0.4em; font-weight:700; text-align:center;">
          </div>
          <div class="mb-3">
            <label class="form-label small">New Password</label>
            <input type="password" name="newPassword" class="form-control form-control-glass" required>
            <div class="form-text">8+ characters, with an uppercase letter, lowercase letter, digit, and special character.</div>
          </div>
          <div class="mb-4">
            <label class="form-label small">Confirm New Password</label>
            <input type="password" name="confirmPassword" class="form-control form-control-glass" required>
          </div>
          <button type="submit" class="btn btn-gradient w-100 py-2">Reset Password</button>
        </form>

        <p class="text-center text-soft mt-4 mb-0">
          Didn't get a code?
          <a href="${pageContext.request.contextPath}/forgot-password" style="color:var(--accent); font-weight:600;">Send it again</a>
        </p>
      </div>
    </div>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/aos/2.3.4/aos.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>
