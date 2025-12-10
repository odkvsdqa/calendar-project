// src/composables/useCurrencyConverter.js
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../services/adminApi'

/**
 * 幣別換算 Composable
 * 提供即時匯率查詢與換算功能
 */
export function useCurrencyConverter() {
  // 匯率表 (基準: TWD)
  const rates = ref({
    TWD: 1.0,
    USD: 31.5,
    JPY: 0.21,
    EUR: 34.5,
    CNY: 4.35,
    KRW: 0.024
  })

  // 載入狀態
  const loading = ref(false)
  const lastUpdated = ref(null)

  // 支援的幣別清單
  const currencies = computed(() => Object.keys(rates.value))

  // 幣別符號對照
  const currencySymbols = {
    TWD: 'NT$',
    USD: '$',
    JPY: '¥',
    EUR: '€',
    CNY: '¥',
    KRW: '₩'
  }

  /**
   * 從 API 載入最新匯率
   */
  const loadRates = async () => {
    try {
      loading.value = true
      const response = await adminApi.getExchangeRates()
      
      if (response.data && response.data.rates) {
        rates.value = response.data.rates
        lastUpdated.value = response.data.lastUpdated
      }
    } catch (err) {
      console.warn('⚠️ 載入匯率失敗，使用預設值:', err)
      // 保持預設匯率
    } finally {
      loading.value = false
    }
  }

  /**
   * 將任意幣別換算成另一幣別
   * @param {number} amount - 金額
   * @param {string} fromCurrency - 來源幣別
   * @param {string} toCurrency - 目標幣別
   * @returns {number} 換算後的金額
   */
  const convert = (amount, fromCurrency, toCurrency) => {
    if (!amount || amount === 0) return 0
    if (fromCurrency === toCurrency) return amount

    const fromRate = rates.value[fromCurrency] || 1
    const toRate = rates.value[toCurrency] || 1

    // 公式: 金額 × (來源幣別對TWD匯率) ÷ (目標幣別對TWD匯率)
    const result = (amount * fromRate) / toRate
    return Math.round(result * 100) / 100 // 保留兩位小數
  }

  /**
   * 批次換算：將多幣別金額加總後換算成目標幣別
   * @param {Object} costs - { TWD: 10000, USD: 100, JPY: 5000 }
   * @param {string} targetCurrency - 目標幣別
   * @returns {number} 換算後的總金額
   */
  const convertTotal = (costs, targetCurrency) => {
    let totalInTWD = 0

    // 先全部換算成 TWD
    for (const [currency, amount] of Object.entries(costs)) {
      if (amount && amount > 0) {
        const rate = rates.value[currency] || 1
        totalInTWD += amount * rate
      }
    }

    // 再從 TWD 換算成目標幣別
    const targetRate = rates.value[targetCurrency] || 1
    const result = totalInTWD / targetRate
    return Math.round(result * 100) / 100
  }

  /**
   * 取得幣別符號
   */
  const getSymbol = (currency) => {
    return currencySymbols[currency] || currency
  }

  /**
   * 格式化金額顯示
   * @param {number} amount - 金額
   * @param {string} currency - 幣別
   * @returns {string} 格式化後的字串 (例: "NT$ 35,280")
   */
  const formatAmount = (amount, currency) => {
    const symbol = getSymbol(currency)
    const formatted = new Intl.NumberFormat('en-US').format(amount || 0)
    return `${symbol} ${formatted}`
  }

  // 初始化時載入匯率
  onMounted(() => {
    loadRates()
  })

  return {
    rates,
    currencies,
    loading,
    lastUpdated,
    convert,
    convertTotal,
    getSymbol,
    formatAmount,
    loadRates
  }
}