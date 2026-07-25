import Redis from 'ioredis'

let redisClient: Redis | null = null

export function getRedisClient(): Redis {
  if (!redisClient) {
    const redisUrl = process.env.REDIS_URL || 'redis://127.0.0.1:6380'
    console.log('BFF: Connecting to Redis at:', redisUrl)
    redisClient = new Redis(redisUrl)
    
    redisClient.on('connect', () => {
      console.log('BFF: Successfully connected to Redis.')
    })

    redisClient.on('error', (err) => {
      console.error('BFF: Redis connection error:', err)
    })
  }
  return redisClient
}
