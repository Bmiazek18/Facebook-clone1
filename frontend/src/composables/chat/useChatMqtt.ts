import { ref, onMounted, onUnmounted } from 'vue'
import mqtt from 'mqtt'
import { useGenerateTicket } from '@/composables/shared/useGenerateTicket'

export function useChatMqtt() {
  const { generateTicket } = useGenerateTicket()
  const config = useRuntimeConfig()
  const isMqttConnected = ref(false)
  const mqttClientId = ref('')
  let mqttClient: mqtt.MqttClient | null = null
  let worker: SharedWorker | null = null
  let workerPort: MessagePort | null = null
  let messageCallback: ((topic: string, payload: any) => void) | null = null
  let heartbeatInterval: any = null
  let reconnectTimeout: any = null
  let retryCount = 0
  let activeUserId: string | null = null

  // Direct connection offline queue (fallback when SharedWorker is not active)
  const directOutboundQueue: Array<{ topic: string; payload: any; options?: any }> = []

  function getBackoffDelay(): number {
    const base = Math.min(30000, 1000 * Math.pow(1.5, retryCount))
    const jitter = Math.random() * 500
    retryCount++
    return base + jitter
  }

  function resetBackoff() {
    retryCount = 0
  }

  function flushDirectQueue() {
    if (!mqttClient || !mqttClient.connected || directOutboundQueue.length === 0) return
    console.log(`[Direct MQTT] Flushing ${directOutboundQueue.length} queued messages...`)
    while (directOutboundQueue.length > 0 && mqttClient && mqttClient.connected) {
      const item = directOutboundQueue.shift()
      if (item) {
        try {
          mqttClient.publish(item.topic, JSON.stringify(item.payload), item.options)
        } catch (err) {
          console.error('[Direct MQTT] Failed to flush message:', err)
          directOutboundQueue.unshift(item)
          break
        }
      }
    }
  }

  // Handle network online event
  const handleNetworkOnline = () => {
    console.log('Frontend MQTT: Network restored (online event). Triggering immediate reconnect...')
    resetBackoff()
    if (activeUserId && !isMqttConnected.value) {
      if (reconnectTimeout) clearTimeout(reconnectTimeout)
      if (workerPort) {
        reconnectWorker(activeUserId)
      } else if (messageCallback) {
        connectMqtt(activeUserId, messageCallback)
      }
    }
  }

  if (typeof window !== 'undefined') {
    window.addEventListener('online', handleNetworkOnline)
  }

  async function connectMqtt(
    userId: string,
    onMessage: (topic: string, payload: any) => void
  ) {
    messageCallback = onMessage
    activeUserId = userId

    if (!userId || userId === '0' || userId === '1') return

    // --- CHECK FOR SHARED WORKER SUPPORT ---
    if (typeof window !== 'undefined' && 'SharedWorker' in window) {
      if (!worker) {
        console.log('Frontend MQTT: Using SharedWorker for WebSocket connection.')
        try {
          worker = new SharedWorker('/mqtt-worker.js')
          workerPort = worker.port

          workerPort.onmessage = (event) => {
            const data = event.data
            if (data.type === 'STATUS') {
              const connected = (data.state === 'CONNECTED')
              isMqttConnected.value = connected
              console.log(`Frontend MQTT (Worker status): ${data.state}`)

              if (connected) {
                resetBackoff()
              } else if (data.state === 'OFFLINE' || data.state === 'DISCONNECTED') {
                const delay = getBackoffDelay()
                console.warn(`Frontend MQTT: Worker disconnected. Reconnecting in ${(delay / 1000).toFixed(1)}s (retry #${retryCount})...`)
                if (reconnectTimeout) clearTimeout(reconnectTimeout)
                reconnectTimeout = setTimeout(() => reconnectWorker(userId), delay)
              }
            } else if (data.type === 'MESSAGE') {
              if (messageCallback) {
                messageCallback(data.topic, data.payload)
              }
            }
          }

          workerPort.start()

          window.addEventListener('beforeunload', () => {
            if (workerPort) {
              workerPort.postMessage({ type: 'CLOSE' })
            }
          })
        } catch (err) {
          console.error('Frontend MQTT: Failed to initialize SharedWorker, falling back to direct connection:', err)
          worker = null
          workerPort = null
        }
      }

      if (workerPort) {
        if (!isMqttConnected.value) {
          await reconnectWorker(userId)
        }
        return
      }
    }

    // --- FALLBACK: DIRECT MQTT CONNECTION ---
    if (mqttClient) {
      mqttClient.end(true)
      mqttClient = null
    }

    try {
      const ticket = await generateTicket(userId)
      if (!ticket || !ticket.trim()) {
        throw new Error('Empty MQTT ticket')
      }

      const brokerUrl = config.public.mqttUrl
      console.log('Connecting directly to MQTT broker with one-time ticket')

      const cId = 'client-id-' + Math.random().toString(36).substring(7)
      mqttClientId.value = cId
      const client = mqtt.connect(brokerUrl, {
        clientId: cId,
        username: ticket,
        password: ticket,
        reconnectPeriod: 0,
        keepalive: 60,
      })

      mqttClient = client

      client.on('connect', () => {
        isMqttConnected.value = true
        resetBackoff()
        console.log('Frontend MQTT: Connected directly to broker.')
        client.subscribe('chat/messages/user/' + userId)
        client.subscribe('user/' + userId + '/notifications')

        // Flush offline queue
        flushDirectQueue()

        // Start presence heartbeat locally when connected directly
        if (heartbeatInterval) {
          clearInterval(heartbeatInterval)
        }

        try {
          client.publish('user/presence/heartbeat', JSON.stringify({ userId }))
        } catch (err) {
          console.error('[Direct MQTT] Failed to send immediate heartbeat:', err)
        }

        heartbeatInterval = setInterval(() => {
          if (client && client.connected) {
            try {
              client.publish('user/presence/heartbeat', JSON.stringify({ userId }))
            } catch (err) {
              console.error('[Direct MQTT] Failed to send heartbeat:', err)
            }
          }
        }, 30000)
      })

      client.on('offline', () => {
        isMqttConnected.value = false
        if (heartbeatInterval) {
          clearInterval(heartbeatInterval)
          heartbeatInterval = null
        }
        const delay = getBackoffDelay()
        console.warn(`Frontend MQTT: Connection lost. Reconnecting in ${(delay / 1000).toFixed(1)}s (retry #${retryCount})...`)
        client.end(true)
        if (reconnectTimeout) clearTimeout(reconnectTimeout)
        reconnectTimeout = setTimeout(() => connectMqtt(userId, onMessage), delay)
      })

      client.on('error', (err) => {
        console.error('Frontend MQTT: Direct connection error:', err)
        if (heartbeatInterval) {
          clearInterval(heartbeatInterval)
          heartbeatInterval = null
        }
        client.end(true)
        isMqttConnected.value = false
        const delay = getBackoffDelay()
        if (reconnectTimeout) clearTimeout(reconnectTimeout)
        reconnectTimeout = setTimeout(() => connectMqtt(userId, onMessage), delay)
      })

      client.on('message', (topic, messageBuffer) => {
        try {
          const payload = JSON.parse(messageBuffer.toString())
          onMessage(topic, payload)
        } catch (err) {
          console.error('Frontend MQTT: Parse error:', err)
        }
      })
    } catch (err) {
      console.error('Failed to establish direct MQTT connection:', err)
      const delay = getBackoffDelay()
      if (reconnectTimeout) clearTimeout(reconnectTimeout)
      reconnectTimeout = setTimeout(() => connectMqtt(userId, onMessage), delay)
    }
  }

  async function reconnectWorker(userId: string) {
    if (!workerPort) return
    try {
      const ticket = await generateTicket(userId)
      const brokerUrl = config.public.mqttUrl
      workerPort.postMessage({
        type: 'CONNECT',
        userId,
        brokerUrl,
        ticket,
      })
    } catch (err) {
      console.error('Frontend MQTT: Failed to generate ticket for SharedWorker:', err)
      const delay = getBackoffDelay()
      if (reconnectTimeout) clearTimeout(reconnectTimeout)
      reconnectTimeout = setTimeout(() => reconnectWorker(userId), delay)
    }
  }

  function disconnectMqtt() {
    if (reconnectTimeout) {
      clearTimeout(reconnectTimeout)
      reconnectTimeout = null
    }
    if (heartbeatInterval) {
      clearInterval(heartbeatInterval)
      heartbeatInterval = null
    }
    if (workerPort) {
      workerPort.postMessage({ type: 'CLOSE' })
      workerPort.close()
      worker = null
      workerPort = null
    }
    if (mqttClient) {
      mqttClient.end(true)
      mqttClient = null
    }
    if (typeof window !== 'undefined') {
      window.removeEventListener('online', handleNetworkOnline)
    }
    isMqttConnected.value = false
  }

  function publishMqtt(topic: string, payload: any, options?: any) {
    if (workerPort && isMqttConnected.value) {
      workerPort.postMessage({
        type: 'PUBLISH',
        topic,
        payload,
        options
      })
      return true
    }
    if (mqttClient && isMqttConnected.value) {
      mqttClient.publish(topic, JSON.stringify(payload), options)
      return true
    }
    // Queue offline message
    if (directOutboundQueue.length >= 100) {
      directOutboundQueue.shift()
    }
    directOutboundQueue.push({ topic, payload, options })
    console.warn(`[Direct MQTT] Offline: Buffered message for topic "${topic}".`)
    return false
  }

  return {
    isMqttConnected,
    mqttClientId,
    connectMqtt,
    disconnectMqtt,
    publishMqtt
  }
}
