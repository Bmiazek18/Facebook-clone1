// public/session-worker.js

const ports = new Set();
let expiresAt = null;
let lastActivity = Date.now();
const IDLE_TIMEOUT = 5 * 60 * 1000; // 5 minutes
let refreshTimer = null;

// Helper to broadcast messages to all connected ports
function broadcast(message) {
  for (const port of ports) {
    try {
      port.postMessage(message);
    } catch (err) {
      ports.delete(port);
    }
  }
}

self.onconnect = function (e) {
  const port = e.ports[0];
  ports.add(port);
  console.log(`[Session Worker] Tab connected. Total tabs: ${ports.size}`);

  // Send current status to the newly connected tab
  port.postMessage({
    type: 'STATUS',
    expiresAt: expiresAt,
    lastActivity: lastActivity
  });

  port.onmessage = function (event) {
    const data = event.data;

    switch (data.type) {
      case 'INIT':
        if (data.expiresAt) {
          expiresAt = parseInt(data.expiresAt, 10);
        }
        break;

      case 'ACTIVITY':
        lastActivity = Date.now();
        break;

      case 'FORCE_REFRESH':
        triggerRefresh(true);
        break;

      case 'CLOSE':
        ports.delete(port);
        console.log(`[Session Worker] Tab closed. Remaining tabs: ${ports.size}`);
        if (ports.size === 0) {
          stopTimer();
        }
        break;
    }
  };

  // Start the background refresh timer if this is the first tab
  if (ports.size === 1) {
    startTimer();
  }

  port.start();
};

async function triggerRefresh(force = false) {
  if (!expiresAt && !force) return;

  const timeLeft = expiresAt ? (expiresAt - Date.now()) : 0;

  // Proactively refresh 45 seconds before expiry
  if (force || (timeLeft > 0 && timeLeft <= 45000)) {
    console.log(`[Session Worker] Token expires in ${Math.round(timeLeft / 1000)}s. Performing silent refresh...`);
    broadcast({ type: 'REFRESH_START' });

    try {
      const res = await fetch('/api/auth/refresh', { method: 'POST' });
      if (res.ok) {
        const data = await res.json();
        if (data.success && data.expiresAt) {
          expiresAt = parseInt(data.expiresAt, 10);
          console.log(`[Session Worker] Refresh successful. New expiry: ${expiresAt}`);
          broadcast({ type: 'REFRESH_SUCCESS', expiresAt: expiresAt });
        } else {
          console.warn('[Session Worker] Refresh endpoint returned failure.');
          broadcast({ type: 'REFRESH_FAILURE' });
        }
      } else {
        console.error(`[Session Worker] Refresh failed with status: ${res.status}`);
        broadcast({ type: 'REFRESH_FAILURE' });
      }
    } catch (err) {
      console.error('[Session Worker] Network error during refresh:', err);
      broadcast({ type: 'REFRESH_FAILURE' });
    }
  }
}

function startTimer() {
  if (refreshTimer) return;
  console.log('[Session Worker] Starting background session refresh timer.');
  refreshTimer = setInterval(() => {
    // Only refresh if the user is active (has interacted with at least one tab within IDLE_TIMEOUT)
    if (Date.now() - lastActivity < IDLE_TIMEOUT) {
      triggerRefresh();
    } else {
      console.log('[Session Worker] User is idle. Pausing refresh checks.');
    }
  }, 10000);
}

function stopTimer() {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
    console.log('[Session Worker] Stopped session refresh timer (no active tabs).');
  }
  expiresAt = null;
}
