<template>
  <div class="exchange-rate-section">
    <div class="section-header">
      <h2>💱 {{ $t('admin.components.exchangeRate.title') }}</h2>
      <button @click="refreshRates" :disabled="loading" class="btn-refresh">
        <span v-if="!loading">🔄 {{ $t('common.manualUpdate') }}</span>
        <span v-else>{{ $t('admin.components.exchangeRate.updating') }}</span>
      </button>
    </div>
    
    <div v-if="ratesInfo" class="rates-content">
      <!-- 更新時間 -->
      <div class="update-info">
        <span class="label">{{ $t('admin.components.exchangeRate.lastUpdated') }}:</span>
        <span class="time">{{ formatDateTime(ratesInfo.lastUpdated) }}</span>
        <span class="badge" :class="isStale ? 'stale' : 'fresh'">
          {{ isStale ? $t('admin.exchangeRate.status.stale') : $t('admin.exchangeRate.status.fresh') }}
        </span>
      </div>
      
      <!-- 匯率表格 -->
      <div class="rates-grid">
        <div class="rate-card" v-for="(rate, currency) in displayRates" :key="currency">
          <div class="currency-flag">{{ getCurrencyFlag(currency) }}</div>
          <div class="currency-info">
            <div class="currency-code">{{ currency }}</div>
            <div class="currency-name">{{ $t(`event.currencies.${currency}`) }}</div>
          </div>
          <div class="rate-value">
            <span class="rate-number">{{ formatRate(rate) }}</span>
            <span class="base-currency">TWD</span>
          </div>
        </div>
      </div>
      
      <!-- 說明 -->
      <div class="info-box">
        <p>{{ $t('admin.exchangeRate.info.autoUpdate') }}</p>
        <p>{{ $t('admin.exchangeRate.info.baseCurrency') }}</p>
        <p>{{ $t('admin.exchangeRate.info.dataSource') }}</p>
      </div>
    </div>
    
    <div v-else-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>{{ $t('admin.components.exchangeRate.loading') }}</p>
    </div>
    
    <div v-else class="error-state">
      <p>{{ $t('admin.components.exchangeRate.loadFailed') }}</p>
      <button @click="loadRates" class="btn-retry">{{ $t('common.retry') }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../../services/adminApi'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const ratesInfo = ref(null)
const loading = ref(false)

// 幣別旗幟 Emoji
const currencyFlags = {
  'TWD': '🇹🇼',
  'USD': '🇺🇸',
  'JPY': '🇯🇵',
  'EUR': '🇪🇺',
  'CNY': '🇨🇳',
  'KRW': '🇰🇷'
}

// 排序後的匯率（TWD 排第一）
const displayRates = computed(() => {
  if (!ratesInfo.value || !ratesInfo.value.rates) return {}
  
  const rates = ratesInfo.value.rates
  const sorted = {}
  
  // TWD 排第一
  if (rates['TWD']) sorted['TWD'] = rates['TWD']
  
  // 其他按字母排序
  Object.keys(rates)
    .filter(k => k !== 'TWD')
    .sort()
    .forEach(k => sorted[k] = rates[k])
  
  return sorted
})

// 檢查匯率是否過期（超過 25 小時）
const isStale = computed(() => {
  if (!ratesInfo.value || !ratesInfo.value.lastUpdated) return true
  
  const lastUpdate = new Date(ratesInfo.value.lastUpdated)
  const now = new Date()
  const hoursDiff = (now - lastUpdate) / (1000 * 60 * 60)
  
  return hoursDiff > 25
})

// 載入匯率資訊
const loadRates = async () => {
  try {
    loading.value = true
    const response = await adminApi.getExchangeRates()
    ratesInfo.value = response.data
  } catch (err) {
    console.error('載入匯率失敗:', err)
  } finally {
    loading.value = false
  }
}

// 手動更新匯率
const refreshRates = async () => {
  if (loading.value) return
  
  try {
    loading.value = true
    await adminApi.updateExchangeRates()
    
    // 更新成功後重新載入
    setTimeout(() => {
      loadRates()
    }, 1000)
    
    alert(t('admin.components.exchangeRate.updateSuccess'))
  } catch (err) {
    console.error('更新匯率失敗:', err)
    alert(t('admin.components.exchangeRate.updateFailed'))
  } finally {
    loading.value = false
  }
}

// 格式化時間
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化匯率
const formatRate = (rate) => {
  return parseFloat(rate).toFixed(4)
}

// 取得幣別旗幟
const getCurrencyFlag = (currency) => {
  return currencyFlags[currency] || '🌍'
}

onMounted(() => {
  loadRates()
})
</script>

<style scoped>
.exchange-rate-section {
  padding: 10px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  font-size: 18px;
  color: #333;
  font-weight: 600;
  margin: 0;
}

.btn-refresh {
  padding: 8px 16px;
  background: #557c55;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.btn-refresh:hover:not(:disabled) {
  background: #446344;
  transform: translateY(-1px);
}

.btn-refresh:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.rates-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #eee;
}

.update-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
}

.label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.time {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #333;
}

.badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: bold;
  margin-left: auto;
}

.badge.fresh {
  background: #d1fae5;
  color: #065f46;
}

.badge.stale {
  background: #fee2e2;
  color: #991b1b;
}

.rates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.rate-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px;
  background: #fafbf9;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s;
}

.rate-card:hover {
  border-color: #557c55;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.currency-flag {
  font-size: 32px;
  line-height: 1;
}

.currency-info {
  flex: 1;
}

.currency-code {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.currency-name {
  font-size: 12px;
  color: #666;
}

.rate-value {
  text-align: right;
}

.rate-number {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #557c55;
  font-family: 'Courier New', monospace;
}

.base-currency {
  font-size: 11px;
  color: #999;
}

.info-box {
  padding: 15px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 6px;
}

.info-box p {
  margin: 5px 0;
  font-size: 13px;
  color: #92400e;
}

.loading-state,
.error-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #557c55;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.btn-retry {
  margin-top: 10px;
  padding: 8px 20px;
  background: #557c55;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
</style>