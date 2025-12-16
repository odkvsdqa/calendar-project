import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import CalendarView from '../views/CalendarView.vue'
import AdminView from '../views/AdminView.vue'
import { isAuthenticated, getUserInfo } from '../utils/auth'
import ForgotPasswordView from '../views/ForgotPasswordView.vue'

const routes = [
  {
    path: '/',
    redirect: '/calendar'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView,
    meta: { requiresAuth: false }
  },
  {
    path: '/calendar',
    name: 'Calendar',
    component: CalendarView,
    meta: { requiresAuth: true }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPasswordView,
    meta: { requiresAuth: false }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminView,
    meta: { 
      requiresAuth: true,
      requiresAdmin: true  // 需要管理員權限
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守衛
router.beforeEach((to, from, next) => {
  const { requiresAuth, requiresAdmin } = to.meta
  const authenticated = isAuthenticated()
  const userInfo = getUserInfo()

  // 未登入但需要認證
  if (requiresAuth && !authenticated) {
    return next('/login')
  }

  // 需要管理員權限
  if (requiresAdmin && userInfo?.role !== 'ADMIN') {
    alert('您沒有權限訪問此頁面')
    return next('/calendar')
  }

  // 已登入訪問登入/註冊頁 -> 重定向
  if (authenticated && ['/login', '/register'].includes(to.path)) {
    return next('/calendar')
  }

  next()
})


export default router