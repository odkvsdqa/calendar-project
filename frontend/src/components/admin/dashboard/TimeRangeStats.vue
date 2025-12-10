<template>
  <div class="query-section">
    <h2>{{ $t('admin.components.timeRange.title') }}</h2>
    
    <div class="input-group">
      <div class="time-inputs">
        <label>{{ $t('admin.components.timeRange.start') }}</label>
        <input type="datetime-local" v-model="startTime" class="input-styled" />
        <span class="separator">~</span>
        <label>{{ $t('admin.components.timeRange.end') }}</label>
        <input type="datetime-local" v-model="endTime" class="input-styled" />
      </div>
      <button @click="fetchTimeRangeStats" :disabled="loading" class="btn-query">
        {{ $t('common.query') }}
      </button>
    </div>
    
    <div v-if="result" class="result-box fade-in">
      <h3>{{ $t('admin.components.timeRange.title') }}</h3>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">{{ $t('admin.stats.scheduledUsers') }}:</div>
          <div class="stat-number">{{ result.totalUsers }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">{{ $t('admin.stats.totalEvents') }}:</div>
          <div class="stat-number">{{ result.totalEvents }}</div>
        </div>
        
        <!-- 🔥 金額卡片：加入幣別切換 -->
        <div class="stat-card cost-card">
          <div class="card-header">
            <div class="stat-label">{{ $t('admin.stats.estimatedCost') }}:</div>
            <select v-model="selectedCurrency" class="currency-selector">
              <option v-for="curr in currencies" :key="curr" :value="curr">
                {{ curr }}
              </option>
            </select>
          </div>
          <div class="stat-number cost-text">
            <small>{{ getSymbol(selectedCurrency) }}</small> 
            {{ formatNumber(convertedAmount) }}
          </div>
        </div>
      </div>

      <h4>{{ $t('admin.components.timeRange.hourlyChart') }}</h4>
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
      <p>{{ $t('admin.components.timeRange.empty') }}</p>
    </div>

    <div v-if="loading" class="loading-overlay"><div class="spinner"></div></div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { adminApi } from '../../../services/adminApi'
import { useCurrencyConverter } from '../../../composables/useCurrencyConverter'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { currencies, convert, getSymbol } = useCurrencyConverter()

const startTime = ref('')
const endTime = ref('')
const selectedCurrency = ref('TWD') // 預設幣別
const result = ref(null)
const loading = ref(false)

// 換算後的金額（響應式計算）
const convertedAmount = computed(() => {
  if (!result.value || !result.value.totalCost) return 0
  
  // result.totalCost 是 TWD，需要換算成目標幣別
  return convert(result.value.totalCost, 'TWD', selectedCurrency.value)
})

const fetchTimeRangeStats = async () => {
  if (!startTime.value || !endTime.value) { 
     alert(t('admin.errors.selectTimeRange'));
     return 
  }
  try {
    loading.value = true
    const response = await adminApi.getTimeRangeStats(startTime.value, endTime.value)
    result.value = response.data
  } catch (err) {
    console.error('Query failed:', err)
    alert(t('admin.errors.queryFailed'))
  } finally {
    loading.value = false
  }
}

const sortedHourlyStats = computed(() => { 
  if (!result.value || !result.value.hourlyUserCount) return []
  return Object.entries(result.value.hourlyUserCount).sort((a, b) => a[0].localeCompare(b[0])) 
})

const maxHourlyCount = computed(() => { 
  if (sortedHourlyStats.value.length === 0) return 1
  return Math.max(...sortedHourlyStats.value.map(item => item[1])) 
})

const formatNumber = (val) => new Intl.NumberFormat('en-US').format(val || 0)
</script>

<style scoped>
.query-section { padding: 10px; }
h2 { font-size: 18px; color: #333; margin-bottom: 20px; font-weight: 600; }

.input-group { display: flex; align-items: flex-end; gap: 15px; margin-bottom: 25px; flex-wrap: wrap; }
.time-inputs { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.separator { color: #999; font-weight: bold; }
.input-styled { padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }

.btn-query { padding: 8px 20px; background-color: #557c55; color: white; border: none; border-radius: 6px; cursor: pointer; transition: background 0.2s; }
.btn-query:hover { background-color: #446344; }

.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
.stat-card { background: white; border: 1px solid #eee; border-radius: 8px; padding: 20px; text-align: center; box-shadow: 0 2px 5px rgba(0,0,0,0.03); display: flex; flex-direction: column; justify-content: center; align-items: center; }

.stat-label { font-size: 14px; color: #555; font-weight: bold; margin-bottom: 8px; }
.stat-number { font-size: 28px; color: #557c55; font-weight: 500; line-height: 1.2; }

/* 🔥 金額卡片樣式 */
.cost-card { background: #fafbf9; position: relative; }
.card-header { width: 100%; display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }

/* 🔥 幣別選擇器 */
.currency-selector {
  padding: 4px 8px;
  font-size: 12px;
  border: 1px solid #d97706;
  border-radius: 4px;
  background: white;
  color: #d97706;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  outline: none;
}

.currency-selector:hover {
  background: #fffbeb;
  border-color: #b45309;
}

.currency-selector:focus {
  border-color: #d97706;
  box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.1);
}

.cost-text { color: #d97706; }
.cost-text small { font-size: 14px; color: #b0a496; margin-right: 2px; }

/* 圖表 */
.hourly-stats { background: white; padding: 20px; border-radius: 8px; border: 1px solid #eee; }
.hourly-item { display: flex; align-items: center; margin-bottom: 12px; height: 24px; }
.hour-label { width: 50px; text-align: right; margin-right: 15px; font-family: monospace; color: #666; font-size: 13px; }
.bar-track { flex: 1; background-color: #f5f5f5; border-radius: 12px; height: 100%; position: relative; }
.bar-fill { height: 100%; background: #557c55; border-radius: 12px; display: flex; align-items: center; justify-content: flex-end; padding-right: 8px; min-width: 24px; transition: width 0.6s ease-out; }
.bar-value { color: white; font-size: 11px; font-weight: bold; }

.empty-state { text-align: center; padding: 40px; color: #999; background: #fafbf9; border-radius: 8px; border: 1px dashed #ddd; }
.empty-icon { font-size: 40px; margin-bottom: 10px; }
.loading-overlay { position: absolute; top:0; left:0; width:100%; height:100%; background: rgba(255,255,255,0.7); display: flex; justify-content: center; align-items: center; }
.spinner { width: 30px; height: 30px; border: 3px solid #f3f3f3; border-top: 3px solid #557c55; border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>