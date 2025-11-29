<template>
  <div class="query-section">
    <h2>時間範圍統計</h2>
    <div class="input-group">
      <div class="time-inputs">
        <label>開始:</label>
        <input type="datetime-local" v-model="startTime" />
        <label>結束:</label>
        <input type="datetime-local" v-model="endTime" />
      </div>
      <button @click="fetchTimeRangeStats" :disabled="loading">查詢</button>
    </div>
    
    <div v-if="result" class="result-box fade-in">
      <h3>時間範圍統計</h3>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-number">{{ result.totalUsers }}</div>
          <div class="stat-label">有排程的用戶數</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ result.totalEvents }}</div>
          <div class="stat-label">總事件數</div>
        </div>
        <!-- 🔥 新增：總花費卡片 -->
        <div class="stat-card cost-card">
          <div class="stat-number cost-text">
            <small>NT$</small> {{ formatNumber(result.totalCost) }}
          </div>
          <div class="stat-label">預計總花費</div>
        </div>
      </div>

      <!-- 長條圖 (保留原樣) -->
      <h4>每小時用戶統計圖表:</h4>
      <div class="hourly-stats">
        <div v-for="[hour, count] in sortedHourlyStats" :key="hour" class="hourly-item">
          <span class="hour-label">{{ hour }}</span>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: (count / maxHourlyCount * 100) + '%' }">
              <span class="bar-value" v-if="count > 0">{{ count }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state">
      <div class="empty-icon">🕒</div>
      <p>請設定時間範圍進行分析</p>
    </div>

    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { adminApi } from '../../../services/adminApi'

const startTime = ref('')
const endTime = ref('')
const result = ref(null)
const loading = ref(false)

const fetchTimeRangeStats = async () => {
  if (!startTime.value || !endTime.value) { alert('請選擇開始和結束時間'); return }
  try {
    loading.value = true
    const response = await adminApi.getTimeRangeStats(startTime.value, endTime.value)
    result.value = response.data
  } catch (err) { console.error('查詢失敗:', err); alert('查詢失敗') } 
  finally { loading.value = false }
}

const sortedHourlyStats = computed(() => { if (!result.value || !result.value.hourlyUserCount) return []; return Object.entries(result.value.hourlyUserCount).sort((a, b) => a[0].localeCompare(b[0])) })
const maxHourlyCount = computed(() => { if (sortedHourlyStats.value.length === 0) return 1; return Math.max(...sortedHourlyStats.value.map(item => item[1])) })

// 🔥 格式化
const formatNumber = (val) => new Intl.NumberFormat('en-US').format(val || 0)
</script>

<style scoped>
/* 原有樣式 */
.hourly-stats { margin-top: 20px; background: white; padding: 20px; border-radius: 12px; border: 1px solid #eee; }
.hourly-item { display: flex; align-items: center; margin-bottom: 12px; height: 28px; }
.hour-label { width: 60px; text-align: right; margin-right: 15px; font-family: monospace; color: #666; font-weight: bold; font-size: 14px; }
.bar-track { flex: 1; background-color: #f0f0f0; border-radius: 14px; height: 100%; position: relative; overflow: hidden; }
.bar-fill { height: 100%; background: linear-gradient(90deg, #7aa87a, #557c55); border-radius: 14px; display: flex; align-items: center; justify-content: flex-end; padding-right: 10px; transition: width 0.6s ease-out; min-width: 30px; }
.bar-value { color: white; font-size: 12px; font-weight: bold; text-shadow: 0 1px 2px rgba(0,0,0,0.2); }

/* 🔥 新增樣式 */
.cost-card { background: #fbfbfb; }
.cost-text { color: #8c7b68; /* 枯木/金色系 */ }
.cost-text small { font-size: 14px; color: #b0a496; margin-right: 2px; }

@media (max-width: 768px) { .hourly-stats { padding: 10px; } .hour-label { width: 45px; font-size: 12px; margin-right: 10px; } }
</style>