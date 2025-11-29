<template>
  <div class="day-view">
    <div class="day-view-container">
      <!-- 左側時間軸 -->
      <div class="time-column">
        <div v-for="hour in 24" :key="hour" class="time-slot">
          {{ String(hour - 1).padStart(2, '0') }}:00
        </div>
      </div>

      <!-- 右側事件區 -->
      <div class="events-column">
        <div 
          v-for="hour in 24" 
          :key="hour" 
          class="hour-block" 
          @click="emit('add-event-at-time', hour - 1)"
        >
          <!-- 篩選「視覺上」應該從這個小時開始顯示的事件 -->
          <div 
            v-for="event in getEventsInitiatingAt(hour - 1)"
            :key="event.id"
            class="day-event"
            :style="getDayEventStyle(event, hour - 1)"
            @click.stop="emit('edit-event', event)"
          >
            <strong>{{ event.title }}</strong>
            <div class="event-time">
              {{ formatTimeRange(event.startTime, event.endTime) }}
            </div>
            <!-- 如果高度夠高才顯示描述，避免擠在一起 -->
            <div v-if="getEventDuration(event) > 45 && event.description" class="event-desc">
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
  currentDate: { type: Date, required: true },
  events: { type: Array, default: () => [] }
})

const emit = defineEmits(['add-event-at-time', 'edit-event'])

// 1. 篩選出「應該在這個小時格子開始渲染」的事件
const getEventsInitiatingAt = (hour) => {
  const currentStart = new Date(props.currentDate)
  currentStart.setHours(0, 0, 0, 0)
  const currentEnd = new Date(props.currentDate)
  currentEnd.setHours(23, 59, 59, 999)

  return props.events.filter(event => {
    const eStart = new Date(event.startTime)
    const eEnd = new Date(event.endTime)

    // 排除完全不在今天的事件
    if (eEnd < currentStart || eStart > currentEnd) return false

    // 判斷邏輯：
    // A. 如果事件是「今天」開始的，那它應該出現在它的 startHour 格子
    if (eStart >= currentStart) {
      return eStart.getHours() === hour
    }
    
    // B. 如果事件是「昨天以前」開始的 (跨日)，那它今天應該從 00:00 (hour 0) 開始顯示
    return hour === 0
  })
}

// 2. 計算樣式 (Top, Height, Color)
const getDayEventStyle = (event, hour) => {
  const eStart = new Date(event.startTime)
  const eEnd = new Date(event.endTime)
  
  const dayStart = new Date(props.currentDate)
  dayStart.setHours(0, 0, 0, 0)
  const dayEnd = new Date(props.currentDate)
  dayEnd.setHours(24, 0, 0, 0)

  const visualStart = eStart < dayStart ? dayStart : eStart
  const visualEnd = eEnd > dayEnd ? dayEnd : eEnd

  const durationMinutes = (visualEnd - visualStart) / (1000 * 60)
  const topMinutes = visualStart.getMinutes()
  const pixelPerMinute = 1 

  return {
    top: `${topMinutes * pixelPerMinute}px`,
    height: `${Math.max(durationMinutes * pixelPerMinute, 25)}px`,
    
    // 🔥 修正 1：改回純色，不要在這裡加亂碼
    backgroundColor: event.color || '#557c55',
    
    // 🔥 修正 2：直接使用 opacity 屬性，這絕對會透明 (0.6 = 60% 不透明度)
    opacity: 0.25,
    
    color: 'white',
    position: 'absolute',
    left: '4px',
    right: '10px',
    zIndex: 10,
    
    // 加深一點邊框，因為變透明了，需要邊框來維持形狀感
    border: '1px solid rgba(0,0,0,0.1)',
    borderRadius: '4px',
    
    // 確保文字不會因為透明而太難閱讀，加個文字陰影
    textShadow: '0 1px 2px rgba(0,0,0,0.5)'
  }
}

// 3. 輔助：計算時長分鐘數 (用於判斷要不要顯示描述)
const getEventDuration = (event) => {
  const s = new Date(event.startTime); const e = new Date(event.endTime)
  return (e - s) / 60000
}

// 4. 格式化時間文字
const formatTimeRange = (startIso, endIso) => {
  const s = new Date(startIso)
  const e = new Date(endIso)
  const format = (d) => `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
  return `${format(s)} - ${format(e)}`
}
</script>

<style scoped>
.day-view {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden;
}

.day-view-container {
  display: grid;
  grid-template-columns: 60px 1fr;
  border: 1px solid #d1d5db; /* 加深邊框 */
  border-radius: 4px;
  background: white;
  flex: 1;
  overflow-y: auto; 
  /* height: 0 是 Flex trick，讓它正確捲動 */
  height: 0; 
}

.time-column {
  background: #fafbf9;
  border-right: 1px solid #d1d5db;
}

.time-slot {
  height: 60px; /* 每格 60px */
  padding: 8px;
  border-bottom: 1px solid #e0e0e0;
  font-size: 11px;
  color: #666;
  text-align: center;
  font-family: 'Roboto', sans-serif;
  letter-spacing: 0.05em;
  font-weight: 500;
  box-sizing: border-box;
}

.events-column {
  background: white;
  position: relative;
}

.hour-block {
  height: 60px; /* 必須跟 time-slot 一樣高 */
  border-bottom: 1px solid #999;
  position: relative; /* 讓絕對定位的事件參考這裡 */
  cursor: pointer;
  transition: background 0.2s;
  
  /* 🔥 關鍵：不能設 overflow: hidden，這樣長事件才能跨越格子顯示！ */
  overflow: visible; 
}

.hour-block:hover {
  background: #fcfcfc;
}

.day-event {
  position: absolute;
  /* 預設樣式，style 會覆蓋 */
  background: #557c55;
  color: white;
  padding: 4px 8px;
  border-radius: 3px;
  font-size: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 5px rgba(0,0,0,0.15);
  border-left: 3px solid rgba(0,0,0,0.2);
  
  /* Flex 讓文字垂直置中 (如果事件很短) */
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.day-event:hover {
  filter: brightness(0.95);
  transform: translateX(1px);
  z-index: 20 !important; /* Hover 時浮到最上層 */
}

.day-event strong {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 12px;
  line-height: 1.4;
}

.event-time {
  font-size: 10px;
  opacity: 0.9;
}

.event-desc {
  font-size: 11px;
  margin-top: 2px;
  opacity: 0.85;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多顯示兩行 */
  -webkit-box-orient: vertical;
}

/* RWD */
@media (max-width: 768px) {
  .day-view { padding: 10px; }
  .day-view-container { grid-template-columns: 50px 1fr; }
  .time-slot { font-size: 10px; padding: 8px 2px; }
  .day-event { padding: 2px 4px; left: 2px !important; right: 2px !important; }
}
</style>