<!-- src/components/admin/dashboard/UserSchedule.vue -->
<template>
  <div class="query-section">
    <h2>查詢用戶排程日期</h2>
    <div class="input-group">
      <label>選擇用戶:</label>
      <select v-model="selectedUserId">
        <option value="">-- 請選擇用戶 --</option>
        <option v-for="user in allUsers" :key="user.id" :value="user.id">
          {{ user.username }} ({{ user.email }})
        </option>
      </select>
      <button @click="fetchUserSchedule" :disabled="!selectedUserId || loading">查詢</button>
    </div>
    
    <div v-if="result" class="result-box fade-in">
      <h3>{{ result.username }} 的排程日期</h3>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-number">{{ result.totalScheduleDays }}</div>
          <div class="stat-label">有排程的天數</div>
        </div>
      </div>

      <h4>排程日期列表:</h4>
      <div class="schedule-dates">
        <span v-for="date in result.scheduleDates" :key="date" class="date-tag">
          {{ date }}
        </span>
      </div>
    </div>

    <!-- 空狀態 -->
    <div v-else class="empty-state">
      <div class="empty-icon">👤</div>
      <p>請選擇一位用戶以查看排程</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../../services/adminApi'

const allUsers = ref([])
const selectedUserId = ref('')
const result = ref(null)
const loading = ref(false)

// 載入所有用戶
const loadAllUsers = async () => {
  try {
    loading.value = true
    const response = await adminApi.getAllUsers()
    allUsers.value = response.data
  } catch (err) {
    console.error('載入用戶列表失敗:', err)
  } finally {
    loading.value = false
  }
}

// 查詢用戶排程
const fetchUserSchedule = async () => {
  if (!selectedUserId.value) return
  
  try {
    loading.value = true
    const response = await adminApi.getUserScheduleDates(selectedUserId.value)
    result.value = response.data
  } catch (err) {
    console.error('查詢失敗:', err)
    alert('查詢失敗，請稍後再試')
  } finally {
    loading.value = false
  }
}

// 當組件掛載時 (切換到這個 Tab 時)，才去抓用戶列表
onMounted(() => {
  loadAllUsers()
})
</script>

<style scoped>
/* 專屬樣式：排程日期標籤 */
.schedule-dates {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.date-tag {
  display: inline-block;
  background: white;
  border: 1px solid #557c55;
  color: #557c55;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
</style>