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
          /* 🔥 新增：如果沒金額但有事件，顯示基礎色 (這行是關鍵) */
           /* 因為 getEventsForDay 已經包含了 27~30 號的範圍判斷，所以這幾天都會亮 */
          'has-event': !dayData.isOtherMonth && getEventsForDay(dayData.date).length > 0
        }"
        @click="emit('add-event', dayData.date)"
      >
        <div class="day-header">
          <span 
            class="day-number clickable-num" 
            @click.stop="emit('change-view', 'day', dayData.date)"
          >
            {{ dayData.date.getDate() }}
          </span>
        </div>
        
        <div class="events-wrapper">
          <div 
            v-for="event in getEventsForDay(dayData.date).slice(0,4)"
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
       <!-- 🔥 3. 修改判斷 length > 4，並更改文字 -->
        <div v-if="getEventsForDay(dayData.date).length > 4" class="more-events" @click.stop="openListModal(dayData.date)">
           其他的 {{ getEventsForDay(dayData.date).length - 4 }} 項預定
       </div>

       <!-- 🔥 新增：當日事件列表彈窗 -->
       <div v-if="showListModal" class="list-modal-overlay" @click.self="closeListModal">
         <div class="list-modal">
           <div class="list-header">
             <h3>{{ listDateTitle }}</h3>
               <button class="btn-close" @click="closeListModal">✕</button>
            </div>
            <div class="list-content">
             <div 
               v-for="event in listEvents" 
               :key="event.id" 
               class="list-item"
               @click="handleListEventClick(event)"
              >
            <div class="item-dot" :style="{ background: event.color || '#557c55' }"></div>
            <div class="item-info">
              <span class="item-time">{{ formatTime(event.startTime) }}</span>
              <span class="item-title">{{ event.title }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
</div>
</template>

<script setup>
import { ref, watch, toRef, computed } from 'vue' 

import { useCostAnalysis } from '../composables/useCostAnalysis'
// 如果你的 utils 路徑不同，請確認這行
import { formatDateTimeLocal } from '../utils/dateFormatter' 

const props = defineProps({
  currentDate: { type: Date, required: true },
  events: { type: Array, default: () => [] }
})
const emit = defineEmits(['add-event', 'edit-event','change-view'])

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

// 🔥 處理列表彈窗邏輯
const showListModal = ref(false)
const listEvents = ref([])
const listDate = ref(null)

const listDateTitle = computed(() => {
  if (!listDate.value) return ''
  return `${listDate.value.getMonth() + 1}月${listDate.value.getDate()}日 的行程`
})

const openListModal = (date) => {
  listDate.value = date
  // 這裡取得當天"所有"事件
  listEvents.value = getEventsForDay(date) // 這裡會自動排序，因為 getEventsForDay 裡有 sort
  showListModal.value = true
}

const closeListModal = () => {
  showListModal.value = false
}

const handleListEventClick = (event) => {
  closeListModal()
  emit('edit-event', event) // 打開編輯視窗
}

const formatTime = (isoString) => {
  const d = new Date(isoString)
  return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const getEventStyle = (event, date) => { /* ... 保留你修好的邏輯 ... */ 
  const trackIndex = eventTracks.value.get(event.id) || 0
  const topPosition = trackIndex * 18
  const startDate = new Date(event.startTime); const endDate = new Date(event.endTime)
  const startDay = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
  const endDay = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate())
  const currentDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const isStart = currentDay.getTime() === startDay.getTime(); 
  const isEnd = currentDay.getTime() === endDay.getTime()
  const style = { top: topPosition + 'px', position: 'absolute', zIndex: 10 - trackIndex, left: '0', right: '0',
   backgroundColor: event.color || '#557c55', color: 'white', fontSize: '10px', lineHeight: '16px', padding: '1px 4px', whiteSpace: 'nowrap', overflow: 'hidden', 
   textOverflow: 'ellipsis', marginLeft: isStart ? '2px' : '0', marginRight: isEnd ? '2px' : '0' }
  if (isStart && isEnd) { style.borderRadius = '2px'; style.marginLeft = '1px'; style.marginRight = '0px' } 
  else if (isStart) { style.borderRadius = '2px 0 0 2px'; style.marginLeft = '1px' }
  else if (isEnd) { style.borderRadius = '0 2px 2px 0'; style.marginRight = '0px' }
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
.clickable-num { cursor: pointer; border-radius: 50%; transition: background 0.2s; padding: 2px; }
.clickable-num:hover { background: #eee; color: #333; }
.day:nth-child(7n+1) { border-left: 1px solid #f5f5f5; }
.day {
  /* 🔥 改為固定高度，不要 auto */
  min-height: 120px;
  height: 120px; /* 🔥 鎖死高度 */
  
  /* 其他保持不變 */
  border-right: 1px solid #e0e0e0;
  border-bottom: 1px solid #e0e0e0;
  padding: 0;
  cursor: pointer; 
  position: relative; 
  transition: background 0.2s; 
  background: white;
  box-sizing: border-box;
}
.day:hover { background: #f5f7f5; }
/* Day Header */
/* Day Header: 強制固定高度，確保每一格的起始點完全一致 */
.day-header {
  height: 28px;      /* 🔥 關鍵：鎖死高度 (比圓圈 22px 大一點即可) */
  min-height: 28px;  /* 雙重保險 */
  
  padding: 0 8px;
  margin-bottom: 2px;
  
  display: flex;
  justify-content: space-between;
  align-items: center; /* 讓數字在 28px 內垂直置中 */
  flex-shrink: 0;
}

/* Day Number: 重置行高，避免文字撐開 */
.day-number { 
  font-size: 12px; 
  color: #444; 
  position: relative; 
  z-index: 1; 
  padding-left: 0; 
  
  /* 🔥 關鍵：移除 display: inline-block 或是重置 line-height */
  /* 建議直接設為 block 或 flex 讓它乖乖待在 header 裡 */
  line-height: 1; 
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 🔥 移除舊的 forest-level-1, 2, 3 */

/* 🔥 新增：統一的事件背景色 */
.day.has-event {
  /* 這邊設定你喜歡的底色，例如極淡的森綠色 */
  background-color: rgba(85, 124, 85, 0.25); 
}

.day.other-month { background: #fafafa; }
.day.other-month .day-number { color: #ccc; }
.day.today .clickable-num:hover { background: #444; }
.day.today .day-number { 
  background: #333; 
  color: white; 
  width: 22px;       /* 22px < 28px，所以不會撐開 header */
  height: 22px; 
  border-radius: 50%; 
  font-weight: 500; 
  text-decoration: underline;
  /* 確保沒有 margin 干擾 */
  margin: 0; 
}

.events-wrapper { 
  position: absolute; /* 🔥 改為絕對定位 */
  top: 28px; /* 🔥 從 day-header 下方開始 */
  left: 0;
  right: 0;
  bottom: 20px; /* 🔥 預留底部給「其他的 N 項」 */
  
  display: flex;
  flex-direction: column;
  gap: 1px;
  padding: 0 2px;
  overflow: hidden; /* 🔥 防止事件溢出 */
  pointer-events: none; /* 🔥 讓點擊穿透到 .day */
}

.event-bar { 
  position: relative; /* 🔥 改回 relative，因為已經在 wrapper 裡了 */
  color: white; 
  height: 16px; /* 🔥 縮小一點 */
  padding: 1px 4px; 
  font-size: 10px; 
  line-height: 14px; 
  white-space: nowrap; 
  overflow: hidden; 
  text-overflow: ellipsis; 
  cursor: pointer; 
  transition: opacity 0.2s, transform 0.2s; 
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1); 
  box-sizing: border-box; 
  margin: 0;
  margin-bottom: 1px;
  flex-shrink: 0; /* 🔥 防止被壓縮 */
  pointer-events: auto; /* 🔥 恢復點擊 */
}
.event-bar:hover { opacity: 0.85; transform: translateY(-1px); }
.more-events { 
  /* 🔥 改為絕對定位，固定在格子底部 */
  position: absolute;
  bottom: 2px;
  left: 2px;
  right: 2px;
  
  /* 固定高度 */
  height: 16px;
  line-height: 16px;
  
  /* 文字設定 */
  text-align: left;
  font-size: 9px; 
  color: #666;
  padding-left: 6px;
  
  /* 防止溢出 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  
  /* 外觀 */
  cursor: pointer; 
  background: rgba(255, 255, 255, 0.95); 
  border-radius: 2px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  z-index: 30;
  box-sizing: border-box;
}

.more-events:hover {
  background: #f0f0f0;
  color: #557c55;
  font-weight: 500;
}
/* 🔥 列表彈窗樣式 */
.list-modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.3); z-index: 2000;
  display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(2px);
}
.list-modal {
  background: white; width: 300px; max-height: 80vh;
  border-radius: 4px; box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  display: flex; flex-direction: column; overflow: hidden;
}
.list-header {
  padding: 15px; border-bottom: 1px solid #eee;
  display: flex; justify-content: space-between; align-items: center;
  background: #fafbf9;
}
.list-header h3 { margin: 0; font-size: 16px; color: #557c55; font-weight: 500; }
.btn-close { background: none; border: none; font-size: 18px; cursor: pointer; color: #999; }
.list-content { padding: 10px; overflow-y: auto; }
.list-item {
  display: flex; align-items: center; gap: 10px; padding: 10px;
  border-radius: 4px; cursor: pointer; transition: background 0.2s;
  border-bottom: 1px solid #f9f9f9;
}
.list-item:hover { background: #f5f7f5; }
.item-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.item-info { display: flex; flex-direction: column; overflow: hidden; }
.item-time { font-size: 11px; color: #999; }
.item-title { font-size: 13px; color: #444; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>