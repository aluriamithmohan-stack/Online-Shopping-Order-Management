'use strict';

const express = require('express');
const router  = express.Router();
const db      = require('../database/db');
const { requireAuth } = require('../middleware/auth');

function getCartItems(customerId) {
  return db.all(`
    SELECT
      ci.id         AS cart_item_id,
      ci.quantity,
      p.id          AS product_id,
      p.name,
      p.price,
      p.image_url,
      p.stock,
      p.is_active,
      (p.price * ci.quantity) AS subtotal
    FROM cart_items ci
    JOIN products p ON p.id = ci.product_id
    WHERE ci.customer_id = ?
    ORDER BY ci.added_at DESC
  `, [customerId]);
}

function cartTotal(items) {
  return items.reduce((sum, i) => sum + i.subtotal, 0);
}

// ─── GET /cart ───────────────────────────────────────────────────────────────
router.get('/', requireAuth, (req, res) => {
  const items = getCartItems(req.session.customerId);
  res.render('cart', {
    title: 'Shopping Cart',
    items,
    total: cartTotal(items),
    success: req.flash('success'),
    error: req.flash('error')
  });
});

// ─── POST /cart/add ──────────────────────────────────────────────────────────
router.post('/add', requireAuth, (req, res) => {
  const productId = parseInt(req.body.product_id, 10);
  const quantity  = parseInt(req.body.quantity, 10) || 1;
  const referer   = req.get('Referer') || '/products';

  if (!productId || quantity < 1) {
    req.flash('error', 'Invalid product or quantity.');
    return res.redirect(referer);
  }

  const product = db.get('SELECT * FROM products WHERE id = ? AND is_active = 1', [productId]);
  if (!product) {
    req.flash('error', 'Product not found or unavailable.');
    return res.redirect('/products');
  }

  const existing = db.get(
    'SELECT * FROM cart_items WHERE customer_id = ? AND product_id = ?',
    [req.session.customerId, productId]
  );
  const newQty = existing ? existing.quantity + quantity : quantity;

  if (newQty > product.stock) {
    req.flash('error', `Only ${product.stock} units available. You already have ${existing ? existing.quantity : 0} in your cart.`);
    return res.redirect(referer);
  }

  if (existing) {
    db.run('UPDATE cart_items SET quantity = ? WHERE id = ?', [newQty, existing.id]);
  } else {
    db.run(
      'INSERT INTO cart_items (customer_id, product_id, quantity) VALUES (?, ?, ?)',
      [req.session.customerId, productId, quantity]
    );
  }

  req.flash('success', `"${product.name}" added to cart.`);
  res.redirect(referer);
});

// ─── POST /cart/update ───────────────────────────────────────────────────────
router.post('/update', requireAuth, (req, res) => {
  const cartItemId = parseInt(req.body.cart_item_id, 10);
  const quantity   = parseInt(req.body.quantity, 10);

  if (!cartItemId || isNaN(quantity)) {
    req.flash('error', 'Invalid request.');
    return res.redirect('/cart');
  }

  if (quantity < 1) {
    db.run('DELETE FROM cart_items WHERE id = ? AND customer_id = ?',
      [cartItemId, req.session.customerId]);
    req.flash('success', 'Item removed from cart.');
    return res.redirect('/cart');
  }

  const item = db.get(`
    SELECT ci.id, p.stock, p.name
    FROM cart_items ci
    JOIN products p ON p.id = ci.product_id
    WHERE ci.id = ? AND ci.customer_id = ?
  `, [cartItemId, req.session.customerId]);

  if (!item) {
    req.flash('error', 'Cart item not found.');
    return res.redirect('/cart');
  }

  if (quantity > item.stock) {
    req.flash('error', `Only ${item.stock} units of "${item.name}" available.`);
    return res.redirect('/cart');
  }

  db.run('UPDATE cart_items SET quantity = ? WHERE id = ? AND customer_id = ?',
    [quantity, cartItemId, req.session.customerId]);
  req.flash('success', 'Cart updated.');
  res.redirect('/cart');
});

// ─── POST /cart/remove ───────────────────────────────────────────────────────
router.post('/remove', requireAuth, (req, res) => {
  const cartItemId = parseInt(req.body.cart_item_id, 10);
  if (!cartItemId) {
    req.flash('error', 'Invalid request.');
    return res.redirect('/cart');
  }
  db.run('DELETE FROM cart_items WHERE id = ? AND customer_id = ?',
    [cartItemId, req.session.customerId]);
  req.flash('success', 'Item removed from cart.');
  res.redirect('/cart');
});

// ─── POST /cart/clear ────────────────────────────────────────────────────────
router.post('/clear', requireAuth, (req, res) => {
  db.run('DELETE FROM cart_items WHERE customer_id = ?', [req.session.customerId]);
  req.flash('success', 'Cart cleared.');
  res.redirect('/cart');
});

module.exports = router;
