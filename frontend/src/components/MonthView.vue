<template>
  <div class="month-view">
    <div class="weekdays-header">
      <div v-for="day in weekdays" :key="day" class="weekday-cell">{{ day }}</div>
    </div>

    <div class="calendar-grid">
      <div 
        v-for="(dayData, index) in calendarDays" 
        :key="index"
        class="day"
        :class="{
          'other-month': dayData.isOtherMonth,
          'today': dayData.isToday,
          /* 🔥 綁定熱力圖 */
          [`forest-level-${getCostLevel(dayData.date)}`]: !dayData.isOtherMonth
        }"
        @click="emit('add-event', dayData.date)"
      >
        <div class="day-number">{{ dayData.date.getDate() }}</div>
        
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
       <div v-if="getEventsForDay(dayData.date).length > 3" class="more-events" @click.stop="showMoreEvents(dayData.date)">
          +{{ getEventsForDay(dayData.date).length - 3 }} more
       </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, toRef } from 'vue' // toRef 很重要
import { useCostAnalysis } from '../composables/useCostAnalysis' // 🔥 引入

const props = defineProps({
  currentDate: { type: Date, required: true },
  events: { type: Array, default: () => [] }
})
const emit = defineEmits(['add-event', 'edit-event'])

// 🔥 使用 Composable 處理金錢邏輯
const eventsRef = toRef(props, 'events')
const { getCostLevel } = useCostAnalysis(eventsRef)

// --- 以下維持你原有的日曆邏輯 ---
const weekdays = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT']
const calendarDays = ref([])
const eventTracks = ref(new Map())

const renderCalendar = () => { /* ... 保留你修好的邏輯 ... */ 
  // 簡化顯示，請貼上你原本的 renderCalendar
  const year = props.currentDate.getFullYear()
  const month = props.currentDate.getMonth()
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const prevLastDay = new Date(year, month, 0)
  const firstDayWeek = firstDay.getDay()
  calendarDays.value = []
  for (let i = firstDayWeek - 1; i >= 0; i--) calendarDays.value.push({date: new Date(year, month - 1, prevLastDay.getDate() - i), isOtherMonth: true, isToday: false})
  for (let i = 1; i <= lastDay.getDate(); i++) {
      const today = new Date(); 
      calendarDays.value.push({date: new Date(year, month, i), isOtherMonth: false, isToday: year === today.getFullYear() && month === today.getMonth() && i === today.getDate()})
  }
  const remainingCells = 42 - calendarDays.value.length
  for (let i = 1; i <= remainingCells; i++) calendarDays.value.push({date: new Date(year, month + 1, i), isOtherMonth: true, isToday: false})
  assignEventTracks()
}

const getEventsForDay = (date) => { /* ... 保留你修好的邏輯 ... */ 
  if (!Array.isArray(props.events)) return []
  const targetDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  return props.events.filter(event => {
    const startDate = new Date(event.startTime)
    const endDate = new Date(event.endTime)
    const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
    const endDay = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate())
    return targetDay >= startDay && targetDay <= endDay
  }).sort((a, b) => {
    const trackA = eventTracks.value.get(a.id) || 0; const trackB = eventTracks.value.get(b.id) || 0
    if (trackA !== trackB) return trackA - trackB
    return new Date(a.startTime) - new Date(b.startTime)
  })
}

const assignEventTracks = () => { /* ... 保留你修好的邏輯 ... */ 
  eventTracks.value.clear()
  if (!Array.isArray(props.events) || props.events.length === 0) return
  const sortedEvents = [...props.events].sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
  const tracks = []
  sortedEvents.forEach(event => {
    const eventStart = new Date(event.startTime); const eventEnd = new Date(event.endTime)
    const startDay = new Date(eventStart.getFullYear(), eventStart.getMonth(), eventStart.getDate())
    const endDay = new Date(eventEnd.getFullYear(), eventEnd.getMonth(), eventEnd.getDate())
    let trackIndex = 0
    while (trackIndex < tracks.length) { if (tracks[trackIndex].endDate < startDay) break; trackIndex++ }
    eventTracks.value.set(event.id, trackIndex)
    if (trackIndex >= tracks.length) tracks.push({ endDate: endDay, eventId: event.id })
    else tracks[trackIndex] = { endDate: endDay, eventId: event.id }
  })
}

const getEventStyle = (event, date) => { /* ... 保留你修好的邏輯 ... */ 
  const trackIndex = eventTracks.value.get(event.id) || 0
  const topPosition = trackIndex * 18
  const startDate = new Date(event.startTime); const endDate = new Date(event.endTime)
  const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
  const endDay = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate())
  const currentDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const isStart = currentDay.getTime() === startDay.getTime(); const isEnd = currentDay.getTime() === endDay.getTime()
  const style = { top: topPosition + 'px', position: 'absolute', zIndex: 10 - trackIndex, left: '0', right: '0', backgroundColor: event.color || '#557c55', color: 'white', fontSize: '10px', lineHeight: '16px', padding: '1px 4px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', marginLeft: isStart ? '2px' : '0', marginRight: isEnd ? '2px' : '0' }
  if (isStart && isEnd) { style.borderRadius = '2px'; style.marginLeft = '4px'; style.marginRight = '0px' } else if (isStart) { style.borderRadius = '2px 0 0 2px'; style.marginLeft = '4px' } else if (isEnd) { style.borderRadius = '0 2px 2px 0'; style.marginRight = '0px' }
  return style
}
const getEventTitle = (event) => { return event.description ? event.title + '\n' + event.description : event.title }
const shouldShowTitle = (event, date) => { const s = new Date(event.startTime); return date.getTime() === new Date(s.getFullYear(), s.getMonth(), s.getDate()).getTime() }

renderCalendar()
watch(() => props.currentDate, renderCalendar)
watch(() => props.events, renderCalendar, { deep: true })
</script>

<style scoped>
/* 繼承你原有的 Style + 熱力圖樣式 */
.month-view { flex: 1; display: flex; flex-direction: column; overflow: hidden; padding: 0; height: 100%; background: #fff; }
.weekdays-header { display: grid; grid-template-columns: repeat(7, 1fr); background: #fafbf9; border-bottom: 1px solid #d1d5db; flex-shrink: 0; }
.weekday-cell { text-align: center; padding: 12px 0; font-weight: 500; color: #888; font-size: 11px; letter-spacing: 0.1em; text-transform: uppercase; }
.calendar-grid { display: grid; grid-template-columns: repeat(7, 1fr); grid-auto-rows: 1fr; border-left: 1px solid #e0e0e0; flex: 1; gap: 0; overflow-y: auto; }
.day:nth-child(7n+1) { border-left: 1px solid #f5f5f5; }
.day { border-right: 1px solid #e0e0e0; border-bottom: 1px solid #e0e0e0; padding: 8px 0 4px 0; cursor: pointer; position: relative; transition: background 0.2s; background: white; min-height: 120px; height: auto; box-sizing: border-box; width: 100%; }

/* 🔥 熱力圖樣式：森綠色系 (對應 Clean Code 邏輯) */
.day.forest-level-1 { background-color: rgba(85, 124, 85, 0.03); } /* 極淡 */
.day.forest-level-2 { background-color: rgba(85, 124, 85, 0.08); } /* 淺 */
.day.forest-level-3 { background-color: rgba(85, 124, 85, 0.15); } /* 深 (但不刺眼) */
.day:hover { background: #f5f7f5; }

.day-number { font-size: 12px; color: #444; position: relative; z-index: 1; padding-left: 8px; display: inline-block; line-height: 22px; }
.day.other-month { background: #fafafa; }
.day.other-month .day-number { color: #ccc; }
.day.today .day-number { background: #333; color: white; width: 22px; height: 22px; display: inline-flex; align-items: center; justify-content: center; border-radius: 50%; font-weight: 500; margin-left: 8px; padding-left: 0; text-decoration: underline; }
.events-wrapper { position: relative; width: 100%; min-height: 60px; margin-top: 4px; }
.event-bar { position: absolute; color: white; height: 16px; padding: 2px 6px; font-size: 10px; line-height: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: pointer; transition: opacity 0.2s, transform 0.2s; box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1); box-sizing: border-box; margin: 0; }
.event-bar:hover { opacity: 0.85; transform: translateY(-1px); }
.more-events { position: absolute; bottom: 4px; left: 4px; font-size: 10px; color: #999; cursor: pointer; padding: 2px 4px; background: #f5f5f5; border-radius: 2px; z-index: 20; }
</style>