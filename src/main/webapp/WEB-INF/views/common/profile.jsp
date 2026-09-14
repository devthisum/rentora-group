<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem; max-width: 640px;">
    <h2 class="fw-bold mb-4" data-aos="fade-up">My Profile</h2>

    <c:if test="${not empty sessionScope.successMessage}">
      <div class="alert alert-success">${sessionScope.successMessage}</div>
      <c:remove var="successMessage" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
      <div class="alert alert-danger">${sessionScope.errorMessage}</div>
      <c:remove var="errorMessage" scope="session" />
    </c:if>

    <!-- ============ AVATAR + DETAILS ============ -->
    <div class="glass-card p-4 mb-4" data-aos="fade-up">
      <form method="post" action="${pageContext.request.contextPath}/profile" enctype="multipart/form-data">
        <input type="hidden" name="formType" value="details">

        <div class="d-flex align-items-center gap-4 mb-4">
          <div style="position:relative;">
            <c:choose>
              <c:when test="${not empty sessionScope.user.profileImage}">
                <img id="avatarPreview" src="${sessionScope.user.profileImage}" alt=""
                     style="width:96px;height:96px;border-radius:50%;object-fit:cover;border:2px solid var(--accent);">
              </c:when>
              <c:otherwise>
                <div id="avatarInitials" style="width:96px;height:96px;border-radius:50%;background:var(--accent);
                     color:#fff;display:flex;align-items:center;justify-content:center;font-size:2rem;font-weight:700;">
                  ${fn:substring(sessionScope.user.fullName, 0, 1)}
                </div>
                <img id="avatarPreview" src="" alt="" style="display:none;width:96px;height:96px;border-radius:50%;object-fit:cover;border:2px solid var(--accent);">
              </c:otherwise>
            </c:choose>
          </div>
          <div>
            <label class="btn btn-sm btn-outline-glass mb-1" for="profileImageFile">Change Photo</label>
            <input type="file" name="profileImageFile" id="profileImageFile" accept="image/*" class="d-none">
            <p class="text-soft small mb-0">${sessionScope.user.roleName}</p>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label small">Full Name</label>
          <input type="text" name="fullName" class="form-control form-control-glass" value="${sessionScope.user.fullName}" required>
        </div>
        <div class="mb-3">
          <label class="form-label small">Email</label>
          <input type="email" class="form-control form-control-glass" value="${sessionScope.user.email}" disabled>
          <small class="text-soft">Email can't be changed.</small>
        </div>
        <div class="mb-3">
          <label class="form-label small">Phone</label>
          <input type="text" name="phone" class="form-control form-control-glass" value="${sessionScope.user.phone}" required>
        </div>

        <button type="submit" class="btn btn-gradient w-100 py-2">Save Profile</button>
      </form>
    </div>

    <!-- ============ PASSWORD ============ -->
    <div class="glass-card p-4" data-aos="fade-up">
      <h5 class="fw-bold mb-3">Change Password</h5>
      <form method="post" action="${pageContext.request.contextPath}/profile">
        <input type="hidden" name="formType" value="password">
        <div class="mb-3">
          <label class="form-label small">Current Password</label>
          <input type="password" name="currentPassword" class="form-control form-control-glass" required>
        </div>
        <div class="mb-3">
          <label class="form-label small">New Password</label>
          <input type="password" name="newPassword" class="form-control form-control-glass" required>
          <small class="text-soft">Min 8 chars, upper, lower, digit, symbol</small>
        </div>
        <button type="submit" class="btn btn-outline-glass w-100 py-2">Update Password</button>
      </form>
    </div>
  </section>

  <script>
    const fileInput = document.getElementById('profileImageFile');
    const preview = document.getElementById('avatarPreview');
    const initials = document.getElementById('avatarInitials');
    fileInput.addEventListener('change', () => {
      const file = fileInput.files[0];
      if (!file) return;
      preview.src = URL.createObjectURL(file);
      preview.style.display = 'block';
      if (initials) initials.style.display = 'none';
    });
  </script>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
