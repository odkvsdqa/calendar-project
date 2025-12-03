<template>
  <div class="calendar-view">
    <Header 
      :username="currentUser?.username || 'Guest'" 
      :user-role="currentUser?.role || 'USER'"
      @logout="handleLogout" 
    />
    
    <!-- 🔥 使用 Suspense 包裹非同步元件 -->
    <Suspense>
      <!-- 主要內容 -->
      <template #default>
        <CalendarApp />
      </template>
      
      <!-- Loading 狀態 -->
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
// 🔥 新增這 3 行 import
import { useI18n } from 'vue-i18n'
import { preferenceApi } from '../services/preferenceApi'
const { locale } = useI18n()

// 🔥 新增這個 onMounted hook
onMounted(async () => {
  try {
    const response = await preferenceApi.getLanguage()
    const serverLanguage = response.data.language
    
    const localLanguage = localStorage.getItem('user-locale')
    
    if (localLanguage !== serverLanguage) {
      locale.value = serverLanguage
      localStorage.setItem('user-locale', serverLanguage)
      console.log('✅ 已同步後端語言設定:', serverLanguage)
    }
  } catch (error) {
    console.warn('⚠️ 無法載入後端語言設定 (使用本地語言):', error)
  }
})

const { currentUser, logout: handleLogout } = useAuth()
</script>

<style scoped>
/* 日系極簡風格 */
.calendar-view {
  height: 100vh; 
  height: 100dvh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f9f9f9;
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

/* RWD */
@media (max-width: 768px) {
  :deep(.container),
  :deep(.skeleton-container) {
    margin: 0;
    border: none;
  }
}
</style>