// ✅ 使用 Composable
// composables/useAuth.js
import { ref, computed } from 'vue'
import { getUserInfo, clearAuth, isAuthenticated } from '@/utils/auth'
import { useRouter } from 'vue-router'

export function useAuth() {
  const router = useRouter()
  const currentUser = ref(getUserInfo())
  
  const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
  
  const logout = () => {
    clearAuth()
    router.push('/login')
  }
  
  const requireAdmin = () => {
    if (!isAdmin.value) {
      alert('您沒有權限訪問此頁面')
      router.push('/calendar')
      return false
    }
    return true
  }
  
  return {
    currentUser,
    isAdmin,
    logout,
    requireAdmin,
    isAuthenticated: isAuthenticated()
  }
}

// CalendarView.vue
import { useAuth } from '@/composables/useAuth'

const { currentUser, logout } = useAuth()