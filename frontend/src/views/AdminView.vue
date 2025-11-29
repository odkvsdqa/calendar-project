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
import { useAuth } from '../composables/useAuth'

const { currentUser, logout: handleLogout } = useAuth()

onMounted(() => {
  if (!currentUser.value || currentUser.value.role !== 'ADMIN') {
    alert('您沒有權限訪問此頁面')
    router.push('/calendar')
  }
})
</script>

/* src/views/AdminView.vue */
<style scoped>
.admin-view {
  min-height: 100vh;
  /* ❌ 移除舊的深色漸層 */
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
  
  /* ✅ 改為：清爽的淺灰背景，讓白色卡片更凸顯 */
  background: #f3f4f6; 
  padding-bottom: 40px;
}
</style>