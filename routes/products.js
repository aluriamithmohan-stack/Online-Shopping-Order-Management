'use strict';

const express = require('express');
const router  = express.Router();
const db      = require('../database/db');
const { requireAuth } = require('../middleware/auth');

// ─── GET /products ───────────────────────────────────────────────────────────
router.get('/', requireAuth, (req, res) => {
  const query    = (req.query.q || '').trim();
  const category = (req.query.category || '').trim();

  let sql    = "SELECT * FROM products WHERE is_active = 1";
  let params = [];

  if (query) {
    sql    += " AND name LIKE ?";
    params.push(`%${query}%`);
  }
  if (category) {
    sql    += " AND category = ?";
    params.push(category);
  }
  sql += " ORDER BY created_at DESC";

  const products   = db.all(sql, params);
  const categories = db.all("SELECT DISTINCT category FROM products WHERE is_active = 1 ORDER BY category")
                       .map(r => r.category);

  res.render('products', {
    title: query ? `Search: "${query}"` : 'Product Catalogue',
    products,
    categories,
    searchQuery: query,
    selectedCategory: category,
    success: req.flash('success'),
    error: req.flash('error')
  });
});

// ─── GET /products/:id ───────────────────────────────────────────────────────
router.get('/:id', requireAuth, (req, res) => {
  const product = db.get('SELECT * FROM products WHERE id = ? AND is_active = 1', [req.params.id]);
  if (!product) {
    req.flash('error', 'Product not found.');
    return res.redirect('/products');
  }
  res.render('product-detail', {
    title: product.name,
    product,
    success: req.flash('success'),
    error: req.flash('error')
  });
});

module.exports = router;
