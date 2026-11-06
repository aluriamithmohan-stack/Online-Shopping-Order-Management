'use strict';

const express = require('express');
const router  = express.Router();
const db      = require('../database/db');
const { requireAuth } = require('../middleware/auth');

function generateOrderCode() {
  const date = new Date().toISOString().slice(0, 10).replace(/-/g, '');
  const rand = Math.random().toString(36).substring(2, 6).toUpperCase();
  return `ORD-${date}-${rand}`;
}

// ─── GET /orders ─────────────────────────────────────────────────────────────
router.get('/', requireAuth, (req, res) => {
  const orders = db.all(`
    SELECT o.*, COUNT(oi.id) AS item_count
    FROM orders o
    LEFT JOIN order_items oi ON oi.order_id = o.id
    WHERE o.customer_id = ?
    GROUP BY o.id
    ORDER BY o.created_at DESC
  `, [req.session.customerId]);

  res.render('orders', {
    title: 'My Orders',
    orders,
    success: req.flash('success'),
    error: req.flash('error')
  });
});

// ─── GET /orders/checkout/review ─────────────────────────────────────────────
router.get('/checkout/review', requireAuth, (req, res) => {
  const cartItems = db.all(`
    SELECT ci.id AS cart_item_id, ci.quantity,
           p.id AS product_id, p.name, p.price, p.stock, p.image_url,
           (p.price * ci.quantity) AS subtotal
    FROM cart_items ci
    JOIN products p ON p.id = ci.product_id
    WHERE ci.customer_id = ? AND p.is_active = 1
    ORDER BY ci.added_at DESC
  `, [req.session.customerId]);

  if (cartItems.length === 0) {
    req.flash('error', 'Your cart is empty. Add some products before checkout.');
    return res.redirect('/cart');
  }

  const total = cartItems.reduce((s, i) => s + i.subtotal, 0);
  res.render('checkout', {
    title: 'Checkout',
    cartItems,
    total,
    success: req.flash('success'),
    error: req.flash('error')
  });
});

// ─── POST /orders/checkout/place ─────────────────────────────────────────────
router.post('/checkout/place', requireAuth, (req, res) => {
  const customerId      = req.session.customerId;
  const shippingAddress = (req.body.shipping_address || 'Default Address').trim();

  const placeOrder = db.transaction(() => {
    const cartItems = db.all(`
      SELECT ci.quantity, p.id AS product_id, p.name, p.price, p.stock, p.is_active
      FROM cart_items ci
      JOIN products p ON p.id = ci.product_id
      WHERE ci.customer_id = ?
    `, [customerId]);

    if (cartItems.length === 0) throw new Error('EMPTY_CART');

    for (const item of cartItems) {
      if (!item.is_active) throw new Error(`UNAVAILABLE:${item.name}`);
      if (item.quantity > item.stock) throw new Error(`STOCK:${item.name}:${item.stock}`);
    }

    const totalAmount = cartItems.reduce((s, i) => s + i.price * i.quantity, 0);
    const orderCode   = generateOrderCode();

    db.run(
      'INSERT INTO orders (order_code, customer_id, total_amount, shipping_address) VALUES (?, ?, ?, ?)',
      [orderCode, customerId, totalAmount, shippingAddress]
    );

    // Get the last inserted order ID
    const orderRow = db.get('SELECT id FROM orders WHERE order_code = ?', [orderCode]);
    const orderId  = orderRow.id;

    for (const item of cartItems) {
      db.run(
        'INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price) VALUES (?, ?, ?, ?, ?)',
        [orderId, item.product_id, item.name, item.quantity, item.price]
      );
      db.run('UPDATE products SET stock = stock - ? WHERE id = ?', [item.quantity, item.product_id]);
    }

    db.run('DELETE FROM cart_items WHERE customer_id = ?', [customerId]);
    return { orderId, orderCode, totalAmount };
  });

  try {
    const result = placeOrder();
    req.flash('success', `Order ${result.orderCode} placed successfully!`);
    res.redirect(`/orders/confirmation/${result.orderId}`);
  } catch (err) {
    if (err.message === 'EMPTY_CART') {
      req.flash('error', 'Your cart is empty.');
      return res.redirect('/cart');
    }
    if (err.message.startsWith('UNAVAILABLE:')) {
      req.flash('error', `Product "${err.message.split(':')[1]}" is no longer available.`);
      return res.redirect('/cart');
    }
    if (err.message.startsWith('STOCK:')) {
      const [, name, stock] = err.message.split(':');
      req.flash('error', `Insufficient stock for "${name}". Only ${stock} units available.`);
      return res.redirect('/cart');
    }
    console.error('[ORDER] Checkout error:', err);
    req.flash('error', 'Failed to place order. Please try again.');
    res.redirect('/cart');
  }
});

// ─── GET /orders/confirmation/:id ────────────────────────────────────────────
router.get('/confirmation/:id', requireAuth, (req, res) => {
  const order = db.get(
    'SELECT * FROM orders WHERE id = ? AND customer_id = ?',
    [req.params.id, req.session.customerId]
  );
  if (!order) {
    req.flash('error', 'Order not found.');
    return res.redirect('/orders');
  }
  const items = db.all('SELECT * FROM order_items WHERE order_id = ?', [order.id]);
  res.render('order-confirmation', {
    title: 'Order Confirmed!',
    order, items,
    success: req.flash('success'),
    error: req.flash('error')
  });
});

// ─── GET /orders/:id ─────────────────────────────────────────────────────────
router.get('/:id', requireAuth, (req, res) => {
  const order = db.get(
    'SELECT * FROM orders WHERE id = ? AND customer_id = ?',
    [req.params.id, req.session.customerId]
  );
  if (!order) {
    req.flash('error', 'Order not found.');
    return res.redirect('/orders');
  }
  const items = db.all('SELECT * FROM order_items WHERE order_id = ?', [order.id]);
  res.render('order-detail', {
    title: `Order ${order.order_code}`,
    order, items,
    success: req.flash('success'),
    error: req.flash('error')
  });
});

module.exports = router;
