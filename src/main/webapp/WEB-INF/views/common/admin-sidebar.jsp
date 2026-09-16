<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<c:if test="${sessionScope.user.roleName == 'ADMIN' || sessionScope.user.roleName == 'MAINTENANCE' || sessionScope.user.roleName == 'BOOKING'}">
  <button type="button" class="admin-sidebar-toggle" id="adminSidebarToggle" aria-label="Toggle menu">
    <i class="fa-solid fa-bars"></i>
  </button>
  <div class="admin-sidebar-backdrop" id="adminSidebarBackdrop"></div>

  <aside class="admin-sidebar" id="adminSidebar">
    <div class="admin-sidebar-inner">

      <c:if test="${sessionScope.user.roleName == 'ADMIN'}">
        <div class="admin-sidebar-label">Admin</div>
        <a href="${pageContext.request.contextPath}/admin/dashboard"
           class="admin-sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/dashboard') ? 'active' : ''}">
          <i class="fa-solid fa-gauge"></i><span>Admin Dashboard</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/vehicles"
           class="admin-sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/vehicles') ? 'active' : ''}">
          <i class="fa-solid fa-car"></i><span>Vehicles</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/promotions"
           class="admin-sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/promotions') ? 'active' : ''}">
          <i class="fa-solid fa-tag"></i><span>Promotions</span>
        </a>
        <a href="${pageContext.request.contextPath}/maintenance/dashboard"
           class="admin-sidebar-link ${fn:contains(pageContext.request.requestURI, '/maintenance/') ? 'active' : ''}">
          <i class="fa-solid fa-screwdriver-wrench"></i><span>Maintenance</span>
        </a>
        <a href="${pageContext.request.contextPath}/booking/dashboard"
           class="admin-sidebar-link ${fn:contains(pageContext.request.requestURI, '/booking/') ? 'active' : ''}">
          <i class="fa-solid fa-key"></i><span>Booking Desk</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/inquiries"
           class="admin-sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/inquiries') ? 'active' : ''}">
          <i class="fa-solid fa-message"></i><span>Inquiries</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/staff"
           class="admin-sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/staff') ? 'active' : ''}">
          <i class="fa-solid fa-user-gear"></i><span>Staff</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/announcements"
           class="admin-sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/announcements') ? 'active' : ''}">
          <i class="fa-solid fa-bullhorn"></i><span>Announcements</span>
        </a>
      </c:if>

      <c:if test="${sessionScope.user.roleName == 'MAINTENANCE'}">
        <div class="admin-sidebar-label">Maintenance</div>
        <a href="${pageContext.request.contextPath}/maintenance/dashboard" class="admin-sidebar-link active">
          <i class="fa-solid fa-screwdriver-wrench"></i><span>Maintenance Dashboard</span>
        </a>
      </c:if>

      <c:if test="${sessionScope.user.roleName == 'BOOKING'}">
        <div class="admin-sidebar-label">Booking Staff</div>
        <a href="${pageContext.request.contextPath}/booking/dashboard"
           class="admin-sidebar-link ${fn:contains(pageContext.request.requestURI, '/booking/dashboard') ? 'active' : ''}">
          <i class="fa-solid fa-key"></i><span>Booking Desk</span>
        </a>
        <a href="${pageContext.request.contextPath}/booking/promotions"
           class="admin-sidebar-link ${fn:contains(pageContext.request.requestURI, '/booking/promotions') ? 'active' : ''}">
          <i class="fa-solid fa-tag"></i><span>Promotions</span>
        </a>
      </c:if>

    </div>
  </aside>
</c:if>
