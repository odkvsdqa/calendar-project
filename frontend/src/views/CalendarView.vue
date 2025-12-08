<template>
  <div class="calendar-view">
    <Header 
      :username="currentUser?.username || 'Guest'" 
      :user-role="currentUser?.role || 'USER'"
      @logout="handleLogout" 
    />
    
    <Suspense>
      <template #default>
        <CalendarApp />
      </template>
      
      <template #fallback>
        <SkeletonCalendar />
      </template>
    </Suspense>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '../components/layout/Header.vue'
import CalendarApp from '../components/CalendarApp.vue'
import SkeletonCalendar from '../components/SkeletonCalendar.vue'
import { useAuth } from '../composables/useAuth'
import { useI18n } from 'vue-i18n'
import { preferenceApi } from '../services/preferenceApi'
import { useTheme } from '../composables/useTheme' // 🔥 新增

const { locale } = useI18n()
const { currentUser, logout: handleLogout } = useAuth()
const { initTheme } = useTheme() // 🔥 新增

// 🔥 初始化所有偏好設定
onMounted(async () => {
  // 🔥 新增：檢查是否有 Token
  const token = localStorage.getItem('auth-token')
  
  if (!token) {
    console.log('⏭️ 未登入，使用本地設定')
    
    // 載入本地語言
    const localLanguage = localStorage.getItem('user-locale') || 'zh-TW'
    locale.value = localLanguage
    
    // 初始化主題（會自動檢查 Token）
    await initTheme()
    return // 🔥 直接返回，不呼叫 API
  }
  try {
    // 1. 載入完整偏好設定（語言 + 主題 + 時區）
    const response = await preferenceApi.getPreferences()
    const { language, theme, timezone } = response.data
    
    // 2. 同步語言設定
    const localLanguage = localStorage.getItem('user-locale')
    if (localLanguage !== language) {
      locale.value = language
      localStorage.setItem('user-locale', language)
      console.log('✅ 已同步後端語言設定:', language)
    }
    
    // 3. 初始化主題
    await initTheme()
    
    console.log('✅ 偏好設定載入完成:', { language, theme, timezone })
    
  } catch (error) {
    console.warn('⚠️ 無法載入後端偏好設定 (使用本地設定):', error)
    
    // Fallback: 仍然初始化主題
    await initTheme()
  }
})
</script>

<style scoped>
.calendar-view {
  height: 100vh; 
  height: 100dvh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-primary); /* 🔥 使用 CSS 變數 */
}

:deep(.header) {
  flex-shrink: 0;
}

:deep(.container),
:deep(.skeleton-container) {
  flex: 1;
  margin: 0;
  border-radius: 0;
  box-shadow: none;
}

@media (max-width: 768px) {
  :deep(.container),
  :deep(.skeleton-container) {
    margin: 0;
    border: none;
  }
}
</style>