// ShopEase - Main Client-Side JavaScript

document.addEventListener('DOMContentLoaded', function () {

  // ─── AUTO-DISMISS ALERTS ─────────────────────────────────────────────────
  const alerts = document.querySelectorAll('.alert[data-auto-dismiss]');
  alerts.forEach(function (alert) {
    const delay = parseInt(alert.getAttribute('data-auto-dismiss')) || 4000;
    setTimeout(function () {
      alert.style.transition = 'opacity 0.5s ease';
      alert.style.opacity = '0';
      setTimeout(function () { alert.remove(); }, 500);
    }, delay);
  });

  // Manual dismiss button
  document.querySelectorAll('.alert-dismiss').forEach(function (btn) {
    btn.addEventListener('click', function () {
      const alert = btn.closest('.alert');
      if (alert) {
        alert.style.transition = 'opacity 0.3s ease';
        alert.style.opacity = '0';
        setTimeout(function () { alert.remove(); }, 300);
      }
    });
  });

  // ─── QUANTITY CONTROLS ────────────────────────────────────────────────────
  document.querySelectorAll('.qty-control').forEach(function (control) {
    const input  = control.querySelector('input[type="number"]');
    const minus  = control.querySelector('.qty-minus');
    const plus   = control.querySelector('.qty-plus');
    if (!input) return;

    const min = parseInt(input.getAttribute('min') || '1');
    const max = parseInt(input.getAttribute('max') || '9999');

    if (minus) {
      minus.addEventListener('click', function () {
        const val = parseInt(input.value) || 1;
        if (val > min) { input.value = val - 1; triggerChange(input); }
      });
    }
    if (plus) {
      plus.addEventListener('click', function () {
        const val = parseInt(input.value) || 1;
        if (val < max) { input.value = val + 1; triggerChange(input); }
      });
    }
    input.addEventListener('change', function () {
      let val = parseInt(input.value) || min;
      if (val < min) val = min;
      if (val > max) val = max;
      input.value = val;
    });
  });

  function triggerChange(el) {
    el.dispatchEvent(new Event('change', { bubbles: true }));
  }

  // ─── CONFIRM DIALOGS ──────────────────────────────────────────────────────
  document.querySelectorAll('[data-confirm]').forEach(function (el) {
    el.addEventListener('click', function (e) {
      const msg = el.getAttribute('data-confirm') || 'Are you sure?';
      if (!confirm(msg)) {
        e.preventDefault();
        e.stopPropagation();
      }
    });
  });

  // ─── FORM SUBMIT BUTTONS - PREVENT DOUBLE SUBMIT ─────────────────────────
  document.querySelectorAll('form').forEach(function (form) {
    form.addEventListener('submit', function () {
      const btn = form.querySelector('button[type="submit"]');
      if (btn && !btn.disabled) {
        setTimeout(function () {
          btn.disabled = true;
          const originalText = btn.textContent;
          btn.setAttribute('data-original-text', originalText);
        }, 100);
      }
    });
  });

  // ─── SEARCH BAR FOCUS ─────────────────────────────────────────────────────
  const searchInput = document.getElementById('searchBox');
  if (searchInput) {
    // Auto-focus search input on products page
    const isProductPage = window.location.pathname.includes('/products');
    if (isProductPage && !searchInput.value) {
      // Don't auto-focus to avoid UX issues on mobile
    }
  }

  // ─── ADMIN TABLE FILTER ───────────────────────────────────────────────────
  const adminSearch = document.getElementById('adminProductSearch');
  if (adminSearch) {
    adminSearch.addEventListener('input', function () {
      const q = this.value.toLowerCase();
      const rows = document.querySelectorAll('.admin-product-row');
      rows.forEach(function (row) {
        const name = (row.getAttribute('data-name') || '').toLowerCase();
        row.style.display = name.includes(q) ? '' : 'none';
      });
    });
  }

  // ─── CART TOTAL REAL-TIME PREVIEW ─────────────────────────────────────────
  document.querySelectorAll('.cart-qty-input').forEach(function (input) {
    input.addEventListener('change', function () {
      const row = input.closest('tr') || input.closest('.cart-row');
      if (!row) return;
      const price = parseFloat(row.getAttribute('data-price') || '0');
      const qty   = parseInt(input.value) || 0;
      const sub   = row.querySelector('.cart-subtotal');
      if (sub) {
        sub.textContent = '₹' + (price * qty).toLocaleString('en-IN', { minimumFractionDigits: 2 });
      }
    });
  });

  // ─── SCROLL TO TOP ON PAGE LOAD ───────────────────────────────────────────
  window.scrollTo(0, 0);

});

// Format currency (Indian Rupees)
function formatINR(amount) {
  return '₹' + parseFloat(amount).toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}
