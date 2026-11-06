'use strict';

const express  = require('express');
const bcrypt   = require('bcryptjs');
const router   = express.Router();
const db       = require('../database/db');
const { requireAdmin, redirectIfLoggedIn } = require('../middleware/auth');

// ─── GET /admin/login ────────────────────────────────────────────────────────
router.get('/login', redirectIfLoggedIn, (req, res) => {
  res.render('admin-login', {
    title: 'Admin Login',
    errors: req.flash('error'),
    success: req.flash('success')
  });
});

// ─── POST /admin/login ───────────────────────────────────────────────────────
router.post('/login', redirectIfLoggedIn, (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    req.flash('error', 'Email and password are required.');
    return res.redirect('/admin/login');
  }
  const admin = db.get(
    'SELECT * FROM customers WHERE email = ? AND is_admin = 1',
    [email.trim().toLowerCase()]
  );
  if (!admin || !bcrypt.compareSync(password, admin.password)) {
    req.flash('error', 'Invalid admin credentials.');
    return res.redirect('/admin/login');
  }
  req.session.customerId   = admin.id;
  req.session.customerName = admin.name;
  req.session.isAdmin      = true;
  res.redirect('/admin/dashboard');
});

// ─── GET /admin/dashboard ────────────────────────────────────────────────────
router.get('/dashboard', requireAdmin, (req, res) => {
  const stats = {
    totalProducts : db.get("SELECT COUNT(*) AS c FROM products").c,
    activeProducts: db.get("SELECT COUNT(*) AS c FROM products WHERE is_active = 1").c,
    totalOrders   : db.get("SELECT COUNT(*) AS c FROM orders").c,
    totalCustomers: db.get("SELECT COUNT(*) AS c FROM customers WHERE is_admin = 0").c,
    revenue       : db.get("SELECT COALESCE(SUM(total_amount),0) AS r FROM orders").r
  };

  const products     = db.all('SELECT * FROM products ORDER BY created_at DESC');
  const recentOrders = db.all(`
    SELECT o.*, c.name AS customer_name
    FROM orders o
    JOIN customers c ON c.id = o.customer_id
    ORDER BY o.created_at DESC LIMIT 10
  `);

  res.render('admin-dashboard', {
    title: 'Admin Dashboard',
    stats, products, recentOrders,
    success: req.flash('success'),
    error: req.flash('error')
  });
});

// ─── GET /admin/products/add ─────────────────────────────────────────────────
router.get('/products/add', requireAdmin, (req, res) => {
  res.render('admin-product-add', {
    title: 'Add Product',
    errors: req.flash('errors'),
    formData: req.flash('formData')[0] || {}
  });
});

// ─── POST /admin/products/add ────────────────────────────────────────────────
router.post('/products/add', requireAdmin, (req, res) => {
  const { name, description, price, stock, image_url, category } = req.body;
  const errors = [];

  if (!name || name.trim().length < 2)             errors.push('Product name must be at least 2 characters.');
  if (!description || description.trim().length < 10) errors.push('Description must be at least 10 characters.');
  if (!price || isNaN(parseFloat(price)) || parseFloat(price) < 0) errors.push('Enter a valid price.');
  if (stock === undefined || isNaN(parseInt(stock)) || parseInt(stock) < 0) errors.push('Enter a valid stock quantity.');
  if (!category || category.trim().length < 1)     errors.push('Category is required.');

  if (errors.length > 0) {
    req.flash('errors', errors);
    req.flash('formData', req.body);
    return res.redirect('/admin/products/add');
  }

  const imgUrl = (image_url || '').trim() || '/images/default-product.png';
  db.run(
    'INSERT INTO products (name, description, price, stock, image_url, category) VALUES (?, ?, ?, ?, ?, ?)',
    [name.trim(), description.trim(), parseFloat(price), parseInt(stock), imgUrl, category.trim()]
  );
  req.flash('success', `Product "${name.trim()}" added successfully.`);
  res.redirect('/admin/dashboard');
});

// ─── GET /admin/products/:id/edit ────────────────────────────────────────────
router.get('/products/:id/edit', requireAdmin, (req, res) => {
  const product = db.get('SELECT * FROM products WHERE id = ?', [req.params.id]);
  if (!product) {
    req.flash('error', 'Product not found.');
    return res.redirect('/admin/dashboard');
  }
  res.render('admin-product-edit', {
    title: 'Edit Product',
    product,
    errors: req.flash('errors'),
    success: req.flash('success')
  });
});

// ─── POST /admin/products/:id/edit ───────────────────────────────────────────
router.post('/products/:id/edit', requireAdmin, (req, res) => {
  const { name, description, price, stock, image_url, category, is_active } = req.body;
  const errors = [];

  if (!name || name.trim().length < 2)             errors.push('Product name must be at least 2 characters.');
  if (!description || description.trim().length < 10) errors.push('Description must be at least 10 characters.');
  if (!price || isNaN(parseFloat(price)) || parseFloat(price) < 0) errors.push('Enter a valid price.');
  if (stock === undefined || isNaN(parseInt(stock)) || parseInt(stock) < 0) errors.push('Enter a valid stock quantity.');
  if (!category || category.trim().length < 1)     errors.push('Category is required.');

  if (errors.length > 0) {
    req.flash('errors', errors);
    return res.redirect(`/admin/products/${req.params.id}/edit`);
  }

  const active = is_active === '1' ? 1 : 0;
  const imgUrl = (image_url || '').trim() || '/images/default-product.png';

  db.run(
    'UPDATE products SET name=?, description=?, price=?, stock=?, image_url=?, category=?, is_active=? WHERE id=?',
    [name.trim(), description.trim(), parseFloat(price), parseInt(stock), imgUrl, category.trim(), active, req.params.id]
  );
  req.flash('success', 'Product updated successfully.');
  res.redirect('/admin/dashboard');
});

// ─── POST /admin/products/:id/toggle ─────────────────────────────────────────
router.post('/products/:id/toggle', requireAdmin, (req, res) => {
  const product = db.get('SELECT * FROM products WHERE id = ?', [req.params.id]);
  if (!product) {
    req.flash('error', 'Product not found.');
    return res.redirect('/admin/dashboard');
  }
  const newStatus = product.is_active === 1 ? 0 : 1;
  db.run('UPDATE products SET is_active = ? WHERE id = ?', [newStatus, req.params.id]);
  req.flash('success', newStatus === 1 ? 'Product activated.' : 'Product deactivated.');
  res.redirect('/admin/dashboard');
});

// ─── POST /admin/products/:id/delete ─────────────────────────────────────────
router.post('/products/:id/delete', requireAdmin, (req, res) => {
  db.run('DELETE FROM products WHERE id = ?', [req.params.id]);
  req.flash('success', 'Product deleted successfully.');
  res.redirect('/admin/dashboard');
});

// ─── GET /admin/logout ───────────────────────────────────────────────────────
router.get('/logout', (req, res) => {
  req.session.destroy(() => { res.redirect('/admin/login'); });
});

module.exports = router;
