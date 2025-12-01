<!-- src/components/DayView.vue -->
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
          <!-- 從我們計算好的佈局 (dayLayout) 中取出要在這個小時開始顯示的事件 -->
          <div 
            v-for="layoutItem in getEventsForHour(hour - 1)"
            :key="layoutItem.event.id"
            class="day-event"
            :style="getLayoutStyle(layoutItem)"
            @click.stop="emit('edit-event', layoutItem.event)"
          >
            <strong>{{ layoutItem.event.title }}</strong>
            
            <!-- 🔥 修改：強制顯示 完整日期+時間 -->
            <div class="event-time">
              {{ formatFullDateTime(layoutItem.event.startTime, layoutItem.event.endTime) }}
            </div>
            
            <div v-if="layoutItem.durationMinutes > 45 && layoutItem.event.description" class="event-desc">
              {{ layoutItem.event.description }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { parseDate } from '../utils/dateFormatter'

const props = defineProps({
  currentDate: { type: Date, required: true },
  events: { type: Array, default: () => [] }
})

const emit = defineEmits(['add-event-at-time', 'edit-event'])

// 🔥 核心：全域排版引擎 (Layout Engine)
const dayLayout = computed(() => {
  const dayStart = new Date(props.currentDate); dayStart.setHours(0,0,0,0);
  const dayEnd = new Date(props.currentDate); dayEnd.setHours(23,59,59,999);

  const visualEvents = props.events
    .map(e => {
      const s = parseDate(e.startTime)
      const end = parseDate(e.endTime)
      
      if (isNaN(s.getTime()) || isNaN(end.getTime())) return null
      if (end < dayStart || s > dayEnd) return null

      const effectiveStart = s < dayStart ? dayStart : s
      const effectiveEnd = end > dayEnd ? dayEnd : end
      
      return {
        event: e,
        id: String(e.id),
        startMs: effectiveStart.getTime(),
        endMs: effectiveEnd.getTime(),
        startHour: effectiveStart.getHours(),
        isStartToday: s >= dayStart,
        durationMinutes: (effectiveEnd - effectiveStart) / 60000
      }
    })
    .filter(item => item !== null)

  visualEvents.sort((a, b) => {
    if (a.startMs !== b.startMs) return a.startMs - b.startMs
    return b.durationMinutes - a.durationMinutes
  })

  const clusters = []
  let currentCluster = []
  let clusterEnd = -1

  visualEvents.forEach(item => {
    if (currentCluster.length === 0) {
      currentCluster.push(item)
      clusterEnd = item.endMs
    } else {
      if (item.startMs < clusterEnd) {
        currentCluster.push(item)
        if (item.endMs > clusterEnd) clusterEnd = item.endMs
      } else {
        clusters.push(currentCluster)
        currentCluster = [item]
        clusterEnd = item.endMs
      }
    }
  })
  if (currentCluster.length > 0) clusters.push(currentCluster)

  const finalLayout = [] 

  clusters.forEach(cluster => {
    const columns = [] 
    
    cluster.forEach(item => {
      let placed = false
      for (let i = 0; i < columns.length; i++) {
        if (columns[i] <= item.startMs) {
          columns[i] = item.endMs 
          item.colIndex = i
          placed = true
          break
        }
      }
      if (!placed) {
        columns.push(item.endMs)
        item.colIndex = columns.length - 1
      }
    })

    const totalCols = columns.length
    const widthPct = 100 / totalCols

    cluster.forEach(item => {
      item.width = `${widthPct}%`
      item.left = `${item.colIndex * widthPct}%`
      finalLayout.push(item)
    })
  })

  return finalLayout
})

const getEventsForHour = (hour) => {
  return dayLayout.value.filter(item => {
    if (item.isStartToday) {
      return item.startHour === hour
    }
    return hour === 0
  })
}

const getLayoutStyle = (layoutItem) => {
  const d = new Date(layoutItem.startMs)
  let topMinutes = d.getMinutes()
  
  if (!layoutItem.isStartToday) {
    topMinutes = 0
  }

  return {
    position: 'absolute',
    top: `${topMinutes}px`,
    height: `${Math.max(layoutItem.durationMinutes, 25)}px`,
    width: layoutItem.width,
    left: layoutItem.left,
    backgroundColor: (layoutItem.event.color || '#557c55') + 'E6',
    color: '#000000',
    zIndex: 10 + layoutItem.colIndex,
    textShadow: '0 1px 0 rgba(255,255,255,0.3)', 
    border: '1px solid rgba(0,0,0,0.1)',
    borderRadius: '4px',
    boxSizing: 'border-box',
    fontWeight: 'normal'
  }
}

// 🔥 新增：強制顯示完整日期時間格式
const formatFullDateTime = (startIso, endIso) => {
  const s = parseDate(startIso)
  const e = parseDate(endIso)
  
  // 格式：MM/DD HH:mm
  const format = (d) => {
    const month = d.getMonth() + 1
    const date = d.getDate()
    const hh = String(d.getHours()).padStart(2, '0')
    const mm = String(d.getMinutes()).padStart(2, '0')
    return `${month}/${date} ${hh}:${mm}`
  }

  return `${format(s)} ~ ${format(e)}`
}
</script>

<style scoped>
/* 樣式保持原樣 */
.day-view { padding: 20px; height: 100%; display: flex; flex-direction: column; box-sizing: border-box; overflow: hidden; }
.day-view-container { display: grid; grid-template-columns: 60px 1fr; border: 1px solid #d1d5db; border-radius: 4px; background: white; flex: 1; overflow-y: auto; height: 0; }
.time-column { background: #fafbf9; border-right: 1px solid #d1d5db; }
.time-slot { height: 60px; padding: 8px; border-bottom: 1px solid #e0e0e0; font-size: 11px; color: #666; text-align: center; font-family: 'Roboto', sans-serif; letter-spacing: 0.05em; font-weight: 500; box-sizing: border-box; }
.events-column { background: white; position: relative; }
.hour-block { height: 60px; border-bottom: 1px solid #999; position: relative; overflow: visible; }
.hour-block:hover { background: #fcfcfc; }
.day-event { position: absolute; background: #557c55; color: white; padding: 2px 6px; border-radius: 4px; overflow: hidden; cursor: pointer; transition: all 0.1s; box-shadow: 0 1px 3px rgba(0,0,0,0.2); display: flex; flex-direction: column; justify-content: flex-start; }
.day-event:hover { z-index: 50 !important; box-shadow: 0 4px 8px rgba(0,0,0,0.3); filter: brightness(1.05); }
.day-event strong { font-weight: 600; font-size: 12px; line-height: 1.2; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; text-shadow: 0 1px 1px rgba(0,0,0,0.2); }
.event-time { font-size: 10px; opacity: 0.9; margin-bottom: 2px; }
.event-desc { font-size: 11px; margin-top: 2px; opacity: 0.85; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }

/* 
   🔥 RWD 修正：
   原本這裡有 left: 2px !important; right: 2px !important; 
   這會導致手機版強行覆蓋 JS 算出來的寬度，導致事件疊在一起。
   現在移除該設定，手機版就會乖乖聽 JS 的話排版了。
*/
/* src/components/DayView.vue */

/* 🔥 RWD 手機版優化 */
@media (max-width: 768px) {
  .day-view { 
    padding: 0; /* 移除外圍內距，爭取空間 */
  }
  
  .day-view-container { 
    /* 縮小左側時間軸寬度 (60px -> 40px)，讓右邊事件區大一點 */
    grid-template-columns: 40px 1fr; 
    border: none; /* 手機版移除外框，更簡潔 */
  }
  
  /* 時間軸字體縮小 */
  .time-slot { 
    font-size: 10px; 
    padding: 8px 2px; 
    text-align: right;
    padding-right: 5px;
  }
  
  /* 事件區調整 */
  .day-event { 
    /* 移除左右強制邊距，讓它聽 Layout Engine 的話 */
    /* padding: 2px 4px; */
    
    /* 字體調整 */
    padding: 1px 3px;
    border-radius: 3px;
  }
  
  .day-event strong {
    font-size: 10px; /* 標題縮小 */
  }
  
  .event-time {
    font-size: 9px; /* 時間縮小 */
    line-height: 1;
    margin-bottom: 0;
    opacity: 0.8;
  }
  
  .event-desc {
    display: none; /* 手機版空間太小，直接隱藏描述，只顯示標題與時間 */
  }
}
</style>