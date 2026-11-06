'use strict';
/**
 * Functional Test Suite for Online Shopping and Order Management System
 * Run with: npm test
 * This performs API-level functional tests without a browser.
 */

const http = require('http');
const assert = require('assert');

const BASE_URL = 'http://localhost:3000';
let testsPassed = 0;
let testsFailed = 0;
const results = [];

// ─── HELPERS ────────────────────────────────────────────────────────────────
function makeRequest(options, body = null) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => { data += chunk; });
      res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body: data }));
    });
    req.on('error', reject);
    if (body) req.write(body);
    req.end();
  });
}

async function runTest(name, fn) {
  try {
    await fn();
    console.log(`  ✅ PASS | ${name}`);
    testsPassed++;
    results.push({ id: results.length + 1, name, status: 'PASS', error: null });
  } catch (err) {
    console.log(`  ❌ FAIL | ${name}`);
    console.log(`         | ${err.message}`);
    testsFailed++;
    results.push({ id: results.length + 1, name, status: 'FAIL', error: err.message });
  }
}

// ─── CHECK / START SERVER ─────────────────────────────────────────────────────
const { spawn } = require('child_process');
const path = require('path');
let serverProcess = null;

function pingServer() {
  return new Promise((resolve) => {
    const req = http.get(`${BASE_URL}/`, (res) => resolve(true));
    req.on('error', () => resolve(false));
    req.setTimeout(1000, () => {
      req.destroy();
      resolve(false);
    });
  });
}

async function ensureServerRunning() {
  const isUp = await pingServer();
  if (isUp) return;

  console.log('  [Auto-Start] Launching server on port 3000...');
  serverProcess = spawn('node', ['server.js'], {
    cwd: path.resolve(__dirname, '..'),
    stdio: 'ignore'
  });

  for (let i = 0; i < 20; i++) {
    await new Promise(r => setTimeout(r, 500));
    if (await pingServer()) {
      console.log('  [Auto-Start] Server ready.\n');
      return;
    }
  }
  throw new Error('Server failed to start on port 3000.');
}

// ─── TESTS ──────────────────────────────────────────────────────────────────
async function runAllTests() {
  console.log('\n=================================================');
  console.log('  ShopEase - Functional Test Suite');
  console.log('=================================================\n');

  try {
    await ensureServerRunning();
    console.log('  ✅ Server is running at ' + BASE_URL + '\n');
  } catch (e) {
    console.error('  ❌ ' + e.message);
    process.exit(1);
  }

  // ── TC-F01: Home page loads ─────────────────────────────────────────────
  await runTest('TC-F01: Home page loads (GET /)', async () => {
    const res = await makeRequest({ hostname: 'localhost', port: 3000, path: '/', method: 'GET' });
    assert.ok(res.status === 200 || res.status === 302, `Expected 200/302, got ${res.status}`);
  });

  // ── TC-F02: Register page loads ─────────────────────────────────────────
  await runTest('TC-F02: Customer registration page loads', async () => {
    const res = await makeRequest({ hostname: 'localhost', port: 3000, path: '/auth/register', method: 'GET' });
    assert.strictEqual(res.status, 200, `Expected 200, got ${res.status}`);
    assert.ok(res.body.includes('registerBtn') || res.body.includes('Create Account'), 'Registration form not found');
  });

  // ── TC-F03: Login page loads ─────────────────────────────────────────────
  await runTest('TC-F03: Customer login page loads', async () => {
    const res = await makeRequest({ hostname: 'localhost', port: 3000, path: '/auth/login', method: 'GET' });
    assert.strictEqual(res.status, 200, `Expected 200, got ${res.status}`);
    assert.ok(res.body.includes('loginBtn') || res.body.includes('Sign In'), 'Login form not found');
  });

  // ── TC-F04: Admin login page loads ───────────────────────────────────────
  await runTest('TC-F04: Admin login page loads', async () => {
    const res = await makeRequest({ hostname: 'localhost', port: 3000, path: '/admin/login', method: 'GET' });
    assert.strictEqual(res.status, 200, `Expected 200, got ${res.status}`);
    assert.ok(res.body.includes('adminLoginBtn') || res.body.includes('Admin Login'), 'Admin login form not found');
  });

  // ── TC-F05: Invalid login rejected ───────────────────────────────────────
  await runTest('TC-F05: Invalid login credentials rejected', async () => {
    const body = 'email=wrong@test.com&password=wrongpassword';
    const res = await makeRequest({
      hostname: 'localhost', port: 3000, path: '/auth/login', method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': Buffer.byteLength(body) }
    }, body);
    // Should redirect back to login (302)
    assert.strictEqual(res.status, 302, `Expected redirect 302, got ${res.status}`);
    assert.ok(res.headers.location && res.headers.location.includes('/login'), 'Should redirect to login page');
  });

  // ── TC-F06: Unauthenticated redirect to login ────────────────────────────
  await runTest('TC-F06: Protected routes redirect unauthenticated users', async () => {
    const paths = ['/products', '/cart', '/orders'];
    for (const path of paths) {
      const res = await makeRequest({ hostname: 'localhost', port: 3000, path, method: 'GET' });
      assert.strictEqual(res.status, 302, `${path} should redirect, got ${res.status}`);
      assert.ok(res.headers.location && res.headers.location.includes('/login'),
        `${path} should redirect to login, got ${res.headers.location}`);
    }
  });

  // ── TC-F07: Duplicate email registration blocked ──────────────────────────
  await runTest('TC-F07: Duplicate email registration is blocked', async () => {
    const body = 'name=Test+User&email=admin%40shopease.com&password=test123&confirmPassword=test123';
    const res = await makeRequest({
      hostname: 'localhost', port: 3000, path: '/auth/register', method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': Buffer.byteLength(body) }
    }, body);
    assert.strictEqual(res.status, 302, `Expected redirect, got ${res.status}`);
    // Should redirect back to register
    assert.ok(res.headers.location && res.headers.location.includes('/register'),
      `Expected redirect to register, got ${res.headers.location}`);
  });

  // ── TC-F08: Empty password registration blocked ───────────────────────────
  await runTest('TC-F08: Registration with short password rejected', async () => {
    const body = 'name=Test&email=newtest@test.com&password=12&confirmPassword=12';
    const res = await makeRequest({
      hostname: 'localhost', port: 3000, path: '/auth/register', method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': Buffer.byteLength(body) }
    }, body);
    assert.strictEqual(res.status, 302, `Expected redirect, got ${res.status}`);
  });

  // ── TC-F09: Admin login rejected with wrong credentials ───────────────────
  await runTest('TC-F09: Admin login with wrong credentials rejected', async () => {
    const body = 'email=admin%40shopease.com&password=wrongpassword';
    const res = await makeRequest({
      hostname: 'localhost', port: 3000, path: '/admin/login', method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': Buffer.byteLength(body) }
    }, body);
    assert.strictEqual(res.status, 302, `Expected redirect, got ${res.status}`);
    assert.ok(res.headers.location && res.headers.location.includes('/admin/login'),
      'Should redirect to admin login');
  });

  // ── TC-F10: Cart add requires authentication ──────────────────────────────
  await runTest('TC-F10: Add to cart requires authentication', async () => {
    const body = 'product_id=1&quantity=1';
    const res = await makeRequest({
      hostname: 'localhost', port: 3000, path: '/cart/add', method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': Buffer.byteLength(body) }
    }, body);
    assert.strictEqual(res.status, 302, `Expected redirect, got ${res.status}`);
    assert.ok(res.headers.location && res.headers.location.includes('/login'), 'Should redirect to login');
  });

  // ── TC-F11: Checkout requires authentication ──────────────────────────────
  await runTest('TC-F11: Checkout requires authentication', async () => {
    const res = await makeRequest({ hostname: 'localhost', port: 3000, path: '/orders/checkout/review', method: 'GET' });
    assert.strictEqual(res.status, 302, `Expected redirect, got ${res.status}`);
    assert.ok(res.headers.location && res.headers.location.includes('/login'), 'Should redirect to login');
  });

  // ── TC-F12: Admin dashboard requires admin auth ───────────────────────────
  await runTest('TC-F12: Admin dashboard blocks non-admin access', async () => {
    const res = await makeRequest({ hostname: 'localhost', port: 3000, path: '/admin/dashboard', method: 'GET' });
    assert.strictEqual(res.status, 302, `Expected redirect, got ${res.status}`);
  });

  // ── TC-F13: 404 for unknown routes ───────────────────────────────────────
  await runTest('TC-F13: Unknown routes return 404', async () => {
    const res = await makeRequest({ hostname: 'localhost', port: 3000, path: '/this-route-does-not-exist', method: 'GET' });
    assert.strictEqual(res.status, 404, `Expected 404, got ${res.status}`);
  });

  // ── Print Summary ─────────────────────────────────────────────────────────
  console.log('\n=================================================');
  console.log('  TEST RESULTS SUMMARY');
  console.log('=================================================');
  console.log(`  Total  : ${results.length}`);
  console.log(`  Passed : ${testsPassed} ✅`);
  console.log(`  Failed : ${testsFailed} ❌`);
  console.log('\n  Detailed Results:');
  results.forEach(r => {
    const icon = r.status === 'PASS' ? '✅' : '❌';
    console.log(`  ${icon} TC-F${String(r.id).padStart(2,'0')}: ${r.name}`);
    if (r.error) console.log(`         Error: ${r.error}`);
  });
  console.log('=================================================\n');

  if (serverProcess) {
    serverProcess.kill();
  }

  if (testsFailed > 0) {
    process.exit(1);
  }
}

runAllTests().catch(err => {
  if (serverProcess) serverProcess.kill();
  console.error('Test runner error:', err);
  process.exit(1);
});
