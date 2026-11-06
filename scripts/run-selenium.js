const { spawn } = require('child_process');
const http = require('http');
const path = require('path');

const testName = process.argv[2] || 'MandatoryTests';
const javaHome = process.env.JAVA_HOME && process.env.JAVA_HOME.includes('jdk-25')
  ? process.env.JAVA_HOME
  : 'C:\\Program Files\\Java\\jdk-25';
const mavenPath = 'C:\\Users\\amith\\maven\\apache-maven-3.9.9\\bin';

const baseUrl = process.env.BASE_URL || 'http://localhost:3000';

function checkServer() {
  return new Promise((resolve) => {
    const req = http.get(baseUrl, (res) => {
      resolve(true);
    });
    req.on('error', () => {
      resolve(false);
    });
    req.setTimeout(1000, () => {
      req.destroy();
      resolve(false);
    });
  });
}

async function waitForServer(retries = 30) {
  for (let i = 0; i < retries; i++) {
    const isUp = await checkServer();
    if (isUp) return true;
    await new Promise((r) => setTimeout(r, 500));
  }
  return false;
}

async function main() {
  let serverProcess = null;
  const isAlreadyUp = await checkServer();

  if (!isAlreadyUp) {
    console.log('[Runner] Server not detected on port 3000. Starting node server.js...');
    serverProcess = spawn('node', ['server.js'], {
      cwd: path.resolve(__dirname, '..'),
      stdio: 'inherit'
    });

    const ready = await waitForServer();
    if (!ready) {
      console.error('[Runner] Failed to start server within timeout.');
      if (serverProcess) serverProcess.kill();
      process.exit(1);
    }
    console.log('[Runner] Server is up and ready.');
  } else {
    console.log('[Runner] Existing server detected on port 3000.');
  }

  const env = {
    ...process.env,
    JAVA_HOME: javaHome,
    PATH: `${mavenPath};${process.env.PATH || ''}`
  };

  const mvnCmd = process.platform === 'win32' ? 'mvn.cmd' : 'mvn';

  console.log(`[Runner] Executing test: ${testName}...`);
  const child = spawn(mvnCmd, [
    '-f', 'selenium-tests/pom.xml',
    'test',
    `-Dtest=${testName}`,
    `-DbaseUrl=${baseUrl}`
  ], { stdio: 'inherit', env, shell: true });

  child.on('exit', (code) => {
    if (serverProcess) {
      console.log('[Runner] Stopping background server process...');
      serverProcess.kill();
    }
    process.exit(code || 0);
  });
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
