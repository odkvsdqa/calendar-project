<template>
  <div class="month-view">
    <div class="calendar-grid">
      <div v-for="day in weekdays" :key="day" class="day-header">
        {{ day }}
      </div>
      <div v-if="events.length === 0" class="empty-state">
      <div class="empty-icon">📅</div>
      <p>還沒有任何事件</p>
      <button @click="$emit('add-event')">新增第一個事件</button>
      </div>
      <div 
        v-for="(dayData, index) in calendarDays" 
        :key="index"
        class="day"
        :class="{
          'other-month': dayData.isOtherMonth,
          'today': dayData.isToday
        }"
        @click="emit('add-event', dayData.date)"
      >
        <div class="day-number">{{ dayData.date.getDate() }}</div>
        <div 
          v-for="(event, eIndex) in getEventsForDay(dayData.date)"
          :key="event.id"
          class="event-bar"
          :style="getEventStyle(eIndex, event, dayData.date)"
          @click.stop="emit('edit-event', event)"
          :title="getEventTitle(event)"
        >
          {{ shouldShowTitle(event, dayData.date) ? event.title : '' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

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

const emit = defineEmits(['add-event', 'edit-event'])

const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const calendarDays = ref([])

const renderCalendar = () => {
  const year = props.currentDate.getFullYear()
  const month = props.currentDate.getMonth()
  
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const prevLastDay = new Date(year, month, 0)
  
  const firstDayWeek = firstDay.getDay()
  const lastDayDate = lastDay.getDate()
  const prevLastDayDate = prevLastDay.getDate()

  calendarDays.value = []

  for (let i = firstDayWeek - 1; i >= 0; i--) {
    calendarDays.value.push({
      date: new Date(year, month - 1, prevLastDayDate - i),
      isOtherMonth: true,
      isToday: false
    })
  }

  const today = new Date()
  for (let i = 1; i <= lastDayDate; i++) {
    const isToday = year === today.getFullYear() && 
                   month === today.getMonth() && 
                   i === today.getDate()
    calendarDays.value.push({
      date: new Date(year, month, i),
      isOtherMonth: false,
      isToday: isToday
    })
  }

  const totalCells = calendarDays.value.length
  const remainingDays = (totalCells % 7 === 0) ? 0 : 7 - (totalCells % 7)
  for (let i = 1; i <= remainingDays; i++) {
    calendarDays.value.push({
      date: new Date(year, month + 1, i),
      isOtherMonth: true,
      isToday: false
    })
  }
}

const getEventsForDay = (date) => {
  
  if (!Array.isArray(props.events)) {
    console.warn('events is not an array:', props.events)
    return []
  }
  const targetDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  const dayEvents = props.events.filter(event => {
    const startDate = new Date(event.startTime)
    const endDate = new Date(event.endTime)
    const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
    const endDay = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate())
    
    return targetDay >= startDay && targetDay <= endDay
  }).sort((a, b) => {
    return new Date(a.startTime) - new Date(b.startTime)
  })
  
  return dayEvents
}

const getEventStyle = (index, event) => {
  const topPosition = 35 + (index * 28)
  
  return {
    top: topPosition + 'px',
    left: '4px',
    right: '4px',
    backgroundColor: event.color
  }
}

const getEventTitle = (event) => {
  let title = event.title
  if (event.description) {
    title = title + '\n' + event.description
  }
  return title
}

const shouldShowTitle = (event, date) => {
  const startDate = new Date(event.startTime)
  const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
  const currentDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  return currentDay.getTime() === startDay.getTime()
}

// 初始渲染
renderCalendar()

// 監聽 currentDate 和 events 變化
watch(() => props.currentDate, () => {
  renderCalendar()
})

watch(() => props.events, () => {
  renderCalendar()
}, { deep: true })
</script>

<style scoped>
.month-view {
  padding: 30px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e0e0e0;
  border: 1px solid #e0e0e0;
}

.day-header {
  text-align: center;
  padding: 15px;
  font-weight: bold;
  color: #667eea;
  background: #f0f4ff;
}

.day {
  min-height: 120px;
  background: white;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.day:hover {
  background: #f8f9fa;
}

.day-number {
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
  position: relative;
  z-index: 1;
}

.day.other-month {
  opacity: 0.3;
}

.day.today {
  background: #fff9e6;
}

.day.today .day-number {
  background: #fbbf24;
  color: white;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.event-bar {
  position: absolute;
  height: 24px;
  background: #667eea;
  color: white;
  padding: 2px 8px;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  border-radius: 4px;
  z-index: 2;
  display: flex;
  align-items: center;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.event-bar:hover {
  background: #5568d3;
  z-index: 100;
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
  transform: translateY(-1px);
}


/* ⚠️ 新增 RWD - 平板 (768px 以下) */
@media (max-width: 768px) {
  .month-view {
    padding: 15px;
  }
  
  .day {
    min-height: 100px;
    padding: 5px;
  }
  
  .day-number {
    font-size: 12px;
    margin-bottom: 5px;
  }
  
  .day.today .day-number {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }
  
  .day-header {
    padding: 10px;
    font-size: 13px;
  }
  
  .event-bar {
    font-size: 10px;
    height: 20px;
    padding: 2px 5px;
  }
}

/* ⚠️ 新增 RWD - 手機 (480px 以下) */
@media (max-width: 480px) {
  .month-view {
    padding: 10px;
  }
  
  .calendar-grid {
    gap: 0;
  }
  
  .day {
    min-height: 80px;
    padding: 3px;
  }
  
  .day-number {
    font-size: 11px;
    margin-bottom: 3px;
  }
  
  .day.today .day-number {
    width: 20px;
    height: 20px;
    font-size: 10px;
  }
  
  .day-header {
    padding: 8px 2px;
    font-size: 11px;
  }
  
  .event-bar {
    font-size: 9px;
    height: 18px;
    padding: 1px 3px;
    border-radius: 3px;
    left: 2px;
    right: 2px;
  }
}

/* ⚠️ 新增 RWD - 小手機 (360px 以下) */
@media (max-width: 360px) {
  .month-view {
    padding: 5px;
  }
  
  .day {
    min-height: 70px;
    padding: 2px;
  }
  
  .day-number {
    font-size: 10px;
    margin-bottom: 2px;
  }
  
  .day.today .day-number {
    width: 18px;
    height: 18px;
    font-size: 9px;
  }
  
  .day-header {
    padding: 6px 1px;
    font-size: 10px;
  }
  
  .event-bar {
    font-size: 8px;
    height: 16px;
    padding: 1px 2px;
    left: 1px;
    right: 1px;
  }
  
  /* 小手機上最多顯示 3 個事件，其他隱藏 */
  .event-bar:nth-child(n+6) {
    display: none;
  }
}
</style>