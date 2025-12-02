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
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import AdminDashboard from '../components/admin/AdminDashboard.vue'
import { useAuth } from '../composables/useAuth'

const { currentUser, logout: handleLogout } = useAuth()
const router = useRouter()

onMounted(() => {
  if (!currentUser.value || currentUser.value.role !== 'ADMIN') {
    alert('您沒有權限訪問此頁面')
    router.push('/calendar')
  }
})
</script>

<style scoped>
/* 您原本的樣式：清爽的淺灰背景 */
.admin-view {
  min-height: 100vh;
  background: #f3f4f6; 
  padding-bottom: 40px;
}
</style>