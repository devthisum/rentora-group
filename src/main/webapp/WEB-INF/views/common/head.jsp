<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>RENTORA — Drive Your Journey</title>

<!-- Fonts: Playfair Display (display/serif) + Outfit (body/sans) -->
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,700;1,400&family=Outfit:wght@300;400;500;600;700&display=swap" rel="stylesheet">

<!-- Bootstrap 5 (grid + JS components: dropdown, modal, accordion) -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<!-- Font Awesome -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
<!-- AOS -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/aos/2.3.4/aos.css">

<!-- Rentora theme -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">

<script>
  window.CONTEXT_PATH = '${pageContext.request.contextPath}';
  // Apply saved dark-mode preference BEFORE first paint, so there's no light-mode flash.
  (function () {
    if (window.localStorage.getItem('rentora_theme') === 'dark') {
      document.documentElement.setAttribute('data-theme', 'dark');
    }
  })();
</script>

<div id="loading-screen">
  <div class="text-center">
    <i class="fa-solid fa-car-side loader-car"></i>
    <div class="mt-3 eyebrow" style="justify-content:center;">Loading Rentora</div>
  </div>
</div>
