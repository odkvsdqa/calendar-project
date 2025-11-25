<template>
  <div class="calendar-view">
    <Header 
      :username="currentUser?.username || 'Guest'" 
      :user-role="currentUser?.role || 'USER'"
      @logout="handleLogout" 
    />
    <CalendarApp />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import CalendarApp from '../components/CalendarApp.vue'
import { getUserInfo, clearAuth } from '../utils/auth'

const router = useRouter()
const currentUser = ref(null)

const handleLogout = () => {
  clearAuth()
  router.push('/login')
}

onMounted(() => {
  currentUser.value = getUserInfo()
  console.log('當前用戶:', currentUser.value)
})
</script>

<style scoped>
.calendar-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>