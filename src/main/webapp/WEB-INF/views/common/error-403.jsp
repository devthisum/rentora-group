<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html><head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<section class="container text-center" style="padding-top:10rem;padding-bottom:6rem;">
  <h1 class="hero-title"><span class="highlight">403</span></h1>
  <p class="text-secondary">You don't have permission to access this page.</p>
  <a href="${pageContext.request.contextPath}/" class="btn btn-gradient mt-3">Back to Home</a>
</section>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body></html>
