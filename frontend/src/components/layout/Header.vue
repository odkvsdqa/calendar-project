<template>
  <div class="header">
    <div class="header-content">
      <div class="logo">
        <!-- <span class="logo-icon">📅</span> -->
        <span class="logo-text">SKJL</span>
      </div>
      
      <div class="nav-links">
        <router-link to="/calendar" class="nav-link">日曆</router-link>
        <router-link 
          v-if="isAdmin" 
          to="/admin" 
          class="nav-link admin-link"
        >
          管理後台
        </router-link>
      </div>
      
      <div class="user-section">
        <span class="welcome-text">
          歡迎，{{ username }}
          <span v-if="isAdmin" class="admin-badge">管理員</span>
          <span>     </span>
          <button class="btn-logout" @click="handleLogout">
          登出
        </button>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  username: {
    type: String,
    required: true
  },
  userRole: {
    type: String,
    default: 'USER'
  }
})

const emit = defineEmits(['logout'])

const isAdmin = computed(() => {
  return props.userRole === 'ADMIN'
})

const handleLogout = () => {
  if (confirm('確定要登出嗎？')) {
    emit('logout')
  }
}
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 32px;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
}

.nav-links {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.2);
}

.nav-link.router-link-active {
  background: rgba(255, 255, 255, 0.3);
  font-weight: bold;
}

.admin-link {
  background: rgba(255, 215, 0, 0.3);
}

.admin-link:hover {
  background: rgba(255, 215, 0, 0.5);
}

.user-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.welcome-text {
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-badge {
  background: rgba(255, 215, 0, 0.9);
  color: #333;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.btn-logout {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid white;
  padding: 8px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.3s;
}

.btn-logout:hover {
  background: white;
  color: #667eea;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 15px;
  }
  
  .user-section {
    flex-direction: column;
    gap: 10px;
  }
}
</style>