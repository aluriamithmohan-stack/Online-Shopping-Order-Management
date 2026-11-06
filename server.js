'use strict';

require('dotenv').config();

const express        = require('express');
const session        = require('express-session');
const flash          = require('connect-flash');
const methodOverride = require('method-override');
const path           = require('path');

const app  = express();
const PORT = process.env.PORT || 3000;
const HOST = process.env.HOST || '0.0.0.0';

// ─────────────────────────────────────────────
// VIEW ENGINE
// ─────────────────────────────────────────────
app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));

// ─────────────────────────────────────────────
// MIDDLEWARE
// ─────────────────────────────────────────────
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(methodOverride('_method'));

app.use(session({
  secret: process.env.SESSION_SECRET || 'shopease-dev-secret-change-in-production',
  resave: false,
  saveUninitialized: false,
  cookie: {
    httpOnly: true,
    maxAge: 24 * 60 * 60 * 1000
  }
}));

app.use(flash());

// Make session data available in all views
app.use((req, res, next) => {
  res.locals.session      = req.session;
  res.locals.customerName = req.session.customerName || null;
  res.locals.isAdmin      = req.session.isAdmin || false;
  res.locals.isLoggedIn   = !!req.session.customerId;
  next();
});

// ─────────────────────────────────────────────
// ROUTES
// ─────────────────────────────────────────────
app.use('/auth',     require('./routes/auth'));
app.use('/products', require('./routes/products'));
app.use('/cart',     require('./routes/cart'));
app.use('/orders',   require('./routes/orders'));
app.use('/admin',    require('./routes/admin'));

// Home
app.get('/', (req, res) => {
  if (req.session && req.session.customerId) {
    if (req.session.isAdmin) return res.redirect('/admin/dashboard');
    return res.redirect('/products');
  }
  res.render('index', { title: 'Welcome to ShopEase' });
});

// 404
app.use((req, res) => {
  res.status(404).render('404', { title: 'Page Not Found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('[SERVER ERROR]', err);
  res.status(500).render('error', {
    title: 'Server Error',
    message: process.env.NODE_ENV === 'development' ? err.message : 'Something went wrong.'
  });
});

// ─────────────────────────────────────────────
// INITIALIZE DATABASE THEN START SERVER
// ─────────────────────────────────────────────
const { dbReadyPromise } = require('./database/db');

dbReadyPromise.then(() => {
  app.listen(PORT, HOST, () => {
    console.log(`\n🚀 ShopEase is running at http://localhost:${PORT}`);
    console.log(`   Environment : ${process.env.NODE_ENV || 'development'}`);
    console.log(`   Admin login : http://localhost:${PORT}/admin/login`);
    console.log(`   Press Ctrl+C to stop.\n`);
  });
}).catch(err => {
  console.error('[FATAL] Failed to initialize database:', err);
  process.exit(1);
});

module.exports = app;
