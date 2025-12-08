/**
 * 主題管理 Composable
 * v1.1 支援淺色/深色/跟隨系統
 */
import { ref, watch, onMounted } from 'vue'
import { preferenceApi } from '../services/preferenceApi'

// 全域狀態（跨組件共用）
const currentTheme = ref('system') // light | dark | system
const systemTheme = ref('light')   // 系統實際主題

export function useTheme() {
  
  /**
   * 初始化主題
   * 優先順序：後端 > localStorage > 預設值
   */
  const initTheme = async () => {
    // 🔥 新增：檢查是否有 Token
  const token = localStorage.getItem('auth-token')
  
  if (!token) {
    console.log('⏭️ 未登入，使用本地主題設定')
    const localTheme = localStorage.getItem('user-theme') || 'system'
    currentTheme.value = localTheme
    applyTheme(localTheme)
    return // 🔥 直接返回，不呼叫 API
  }
    try {
      // 1. 從後端獲取偏好設定
      const response = await preferenceApi.getPreferences()
      const serverTheme = response.data.theme
      
      currentTheme.value = serverTheme
      localStorage.setItem('user-theme', serverTheme)
      
      applyTheme(serverTheme)
      console.log('✅ 已載入後端主題設定:', serverTheme)
      
    } catch (error) {
      console.warn('⚠️ 無法載入後端主題，使用本地設定')
      
      // 2. Fallback 到 localStorage
      const localTheme = localStorage.getItem('user-theme') || 'system'
      currentTheme.value = localTheme
      applyTheme(localTheme)
    }
  }
  
  /**
   * 套用主題到 DOM
   */
  const applyTheme = (theme) => {
    // 偵測系統主題
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    systemTheme.value = prefersDark ? 'dark' : 'light'
    
    let finalTheme = theme
    
    // 如果是「跟隨系統」，則使用系統主題
    if (theme === 'system') {
      finalTheme = systemTheme.value
    }
    
    // 套用到 <html> 標籤
    if (finalTheme === 'dark') {
      document.documentElement.setAttribute('data-theme', 'dark')
    } else {
      document.documentElement.removeAttribute('data-theme')
    }
  }
  
  /**
   * 切換主題
   */
  const setTheme = async (theme) => {
    currentTheme.value = theme
    localStorage.setItem('user-theme', theme)
    applyTheme(theme)
    
    // 同步到後端
    try {
      const currentPrefs = await preferenceApi.getPreferences()
      await preferenceApi.updatePreferences({
        ...currentPrefs.data,
        theme: theme
      })
      console.log('✅ 主題已同步至後端:', theme)
    } catch (error) {
      console.warn('⚠️ 主題同步後端失敗:', error)
    }
  }
  
  /**
   * 監聽系統主題變化
   */
  const watchSystemTheme = () => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    
    mediaQuery.addEventListener('change', (e) => {
      systemTheme.value = e.matches ? 'dark' : 'light'
      
      // 如果當前是「跟隨系統」，則重新套用
      if (currentTheme.value === 'system') {
        applyTheme('system')
      }
    })
  }
  
  // 🔥 在組件掛載時自動初始化
  onMounted(() => {
    watchSystemTheme()
  })
  
  return {
    currentTheme,
    systemTheme,
    initTheme,
    setTheme,
    applyTheme
  }
}