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

const getEventsForHour = (hour) => {
  const year = props.currentDate.getFullYear()
  const month = props.currentDate.getMonth()
  const day = props.currentDate.getDate()
  
  return props.events.filter(event => {
    const startDate = new Date(event.startTime)
    const endDate = new Date(event.endTime)
    
    const currentDay = new Date(year, month, day)
    const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
    const endDay = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate())
    
    if (currentDay < startDay || currentDay > endDay) return false
    
    if (currentDay.getTime() === startDay.getTime()) {
      const startHour = startDate.getHours()
      const endHour = endDate.getHours()
      
      if (currentDay.getTime() === endDay.getTime()) {
        return hour >= startHour && hour <= endHour
      }
      return hour >= startHour
    }
    
    if (currentDay.getTime() === endDay.getTime()) {
      return hour <= endDate.getHours()
    }
    
    return true
  }).sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
}

const shouldShowEventAtHour = (event, hour) => {
  const startDate = new Date(event.startTime)
  const year = props.currentDate.getFullYear()
  const month = props.currentDate.getMonth()
  const day = props.currentDate.getDate()
  const currentDay = new Date(year, month, day)
  const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
  
  if (currentDay.getTime() === startDay.getTime()) {
    return hour === startDate.getHours()
  }
  
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
    backgroundColor: event.color || '#667eea',
    height: height + 'px',
    top: topPosition + 'px',
    left: '5px',
    right: '5px'
  }
}

const formatTime = (dateTime) => {
  const date = new Date(dateTime)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return hours + ':' + minutes
}
</script>

<style scoped>
.day-view {
  padding: 30px;
}

.day-view-container {
  display: grid;
  grid-template-columns: 80px 1fr;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.time-column {
  background: #f8f9fa;
  border-right: 1px solid #e0e0e0;
}

.time-slot {
  height: 60px;
  padding: 8px;
  border-bottom: 1px solid #e0e0e0;
  font-size: 12px;
  color: #666;
  text-align: center;
}

.events-column {
  background: white;
}

.hour-block {
  height: 60px;
  border-bottom: 1px solid #e0e0e0;
  position: relative;
  cursor: pointer;
  transition: background 0.2s;
}

.hour-block:hover {
  background: #f8f9fa;
}

.day-event {
  position: absolute;
  background: #667eea;
  color: white;
  padding: 8px;
  border-radius: 4px;
  font-size: 13px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.day-event:hover {
  background: #5568d3;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  transform: translateX(-2px);
  z-index: 100;
}

.event-time {
  font-size: 11px;
  margin-top: 2px;
  opacity: 0.9;
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
    padding: 15px;
  }
  
  .day-view-container {
    grid-template-columns: 60px 1fr;
  }
  
  .time-slot {
    height: 50px;
    padding: 5px;
    font-size: 10px;
  }
  
  .hour-block {
    height: 50px;
  }
  
  .day-event {
    padding: 6px;
    font-size: 11px;
    left: 3px;
    right: 3px;
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