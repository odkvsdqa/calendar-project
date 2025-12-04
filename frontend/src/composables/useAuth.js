// src/composables/useAuth.js
import { ref, computed } from 'vue'
import { getUserInfo, removeToken, removeUserInfo } from '../utils/auth'
import { useRouter } from 'vue-router'

// 建立一個全域狀態 (Singleton)，確保所有元件拿到的資料是同步的
const globalUser = ref(getUserInfo())

export function useAuth() {
  const router = useRouter()

  // 1. 重新載入使用者的函式
  const fetchUser = () => {
    const user = getUserInfo()
    globalUser.value = user // 更新全域狀態
    return user
  }

  // 2. 登出函式
  const logout = () => {
    removeToken()
    removeUserInfo()
    globalUser.value = null // 清空狀態
    router.push('/login')
  }

  // 3. 判斷是否登入
  const isAuthenticated = computed(() => !!globalUser.value)

  // 4. 初始化檢查：如果全域狀態是空的，但 LocalStorage 有資料，就抓出來
  // 這行是解決 "Guest" 問題的關鍵
  if (!globalUser.value) {
    fetchUser()
  }

  return {
    currentUser: globalUser, // 回傳 Ref，這樣畫面才會響應變化
    isAuthenticated,
    fetchUser, // 把這個暴露出去，讓 Login 成功後可以呼叫
    logout
  }
}