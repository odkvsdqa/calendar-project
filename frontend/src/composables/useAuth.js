import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo, clearAuth, isAuthenticated } from '../utils/auth'

export function useAuth() {
  const router = useRouter()
  const currentUser = ref(getUserInfo())
  
  const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
  
  const logout = () => {
    clearAuth()
    router.push('/login')
  }
  
  return {
    currentUser,
    isAdmin,
    logout,
    isAuthenticated: isAuthenticated()
  }
}