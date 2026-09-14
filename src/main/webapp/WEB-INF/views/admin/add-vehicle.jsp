<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head><jsp:include page="/WEB-INF/views/common/head.jsp" /></head>
<body>
  <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

  <section class="container" style="padding-top: 7rem; padding-bottom: 4rem; max-width: 760px;">
    <h2 class="fw-bold mb-4" data-aos="fade-up">Add Vehicle to Stock</h2>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <div class="glass-card p-4" data-aos="fade-up">
      <form method="post" action="${pageContext.request.contextPath}/admin/vehicles" enctype="multipart/form-data">
        <div class="row">
          <div class="col-md-6 mb-3">
            <label class="form-label small">Category</label>
            <select name="categoryId" id="categorySelect" class="form-select form-control-glass" required>
              <option value="1" data-name="Car">Car</option>
              <option value="2" data-name="SUV">SUV</option>
              <option value="3" data-name="Luxury">Luxury</option>
              <option value="4" data-name="Sports">Sports</option>
              <option value="5" data-name="Van">Van</option>
              <option value="6" data-name="Bus">Bus</option>
              <option value="7" data-name="Motorcycle">Motorcycle</option>
              <option value="8" data-name="ThreeWheeler">Three Wheeler</option>
              <option value="9" data-name="Electric Vehicle">Electric Vehicle</option>
            </select>
            <input type="hidden" name="categoryName" id="categoryName" value="Car">
          </div>
          <div class="col-md-6 mb-3">
            <label class="form-label small">Vehicle Number</label>
            <input type="text" name="vehicleNumber" placeholder="ABC-1234" class="form-control form-control-glass" required>
          </div>
        </div>

        <div class="row">
          <div class="col-md-6 mb-3">
            <label class="form-label small">Brand</label>
            <input type="text" id="brandInput" name="brand" list="brandList" class="form-control form-control-glass"
                   placeholder="Pick from the list or type your own" required>
            <datalist id="brandList"></datalist>
          </div>
          <div class="col-md-6 mb-3">
            <label class="form-label small">Model</label>
            <input type="text" name="model" list="modelList" class="form-control form-control-glass"
                   placeholder="Pick a suggestion or type your own" required>
            <datalist id="modelList"></datalist>
          </div>
        </div>

        <div class="row">
          <div class="col-md-3 mb-3">
            <label class="form-label small">Year</label>
            <input type="number" name="year" class="form-control form-control-glass" min="1990" max="2100" required>
          </div>
          <div class="col-md-3 mb-3">
            <label class="form-label small">Seats</label>
            <input type="number" name="seats" class="form-control form-control-glass" min="1" placeholder="Auto by category">
          </div>
          <div class="col-md-3 mb-3">
            <label class="form-label small">Transmission</label>
            <select name="transmission" class="form-select form-control-glass">
              <option value="">Auto by category</option>
              <option value="MANUAL">Manual</option>
              <option value="AUTOMATIC">Automatic</option>
            </select>
          </div>
          <div class="col-md-3 mb-3">
            <label class="form-label small">Fuel Type</label>
            <select name="fuelType" class="form-select form-control-glass">
              <option value="">Auto by category</option>
              <option value="PETROL">Petrol</option>
              <option value="DIESEL">Diesel</option>
              <option value="ELECTRIC">Electric</option>
              <option value="HYBRID">Hybrid</option>
            </select>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label small">Price per Day (Rs.)</label>
          <input type="number" step="0.01" name="pricePerDay" class="form-control form-control-glass" required>
        </div>

        <hr class="my-4">
        <h6 class="fw-bold text-uppercase mb-3" style="font-size:.78rem; letter-spacing:.04em; color:var(--ink4);">Technical Specification</h6>
        <div class="row">
          <div class="col-md-3 mb-3">
            <label class="form-label small">Doors</label>
            <input type="number" name="doors" min="1" max="8" placeholder="e.g. 4" class="form-control form-control-glass">
          </div>
          <div class="col-md-3 mb-3">
            <label class="form-label small">Air Conditioner</label>
            <select name="airConditioner" class="form-select form-control-glass">
              <option value="YES" selected>Yes</option>
              <option value="NO">No</option>
            </select>
          </div>
          <div class="col-md-6 mb-3">
            <label class="form-label small">Distance (km)</label>
            <input type="number" name="mileage" min="0" placeholder="e.g. 500" class="form-control form-control-glass">
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label small">Car Equipment</label>
          <input type="text" name="features" placeholder="ABS, Air Bags, Cruise Control, Bluetooth, GPS" class="form-control form-control-glass">
          <small class="text-soft">Comma-separated list — shown as a checklist on the vehicle details page.</small>
        </div>

        <div class="mb-3">
          <label class="form-label small">Vehicle Photo</label>
          <input type="file" name="imageFile" id="imageFile" accept="image/*" class="form-control form-control-glass">
          <small class="text-soft">Choose a photo from your device — or leave this empty and paste a link below instead.</small>
          <img id="imagePreview" src="" alt="" style="display:none; max-height:160px; margin-top:.75rem; border-radius:12px;">
        </div>

        <div class="mb-3">
          <label class="form-label small">…or Image URL</label>
          <input type="url" name="imageUrl" placeholder="https://..." class="form-control form-control-glass">
        </div>

        <div class="mb-3">
          <label class="form-label small">Description</label>
          <textarea name="description" rows="3" class="form-control form-control-glass"></textarea>
        </div>

        <button type="submit" class="btn btn-gradient w-100 py-2 mt-2">Add Vehicle</button>
      </form>
    </div>
  </section>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/vehicle-brands.js"></script>
  <script>
    const categorySelect = document.getElementById('categorySelect');
    const categoryName = document.getElementById('categoryName');
    function syncCategoryName() {
      categoryName.value = categorySelect.options[categorySelect.selectedIndex].dataset.name;
    }
    categorySelect.addEventListener('change', syncCategoryName);
    syncCategoryName();

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
