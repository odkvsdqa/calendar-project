// src/composables/useAuth.js
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo, removeToken, removeUserInfo } from '../utils/auth'

const currentUser = ref(getUserInfo())

export function useAuth() {
  const router = useRouter()

  const logout = () => {
    // 還原登出邏輯
    removeToken()
    removeUserInfo()
    
    currentUser.value = null
    
    router.push('/login')
  }

  const refreshUser = () => {
    currentUser.value = getUserInfo()
  }

  return {
    currentUser,
    logout,
    refreshUser
  }
}