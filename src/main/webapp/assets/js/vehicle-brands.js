/**
 * Brand -> Model suggestions for the Add/Edit Vehicle forms.
 * These populate <datalist> options, which means the admin can still type
 * anything not on the list — the dropdown is just a helpful shortcut.
 */
const VEHICLE_BRAND_MODELS = {
  "Toyota": ["Corolla", "Camry", "Prius", "Land Cruiser", "Hilux", "RAV4", "Yaris", "Aqua", "Hiace", "Prado", "Vitz", "Allion", "Premio", "Axio"],
  "Honda": ["Civic", "Accord", "CR-V", "Fit", "Vezel", "City", "Grace", "Freed", "Insight", "Odyssey"],
  "Nissan": ["Leaf", "X-Trail", "Note", "Sunny", "Navara", "Patrol", "March", "Sylphy", "Caravan"],
  "Suzuki": ["Swift", "Alto", "Wagon R", "Vitara", "Jimny", "Every", "Baleno", "Celerio"],
  "Mitsubishi": ["Montero", "Outlander", "Lancer", "Pajero", "L200", "Attrage", "Mirage"],
  "Hyundai": ["Elantra", "Tucson", "Santa Fe", "Accent", "i10", "i20", "Kona", "Creta"],
  "Kia": ["Sportage", "Sorento", "Rio", "Picanto", "Seltos", "Cerato"],
  "BMW": ["3 Series", "5 Series", "X1", "X3", "X5", "7 Series", "Z4"],
  "Mercedes-Benz": ["C-Class", "E-Class", "S-Class", "GLA", "GLC", "GLE", "A-Class"],
  "Audi": ["A3", "A4", "A6", "Q3", "Q5", "Q7"],
  "Volkswagen": ["Golf", "Polo", "Tiguan", "Passat", "Jetta"],
  "Ford": ["Ranger", "Everest", "Focus", "Ecosport", "Explorer"],
  "Tata": ["Nexon", "Xenon", "Ace", "Dimo Batta"],
  "Perodua": ["Axia", "Bezza", "Myvi", "Alza"],
  "Mahindra": ["Bolero", "Scorpio", "XUV500"],
  "Yamaha": ["FZ", "R15", "MT-15", "Fazer", "Ray ZR"],
  "Bajaj": ["Pulsar", "Discover", "CT100", "Avenger", "RE Three Wheeler"],
  "TVS": ["Apache", "Ntorq", "Star City"],
  "Tesla": ["Model 3", "Model Y", "Model S", "Model X"],
  "BYD": ["Atto 3", "Dolphin", "Seal"],
  "Isuzu": ["D-Max", "Elf", "NPR"],
  "Mazda": ["CX-5", "Mazda2", "Mazda3", "CX-3"]
};

/** Fills the #brandList datalist with all known brand names. Call once on page load. */
function populateBrandDatalist() {
  const list = document.getElementById('brandList');
  if (!list) return;
  Object.keys(VEHICLE_BRAND_MODELS).sort().forEach(brand => {
    const opt = document.createElement('option');
    opt.value = brand;
    list.appendChild(opt);
  });
}

/** Refreshes #modelList with suggestions for whatever brand is currently typed into the brand input. */
function refreshModelDatalist(brandValue) {
  const list = document.getElementById('modelList');
  if (!list) return;
  list.innerHTML = '';
  const models = VEHICLE_BRAND_MODELS[brandValue] || [];
  models.forEach(model => {
    const opt = document.createElement('option');
    opt.value = model;
    list.appendChild(opt);
  });
}

document.addEventListener('DOMContentLoaded', () => {
  populateBrandDatalist();
  const brandInput = document.getElementById('brandInput');
  if (brandInput) {
    refreshModelDatalist(brandInput.value);
    brandInput.addEventListener('input', () => refreshModelDatalist(brandInput.value));
  }
});
