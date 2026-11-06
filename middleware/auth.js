'use strict';

/**
 * Authentication Middleware
 * Protects routes that require a logged-in customer or admin.
 */

// Require a logged-in customer (any user)
function requireAuth(req, res, next) {
  if (req.session && req.session.customerId) {
    return next();
  }
  req.flash('error', 'Please log in to continue.');
  res.redirect('/auth/login');
}

// Require an admin user
function requireAdmin(req, res, next) {
  if (req.session && req.session.customerId && req.session.isAdmin) {
    return next();
  }
  if (req.session && req.session.customerId && !req.session.isAdmin) {
    req.flash('error', 'Access denied. Admin privileges required.');
    res.redirect('/products');
    return;
  }
  req.flash('error', 'Please log in as admin.');
  res.redirect('/admin/login');
}

// Redirect already-logged-in users away from auth pages
function redirectIfLoggedIn(req, res, next) {
  if (req.session && req.session.customerId) {
    if (req.session.isAdmin) return res.redirect('/admin/dashboard');
    return res.redirect('/products');
  }
  next();
}

module.exports = { requireAuth, requireAdmin, redirectIfLoggedIn };
