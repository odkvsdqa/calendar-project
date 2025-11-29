<!-- src/components/admin/dashboard/DateQuery.vue -->
<template>
  <div class="query-section">
    <h2>查詢特定日期的用戶排程</h2>
    <div class="input-group">
      <label>選擇日期:</label>
      <input type="date" v-model="selectedDate" />
      <button @click="fetchUsersByDate" :disabled="loading">查詢</button>
    </div>
    
    <!-- 有結果時顯示 -->
    <div v-if="result" class="result-box fade-in">
      <h3>查詢結果 ({{ result.date }})</h3>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-number">{{ result.userCount }}</div>
          <div class="stat-label">有排程的用戶數</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ result.eventCount }}</div>
          <div class="stat-label">總事件數</div>
        </div>
      </div>

      <h4>用戶列表:</h4>
      <div class="user-list">
        <div v-for="user in result.users" :key="user.id" class="user-item">
          <span class="user-icon">👤</span>
          <div class="user-info">
            <span class="user-name">{{ user.username }}</span>
            <span class="user-email">{{ user.email }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空狀態 -->
    <div v-else class="empty-state">
      <div class="empty-icon">📅</div>
      <p>請選擇日期並點擊查詢</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { adminApi } from '../../../services/adminApi'

// 預設為今天
const today = new Date().toISOString().split('T')[0]
const selectedDate = ref(today)
const result = ref(null)
const loading = ref(false)

const fetchUsersByDate = async () => {
  if (!selectedDate.value) {
    alert('請選擇日期')
    return
  }
  
  try {
    loading.value = true
    const response = await adminApi.getUsersByDate(selectedDate.value)
    result.value = response.data
  } catch (err) {
    console.error('查詢失敗:', err)
    alert('查詢失敗，請稍後再試')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 專屬樣式：優化後的用戶列表 Grid 排版 */
.user-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.user-item {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 15px;
  border-radius: 10px;
  border: 1px solid #eee;
  box-shadow: 0 2px 5px rgba(0,0,0,0.03);
  transition: transform 0.2s, box-shadow 0.2s;
}

.user-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.08);
  border-color: #557c55;
}

.user-icon {
  font-size: 24px;
  background: #f3f4f6;
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  margin-right: 15px;
}

.user-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.user-name {
  font-weight: bold;
  color: #333;
  font-size: 15px;
}

.user-email {
  color: #888;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>