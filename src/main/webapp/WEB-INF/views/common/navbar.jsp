<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<nav class="navbar navbar-expand-lg navbar-glass" id="mainNav">
  <div class="container">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/">
      <span class="dot"></span>RENTORA
    </a>

    <button class="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#navMain">
      <i class="fa-solid fa-bars fs-4" style="color:var(--accent)"></i>
    </button>

    <div class="collapse navbar-collapse" id="navMain">
      <ul class="navbar-nav mx-auto align-items-lg-center">
        <c:if test="${sessionScope.user.roleName != 'ADMIN'}">
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/">Home</a></li>
        </c:if>

        <c:if test="${sessionScope.user.roleName != 'ADMIN' && sessionScope.user.roleName != 'MAINTENANCE' && sessionScope.user.roleName != 'BOOKING'}">
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/vehicles">Vehicles</a></li>
        </c:if>

        <c:choose>
          <c:when test="${not empty sessionScope.user}">
            <c:if test="${sessionScope.user.roleName == 'RENTER'}">
              <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/renter/dashboard">My Dashboard</a></li>
              <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/renter/wishlist"><i class="fa-solid fa-heart me-1"></i>Wishlist</a></li>
              <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/announcements"><i class="fa-solid fa-bullhorn me-1"></i>Announcements</a></li>
            </c:if>
            <%-- ADMIN / MAINTENANCE / BOOKING nav links live in the sidebar now — see admin-sidebar.jsp below --%>
          </c:when>
          <c:otherwise>
          </c:otherwise>
        </c:choose>
      </ul>

      <div class="d-flex gap-2 nav-actions mt-3 mt-lg-0 align-items-center">
        <c:if test="${not empty sessionScope.user}">
          <div class="dropdown profile-dropdown">
            <a href="#" class="d-inline-flex align-items-center" id="profileMenuBtn" role="button"
               data-bs-toggle="dropdown" aria-expanded="false" title="Account">
              <c:choose>
                <c:when test="${not empty sessionScope.user.profileImage}">
                  <img src="${sessionScope.user.profileImage}" alt=""
                       style="width:34px;height:34px;border-radius:50%;object-fit:cover;border:2px solid var(--accent);">
                </c:when>
                <c:otherwise>
                  <span style="width:34px;height:34px;border-radius:50%;background:var(--accent);color:#fff;
                        display:inline-flex;align-items:center;justify-content:center;font-weight:700;font-size:.9rem;">
                    ${fn:substring(sessionScope.user.fullName, 0, 1)}
                  </span>
                </c:otherwise>
              </c:choose>
            </a>
            <ul class="dropdown-menu dropdown-menu-end profile-dropdown-menu" aria-labelledby="profileMenuBtn">
              <li class="profile-dropdown-header">
                <div class="fw-semibold" style="color:var(--ink);">${sessionScope.user.fullName}</div>
                <div style="color:var(--ink4);font-size:.78rem;">${sessionScope.user.email}</div>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile"><i class="fa-solid fa-user me-2"></i>My Profile</a></li>
              <c:if test="${sessionScope.user.roleName == 'RENTER'}">
                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/renter/payment-methods"><i class="fa-solid fa-credit-card me-2"></i>Payment Methods</a></li>
                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/renter/inquiries"><i class="fa-solid fa-message me-2"></i>Inquiries</a></li>
              </c:if>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item dropdown-item-danger" href="${pageContext.request.contextPath}/logout"><i class="fa-solid fa-right-from-bracket me-2"></i>Logout</a></li>
            </ul>
          </div>
        </c:if>
        <button type="button" class="theme-switch" id="themeToggleBtn" title="Toggle dark mode">
          <span class="theme-switch-thumb" id="themeToggleThumb">🌙</span>
        </button>
        <c:if test="${empty sessionScope.user}">
          <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-glass">Login</a>
          <a href="${pageContext.request.contextPath}/register" class="btn btn-gradient">Register</a>
        </c:if>
      </div>
    </div>
  </div>
</nav>

<jsp:include page="/WEB-INF/views/common/admin-sidebar.jsp" />
