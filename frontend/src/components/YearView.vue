<template>
  <div class="year-view">
    <div class="months-grid">
      <div v-for="monthIndex in 12" :key="monthIndex" class="month-card">
        <h3 class="month-title">{{ monthIndex }}月</h3>
        <div class="mini-calendar">
          <div v-for="day in ['日', '一', '二', '三', '四', '五', '六']" :key="day" class="mini-day-header">
            {{ day }}
          </div>
          <div 
            v-for="(dayData, index) in getMonthDays(monthIndex - 1)" 
            :key="index"
            class="mini-day"
            :class="{
              'other-month': dayData.isOtherMonth,
              'today': dayData.isToday,
              'has-events': hasEvents(dayData.date)
            }"
            @click="emit('go-to-date', dayData.date)"
          >
            {{ dayData.date.getDate() }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  currentDate: {
    type: Date,
    required: true
  },
  events: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['go-to-date'])

const getMonthDays = (monthIndex) => {
  const year = props.currentDate.getFullYear()
  const firstDay = new Date(year, monthIndex, 1)
  const lastDay = new Date(year, monthIndex + 1, 0)
  const prevLastDay = new Date(year, monthIndex, 0)
  
  const firstDayWeek = firstDay.getDay()
  const lastDayDate = lastDay.getDate()
  const prevLastDayDate = prevLastDay.getDate()

  const days = []

  for (let i = firstDayWeek - 1; i >= 0; i--) {
    days.push({
      date: new Date(year, monthIndex - 1, prevLastDayDate - i),
      isOtherMonth: true,
      isToday: false
    })
  }

  const today = new Date()
  for (let i = 1; i <= lastDayDate; i++) {
    const isToday = year === today.getFullYear() && 
                   monthIndex === today.getMonth() && 
                   i === today.getDate()
    days.push({
      date: new Date(year, monthIndex, i),
      isOtherMonth: false,
      isToday: isToday
    })
  }

  const totalCells = days.length
  const remainingDays = (totalCells % 7 === 0) ? 0 : 7 - (totalCells % 7)
  for (let i = 1; i <= remainingDays; i++) {
    days.push({
      date: new Date(year, monthIndex + 1, i),
      isOtherMonth: true,
      isToday: false
    })
  }

  return days
}

const hasEvents = (date) => {
  if (!Array.isArray(props.events)) {
    console.warn('events is not an array:', props.events)
    return []
  }
  const targetDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  return props.events.some(event => {
    const startDate = new Date(event.startTime)
    const endDate = new Date(event.endTime)
    const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
    const endDay = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate())
    
    return targetDay >= startDay && targetDay <= endDay
  })
}
</script>

<style scoped>
.year-view {
  padding: 30px;
}

.months-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.month-card {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 15px;
  transition: all 0.3s;
}

.month-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.month-title {
  text-align: center;
  color: #667eea;
  margin-bottom: 10px;
  font-size: 18px;
}

.mini-calendar {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.mini-day-header {
  text-align: center;
  font-size: 10px;
  font-weight: bold;
  color: #667eea;
  padding: 5px 2px;
}

.mini-day {
  text-align: center;
  padding: 8px 2px;
  font-size: 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.mini-day:hover {
  background: #f0f4ff;
}

.mini-day.other-month {
  opacity: 0.3;
}

.mini-day.today {
  background: #fbbf24;
  color: white;
  font-weight: bold;
}

.mini-day.has-events {
  background: #667eea;
  color: white;
}

/* ⚠️ 新增 RWD - 平板 (768px 以下) */
@media (max-width: 768px) {
  .year-view {
    padding: 15px;
  }
  
  .months-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 15px;
  }
  
  .month-card {
    padding: 12px;
    border-radius: 10px;
  }
  
  .month-title {
    font-size: 16px;
    margin-bottom: 8px;
  }
  
  .mini-day-header {
    font-size: 9px;
    padding: 4px 1px;
  }
  
  .mini-day {
    padding: 6px 1px;
    font-size: 11px;
  }
}

/* ⚠️ 新增 RWD - 手機 (480px 以下) */
@media (max-width: 480px) {
  .year-view {
    padding: 10px;
  }
  
  .months-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .month-card {
    padding: 10px;
    border-radius: 8px;
    border-width: 1px;
  }
  
  .month-title {
    font-size: 15px;
    margin-bottom: 6px;
  }
  
  .mini-calendar {
    gap: 1px;
  }
  
  .mini-day-header {
    font-size: 8px;
    padding: 3px 0;
  }
  
  .mini-day {
    padding: 5px 0;
    font-size: 10px;
    border-radius: 3px;
  }
}

/* ⚠️ 新增 RWD - 小手機 (360px 以下) */
@media (max-width: 360px) {
  .year-view {
    padding: 8px;
  }
  
  .months-grid {
    gap: 10px;
  }
  
  .month-card {
    padding: 8px;
    border-radius: 6px;
  }
  
  .month-title {
    font-size: 14px;
    margin-bottom: 5px;
  }
  
  .mini-day-header {
    font-size: 7px;
    padding: 2px 0;
  }
  
  .mini-day {
    padding: 4px 0;
    font-size: 9px;
  }
}

/* ⚠️ 新增 RWD - 超小手機 (320px 以下) */
@media (max-width: 320px) {
  .year-view {
    padding: 5px;
  }
  
  .months-grid {
    gap: 8px;
  }
  
  .month-card {
    padding: 6px;
  }
  
  .month-title {
    font-size: 13px;
    margin-bottom: 4px;
  }
  
  .mini-calendar {
    gap: 0;
  }
  
  .mini-day-header {
    font-size: 6px;
    padding: 1px 0;
  }
  
  .mini-day {
    padding: 3px 0;
    font-size: 8px;
  }
}
</style>