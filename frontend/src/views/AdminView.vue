<template>
  <div class="admin-view">
    <Header 
      :username="currentUser?.username || 'Admin'" 
      :user-role="currentUser?.role || 'ADMIN'"
      @logout="handleLogout" 
    />
    <AdminDashboard />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import AdminDashboard from '../components/admin/AdminDashboard.vue'
import { getUserInfo, clearAuth } from '../utils/auth'

const router = useRouter()
const currentUser = ref(null)

const handleLogout = () => {
  clearAuth()
  router.push('/login')
}

onMounted(() => {
  currentUser.value = getUserInfo()
  
  // 安全檢查：確保是管理員
  if (!currentUser.value || currentUser.value.role !== 'ADMIN') {
    alert('您沒有權限訪問此頁面')
    router.push('/calendar')
  }
})
</script>

<style scoped>
.admin-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>