<template>
  <div class="year-view">
    <div class="months-grid">
      <div v-for="monthIndex in 12" :key="monthIndex" class="month-card">
        <h3 class="month-title clickable" @click="emit('change-view', 'month', new Date(currentDate.getFullYear(), monthIndex - 1, 1))">
          {{ monthIndex }}月
        </h3>
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
              'has-events': !dayData.isOtherMonth && hasEvents(dayData.date)   
            }"
            :title="getDailyCost(dayData.date) > 0 ? `$${formatCost(getDailyCost(dayData.date))}` : ''"
            @click.stop="emit('change-view', 'day', dayData.date)"
          >
            <span v-if="!dayData.isOtherMonth">{{ dayData.date.getDate() }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { toRef } from 'vue'
import { useCostAnalysis } from '../composables/useCostAnalysis'
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

// 🔥 2. 新增 change-view 定義
const emit = defineEmits(['go-to-date', 'change-view'])
// 🔥 2. 補上這裡的邏輯 (之前漏掉了)
const eventsRef = toRef(props, 'events')
const { getCostLevel, getDailyCost, formatCost } = useCostAnalysis(eventsRef)
const getMonthDays = (monthIndex) => {
  const year = props.currentDate.getFullYear()
  const firstDay = new Date(year, monthIndex, 1)
  const lastDay = new Date(year, monthIndex + 1, 0)
  const prevLastDay = new Date(year, monthIndex, 0)
  
  const firstDayWeek = firstDay.getDay()
  const lastDayDate = lastDay.getDate()
  // const prevLastDayDate = prevLastDay.getDate() // 這行沒用到可以註解

  const days = []

  // 補上個月
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    days.push({
      date: new Date(year, monthIndex - 1, prevLastDay.getDate() - i), // 修正變數引用
      isOtherMonth: true,
      isToday: false
    })
  }

  // 當月
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

  // 補下個月
  while (days.length % 7 !== 0) {
    const nextDate = days.length - (firstDayWeek + lastDayDate) + 1
    days.push({
      date: new Date(year, monthIndex + 1, nextDate), // 簡化邏輯
      isOtherMonth: true,
      isToday: false
    })
  }

  return days
}

// 判斷當天是否有事件 (支援跨日事件)
const hasEvents = (date) => {
  // 取得當前格子的時間戳記 (標準化為當日 00:00:00)
  const cellTime = new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime()

  return props.events.some(event => {
    const start = new Date(event.startTime)
    const end = new Date(event.endTime)

    // 將事件的開始與結束時間也標準化為 00:00:00，避免因為幾點幾分而判斷錯誤
    const sTime = new Date(start.getFullYear(), start.getMonth(), start.getDate()).getTime()
    const eTime = new Date(end.getFullYear(), end.getMonth(), end.getDate()).getTime()

    // 🔥 關鍵修正：只要格子時間在 [開始, 結束] 區間內，就算有事件
    return cellTime >= sTime && cellTime <= eTime
  })
}
</script>

<style scoped>
.year-view {
  padding: 30px;
  /* 👇 新增：給年視圖一個最小高度 */
  height: 100%; /* 繼承高度 */
  display: flex;
  flex-direction: column;
  overflow-y: auto; /* 年視圖通常比較長，允許捲動 */
}

.months-grid {
  display: grid;
  /* 自適應欄位 */
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  padding-bottom: 20px; 
}

.month-card {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px; /* 稍微加大內距 */
  transition: all 0.3s;

  /* 👇 新增：讓卡片內容均勻分佈 */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.month-card:hover {
  border-color: #557c55;
  box-shadow: 0 4px 12px rgba(85, 124, 85, 0.2);
}

.month-title {
  text-align: center;
  color: #557c55;
  margin-bottom: 10px;
  font-size: 18px;
}

.mini-calendar {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;

   /* 🔥 關鍵修正：利用 Gap 製作格線 */
  gap: 1px;               /* 縫隙寬度 = 格線粗細 */
  background-color: #d1d5db; /* 縫隙顏色 = 格線顏色 (冷灰) */
  border: 1px solid #d1d5db; /* 最外圈的框線 */
  
  border-radius: 4px; /* 或是 0，看你喜好 */
  overflow: hidden;   /* 確保圓角內內容不溢出 */
}

.mini-day-header {
  text-align: center;
  font-size: 10px;
  font-weight: bold;
  color: #557c55;
  padding: 5px 2px;
}

.mini-day {
/* 🔥 關鍵：格子設為白色，蓋住背景，只露出縫隙 */
  background-color: white;
  text-align: center;
 padding: 4px  2px;  /* 調整內距 */
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s;
  /* 確保高度一致 */
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mini-day:hover {
  background: #f5f7f5;
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
  background: #557c55;
  color: white;
}

.clickable { cursor: pointer; transition: color 0.2s; }
.clickable:hover { color: #446344; text-decoration: underline; }

/* ⚠️ 新增 RWD - 平板 (768px 以下) */
@media (max-width: 768px) {
  .year-view {
    padding: 15px;
  }
  
  .months-grid {
    /* 手機版強制一欄，避免擠壓 */
    grid-template-columns: 1fr; 
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