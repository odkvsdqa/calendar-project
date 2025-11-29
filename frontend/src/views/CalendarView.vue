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