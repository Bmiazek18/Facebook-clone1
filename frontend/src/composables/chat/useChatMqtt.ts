import { ref } from 'vue'
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

  async function connectMqtt(
    userId: string,
    onMessage: (topic: string, payload: any) => void
  ) {
    messageCallback = onMessage

    if (!userId || userId === '0' || userId === '1') return

    // --- CHECK FOR SHARED WORKER SUPPORT ---
    if (typeof window !== 'undefined' && 'SharedWorker' in window) {
      if (!worker) {
        console.log('Frontend MQTT: Using SharedWorker for WebSocket connection.')
        try {
          worker = new SharedWorker('/mqtt-worker.js')
          workerPort = worker.port

          workerPort.onmessage = (event) => {
            const data = event.data;
            if (data.type === 'STATUS') {
              isMqttConnected.value = (data.state === 'CONNECTED')
              console.log(`Frontend MQTT (Worker status): ${data.state}`)

              if (data.state === 'OFFLINE' || data.state === 'DISCONNECTED') {
                // If worker disconnected or went offline, trigger ticket generation and connect again
                setTimeout(() => reconnectWorker(userId), 3000)
              }
            } else if (data.type === 'MESSAGE') {
              if (messageCallback) {
                messageCallback(data.topic, data.payload)
              }
            }
          }

          workerPort.start()

          // Register page closure to inform worker
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
        // If not connected, get a ticket and send CONNECT message to worker
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
      })

      mqttClient = client

      client.on('connect', () => {
        isMqttConnected.value = true
        console.log('Frontend MQTT: Connected directly to broker.')
        client.subscribe('chat/messages/user/' + userId)
      })

      client.on('offline', () => {
        isMqttConnected.value = false
        console.warn('Frontend MQTT: Connection lost. Reconnecting in 3s...')
        client.end(true)
        setTimeout(() => connectMqtt(userId, onMessage), 3000)
      })

      client.on('error', (err) => {
        console.error('Frontend MQTT: Direct connection error:', err)
        client.end(true)
        isMqttConnected.value = false
        setTimeout(() => connectMqtt(userId, onMessage), 5000)
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
      setTimeout(() => connectMqtt(userId, onMessage), 5000)
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
    }
  }

  function disconnectMqtt() {
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
