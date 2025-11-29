<template>
  <div class="month-view">
    <!-- 星期標題列 -->
    <div class="weekdays-header">
      <div v-for="day in weekdays" :key="day" class="weekday-cell">
        {{ day }}
      </div>
    </div>

    <!-- 日期網格 -->
    <div class="calendar-grid">
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
        
        <!-- 事件容器 -->
        <div class="events-wrapper">
          <div 
            v-for="event in getEventsForDay(dayData.date)"
            :key="event.id"
            class="event-bar"
            :style="getEventStyle(event, dayData.date)"
            @click.stop="emit('edit-event', event)"
            :title="getEventTitle(event)"
          >
            <span v-if="shouldShowTitle(event, dayData.date)" class="event-title">
              {{ event.title }}
            </span>
          </div>
        </div>
        <!-- 👇 新增這段 -->
       <div 
       v-if="getEventsForDay(dayData.date).length > 3" 
       class="more-events"
       @click.stop="showMoreEvents(dayData.date)"
       >
       +{{ getEventsForDay(dayData.date).length - 3 }} more
</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { isSameDay } from '../utils/dateFormatter'

const props = defineProps({
  currentDate: { type: Date, required: true },
  events: { type: Array, default: () => [] }
})

const emit = defineEmits(['add-event', 'edit-event'])

const weekdays = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT']
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

  // 1. 上個月的天數
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    calendarDays.value.push({
      date: new Date(year, month - 1, prevLastDayDate - i),
      isOtherMonth: true,
      isToday: false
    })
  }

  // 2. 當月的天數
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

  // 3. 下個月的天數，湊滿 42 格
  const totalCellsFilled = calendarDays.value.length
  const totalCellsNeeded = 42
  const remainingCells = totalCellsNeeded - totalCellsFilled
  for (let i = 1; i <= remainingCells; i++) {
    calendarDays.value.push({
      date: new Date(year, month + 1, i),
      isOtherMonth: true,
      isToday: false
    })
  }
  
  assignEventTracks()
}

const getEventsForDay = (date) => {
  if (!Array.isArray(props.events)) return []
  
  const targetDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  return props.events.filter(event => {
    const startDate = new Date(event.startTime)
    const endDate = new Date(event.endTime)
    const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
    const endDay = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate())
    
    return targetDay >= startDay && targetDay <= endDay
  }).sort((a, b) => {
    // 先按軌道排序，再按開始時間
    const trackA = eventTracks.value.get(a.id) || 0
    const trackB = eventTracks.value.get(b.id) || 0
    if (trackA !== trackB) return trackA - trackB
    return new Date(a.startTime) - new Date(b.startTime)
  })
}

// 🔥 改進的軌道分配演算法
const eventTracks = ref(new Map())

const assignEventTracks = () => {
  eventTracks.value.clear()
  
  if (!Array.isArray(props.events) || props.events.length === 0) return
  
  // 按開始時間排序
  const sortedEvents = [...props.events].sort((a, b) => 
    new Date(a.startTime) - new Date(b.startTime)
  )
  
  // 使用更精確的軌道分配
  const tracks = [] // [{ endDate, eventId }]
  
  sortedEvents.forEach(event => {
    const eventStart = new Date(event.startTime)
    const eventEnd = new Date(event.endTime)
    
    // 標準化為日期（去掉時間）
    const startDay = new Date(eventStart.getFullYear(), eventStart.getMonth(), eventStart.getDate())
    const endDay = new Date(eventEnd.getFullYear(), eventEnd.getMonth(), eventEnd.getDate())
    
    // 找第一個可用的軌道
    let trackIndex = 0
    while (trackIndex < tracks.length) {
      const track = tracks[trackIndex]
      // 如果該軌道的最後事件已經結束（不重疊）
      if (track.endDate < startDay) {
        break
      }
      trackIndex++
    }
    
    // 分配軌道
    eventTracks.value.set(event.id, trackIndex)
    
    // 更新軌道資訊
    if (trackIndex >= tracks.length) {
      tracks.push({ endDate: endDay, eventId: event.id })
    } else {
      tracks[trackIndex] = { endDate: endDay, eventId: event.id }
    }
  })
}

const getEventStyle = (event, date) => {
  const trackIndex = eventTracks.value.get(event.id) || 0
  const topPosition = trackIndex * 18
  
  const startDate = new Date(event.startTime)
  const endDate = new Date(event.endTime)
  
  const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
  const endDay = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate())
  const currentDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  const isStart = currentDay.getTime() === startDay.getTime()
  const isEnd = currentDay.getTime() === endDay.getTime()

  const style = {
    top: topPosition + 'px',
    position: 'absolute',
    zIndex: 10 - trackIndex,
    left: '0',
    right: '0',
    // 👇 關鍵新增：直接使用事件顏色，如果沒有就用預設抹茶綠
    backgroundColor: event.color || '#557c55', 
    color: 'white', // 文字白色
    fontSize: '10px', // 字體稍微縮小適應 18px 高度
    lineHeight: '16px', // 垂直置中
    padding: '1px 4px', // 內距
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    marginLeft: isStart ? '2px' : '0',     // 👈 改用 margin
    marginRight: isEnd ? '2px' : '0'       // 👈 改用 margin
  }
  
  // 圓角
  if (isStart && isEnd) {
    style.borderRadius = '2px'
    style.marginLeft = '4px'
    style.marginRight = '0px'
  } else if (isStart) {
    style.borderRadius = '2px 0 0 2px'
    style.marginLeft = '4px'
    style.marginRight = '0'
  } else if (isEnd) {
    style.borderRadius = '0 2px 2px 0'
    style.marginLeft = '0'
    style.marginRight = '0px'
  } else {
    style.borderRadius = '0'
    style.marginLeft = '0'
    style.marginRight = '0'
  }
  
  return style
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

renderCalendar()

watch(() => props.currentDate, () => renderCalendar())
watch(() => props.events, () => {
  renderCalendar()
}, { deep: true })
</script>

<style scoped>
/* 日系極簡風格 */
.month-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
  height: 100%;
  background: #fff;
}

.more-events {
  position: absolute;
  bottom: 4px;
  left: 4px;
  font-size: 10px;
  color: #999;
  cursor: pointer;
  padding: 2px 4px;
  background: #f5f5f5;
  border-radius: 2px;
  z-index: 20;
}

.more-events:hover {
  background: #e5e5e5;
  color: #666;
}

/* 星期標題 */
.weekdays-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #fafbf9;
  border-bottom: 1px solid #d1d5db;
  flex-shrink: 0;
}

.weekday-cell {
  text-align: center;
  padding: 12px 0;
  font-weight: 500;
  color: #888;
  font-size: 11px;
  letter-spacing: 0.1em;
   text-transform: uppercase; /*　？ */
}

/* 日期網格 */
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  /* grid-auto-rows: minmax(100px, auto); 🔥 改為 100px 最小高度，給事件更多空間 */
   grid-auto-rows: 1fr; 
  /* 🔥 新增：給左邊和上面補上邊框，讓最左上角的格子也有線 */
  border-left: 1px solid #e0e0e0;
  flex: 1;
  gap: 0;
  overflow-y: auto;
}
.day:nth-child(7n+1) {
  border-left: 1px solid #f5f5f5;
}

/* 日期格子 */
.day {
  border-right: 1px solid #e0e0e0;
  border-bottom: 1px solid#e0e0e0;
  padding: 8px 0 4px 0;
  cursor: pointer;
  position: relative;
  transition: background 0.2s;
  background: white;
  min-height: 120px; /* 🔥 確保最小高度 */
  height: auto;
  box-sizing: border-box;
  width: 100%;
}

.day:hover {
  background: #f5f7f5;
}

/* 日期數字 */
.day-number {
  font-size: 12px;
  color: #444;
  font-family: 'Roboto', 'Helvetica Neue', sans-serif;
  font-weight: 400;
  position: relative; /* 👈 改為 relative */
  z-index: 1;
  margin-bottom: 0; /* 👈 移除 margin-bottom */
  padding-left: 8px;
  display: inline-block;
  line-height: 22px;     /* 👈 新增：與黑圈圈高度一致 */
}

/* 非當月日期 */
.day.other-month {
  background: #fafafa;
}

.day.other-month .day-number {
  color: #ccc;
}

.day.other-month .event-bar {
  opacity: 0.5;
}

/* 今天 */
.day.today .day-number {
  background: #333;
  color: white;
  width: 22px;
  height: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: 500;
  margin-left: 8px;      /* 👈 新增：與其他日期對齊 */
  padding-left: 0;       /* 👈 新增：取消內部 padding */
   text-decoration: underline; /* 增加底線強調 */
}

.events-wrapper {
  position: relative; /* 👈 關鍵：作為事件條的定位參考 */
  width: 100%;
  min-height: 60px;
  margin-top: 4px; /* 👈 與日期數字保持距離 */
}

/* 事件條 - 莫蘭迪色系 */
.event-bar {
  position: absolute;
  color: white;
  height: 16px; /* 🔥 固定高度 16px */
  padding: 2px 6px;
  font-size: 10px;
  line-height: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.2s;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1); /* 🔥 加上陰影增加層次感 */
  box-sizing: border-box; /* 👈 新增這行，關鍵！ */
  margin: 0;
}

.event-bar:hover {
  opacity: 0.85;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.event-title {
  display: inline-block;
  vertical-align: middle;
}

/* 顏色 */
.event-bar.blue {
  background-color: #7c8db5;
}

.event-bar.purple {
  background-color: #bfaac1;
}

.event-bar.green {
  background-color: #9cb094;
}

/* RWD */
@media (max-width: 768px) {
  .day {
    padding: 4px;
    min-height: 90px;
  }
  
  .day-number {
    font-size: 11px;
    padding-left: 4px;
  }
  
  .event-bar {
    height: 14px;
    font-size: 9px;
    line-height: 10px;
    padding: 2px 4px;
  }
  
  .calendar-grid {
    grid-auto-rows: minmax(90px, auto);
  }
   /* 👇 新增：調整手機版的事件間距 */
  .events-wrapper {
    margin-top: 2px; /* 👈 減少上邊距 */
  }
}

@media (max-height: 850px) {
  .weekday-cell {
    padding: 8px 0;
    font-size: 10px;
  }
  
  .event-bar {
    height: 15px;
    font-size: 9px;
  }
}
</style>