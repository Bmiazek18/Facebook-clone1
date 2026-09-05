import { defineEventHandler } from 'h3'
import { handleTusProxy } from '../utils/tusProxy'

export default defineEventHandler(async (event) => {
  return handleTusProxy(event, '')
})
