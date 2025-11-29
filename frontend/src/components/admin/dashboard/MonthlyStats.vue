<!-- src/components/admin/dashboard/MonthlyStats.vue -->
<template>
  <div class="query-section">
    <h2>月份統計數據</h2>
    <div class="input-group">
      <label>年份:</label>
      <input type="number" v-model.number="selectedYear" min="2020" max="2030" />
      <label>月份:</label>
      <input type="number" v-model.number="selectedMonth" min="1" max="12" />
      <button @click="fetchMonthlyStats" :disabled="loading">查詢</button>
    </div>
    
    <div v-if="result" class="result-box fade-in">
      <h3>{{ result.year }}年 {{ result.month }}月 統計</h3>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-number">{{ result.totalUsers }}</div>
          <div class="stat-label">活躍用戶數</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ result.totalEvents }}</div>
          <div class="stat-label">總事件數</div>
        </div>
      </div>

      <h4>每日用戶統計:</h4>
      <div class="daily-stats">
        <!-- 這裡如果你需要確保順序，也可以像 TimeRange 那樣加個 computed 排序，
             但通常日期 key 是字串，大多瀏覽器會自動排好 -->
        <div v-for="(count, date) in result.dailyUserCount" :key="date" class="list-row">
          <span class="row-label">{{ date }}</span>
          <span class="row-value">{{ count }} 人</span>
        </div>
      </div>
    </div>

    <!-- 空狀態 -->
    <div v-else class="empty-state">
      <div class="empty-icon">📊</div>
      <p>請選擇月份查看統計報表</p>
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

const today = new Date()
const selectedYear = ref(today.getFullYear())
const selectedMonth = ref(today.getMonth() + 1)
const result = ref(null)
const loading = ref(false)

const fetchMonthlyStats = async () => {
  try {
    loading.value = true
    const response = await adminApi.getMonthlyStats(selectedYear.value, selectedMonth.value)
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
/* 專屬樣式：每日統計列表 */
.daily-stats {
  margin-top: 15px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
}

.list-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  background: white;
  transition: background 0.2s;
}

.list-row:last-child {
  border-bottom: none;
}

.list-row:hover {
  background: #f9f9f9;
}

.row-label {
  font-family: monospace;
  font-weight: bold;
  color: #555;
  font-size: 14px;
}

.row-value {
  font-weight: bold;
  color: #557c55;
  background: #e8f5e9;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
}
</style>