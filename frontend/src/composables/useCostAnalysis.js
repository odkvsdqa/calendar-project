import { computed } from 'vue'

export function useCostAnalysis(eventsRef) {
  
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

  const maxDailyCost = computed(() => {
    const values = Object.values(dailyCosts.value)
    if (values.length === 0) return 0
    return Math.max(...values)
  })

  // 0: 無, 1: 低, 2: 中, 3: 高
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
    const dateKey = date.toISOString().split('T')[0]
    return dailyCosts.value[dateKey] || 0
  }

  const formatCost = (val) => {
    if (!val) return ''
    return new Intl.NumberFormat('zh-TW', { style: 'decimal' }).format(val)
  }

  return { dailyCosts, getCostLevel, formatCost, getDailyCost }
}