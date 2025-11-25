<template>
  <div class="admin-dashboard">
    <div class="dashboard-container">
      <h1>📊 管理員儀表板</h1>
      
      <!-- 統計模式切換 -->
      <div class="mode-selector">
        <button 
          @click="mode = 'date'" 
          :class="{ active: mode === 'date' }"
        >
          按日期查詢
        </button>
        <button 
          @click="mode = 'month'" 
          :class="{ active: mode === 'month' }"
        >
          按月份統計
        </button>
        <button 
          @click="mode = 'timeRange'" 
          :class="{ active: mode === 'timeRange' }"
        >
          按時間範圍統計
        </button>
        <button 
          @click="mode = 'userSchedule'" 
          :class="{ active: mode === 'userSchedule' }"
        >
          用戶排程查詢
        </button>
      </div>

      <!-- 按日期查詢 -->
      <div v-if="mode === 'date'" class="query-section">
        <h2>查詢特定日期的用戶排程</h2>
        <div class="input-group">
          <label>選擇日期:</label>
          <input type="date" v-model="selectedDate" />
          <button @click="fetchUsersByDate" :disabled="loading">查詢</button>
        </div>
        
        <div v-if="dateResult" class="result-box">
          <h3>查詢結果 ({{ dateResult.date }})</h3>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-number">{{ dateResult.userCount }}</div>
              <div class="stat-label">有排程的用戶數</div>
            </div>
            <div class="stat-card">
              <div class="stat-number">{{ dateResult.eventCount }}</div>
              <div class="stat-label">總事件數</div>
            </div>
          </div>
          
          <h4>用戶列表:</h4>
          <div class="user-list">
            <div v-for="user in dateResult.users" :key="user.id" class="user-item">
              <span class="user-icon">👤</span>
              <span class="user-name">{{ user.username }}</span>
              <span class="user-email">{{ user.email }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 按月份統計 -->
      <div v-else-if="mode === 'month'" class="query-section">
        <h2>月份統計數據</h2>
        <div class="input-group">
          <label>年份:</label>
          <input type="number" v-model.number="selectedYear" min="2020" max="2030" />
          <label>月份:</label>
          <input type="number" v-model.number="selectedMonth" min="1" max="12" />
          <button @click="fetchMonthlyStats" :disabled="loading">查詢</button>
        </div>
        
        <div v-if="monthResult" class="result-box">
          <h3>{{ monthResult.year }}年 {{ monthResult.month }}月 統計</h3>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-number">{{ monthResult.totalUsers }}</div>
              <div class="stat-label">活躍用戶數</div>
            </div>
            <div class="stat-card">
              <div class="stat-number">{{ monthResult.totalEvents }}</div>
              <div class="stat-label">總事件數</div>
            </div>
          </div>
          
          <h4>每日用戶統計:</h4>
          <div class="daily-stats">
            <div 
              v-for="(count, date) in monthResult.dailyUserCount" 
              :key="date"
              class="daily-item"
            >
              <span class="date">{{ date }}</span>
              <span class="count-badge">{{ count }} 人</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 按時間範圍統計 -->
      <div v-else-if="mode === 'timeRange'" class="query-section">
        <h2>時間範圍統計(支援小時級別)</h2>
        <div class="input-group">
          <label>開始時間:</label>
          <input type="datetime-local" v-model="startTime" />
          <label>結束時間:</label>
          <input type="datetime-local" v-model="endTime" />
          <button @click="fetchTimeRangeStats" :disabled="loading">查詢</button>
        </div>
        
        <div v-if="timeRangeResult" class="result-box">
          <h3>時間範圍統計</h3>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-number">{{ timeRangeResult.totalUsers }}</div>
              <div class="stat-label">有排程的用戶數</div>
            </div>
            <div class="stat-card">
              <div class="stat-number">{{ timeRangeResult.totalEvents }}</div>
              <div class="stat-label">總事件數</div>
            </div>
          </div>
          
          <h4>每小時用戶統計:</h4>
          <div class="hourly-stats">
            <div 
              v-for="(count, hour) in timeRangeResult.hourlyUserCount" 
              :key="hour"
              class="hourly-item"
            >
              <span class="hour">{{ hour }}</span>
              <div class="hour-bar" :style="{ width: (count * 30) + 'px' }">
                {{ count }} 人
              </div>
            </div>
          </div>
          
          <h4>用戶列表:</h4>
          <div class="user-list">
            <div v-for="user in timeRangeResult.users" :key="user.id" class="user-item">
              <span class="user-icon">👤</span>
              <span class="user-name">{{ user.username }}</span>
              <span class="user-email">{{ user.email }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 用戶排程查詢 -->
      <div v-else-if="mode === 'userSchedule'" class="query-section">
        <h2>查詢用戶排程日期</h2>
        <div class="input-group">
          <label>選擇用戶:</label>
          <select v-model="selectedUserId" @change="fetchUserSchedule">
            <option value="">-- 請選擇用戶 --</option>
            <option v-for="user in allUsers" :key="user.id" :value="user.id">
              {{ user.username }} ({{ user.email }})
            </option>
          </select>
        </div>
        
        <div v-if="userScheduleResult" class="result-box">
          <h3>{{ userScheduleResult.username }} 的排程日期</h3>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-number">{{ userScheduleResult.totalScheduleDays }}</div>
              <div class="stat-label">有排程的天數</div>
            </div>
          </div>
          
          <h4>排程日期列表:</h4>
          <div class="schedule-dates">
            <span 
              v-for="date in userScheduleResult.scheduleDates" 
              :key="date"
              class="date-badge"
            >
              {{ date }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading">載入中...</div>
      <div v-if="error" class="error">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../services/adminApi'

// 取得今天日期
const today = new Date()
const year = today.getFullYear()
const month = today.getMonth() + 1
const dateStr = today.toISOString().split('T')[0]

// 響應式數據
const mode = ref('date')
const loading = ref(false)
const error = ref(null)

// 日期查詢
const selectedDate = ref(dateStr)
const dateResult = ref(null)

// 月份統計
const selectedYear = ref(year)
const selectedMonth = ref(month)
const monthResult = ref(null)

// 時間範圍統計
const startTime = ref('')
const endTime = ref('')
const timeRangeResult = ref(null)

// 用戶排程查詢
const allUsers = ref([])
const selectedUserId = ref('')
const userScheduleResult = ref(null)

// 載入所有用戶
const loadAllUsers = async () => {
  try {
    const response = await adminApi.getAllUsers()
    allUsers.value = response.data
  } catch (err) {
    console.error('載入用戶列表失敗:', err)
  }
}

// 按日期查詢
const fetchUsersByDate = async () => {
  if (!selectedDate.value) {
    alert('請選擇日期')
    return
  }
  
  try {
    loading.value = true
    error.value = null
    const response = await adminApi.getUsersByDate(selectedDate.value)
    dateResult.value = response.data
  } catch (err) {
    console.error('查詢失敗:', err)
    error.value = '查詢失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}

// 按月份統計
const fetchMonthlyStats = async () => {
  try {
    loading.value = true
    error.value = null
    const response = await adminApi.getMonthlyStats(selectedYear.value, selectedMonth.value)
    monthResult.value = response.data
  } catch (err) {
    console.error('查詢失敗:', err)
    error.value = '查詢失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}

// 按時間範圍統計
const fetchTimeRangeStats = async () => {
  if (!startTime.value || !endTime.value) {
    alert('請選擇開始和結束時間')
    return
  }
  
  try {
    loading.value = true
    error.value = null
    const response = await adminApi.getTimeRangeStats(startTime.value, endTime.value)
    timeRangeResult.value = response.data
  } catch (err) {
    console.error('查詢失敗:', err)
    error.value = '查詢失敗,請稍後再試'
  } finally {
    loading.value = false
  }
}

// 查詢用戶排程
const fetchUserSchedule = async () => {
  if (!selectedUserId.value) return
  
  try {
    loading.value = true
    error.value = null
    const response = await adminApi.getUserScheduleDates(selectedUserId.value)
    userScheduleResult.value = response.data
  } catch (err) {
    console.error('查詢失敗:', err)
    error.value = '查詢失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}

// 組件掛載時載入用戶列表
onMounted(() => {
  loadAllUsers()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 30px;
  min-height: calc(100vh - 80px);
}

.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

h1 {
  color: #667eea;
  margin-bottom: 30px;
  font-size: 32px;
}

h2 {
  color: #333;
  margin-bottom: 20px;
  font-size: 24px;
}

.mode-selector {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.mode-selector button {
  padding: 12px 24px;
  border: 2px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.mode-selector button:hover {
  background: #f0f4ff;
}

.mode-selector button.active {
  background: #667eea;
  color: white;
}

.query-section {
  margin-top: 20px;
}

.input-group {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.input-group label {
  font-weight: bold;
  color: #555;
}

.input-group input,
.input-group select {
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.input-group button {
  padding: 10px 24px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.input-group button:hover:not(:disabled) {
  background: #059669;
}

.input-group button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.result-box {
  background: #f8f9fa;
  padding: 30px;
  border-radius: 12px;
  margin-top: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 48px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

h4 {
  color: #667eea;
  margin: 20px 0 10px 0;
  font-size: 18px;
}

.user-list {
  display: grid;
  gap: 10px;
}

.user-item {
  background: white;
  padding: 15px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-icon {
  font-size: 24px;
}

.user-name {
  font-weight: bold;
  color: #333;
}

.user-email {
  color: #666;
  font-size: 14px;
}

.daily-stats,
.hourly-stats {
  display: grid;
  gap: 10px;
}

.daily-item,
.hourly-item {
  background: white;
  padding: 12px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date,
.hour {
  font-weight: bold;
  color: #333;
}

.count-badge {
  background: #667eea;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
}

.hour-bar {
  background: linear-gradient(90deg, #667eea, #764ba2);
  color: white;
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
  min-width: 60px;
}

.schedule-dates {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.date-badge {
  background: #667eea;
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
}

.loading {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #667eea;
}

.error {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #ef4444;
  background: #fee;
  border-radius: 8px;
  margin-top: 20px;
}
</style>