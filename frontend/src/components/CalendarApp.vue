<template>
  <div class="container">
    <!-- 控制列 -->
    <div class="controls">
      <!-- 左側: 導航按鈕 -->
      <div class="btn-group">
        <button @click="previousPeriod">◀</button>
        <button @click="goToToday">今天</button>
        <button @click="nextPeriod">▶</button>
      </div>
      
      <!-- 中間: 日期標題 -->
      <div class="date-title">{{ currentPeriodText }}</div>

      <!-- 右側: 視圖切換 + 新增按鈕 -->
      <div class="right-group">
        <div class="btn-group">
          <button 
            @click="viewMode = 'year'" 
            :class="{ active: viewMode === 'year' }"
          >
            年
          </button>
          <button 
            @click="viewMode = 'month'" 
            :class="{ active: viewMode === 'month' }"
          >
            月
          </button>
          <button 
            @click="viewMode = 'day'" 
            :class="{ active: viewMode === 'day' }"
          >
            日
          </button>
        </div>
        <button class="btn-primary" @click="openEventModal()">+ 新增</button>
      </div>
    </div>

    <!-- 後續載入時的 Loading 遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <YearView
      v-if="viewMode === 'year'"
      :current-date="currentDate"
      :events="events"
      @go-to-date="goToDate"
    />

    <MonthView
      v-else-if="viewMode === 'month'"
      :current-date="currentDate"
      :events="events"
      @add-event="openEventModal"
      @edit-event="editEvent"
    />

    <DayView
      v-else-if="viewMode === 'day'"
      :current-date="currentDate"
      :events="events"
      @add-event-at-time="openEventModalAtTime"
      @edit-event="editEvent"
    />

    <EventModal
      v-if="showModal"
      :event-form="eventForm"
      :modal-title="modalTitle"
      @close="closeEventModal"
      @save="saveEvent"
      @delete="deleteEvent"
    />
  </div>
</template>

<script setup>
import { formatDateTimeLocal, formatToIsoString } from '../utils/dateFormatter'
import { ref, computed } from 'vue'
import YearView from './YearView.vue'
import MonthView from './MonthView.vue'
import DayView from './DayView.vue'
import EventModal from './EventModal.vue'
import { eventApi } from '../services/api'
import { handleApiError } from '../utils/errorHandle'
import { useToast } from '../composables/useToast'

const { showToast } = useToast()
const currentDate = ref(new Date())
const viewMode = ref('month')
const showModal = ref(false)
const loading = ref(false)
const error = ref(null)
const eventForm = ref({
  id: null,
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  color: '#7c8db5'
})

// 🔥 關鍵改動：使用 await 來初始化資料
const events = ref([])
try {
  const response = await eventApi.getAllEvents()
  events.value = Array.isArray(response.data) ? response.data : []
} catch (err) {
  error.value = handleApiError(err, '載入事件失敗')
  events.value = []
}

const currentPeriodText = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth() + 1
  const day = currentDate.value.getDate()
  
  if (viewMode.value === 'year') {
    return year + '年'
  } else if (viewMode.value === 'month') {
    return year + '年 ' + month + '月'
  } else {
    return year + '年 ' + month + '月 ' + day + '日'
  }
})

const modalTitle = computed(() => {
  return eventForm.value.id ? '編輯事件' : '新增事件'
})

// 後續的重新載入函數（用於 CRUD 操作後）
const loadEvents = async () => {
  try {
    loading.value = true
    error.value = null
    const response = await eventApi.getAllEvents()
    
    if (Array.isArray(response.data)) {
      events.value = response.data
    } else {
      events.value = []
      error.value = 'API 返回格式錯誤'
    }
  } catch (err) {
    error.value = handleApiError(err, '載入事件失敗')
    events.value = []
  } finally {
    loading.value = false
  }
}

const previousPeriod = () => {
  if (viewMode.value === 'year') {
    currentDate.value = new Date(currentDate.value.getFullYear() - 1, 0, 1)
  } else if (viewMode.value === 'month') {
    currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() - 1, 1)
  } else {
    currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth(), currentDate.value.getDate() - 1)
  }
}

const nextPeriod = () => {
  if (viewMode.value === 'year') {
    currentDate.value = new Date(currentDate.value.getFullYear() + 1, 0, 1)
  } else if (viewMode.value === 'month') {
    currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() + 1, 1)
  } else {
    currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth(), currentDate.value.getDate() + 1)
  }
}

const goToToday = () => {
  currentDate.value = new Date()
}

const goToDate = (date) => {
  currentDate.value = new Date(date)
  viewMode.value = 'day'
}

const openEventModal = (date) => {
  showModal.value = true
  eventForm.value = {
    id: null,
    title: '',
    description: '',
    startTime: '',
    endTime: '',
    color: '#7c8db5',
    estimatedCost: null // 🔥 補上這行：讓表單知道有這個欄位
  }
  
  if (date) {
    const startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 9, 0)
    const endDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 10, 0)
    eventForm.value.startTime = formatDateTimeLocal(startDate)
    eventForm.value.endTime = formatDateTimeLocal(endDate)
  }
}

const openEventModalAtTime = (hour) => {
  const date = currentDate.value
  const startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), hour, 0)
  const endDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), hour + 1, 0)
  
  showModal.value = true
  eventForm.value = {
    id: null,
    title: '',
    description: '',
    startTime: formatDateTimeLocal(startDate),
    endTime: formatDateTimeLocal(endDate),
    color: '#7c8db5',
    estimatedCost: null // 🔥 補上這行：讓表單知道有這個欄位
  }
}

const editEvent = (event) => {
  showModal.value = true
  eventForm.value = {
    id: event.id,
    title: event.title,
    description: event.description || '',
    startTime: formatDateTimeLocal(new Date(event.startTime)),
    endTime: formatDateTimeLocal(new Date(event.endTime)),
    color: event.color || '#7c8db5',
    estimatedCost: event.estimatedCost || null 
  }
}

const closeEventModal = () => {
  showModal.value = false
}

const saveEvent = async (eventData) => {
  try {
    loading.value = true
    
    const eventToSave = {
      ...eventData,
      startTime: formatToIsoString(eventData.startTime),
      endTime: formatToIsoString(eventData.endTime)
    }
    
    if (eventData.id) {
      await eventApi.updateEvent(eventData.id, eventToSave)
    } else {
      await eventApi.createEvent(eventToSave)
    }
    
    await loadEvents()
    closeEventModal()
    showToast('儲存成功', 'success')
  } catch (err) {
    showToast('操作失敗：' + (err.response?.data?.message || err.message), 'error')
  } finally {
    loading.value = false
  }
}

const deleteEvent = async (eventId) => {
  try {
    loading.value = true
    await eventApi.deleteEvent(eventId)
    await loadEvents()
    closeEventModal()
    showToast('刪除成功', 'success')
  } catch (err) {
    showToast('刪除失敗：' + (err.response?.data?.message || err.message), 'error')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 日系極簡風格 */
.container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-radius: 0;
  overflow: hidden;
  position: relative;
}

/* 控制列 - 極簡線框風格 */
.controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 40px;
  background: white;
  border-bottom: 1px solid #f5f5f5;
  flex-shrink: 0;
  flex-wrap: wrap;
  gap: 15px;
}

/* 按鈕組 */
.btn-group {
  display: flex;
  gap: 0;
}

.btn-group button {
  background: white;
  border: 1px solid #e0e0e0;
  color: #666;
  padding: 6px 16px;
  font-size: 12px;
  cursor: pointer;
  border-radius: 0;
  margin-left: -1px;
  transition: all 0.2s;
  font-weight: 400;
  letter-spacing: 0.05em;
}

.btn-group button:first-child {
  border-top-left-radius: 2px;
  border-bottom-left-radius: 2px;
  margin-left: 0;
}

.btn-group button:last-child {
  border-top-right-radius: 2px;
  border-bottom-right-radius: 2px;
}

.btn-group button:hover {
  background: #fcfcfc;
  color: #333;
  border-color: #ccc;
  z-index: 1;
  position: relative;
}

.btn-group button.active {
  background: #333;
  color: white;
  border-color: #333;
  font-weight: 500;
}

/* 日期標題 */
.date-title {
  font-size: 20px;
  font-weight: 400;
  letter-spacing: 0.1em;
  color: #222;
}

/* 右側組合 */
.right-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 新增按鈕 */
.btn-primary {
  background: #557c55;
  color: white;
  border: none;
  padding: 8px 20px;
  font-size: 13px;
  border-radius: 2px;
  letter-spacing: 0.05em;
  cursor: pointer;
  box-shadow: 0 2px 5px rgba(85, 124, 85, 0.25); 
  transition: all 0.2s;
  font-weight: 400;
}

.btn-primary:hover {
  /* 👇 修改：Hover 變得更深一點 */
  background: #446344; 
  opacity: 1; /* 原本是 opacity 0.9，改顏色比較質感 */
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 50;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #557c55;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error {
  padding: 10px;
  background: #fee;
  color: #c00;
  text-align: center;
}

/* RWD */
@media (max-width: 768px) {
  .controls {
    padding: 8px 10px;
    gap: 8px;
    flex-direction: column;
    align-items: stretch;
  }
  
  .controls > :first-child,
  .date-title {
    display: flex;
    justify-content: center;
  }
  
  .right-group {
    justify-content: space-between;
    width: 100%;
  }
  
  .date-title {
    font-size: 16px;
    order: -1;
  }
}
</style>