import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue'), meta: { guest: true } },
    { path: '/register', name: 'Register', component: () => import('../views/RegisterView.vue'), meta: { guest: true } },
    { path: '/dashboard', name: 'Dashboard', component: () => import('../views/DashboardView.vue'), meta: { requiresAuth: true } },
    { path: '/errors/401', name: 'Error401', component: () => import('../views/errors/401.vue') },
    { path: '/errors/403', name: 'Error403', component: () => import('../views/errors/403.vue') },
    { path: '/errors/404', name: 'Error404', component: () => import('../views/errors/404.vue') },
    { path: '/errors/500', name: 'Error500', component: () => import('../views/errors/500.vue') },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/errors/404.vue') },
  ],
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.accessToken) {
    next({ name: 'Login' })
  } else if (to.meta.guest && auth.accessToken) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
