import { computed } from 'vue'

export function useCostAnalysis(eventsRef) {
  
  // 🔥 輔助函式：取得本地日期的 YYYY-MM-DD (解決時區偏移問題)
  const getLocalDateKey = (dateInput) => {
    const d = new Date(dateInput)
    // 確保這裡是用本地時間 getFullYear / getMonth / getDate
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  // 計算每日總花費
  const dailyCosts = computed(() => {
    const costs = {}
    if (!eventsRef.value || !Array.isArray(eventsRef.value)) return costs

    eventsRef.value.forEach(event => {
      const cost = parseFloat(event.estimatedCost || 0)
      if (cost <= 0) return
      
      // 🔥 修正 1：使用本地時間 Key
      const dateKey = getLocalDateKey(event.startTime)
      
      if (!costs[dateKey]) costs[dateKey] = 0
      costs[dateKey] += cost
    })
    return costs
  })

  const maxDailyCost = computed(() => {
    const values = Object.values(dailyCosts.value)
    if (values.length === 0) return 0
    return Math.max(...values)
  })

  const getCostLevel = (date) => {
    if (!date) return 0
    // 🔥 修正 2：使用本地時間 Key
    const dateKey = getLocalDateKey(date)
    
    const cost = dailyCosts.value[dateKey] || 0
    const max = maxDailyCost.value

    if (cost === 0 || max === 0) return 0
    const ratio = cost / max
    if (ratio > 0.70) return 3
    if (ratio > 0.35) return 2
    return 1
  }

  const getDailyCost = (date) => {
    const dateKey = getLocalDateKey(date)
    return dailyCosts.value[dateKey] || 0
  }

  const formatCost = (val) => {
    if (!val) return ''
    return new Intl.NumberFormat('zh-TW', { style: 'decimal' }).format(val)
  }

  return { dailyCosts, getCostLevel, formatCost, getDailyCost }
}