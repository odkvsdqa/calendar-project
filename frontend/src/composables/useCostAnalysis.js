import { computed } from 'vue'

export function useCostAnalysis(eventsRef) {
  
  // 🔥 幣別符號對照表
  const currencySymbols = {
    'TWD': 'NT$',
    'USD': '$',
    'JPY': '¥',
    'EUR': '€',
    'CNY': '¥',
    'KRW': '₩'
  }
  
  // 輔助函式：取得本地日期的 YYYY-MM-DD
  const getLocalDateKey = (dateInput) => {
    const d = new Date(dateInput)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  // 🔥 計算每日總花費（按幣別分組）
  const dailyCosts = computed(() => {
    const costs = {}
    if (!eventsRef.value || !Array.isArray(eventsRef.value)) return costs

    eventsRef.value.forEach(event => {
      const cost = parseFloat(event.estimatedCost || 0)
      if (cost <= 0) return
      
      const dateKey = getLocalDateKey(event.startTime)
      const currency = event.currency || 'TWD'
      
      if (!costs[dateKey]) {
        costs[dateKey] = {}
      }
      if (!costs[dateKey][currency]) {
        costs[dateKey][currency] = 0
      }
      costs[dateKey][currency] += cost
    })
    return costs
  })

  // 🔥 計算最大花費（僅用於熱力圖，以 TWD 為基準）
  const maxDailyCost = computed(() => {
    const values = Object.values(dailyCosts.value).map(dayCosts => {
      // 只取 TWD 的值（或加總所有幣別，看你需求）
      return dayCosts['TWD'] || 0
    })
    if (values.length === 0) return 0
    return Math.max(...values)
  })

  const getCostLevel = (date) => {
    if (!date) return 0
    const dateKey = getLocalDateKey(date)
    
    const dayCosts = dailyCosts.value[dateKey] || {}
    const cost = dayCosts['TWD'] || 0 // 以 TWD 為基準
    const max = maxDailyCost.value

    if (cost === 0 || max === 0) return 0
    const ratio = cost / max
    if (ratio > 0.70) return 3
    if (ratio > 0.35) return 2
    return 1
  }

  // 🔥 取得某日的完整花費（包含所有幣別）
  const getDailyCost = (date) => {
    const dateKey = getLocalDateKey(date)
    return dailyCosts.value[dateKey] || {}
  }

  // 🔥 格式化金額（含幣別符號）
  const formatCost = (val, currency = 'TWD') => {
    if (!val) return ''
    const formatted = new Intl.NumberFormat('zh-TW', { 
      style: 'decimal',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(val)
    
    const symbol = currencySymbols[currency] || currency
    return `${symbol} ${formatted}`
  }

  // 🔥 格式化多幣別顯示（用於 Tooltip）
  const formatMultiCurrencyCost = (costsObj) => {
    if (!costsObj || Object.keys(costsObj).length === 0) return ''
    
    return Object.entries(costsObj)
      .map(([currency, amount]) => formatCost(amount, currency))
      .join(' + ')
  }

  return { 
    dailyCosts, 
    getCostLevel, 
    formatCost, 
    getDailyCost,
    formatMultiCurrencyCost // 🔥 新增
  }
}