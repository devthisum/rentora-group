// ==========================================================
// RENTORA — Global Frontend Behavior
// GSAP + AOS init, loading screen, micro-interactions
// ==========================================================

document.addEventListener('DOMContentLoaded', function () {

    // ---- Dark mode toggle (persisted via localStorage, applied pre-paint in
    //      head.jsp to avoid a flash of light mode on reload) ----
    const themeBtn = document.getElementById('themeToggleBtn');
    const themeThumb = document.getElementById('themeToggleThumb');
    function syncThemeIcon() {
        if (themeThumb) {
            themeThumb.textContent = document.documentElement.getAttribute('data-theme') === 'dark' ? '☀️' : '🌙';
        }
    }
    syncThemeIcon();
    if (themeBtn) {
        themeBtn.addEventListener('click', () => {
            const html = document.documentElement;
            if (html.getAttribute('data-theme') === 'dark') {
                html.removeAttribute('data-theme');
                window.localStorage.setItem('rentora_theme', 'light');
            } else {
                html.setAttribute('data-theme', 'dark');
                window.localStorage.setItem('rentora_theme', 'dark');
            }
            syncThemeIcon();
        });
    }

    // ---- Toast helper — window.showToast(message, isError) is available
    //      globally for any inline script on any page to call. ----
    window.showToast = function (message, isError) {
        let toast = document.getElementById('rentoraToast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'rentoraToast';
            toast.className = 'toast-rentora';
            document.body.appendChild(toast);
        }
        toast.textContent = message;
        toast.style.background = isError ? '#c53030' : 'var(--ink)';
        toast.classList.add('show');
        clearTimeout(toast._hideTimer);
        toast._hideTimer = setTimeout(() => toast.classList.remove('show'), 3000);
    };

    // ---- Loading screen fade-out (with a hard timeout fallback so a slow/failed
    //      resource can never leave the user staring at the loader forever) ----
    const loadingScreen = document.getElementById('loading-screen');
    if (loadingScreen) {
        let hidden = false;
        const hideLoader = () => {
            if (hidden) return;
            hidden = true;
            loadingScreen.style.opacity = '0';
            setTimeout(() => loadingScreen.style.display = 'none', 500);
        };
        window.addEventListener('load', () => setTimeout(hideLoader, 250));
        setTimeout(hideLoader, 2500); // hard fallback regardless of the load event
    }

    // ---- Hero headline typewriter — types out each line from
    //      #heroTypewriter's data-lines (pipe-separated), then leaves a
    //      blinking cursor. Falls back to plain text instantly if the
    //      element is missing (other pages) or something goes wrong. ----
    try {
        const twEl = document.getElementById('heroTypewriter');
        if (twEl) {
            const lines = (twEl.dataset.lines || '').split('|').filter(Boolean);
            twEl.innerHTML = '<span class="tw-line"></span><span class="tw-cursor"></span>';
            const lineSpan = twEl.querySelector('.tw-line');
            const cursor = twEl.querySelector('.tw-cursor');
            let lineIdx = 0, charIdx = 0;
            const CHAR_SPEED = 42, LINE_PAUSE = 260;
            function typeStep() {
                if (lineIdx >= lines.length) return;
                const current = lines[lineIdx];
                if (charIdx <= current.length) {
                    let html = '';
                    for (let i = 0; i < lineIdx; i++) html += lines[i] + '<br>';
                    html += current.slice(0, charIdx);
                    lineSpan.innerHTML = html;
                    charIdx++;
                    setTimeout(typeStep, CHAR_SPEED);
                } else {
                    lineIdx++; charIdx = 0;
                    setTimeout(typeStep, LINE_PAUSE);
                }
            }
            typeStep();
        }
    } catch (e) { /* typewriter failing must never hide the headline */ }

    // ---- AOS (scroll animations) — the real safety net lives here, not in
    //      CSS: if AOS truly failed to load, we add a class that forces every
    //      [data-aos] element visible. If AOS loaded fine (the normal case),
    //      this never fires and animations play normally on every page. ----
    try {
        if (window.AOS) {
            AOS.init({ duration: 750, easing: 'ease-out-cubic', once: true, offset: 60 });
        } else {
            document.documentElement.classList.add('no-aos');
        }
    } catch (e) {
        document.documentElement.classList.add('no-aos');
    }

    // ---- GSAP hero entrance — targets the actual homepage hero markup.
    //      Headline itself is revealed by the typewriter above, so the
    //      timeline is delayed to roughly match typing finishing, then the
    //      rest cascades in — including the hero photo and its two floating
    //      trust cards. Only runs if GSAP loaded; a failed CDN load just
    //      skips the extra flourish (AOS/.reveal still cover the rest of
    //      the page). ----
    try {
        if (window.gsap) {
            const heroTl = gsap.timeline({ defaults: { ease: 'power3.out' }, delay: 1.1 });
            heroTl
                .from('.hero-v3 .fs-5', { y: 18, opacity: 0, duration: 0.7 })
                .from('.hero-tab-group', { y: 18, opacity: 0, duration: 0.7 }, '-=0.45')
                .from('.hero-search-pill-v3', { y: 18, opacity: 0, duration: 0.7 }, '-=0.45')
                .from('.hero-v3 .hero-img-wrap', { scale: 1.08, opacity: 0, duration: 1, ease: 'power3.out' }, '-=0.5')
                .from('.hero-v3 .floating-card', { scale: 0.8, opacity: 0, duration: 0.7, stagger: 0.15, ease: 'back.out(1.5)' }, '-=0.5');

            if (window.ScrollTrigger) {
                gsap.utils.toArray('.vehicle-card').forEach((card, i) => {
                    gsap.from(card, {
                        scrollTrigger: { trigger: card, start: 'top 90%' },
                        y: 40, opacity: 0, duration: 0.6, delay: i * 0.05, ease: 'power2.out'
                    });
                });
            }
        }
    } catch (e) { /* GSAP failing must never break the page */ }

    // ---- Number count-up for stat cards / stat numbers ----
    document.querySelectorAll('[data-count]').forEach(el => {
        const target = parseFloat(el.dataset.count);
        const decimals = parseInt(el.dataset.decimal || '0', 10);
        let current = 0;
        const step = Math.max(target / 60, 0.1);
        const tick = () => {
            current = Math.min(current + step, target);
            el.textContent = current.toFixed(decimals);
            if (current < target) {
                requestAnimationFrame(tick);
            } else {
                el.textContent = target.toLocaleString(undefined, { minimumFractionDigits: decimals, maximumFractionDigits: decimals });
            }
        };
        // only animate once the element is actually visible
        try {
            const obs = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) { tick(); obs.unobserve(el); }
                });
            }, { threshold: 0.4 });
            obs.observe(el);
        } catch (e) { tick(); } // fallback for older browsers without IntersectionObserver
    });

    // ---- Navbar scroll state (adds shadow/blur once the page scrolls) ----
    const mainNav = document.getElementById('mainNav');
    const backToTop = document.getElementById('backToTop');
    window.addEventListener('scroll', () => {
        if (mainNav) mainNav.classList.toggle('scrolled', window.scrollY > 40);
        if (backToTop) backToTop.classList.toggle('show', window.scrollY > 500);
    });

    // ---- Back to top ----
    if (backToTop) {
        backToTop.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
    }

    // ---- Category slideshow arrows (homepage "Choose the car that suits you") ----
    const catSlider = document.getElementById('categorySlider');
    if (catSlider) {
        const prevBtn = document.querySelector('.cat-slider-prev');
        const nextBtn = document.querySelector('.cat-slider-next');
        const scrollAmount = () => (catSlider.querySelector('.cat-slide')?.offsetWidth || 320) + 24;

        function atEnd() { return catSlider.scrollLeft + catSlider.clientWidth >= catSlider.scrollWidth - 5; }
        function slideNext() {
            if (atEnd()) catSlider.scrollTo({ left: 0, behavior: 'smooth' });
            else catSlider.scrollBy({ left: scrollAmount(), behavior: 'smooth' });
        }
        function slidePrev() { catSlider.scrollBy({ left: -scrollAmount(), behavior: 'smooth' }); }

        if (prevBtn) prevBtn.addEventListener('click', () => { slidePrev(); restartAutoplay(); });
        if (nextBtn) nextBtn.addEventListener('click', () => { slideNext(); restartAutoplay(); });

        // Auto-advance every 3.5s; pauses while the user is hovering/touching/dragging it.
        let autoplayTimer = null;
        function startAutoplay() { autoplayTimer = setInterval(slideNext, 3500); }
        function stopAutoplay() { clearInterval(autoplayTimer); }
        function restartAutoplay() { stopAutoplay(); startAutoplay(); }

        startAutoplay();
        catSlider.addEventListener('mouseenter', stopAutoplay);
        catSlider.addEventListener('mouseleave', startAutoplay);
        catSlider.addEventListener('touchstart', stopAutoplay, { passive: true });
        catSlider.addEventListener('touchend', startAutoplay);
    }

    // ---- Button ripple effect ----
    document.querySelectorAll('.btn-gradient, .btn-rentora').forEach(btn => {
        btn.addEventListener('click', function (e) {
            const rect = this.getBoundingClientRect();
            const ripple = document.createElement('span');
            ripple.className = 'ripple';
            ripple.style.left = (e.clientX - rect.left) + 'px';
            ripple.style.top = (e.clientY - rect.top) + 'px';
            ripple.style.width = ripple.style.height = Math.max(rect.width, rect.height) + 'px';
            this.appendChild(ripple);
            setTimeout(() => ripple.remove(), 650);
        });
    });

    // ---- Wishlist heart toggle — calls the real backend endpoint and updates
    //      the icon based on the actual saved/removed result. Centralized here
    //      so every page (homepage, search results, saved list) shares one
    //      implementation instead of duplicating it per-JSP. Uses event
    //      delegation on document so it keeps working even if cards are
    //      re-rendered or added after this script first ran. ----
    document.addEventListener('click', async function (e) {
        const btn = e.target.closest('.wishlist-toggle-btn');
        if (!btn) return;
        e.preventDefault();

        const vehicleId = btn.dataset.vehicleId;
        const icon = btn.querySelector('i');
        const removeCardOnUnfavorite = btn.dataset.removeOnUnfavorite === 'true';
        const card = removeCardOnUnfavorite ? btn.closest('.col-md-4, .col-lg-4, .vehicle-card-v2, .vehicle-card') : null;

        try {
            const contextPath = window.CONTEXT_PATH || '';
            const resp = await fetch(contextPath + '/renter/wishlist/toggle', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'vehicleId=' + encodeURIComponent(vehicleId)
            });

            if (resp.status === 401 || resp.status === 403) {
                window.location.href = contextPath + '/login';
                return;
            }
            if (!resp.ok) { console.warn('Wishlist toggle failed:', resp.status); return; }

            const result = await resp.text();
            const nowFavorited = result === 'added';

            if (icon) {
                icon.classList.toggle('fa-solid', nowFavorited);
                icon.classList.toggle('fa-regular', !nowFavorited);
                icon.style.color = nowFavorited ? '#EF4444' : '';
            }

            if (!nowFavorited && removeCardOnUnfavorite && card) {
                card.style.transition = 'opacity 0.3s ease';
                card.style.opacity = '0';
                setTimeout(() => card.remove(), 300);
            }
        } catch (err) { console.warn('Wishlist toggle error:', err); }
    });

    // ---- Password strength micro-feedback (register page) ----
    const pwdInput = document.getElementById('password');
    const pwdHint = document.getElementById('password-hint');
    if (pwdInput && pwdHint) {
        pwdInput.addEventListener('input', () => {
            const strong = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!]).{8,}$/.test(pwdInput.value);
            pwdHint.textContent = strong ? 'Strong password ✓' : 'Min 8 chars, upper, lower, digit, symbol';
            pwdHint.style.color = strong ? '#22c55e' : '#5B6B82';
        });
    }

    // ---- Booking total live estimate (AJAX-free client-side preview only) ----
    const startDateEl = document.getElementById('startDate');
    const endDateEl = document.getElementById('endDate');
    const priceEstimateEl = document.getElementById('priceEstimate');
    const pricePerDayAttr = document.getElementById('booking-form');

    function recalcEstimate() {
        if (!startDateEl || !endDateEl || !priceEstimateEl || !pricePerDayAttr) return;
        const pricePerDay = parseFloat(pricePerDayAttr.dataset.pricePerDay || '0');
        const start = new Date(startDateEl.value);
        const end = new Date(endDateEl.value);
        if (!isNaN(start) && !isNaN(end) && end >= start) {
            const days = Math.round((end - start) / (1000 * 60 * 60 * 24)) + 1;
            const total = (days * pricePerDay * 1.05).toFixed(2); // matches CardPaymentStrategy 5% fee
            priceEstimateEl.textContent = `Estimated total: Rs. ${total} (${days} day${days > 1 ? 's' : ''})`;
        }
    }
    [startDateEl, endDateEl].forEach(el => el && el.addEventListener('change', recalcEstimate));
});

// ==========================================================
// Homepage hero v3 — category pill tabs feed the hidden
// "category" field on the search form (Economy/Luxury/SUVs).
// ==========================================================
document.addEventListener('DOMContentLoaded', function () {
    const tabs = document.querySelectorAll('.hero-tab');
    const categoryInput = document.getElementById('heroCategoryInput');
    if (tabs.length && categoryInput) {
        tabs.forEach(tab => {
            tab.addEventListener('click', function () {
                tabs.forEach(t => t.classList.remove('active'));
                this.classList.add('active');
                categoryInput.value = this.dataset.category || '';
            });
        });
        categoryInput.value = document.querySelector('.hero-tab.active')?.dataset.category || '';
    }
});

// ==========================================================
// Vehicle fleet page — grid / list view toggle (remembers the
// choice in localStorage so it persists across page loads).
// ==========================================================
document.addEventListener('DOMContentLoaded', function () {
    const toggle = document.getElementById('fleetViewToggle');
    const list = document.getElementById('fleetList');
    if (!toggle || !list) return;

    const buttons = toggle.querySelectorAll('button');

    function setView(view) {
        buttons.forEach(b => b.classList.toggle('active', b.dataset.view === view));
        list.classList.toggle('grid-view', view === 'grid');
        try { localStorage.setItem('rentora_fleet_view', view); } catch (e) { /* ignore */ }
    }

    buttons.forEach(btn => {
        btn.addEventListener('click', function () { setView(this.dataset.view); });
    });

    let savedView = 'list';
    try { savedView = localStorage.getItem('rentora_fleet_view') || 'list'; } catch (e) { /* ignore */ }
    setView(savedView);
});

// ==========================================================
// Admin/Maintenance/Booking sidebar — shifts page content over
// when present (no per-page markup changes needed), and handles
// the mobile slide-in toggle.
// ==========================================================
document.addEventListener('DOMContentLoaded', function () {
    const sidebar = document.getElementById('adminSidebar');
    if (!sidebar) return;

    document.body.classList.add('has-admin-sidebar');

    const toggleBtn = document.getElementById('adminSidebarToggle');
    const backdrop = document.getElementById('adminSidebarBackdrop');

    function closeSidebar() {
        sidebar.classList.remove('open');
        backdrop.classList.remove('open');
    }
    function openSidebar() {
        sidebar.classList.add('open');
        backdrop.classList.add('open');
    }

    if (toggleBtn) {
        toggleBtn.addEventListener('click', function () {
            sidebar.classList.contains('open') ? closeSidebar() : openSidebar();
        });
    }
    if (backdrop) {
        backdrop.addEventListener('click', closeSidebar);
    }
    // Close automatically after navigating (mobile) so it doesn't stay open on the next page.
    sidebar.querySelectorAll('a').forEach(link => link.addEventListener('click', closeSidebar));
});

// ==========================================================
// RENTORA brand logo — splits "RENTORA" into per-letter spans
// so CSS can wave/stagger them on hover (navbar + footer, every
// page, no per-page markup needed).
// ==========================================================
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.brand-text').forEach(function (el) {
        const text = el.textContent;
        el.textContent = '';
        text.split('').forEach(function (ch, i) {
            const span = document.createElement('span');
            span.className = 'brand-letter';
            span.style.setProperty('--i', i);
            span.textContent = ch;
            el.appendChild(span);
        });
    });
});

// ==========================================================
// Animated stat counters — any element with [data-count-to]
// counts up from 0 once it scrolls into view (hero stats,
// admin dashboard stat cards). Non-numeric text is left alone.
// ==========================================================
document.addEventListener('DOMContentLoaded', function () {
    const counters = document.querySelectorAll('.hero-stat-value, .hstat-num, .admin-stat-value');
    if (!counters.length) return;

    function animateCounter(el) {
        const raw = el.textContent.trim();
        const match = raw.match(/^([^\d]*)(\d[\d,]*)(.*)$/);
        if (!match) return; // no digits to animate (e.g. plain text like "24/7")
        const [, prefix, numStr, suffix] = match;
        const target = parseInt(numStr.replace(/,/g, ''), 10);
        if (isNaN(target)) return;
        const duration = 1100;
        const start = performance.now();
        function tick(now) {
            const progress = Math.min((now - start) / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            const current = Math.round(target * eased);
            el.textContent = prefix + current.toLocaleString() + suffix;
            if (progress < 1) requestAnimationFrame(tick);
        }
        requestAnimationFrame(tick);
    }

    const io = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                animateCounter(entry.target);
                io.unobserve(entry.target);
            }
        });
    }, { threshold: 0.4 });

    counters.forEach(function (el) { io.observe(el); });
});
