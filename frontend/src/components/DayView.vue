<template>
  <div class="day-view">
    <div class="day-view-container">
      <div class="time-column">
        <div v-for="hour in 24" :key="hour" class="time-slot">
          {{ String(hour - 1).padStart(2, '0') }}:00
        </div>
      </div>
      <div class="events-column">
        <div 
          v-for="hour in 24" 
          :key="hour" 
          class="hour-block" 
          @click="handleAddEvent(hour - 1)"
        >
          <div 
            v-for="event in getEventsForHour(hour - 1)"
            :key="event.id"
            v-show="shouldShowEventAtHour(event, hour - 1)"
            class="day-event"
            :style="getDayEventStyle(event)"
            @click.stop="handleEditEvent(event)"
          >
            <strong>{{ event.title }}</strong>
            <div class="event-time">
              {{ formatTime(event.startTime) }} - {{ formatTime(event.endTime) }}
            </div>
            <div v-if="event.description" class="event-desc">
              {{ event.description }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { formatDateTimeLocal, formatTime, isSameDay } from '../utils/dateFormatter'
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

const emit = defineEmits(['add-event-at-time', 'edit-event'])

const handleAddEvent = (hour) => {
  emit('add-event-at-time', hour)
}

const handleEditEvent = (event) => {
  emit('edit-event', event)
}

// const getEventsForHour = (hour) => {
//   return props.events.filter(event => {
//     const startDate = new Date(event.startTime)
//     const endDate = new Date(event.endTime)
    
//     const currentDay = new Date(year, month, day)
//     const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
//     const endDay = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate())
    
//     if (currentDay < startDay || currentDay > endDay) return false
    
//     if (currentDay.getTime() === startDay.getTime()) {
//       const startHour = startDate.getHours()
//       const endHour = endDate.getHours()
      
//       if (currentDay.getTime() === endDay.getTime()) {
//         return hour >= startHour && hour <= endHour
//       }
//       return hour >= startHour
//     }
    
//     if (currentDay.getTime() === endDay.getTime()) {
//       return hour <= endDate.getHours()
//     }
    
//     return true
//   }).sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
// }

const getEventsForHour = (hour) => {
  // ❌ 刪除所有 new Date(year, month, day) 的手動比較邏輯
  
  // ✅ 修改為：
  return props.events.filter(event => {
    const startDate = new Date(event.startTime)
    const endDate = new Date(event.endTime)
    
    // 判斷是否為「跨日事件」的中間部分，或是當日事件
    // 這裡為了簡化，我們先針對最常見的「單日事件」或「精準小時顯示」做優化：
    
    // 如果事件開始日期與當前視圖日期相同
    if (isSameDay(props.currentDate, startDate)) {
       return startDate.getHours() <= hour && (isSameDay(startDate, endDate) ? endDate.getHours() >= hour : true)
    }
    return false
    // (註：如果你的邏輯很複雜涉及跨多天，保留你原本的寫法可能更保險，但 isSameDay 可以用來替換掉 if (currentDay.getTime() === startDay.getTime()) 這種寫法)
  }).sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
}

// const shouldShowEventAtHour = (event, hour) => {
//   const startDate = new Date(event.startTime)
//   const year = props.currentDate.getFullYear()
//   const month = props.currentDate.getMonth()
//   const day = props.currentDate.getDate()
//   const currentDay = new Date(year, month, day)
//   const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
  
//   if (currentDay.getTime() === startDay.getTime()) {
//     return hour === startDate.getHours()
//   }
  
//   return hour === 0
// }

const shouldShowEventAtHour = (event, hour) => {
  const startDate = new Date(event.startTime)
  
  // ❌ 刪除 const year = ... const currentDay = ... 那一堆
  
  // ✅ 修改為：
  // 如果是今天開始的事件，且小時吻合
  if (isSameDay(props.currentDate, startDate)) {
    return hour === startDate.getHours()
  }
  
  // 如果是跨日事件的中間天，顯示在 0 點
  return hour === 0
}

const getDayEventStyle = (event) => {
  if (!Array.isArray(props.events)) {
    console.warn('events is not an array:', props.events)
    return []
  }
  const year = props.currentDate.getFullYear()
  const month = props.currentDate.getMonth()
  const day = props.currentDate.getDate()
  const currentDay = new Date(year, month, day)
  
  const startDate = new Date(event.startTime)
  const endDate = new Date(event.endTime)
  const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
  const endDay = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate())
  
  let startMinutes, endMinutes
  
  if (currentDay.getTime() === startDay.getTime()) {
    startMinutes = startDate.getHours() * 60 + startDate.getMinutes()
    if (currentDay.getTime() === endDay.getTime()) {
      endMinutes = endDate.getHours() * 60 + endDate.getMinutes()
    } else {
      endMinutes = 24 * 60
    }
  } else {
    startMinutes = 0
    if (currentDay.getTime() === endDay.getTime()) {
      endMinutes = endDate.getHours() * 60 + endDate.getMinutes()
    } else {
      endMinutes = 24 * 60
    }
  }
  
  const duration = endMinutes - startMinutes
  const topPosition = (startMinutes / 60) * 60
  const height = Math.max((duration / 60) * 60, 30)
  
  return {
    backgroundColor: event.color || '#557c55',
    height: height + 'px',
    top: topPosition + 'px',
    left: '5px',
    right: '5px'
  }
}

</script>

<style scoped>
/* 讓外層容器撐滿高度，並不允許外層捲動 */
.day-view {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden; 
}

/* 內層容器：負責顯示捲動條 */
.day-view-container {
  display: grid;
  grid-template-columns: 60px 1fr; /* 時間欄寬度稍微調小一點 */
  border: 1px solid #d1d5db;
  border-radius: 4px;
  background: white;
  
  /* 🔥 關鍵修正：允許垂直捲動，並佔滿剩餘空間 */
  flex: 1;
  overflow-y: auto; 
  height: 0; /* Flexbox 的小技巧，強迫子元素依照 flex:1 計算高度 */
}

.time-column {
  background: #fafbf9; /* 日系極簡的米白色 */
  border-right: 1px solid #d1d5db;
}

.time-slot {
  height: 60px; /* 每一小時的高度 */
  padding: 8px;
  border-bottom: 1px solid #e0e0e0;
  font-size: 11px;
  color: #666;
  text-align: center;
  font-family: 'Roboto', sans-serif;
  letter-spacing: 0.05em;
}

.events-column {
  background: white;
  position: relative; /* 讓絕對定位的事件條參考這裡 */
}

.hour-block {
  height: 60px; /* 必須跟 time-slot 一樣高 */
  border-bottom: 1px solid #e0e0e0;
  position: relative;
  cursor: pointer;
  transition: background 0.2s;
}

.hour-block:hover {
  background: #fcfcfc;
}

/* 事件條樣式 (保持日系極簡) */
.day-event {
  position: absolute;
  /* 預設顏色，會被 inline-style 覆蓋 */
  background: #557c55; 
  color: white;
  padding: 4px 8px;
  border-radius: 2px;
  font-size: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 5px rgba(0,0,0,0.15);
  border-left: 3px solid rgba(0,0,0,0.2); /* 增加一點質感 */
  z-index: 10;
  
  /* 避免文字太長跑版 */
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.day-event:hover {
  filter: brightness(0.95);
  transform: translateX(1px);
  z-index: 20;
}

.day-event strong {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 12px;
  line-height: 1.2;
}

.event-time {
  font-size: 10px;
  opacity: 0.9;
  margin-top: 1px;
}

.event-desc {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ⚠️ 新增 RWD - 平板 (768px 以下) */
@media (max-width: 768px) {
  .day-view {
    padding: 10px;
  }
  
  .day-view-container {
    grid-template-columns: 50px 1fr;
  }
  
  .time-slot {
    padding: 8px 2px;
    font-size: 10px;
  }
  
  .hour-block {
    height: 50px;
  }
  
  .day-event {
    padding: 2px 4px;
    font-size: 11px;
    left: 2px !important;
    right: 2px !important;
  }
  
  .day-event strong {
    font-size: 11px;
  }
  
  .event-time {
    font-size: 9px;
  }
  
  .event-desc {
    font-size: 9px;
    margin-top: 2px;
  }
}

/* ⚠️ 新增 RWD - 手機 (480px 以下) */
@media (max-width: 480px) {
  .day-view {
    padding: 10px;
  }
  
  .day-view-container {
    grid-template-columns: 50px 1fr;
    border-radius: 4px;
  }
  
  .time-slot {
    height: 40px;
    padding: 3px;
    font-size: 9px;
  }
  
  .hour-block {
    height: 40px;
  }
  
  .day-event {
    padding: 4px;
    font-size: 10px;
    border-radius: 3px;
    left: 2px;
    right: 2px;
  }
  
  .day-event strong {
    font-size: 10px;
    display: block;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .event-time {
    font-size: 8px;
    margin-top: 1px;
  }
  
  .event-desc {
    display: none; /* 手機上隱藏描述，節省空間 */
  }
}

/* ⚠️ 新增 RWD - 小手機 (360px 以下) */
@media (max-width: 360px) {
  .day-view {
    padding: 5px;
  }
  
  .day-view-container {
    grid-template-columns: 45px 1fr;
  }
  
  .time-slot {
    height: 35px;
    padding: 2px;
    font-size: 8px;
  }
  
  .hour-block {
    height: 35px;
  }
  
  .day-event {
    padding: 3px;
    font-size: 9px;
  }
  
  .day-event strong {
    font-size: 9px;
  }
  
  .event-time {
    font-size: 7px;
  }
}
</style>