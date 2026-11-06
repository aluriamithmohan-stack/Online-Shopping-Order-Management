'use strict';

/**
 * database/db.js
 * SQLite database layer using sql.js (pure JavaScript WebAssembly, no native deps).
 * Provides a synchronous-style API compatible with the Express route files.
 */

const path      = require('path');
const fs        = require('fs');
const bcryptjs  = require('bcryptjs');
const initSqlJs = require('sql.js');

// ── Resolve database file path ───────────────────────────────────────────────
const DB_PATH = process.env.DB_PATH
  ? path.resolve(process.env.DB_PATH)
  : path.join(__dirname, 'shop.db');

// ── Ensure directory exists ──────────────────────────────────────────────────
const dbDir = path.dirname(DB_PATH);
if (!fs.existsSync(dbDir)) {
  fs.mkdirSync(dbDir, { recursive: true });
}

// ── Module-level DB reference ─────────────────────────────────────────────────
let DB = null;

// ── Persist to disk ───────────────────────────────────────────────────────────
function persist() {
  const data   = DB.export();
  const buffer = Buffer.from(data);
  fs.writeFileSync(DB_PATH, buffer);
}

// ── Low-level helpers (bypass DB_READY check, used only during init) ──────────
function _run(sql, params = []) {
  DB.run(sql, params.length > 0 ? params : undefined);
}

function _get(sql, params = []) {
  const stmt = DB.prepare(sql);
  if (params.length > 0) stmt.bind(params);
  const hasRow = stmt.step();
  if (!hasRow) { stmt.free(); return null; }
  const row = stmt.getAsObject();
  stmt.free();
  return row;
}

function _all(sql, params = []) {
  let results;
  if (params.length > 0) {
    results = DB.exec(sql, params);
  } else {
    results = DB.exec(sql);
  }
  if (!results || results.length === 0) return [];
  const { columns, values } = results[0];
  return values.map(row => {
    const obj = {};
    columns.forEach((col, i) => { obj[col] = row[i]; });
    return obj;
  });
}

// ── Public API ─────────────────────────────────────────────────────────────────
let IN_TRANSACTION = false;

function run(sql, params = []) {
  assertReady();
  _run(sql, params);
  if (!IN_TRANSACTION) {
    persist();
  }
  const r = DB.exec('SELECT last_insert_rowid() AS id');
  const lastId = r.length > 0 ? r[0].values[0][0] : null;
  return { lastInsertRowid: lastId };
}

function get(sql, params = []) {
  assertReady();
  return _get(sql, params);
}

function all(sql, params = []) {
  assertReady();
  return _all(sql, params);
}

function exec(sql) {
  assertReady();
  DB.run(sql);
  persist();
}

function prepare(sql) {
  return {
    run(...args) {
      return run(sql, args.flat());
    },
    get(...args) {
      return get(sql, args.flat());
    },
    all(...args) {
      return all(sql, args.flat());
    }
  };
}

function transaction(fn) {
  return function (...args) {
    assertReady();
    IN_TRANSACTION = true;
    _run('BEGIN TRANSACTION');
    try {
      const result = fn(...args);
      _run('COMMIT');
      IN_TRANSACTION = false;
      persist();
      return result;
    } catch (err) {
      _run('ROLLBACK');
      IN_TRANSACTION = false;
      throw err;
    }
  };
}

let DB_READY = false;
function assertReady() {
  if (!DB_READY || !DB) throw new Error('[DB] Database not yet initialized.');
}

// ── CREATE TABLES ─────────────────────────────────────────────────────────────
function createTables() {
  _run(`CREATE TABLE IF NOT EXISTS customers (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    name       TEXT    NOT NULL,
    email      TEXT    NOT NULL UNIQUE,
    password   TEXT    NOT NULL,
    is_admin   INTEGER NOT NULL DEFAULT 0,
    created_at TEXT    NOT NULL DEFAULT (datetime('now'))
  )`);

  _run(`CREATE TABLE IF NOT EXISTS products (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    description TEXT    NOT NULL,
    price       REAL    NOT NULL,
    stock       INTEGER NOT NULL DEFAULT 0,
    image_url   TEXT    NOT NULL DEFAULT '/images/default-product.png',
    category    TEXT    NOT NULL DEFAULT 'General',
    is_active   INTEGER NOT NULL DEFAULT 1,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now'))
  )`);

  _run(`CREATE TABLE IF NOT EXISTS cart_items (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    product_id  INTEGER NOT NULL,
    quantity    INTEGER NOT NULL DEFAULT 1,
    added_at    TEXT    NOT NULL DEFAULT (datetime('now')),
    UNIQUE(customer_id, product_id)
  )`);

  _run(`CREATE TABLE IF NOT EXISTS orders (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    order_code       TEXT    NOT NULL UNIQUE,
    customer_id      INTEGER NOT NULL,
    total_amount     REAL    NOT NULL,
    status           TEXT    NOT NULL DEFAULT 'Confirmed',
    shipping_address TEXT    NOT NULL DEFAULT 'Default Address',
    created_at       TEXT    NOT NULL DEFAULT (datetime('now'))
  )`);

  _run(`CREATE TABLE IF NOT EXISTS order_items (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id     INTEGER NOT NULL,
    product_id   INTEGER NOT NULL,
    product_name TEXT    NOT NULL,
    quantity     INTEGER NOT NULL,
    unit_price   REAL    NOT NULL
  )`);
}

// ── SEEDS ─────────────────────────────────────────────────────────────────────
function seedAdmin() {
  const email = process.env.ADMIN_EMAIL || 'admin@shopease.com';
  const pass  = process.env.ADMIN_PASSWORD || 'Admin@123';
  const existing = _get('SELECT id FROM customers WHERE email = ?', [email]);
  if (!existing) {
    const hashed = bcryptjs.hashSync(pass, 10);
    _run('INSERT INTO customers (name, email, password, is_admin) VALUES (?, ?, ?, 1)',
      ['ShopEase Admin', email, hashed]);
    console.log(`[DB] Admin created: ${email}`);
  }
}

function seedDemoCustomer() {
  const email = 'demo@shopease.com';
  const existing = _get('SELECT id FROM customers WHERE email = ?', [email]);
  if (!existing) {
    const hashed = bcryptjs.hashSync('Demo@123', 10);
    _run('INSERT INTO customers (name, email, password, is_admin) VALUES (?, ?, ?, 0)',
      ['Demo Customer', email, hashed]);
    console.log(`[DB] Demo customer created: ${email}`);
  }
}

function seedProducts() {
  const countRes = DB.exec('SELECT COUNT(*) FROM products');
  const cnt = countRes.length > 0 ? countRes[0].values[0][0] : 0;
  if (cnt > 0) return;

  const prods = [
    ['Apple iPhone 15 Pro', '6.1-inch Super Retina XDR display, A17 Pro chip, 48MP main camera, Titanium design.', 134900, 25, '/images/default-product.png', 'Electronics'],
    ['Samsung Galaxy S24 Ultra', '6.8-inch QHD+ Dynamic AMOLED, Snapdragon 8 Gen 3, 200MP camera, built-in S Pen.', 129999, 20, '/images/default-product.png', 'Electronics'],
    ['Sony WH-1000XM5 Headphones', 'Industry-leading noise cancellation, 30-hour battery life, Multipoint connection.', 26990, 40, '/images/default-product.png', 'Electronics'],
    ['Apple MacBook Air M3', '13.6-inch Liquid Retina display, M3 chip, 8GB RAM, 256GB SSD, 18-hour battery.', 114900, 15, '/images/default-product.png', 'Computers'],
    ['Nike Air Max 270', 'Lightweight running shoes with Max Air unit, breathable mesh upper, rubber outsole.', 10995, 60, '/images/default-product.png', 'Footwear'],
    ["Levi's 511 Slim Jeans", 'Classic slim fit denim jeans, 99% cotton, available in multiple washes.', 3999, 80, '/images/default-product.png', 'Clothing'],
    ['Instant Pot Duo 7-in-1', 'Electric pressure cooker, slow cooker, rice cooker, steamer, saute and yogurt maker.', 8999, 35, '/images/default-product.png', 'Kitchen'],
    ['Kindle Paperwhite', '6.8-inch 300ppi display, adjustable warm light, waterproof, 10 weeks battery life.', 15999, 50, '/images/default-product.png', 'Electronics'],
    ['Bosch Cordless Drill', '18V brushless motor, 2-speed gearbox, LED light, includes 2 batteries and charger.', 7490, 30, '/images/default-product.png', 'Tools'],
    ['The Alchemist - Paulo Coelho', 'International bestseller - a philosophical novel about following your dreams.', 299, 100, '/images/default-product.png', 'Books']
  ];

  for (const p of prods) {
    _run('INSERT INTO products (name, description, price, stock, image_url, category) VALUES (?, ?, ?, ?, ?, ?)', p);
  }
  console.log(`[DB] ${prods.length} sample products seeded.`);
}

// ── INIT (async): returns a Promise that resolves when DB is ready ─────────────
const dbReadyPromise = initSqlJs().then((SQL) => {
  // Load from disk or create new
  if (fs.existsSync(DB_PATH)) {
    const fileBuffer = fs.readFileSync(DB_PATH);
    DB = new SQL.Database(fileBuffer);
    console.log(`[DB] Loaded database from: ${DB_PATH}`);
  } else {
    DB = new SQL.Database();
    console.log(`[DB] New database created at: ${DB_PATH}`);
  }

  // Setup
  createTables();
  seedAdmin();
  seedDemoCustomer();
  seedProducts();

  // Persist seeded data
  persist();

  DB_READY = true;
  console.log('[DB] Ready.');
  return DB;
});

module.exports = { dbReadyPromise, run, get, all, exec, prepare, transaction, persist };
