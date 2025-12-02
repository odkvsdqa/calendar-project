<template>
  <div class="query-section">
    <h2>{{ $t('admin.components.monthlyStats.title') }}</h2>
    
    <div class="input-group">
      <label>{{ $t('common.year') }}:</label>
      <input type="number" v-model.number="selectedYear" min="2000" max="2100" class="input-styled input-short" />
      
      <label>{{ $t('common.month') }}:</label>
      <input type="number" v-model.number="selectedMonth" min="1" max="12" class="input-styled input-short" />
      
      <button @click="fetchMonthlyStats" :disabled="loading" class="btn-query">
        {{ $t('common.query') }}
      </button>
    </div>
    
    <div v-if="result" class="result-box fade-in">
      <h3>
        {{ result.year }} {{ $t('common.year') }} 
        {{ result.month }} {{ $t('common.month') }} 
        {{ $t('admin.components.monthlyStats.resultTitle') }}
      </h3>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">{{ $t('admin.stats.activeUsers') }}:</div>
          <div class="stat-number">{{ result.totalUsers }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">{{ $t('admin.stats.totalEvents') }}:</div>
          <div class="stat-number">{{ result.totalEvents }}</div>
        </div>
        <div class="stat-card cost-card">
          <div class="stat-label">{{ $t('admin.stats.totalBudget') }}:</div>
          <div class="stat-number cost-text">
             <small>NT$</small> {{ formatNumber(result.totalCost) }}
          </div>
        </div>
      </div>

      <h4>{{ $t('admin.components.monthlyStats.dailyStats') }}</h4>
      <div class="daily-stats">
        <div v-for="(count, date) in result.dailyUserCount" :key="date" class="list-row">
          <span class="row-label">{{ date }}</span>
          <span class="row-value">{{ count }} {{ $t('common.person') }}</span>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state">
      <div class="empty-icon">📊</div>
      <p>{{ $t('admin.components.monthlyStats.empty') }}</p>
    </div>
    <div v-if="loading" class="loading-overlay"><div class="spinner"></div></div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { adminApi } from '../../../services/adminApi'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
    console.error('Query failed:', err)
    alert(t('admin.errors.queryFailed'))
  } finally {
    loading.value = false
  }
}
const formatNumber = (val) => new Intl.NumberFormat('en-US').format(val || 0)
</script>

<style scoped>
/* 樣式與 DateQuery 一致，這裡只列出差異部分 */
.query-section { padding: 10px; }
h2 { font-size: 18px; color: #333; margin-bottom: 20px; font-weight: 600; }

.input-group { display: flex; align-items: center; gap: 15px; margin-bottom: 25px; flex-wrap: wrap; }
.input-styled { padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.input-short { width: 80px; } /* 年月輸入框短一點 */

.btn-query { padding: 8px 20px; background-color: #557c55; color: white; border: none; border-radius: 6px; cursor: pointer; transition: background 0.2s; }
.btn-query:hover { background-color: #446344; }

.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
.stat-card { background: white; border: 1px solid #eee; border-radius: 8px; padding: 20px; text-align: center; box-shadow: 0 2px 5px rgba(0,0,0,0.03); display: flex; flex-direction: column; justify-content: center; align-items: center; }

/* 統一字體 */
.stat-label { font-size: 14px; color: #555; font-weight: bold; margin-bottom: 8px; }
.stat-number { font-size: 28px; color: #557c55; font-weight: 500; line-height: 1.2; }

/* 金額特別色 */
.cost-card { background: #fafbf9; }
.cost-text { color: #d97706; /* 金色系 */ }
.cost-text small { font-size: 14px; color: #b0a496; margin-right: 2px; }

/* 列表樣式 */
.daily-stats { border-radius: 8px; overflow: hidden; border: 1px solid #eee; }
.list-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 20px; border-bottom: 1px solid #eee; background: white; }
.list-row:last-child { border-bottom: none; }
.row-label { font-family: monospace; font-weight: 500; color: #555; }
.row-value { font-weight: bold; color: #557c55; }

.empty-state { text-align: center; padding: 40px; color: #999; background: #fafbf9; border-radius: 8px; border: 1px dashed #ddd; }
.empty-icon { font-size: 40px; margin-bottom: 10px; }
.loading-overlay { position: absolute; top:0; left:0; width:100%; height:100%; background: rgba(255,255,255,0.7); display: flex; justify-content: center; align-items: center; }
.spinner { width: 30px; height: 30px; border: 3px solid #f3f3f3; border-top: 3px solid #557c55; border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>