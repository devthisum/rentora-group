<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem; max-width: 760px;">
    <h2 class="fw-bold mb-4" data-aos="fade-up">Edit Vehicle</h2>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <c:if test="${not empty vehicle}">
      <div class="glass-card p-4" data-aos="fade-up">
        <form method="post" action="${not empty formAction ? formAction : pageContext.request.contextPath.concat('/admin/vehicles/edit')}" enctype="multipart/form-data">
          <input type="hidden" name="vehicleId" value="${vehicle.vehicleId}">

          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label small">Vehicle Number</label>
              <input type="text" class="form-control form-control-glass" value="${vehicle.vehicleNumber}" disabled>
              <small class="text-soft">Vehicle number can't be changed once registered.</small>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label small">Category</label>
              <input type="text" class="form-control form-control-glass" value="${vehicle.categoryName}" disabled>
            </div>
          </div>

          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label small">Brand</label>
              <input type="text" id="brandInput" name="brand" list="brandList" class="form-control form-control-glass" value="${vehicle.brand}" required>
              <datalist id="brandList"></datalist>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label small">Model</label>
              <input type="text" name="model" list="modelList" class="form-control form-control-glass" value="${vehicle.model}" required>
              <datalist id="modelList"></datalist>
            </div>
          </div>

          <div class="row">
            <div class="col-md-3 mb-3">
              <label class="form-label small">Year</label>
              <input type="number" name="year" class="form-control form-control-glass" value="${vehicle.year}" min="1990" max="2100" required>
            </div>
            <div class="col-md-3 mb-3">
              <label class="form-label small">Seats</label>
              <input type="number" name="seats" class="form-control form-control-glass" value="${vehicle.seats}" min="1">
            </div>
            <div class="col-md-3 mb-3">
              <label class="form-label small">Transmission</label>
              <select name="transmission" class="form-select form-control-glass">
                <option value="MANUAL" ${vehicle.transmission == 'MANUAL' ? 'selected' : ''}>Manual</option>
                <option value="AUTOMATIC" ${vehicle.transmission == 'AUTOMATIC' ? 'selected' : ''}>Automatic</option>
              </select>
            </div>
            <div class="col-md-3 mb-3">
              <label class="form-label small">Fuel Type</label>
              <select name="fuelType" class="form-select form-control-glass">
                <option value="PETROL" ${vehicle.fuelType == 'PETROL' ? 'selected' : ''}>Petrol</option>
                <option value="DIESEL" ${vehicle.fuelType == 'DIESEL' ? 'selected' : ''}>Diesel</option>
                <option value="ELECTRIC" ${vehicle.fuelType == 'ELECTRIC' ? 'selected' : ''}>Electric</option>
                <option value="HYBRID" ${vehicle.fuelType == 'HYBRID' ? 'selected' : ''}>Hybrid</option>
              </select>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label small">Price per Day (Rs.)</label>
            <input type="number" step="0.01" name="pricePerDay" class="form-control form-control-glass" value="${vehicle.pricePerDay}" required>
          </div>

          <hr class="my-4">
          <h6 class="fw-bold text-uppercase mb-3" style="font-size:.78rem; letter-spacing:.04em; color:var(--ink4);">Technical Specification</h6>
          <div class="row">
            <div class="col-md-3 mb-3">
              <label class="form-label small">Doors</label>
              <input type="number" name="doors" min="1" max="8" class="form-control form-control-glass" value="${vehicle.doors}">
            </div>
            <div class="col-md-3 mb-3">
              <label class="form-label small">Air Conditioner</label>
              <select name="airConditioner" class="form-select form-control-glass">
                <option value="YES" ${vehicle.airConditioner != 'NO' ? 'selected' : ''}>Yes</option>
                <option value="NO" ${vehicle.airConditioner == 'NO' ? 'selected' : ''}>No</option>
              </select>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label small">Distance (km)</label>
              <input type="number" name="mileage" min="0" class="form-control form-control-glass" value="${vehicle.mileage}">
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label small">Car Equipment</label>
            <input type="text" name="features" class="form-control form-control-glass" value="${vehicle.features}"
                   placeholder="ABS, Air Bags, Cruise Control, Bluetooth, GPS">
            <small class="text-soft">Comma-separated list — shown as a checklist on the vehicle details page.</small>
          </div>

          <div class="mb-3">
            <label class="form-label small">Current Photo</label><br>
            <c:if test="${not empty vehicle.imageUrl}">
              <img src="${vehicle.imageUrl}" alt="" style="max-height:140px; border-radius:12px; margin-bottom:.75rem;">
            </c:if>
            <label class="form-label small d-block">Replace Photo</label>
            <input type="file" name="imageFile" id="imageFile" accept="image/*" class="form-control form-control-glass">
            <small class="text-soft">Leave empty to keep the current photo.</small>
            <img id="imagePreview" src="" alt="" style="display:none; max-height:160px; margin-top:.75rem; border-radius:12px;">
          </div>

          <div class="mb-3">
            <label class="form-label small">…or Image URL</label>
            <input type="url" name="imageUrl" value="${vehicle.imageUrl}" class="form-control form-control-glass">
          </div>

          <div class="mb-3">
            <label class="form-label small">Description</label>
            <textarea name="description" rows="3" class="form-control form-control-glass">${vehicle.description}</textarea>
          </div>

          <div class="d-flex gap-2 mt-2">
            <button type="submit" class="btn btn-gradient flex-grow-1 py-2">Save Changes</button>
            <a href="${not empty cancelUrl ? cancelUrl : pageContext.request.contextPath.concat('/admin/vehicles')}" class="btn btn-outline-glass py-2">Cancel</a>
          </div>
        </form>
      </div>
    </c:if>
  </section>

  <script src="${pageContext.request.contextPath}/assets/js/vehicle-brands.js"></script>
  <script>
    const imageFile = document.getElementById('imageFile');
    const imagePreview = document.getElementById('imagePreview');
    imageFile.addEventListener('change', () => {
      const file = imageFile.files[0];
      if (!file) { imagePreview.style.display = 'none'; return; }
      imagePreview.src = URL.createObjectURL(file);
      imagePreview.style.display = 'block';
    });
  </script>

  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
