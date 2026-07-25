import { defineNuxtRouteMiddleware, useHead } from '#imports'

export default defineNuxtRouteMiddleware((to) => {
  // Prefer explicit page meta title, then route meta.title
  const pageTitle = (to.meta && (to.meta.title as string)) || ''

  if (pageTitle) {
    useHead({ title: pageTitle })
  }
})
