// public/mqtt-worker.js

importScripts('/mqtt.min.js');

const ports = new Set();
let mqttClient = null;
let connectionState = 'DISCONNECTED'; // DISCONNECTED, CONNECTING, CONNECTED, OFFLINE
let currentUserId = null;
let currentBrokerUrl = null;

// Helper to broadcast message to all active ports
function broadcast(message) {
  for (const port of ports) {
    try {
      port.postMessage(message);
    } catch (err) {
      // Port might be dead, remove it
      ports.delete(port);
    }
  }
}

self.onconnect = function (e) {
  const port = e.ports[0];
  ports.add(port);
  console.log(`[MQTT Worker] New tab connected. Total tabs: ${ports.size}`);

  // Send current status to the newly connected tab
  port.postMessage({
    type: 'STATUS',
    state: connectionState,
    userId: currentUserId
  });

  port.onmessage = function (event) {
    const data = event.data;

    switch (data.type) {
      case 'CONNECT':
        handleConnect(data.userId, data.brokerUrl, data.ticket);
        break;

      case 'PUBLISH':
        handlePublish(data.topic, data.payload, data.options);
        break;

      case 'CLOSE':
        ports.delete(port);
        console.log(`[MQTT Worker] Tab closed. Remaining tabs: ${ports.size}`);
        if (ports.size === 0) {
          handleDisconnect();
        }
        break;
    }
  };

  port.start();
};

function handleConnect(userId, brokerUrl, ticket) {
  currentUserId = userId;
  currentBrokerUrl = brokerUrl;

  if (!ticket || typeof ticket !== 'string' || ticket.trim() === '') {
    connectionState = 'OFFLINE';
    broadcast({ type: 'STATUS', state: connectionState, userId });
    return;
  }

  if (mqttClient && mqttClient.connected) {
    connectionState = 'CONNECTED';
    broadcast({ type: 'STATUS', state: connectionState, userId });
    return;
  }

  if (connectionState === 'CONNECTING') {
    return;
  }

  connectionState = 'CONNECTING';
  broadcast({ type: 'STATUS', state: connectionState, userId });

  console.log(`[MQTT Worker] Connecting to: ${brokerUrl}`);

  try {
    const client = self.mqtt.connect(brokerUrl, {
      clientId: 'worker-id-' + Math.random().toString(36).substring(7),
      username: ticket,
      password: ticket,
      reconnectPeriod: 0,
    });

    mqttClient = client;

    client.on('connect', () => {
      connectionState = 'CONNECTED';
      console.log('[MQTT Worker] Connected to broker.');
      client.subscribe('chat/messages/user/' + userId);
      broadcast({ type: 'STATUS', state: connectionState, userId });
    });

    client.on('offline', () => {
      connectionState = 'OFFLINE';
      console.warn('[MQTT Worker] Connection went offline. Requesting new ticket...');
      broadcast({ type: 'STATUS', state: connectionState, userId });
      client.end(true);
      mqttClient = null;
    });

    client.on('error', (err) => {
      connectionState = 'OFFLINE';
      console.error('[MQTT Worker] Connection error:', err);
      broadcast({ type: 'STATUS', state: connectionState, userId });
      client.end(true);
      mqttClient = null;
    });

    client.on('message', (topic, messageBuffer) => {
      try {
        const payload = JSON.parse(messageBuffer.toString());
        broadcast({ type: 'MESSAGE', topic, payload });
      } catch (err) {
        console.error('[MQTT Worker] Failed to parse message:', err);
      }
    });

  } catch (err) {
    connectionState = 'OFFLINE';
    console.error('[MQTT Worker] Init failed:', err);
    broadcast({ type: 'STATUS', state: connectionState, userId });
  }
}

function handlePublish(topic, payload, options) {
  if (mqttClient && mqttClient.connected) {
    mqttClient.publish(topic, JSON.stringify(payload), options);
  } else {
    console.warn('[MQTT Worker] Cannot publish, client not connected.');
  }
}

function handleDisconnect() {
  if (mqttClient) {
    mqttClient.end(true);
    mqttClient = null;
  }
  connectionState = 'DISCONNECTED';
  currentUserId = null;
  currentBrokerUrl = null;
  console.log('[MQTT Worker] Disconnected from broker since all tabs closed.');
}
