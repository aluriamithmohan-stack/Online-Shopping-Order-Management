'use strict';

const express  = require('express');
const bcrypt   = require('bcryptjs');
const router   = express.Router();
const db       = require('../database/db');
const { redirectIfLoggedIn } = require('../middleware/auth');

// ─── GET /auth/register ──────────────────────────────────────────────────────
router.get('/register', redirectIfLoggedIn, (req, res) => {
  res.render('register', {
    title: 'Create Account',
    errors: req.flash('errors'),
    success: req.flash('success'),
    formData: req.flash('formData')[0] || {}
  });
});

// ─── POST /auth/register ─────────────────────────────────────────────────────
router.post('/register', redirectIfLoggedIn, (req, res) => {
  const { name, email, password, confirmPassword } = req.body;
  const errors = [];

  if (!name || name.trim().length < 2) errors.push('Full name must be at least 2 characters.');
  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) errors.push('Please enter a valid email address.');
  if (!password || password.length < 6) errors.push('Password must be at least 6 characters.');
  if (password !== confirmPassword) errors.push('Passwords do not match.');

  if (errors.length > 0) {
    req.flash('errors', errors);
    req.flash('formData', { name, email });
    return res.redirect('/auth/register');
  }

  const normalizedEmail = email.trim().toLowerCase();
  const existing = db.get('SELECT id FROM customers WHERE email = ?', [normalizedEmail]);
  if (existing) {
    req.flash('errors', ['An account with this email already exists.']);
    req.flash('formData', { name, email });
    return res.redirect('/auth/register');
  }

  try {
    const hashed = bcrypt.hashSync(password, 10);
    db.run(
      'INSERT INTO customers (name, email, password) VALUES (?, ?, ?)',
      [name.trim(), normalizedEmail, hashed]
    );
    req.flash('success', 'Account created successfully! Please log in.');
    res.redirect('/auth/login');
  } catch (err) {
    console.error('[AUTH] Registration error:', err);
    req.flash('errors', ['Registration failed. Please try again.']);
    res.redirect('/auth/register');
  }
});

// ─── GET /auth/login ─────────────────────────────────────────────────────────
router.get('/login', redirectIfLoggedIn, (req, res) => {
  res.render('login', {
    title: 'Sign In',
    errors: req.flash('error'),
    success: req.flash('success')
  });
});

// ─── POST /auth/login ────────────────────────────────────────────────────────
router.post('/login', redirectIfLoggedIn, (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    req.flash('error', 'Email and password are required.');
    return res.redirect('/auth/login');
  }

  const customer = db.get('SELECT * FROM customers WHERE email = ?', [email.trim().toLowerCase()]);
  if (!customer || !bcrypt.compareSync(password, customer.password)) {
    req.flash('error', 'Invalid email or password. Please try again.');
    return res.redirect('/auth/login');
  }

  req.session.customerId   = customer.id;
  req.session.customerName = customer.name;
  req.session.isAdmin      = customer.is_admin === 1;

  if (customer.is_admin === 1) return res.redirect('/admin/dashboard');
  res.redirect('/products');
});

// ─── GET /auth/logout ────────────────────────────────────────────────────────
router.get('/logout', (req, res) => {
  req.session.destroy(() => { res.redirect('/auth/login'); });
});

module.exports = router;
