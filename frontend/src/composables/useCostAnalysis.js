import { computed } from 'vue'

export function useCostAnalysis(eventsRef) {
  
  // 計算每日總花費
  const dailyCosts = computed(() => {
    const costs = {}
    if (!eventsRef.value || !Array.isArray(eventsRef.value)) return costs

    eventsRef.value.forEach(event => {
      const cost = parseFloat(event.estimatedCost || 0)
      if (cost <= 0) return
      
      const dateKey = new Date(event.startTime).toISOString().split('T')[0]
      if (!costs[dateKey]) costs[dateKey] = 0
      costs[dateKey] += cost
    })
    return costs
  })

  // 找出區間內最高花費金額，作為熱力圖 100% 基準
  const maxDailyCost = computed(() => {
    const values = Object.values(dailyCosts.value)
    if (values.length === 0) return 0
    return Math.max(...values)
  })

  // 回傳 0-3 的等級，用於 CSS 綁定
  // 0: 無/極低, 1: 輕微, 2: 中等, 3: 高 (森林深處)
  const getCostLevel = (date) => {
    if (!date) return 0
    const dateKey = date.toISOString().split('T')[0]
    const cost = dailyCosts.value[dateKey] || 0
    const max = maxDailyCost.value

    if (cost === 0 || max === 0) return 0
    
    const ratio = cost / max
    if (ratio > 0.70) return 3
    if (ratio > 0.35) return 2
    return 1
  }

  const getDailyCost = (date) => {
    if (!date) return 0
    const dateKey = date.toISOString().split('T')[0]
    return dailyCosts.value[dateKey] || 0
  }

  const formatCost = (val) => {
    if (!val) return ''
    return new Intl.NumberFormat('zh-TW', { 
      style: 'decimal', 
      maximumFractionDigits: 0 
    }).format(val)
  }

  return { dailyCosts, getCostLevel, formatCost, getDailyCost }
}