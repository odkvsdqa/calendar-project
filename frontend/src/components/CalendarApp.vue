<template>
  <div class="container">
    <!--
    <div class="header">
      <h1>📅 我的日曆</h1>
      <p>Vue 3 + Spring Boot 日曆應用</p>
    </div>
  -->

    <div v-if="loading" class="skeleton-calendar">
    <div class="skeleton-header"></div>
    <div class="skeleton-grid">
      <div v-for="i in 35" :key="i" class="skeleton-cell"></div>
    </div> 
    </div>
    <div class="controls">
      <div class="nav-buttons">
        <button @click="previousPeriod">◀ {{ getPreviousLabel }}</button>
        <button @click="nextPeriod">{{ getNextLabel }} ▶</button>
        <button @click="goToToday">今天</button>
      </div>
      <div class="current-period">{{ currentPeriodText }}</div>
      <div class="view-buttons">
        <button 
          @click="viewMode = 'year'" 
          :class="{ active: viewMode === 'year' }"
        >
          年視圖
        </button>
        <button 
          @click="viewMode = 'month'" 
          :class="{ active: viewMode === 'month' }"
        >
          月視圖
        </button>
        <button 
          @click="viewMode = 'day'" 
          :class="{ active: viewMode === 'day' }"
        >
          日視圖
        </button>
      </div>
      <button class="add-event-btn" @click="openEventModal()">+ 新增事件</button>
    </div>

    <div v-if="loading" class="loading">載入中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>

    <YearView
      v-if="viewMode === 'year' && !loading"
      :current-date="currentDate"
      :events="events"
      @go-to-date="goToDate"
    />

    <MonthView
      v-else-if="viewMode === 'month' && !loading"
      :current-date="currentDate"
      :events="events"
      @add-event="openEventModal"
      @edit-event="editEvent"
    />

    <DayView
      v-else-if="viewMode === 'day' && !loading"
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
import { ref, computed, onMounted } from 'vue'
import YearView from './YearView.vue'
import MonthView from './MonthView.vue'
import DayView from './DayView.vue'
import EventModal from './EventModal.vue'
import { eventApi } from '../services/api'
import { handleApiError } from '../utils/errorHandle'


const currentDate = ref(new Date())
const events = ref([])
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
  color: '#667eea'
})

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

const getPreviousLabel = computed(() => {
  if (viewMode.value === 'year') return '上一年'
  if (viewMode.value === 'month') return '上個月'
  return '前一天'
})

const getNextLabel = computed(() => {
  if (viewMode.value === 'year') return '下一年'
  if (viewMode.value === 'month') return '下個月'
  return '下一天'
})

const loadEvents = async () => {
  try {
    loading.value = true
    error.value = null
    console.log('開始載入事件...')
    
    const response = await eventApi.getAllEvents()
    
    // ⚠️ 確保 response.data 是陣列
    if (Array.isArray(response.data)) {
      events.value = response.data
      console.log('成功載入事件:', events.value.length, '個')
    } else {
      console.error('API 返回的不是陣列:', response.data)
      events.value = []
      error.value = 'API 返回格式錯誤'
    }
    events.value = response.data
    console.log('成功載入事件:', events.value.length, '個')
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
    color: '#667eea'
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
    color: '#667eea'
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
    color: event.color || '#667eea'
  }
}

const closeEventModal = () => {
  showModal.value = false
}

const saveEvent = async (eventData) => {
  try {
    loading.value = true
    
    // 轉換日期格式為 ISO 格式
    const eventToSave = {
      ...eventData,
      startTime: new Date(eventData.startTime).toISOString().slice(0, 19),
      endTime: new Date(eventData.endTime).toISOString().slice(0, 19)
    }
    
    if (eventData.id) {
      console.log('更新事件:', eventData.id)
      await eventApi.updateEvent(eventData.id, eventToSave)
    } else {
      console.log('創建新事件')
      await eventApi.createEvent(eventToSave)
    }
    
    await loadEvents()
    closeEventModal()
  } catch (err) {
    console.error('保存事件失敗:', err)
    alert('操作失敗：' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}

const deleteEvent = async (eventId) => {
  if (!confirm('確定要刪除這個事件嗎？')) return
  
  try {
    loading.value = true
    console.log('刪除事件:', eventId)
    await eventApi.deleteEvent(eventId)
    await loadEvents()
    closeEventModal()
  } catch (err) {
    console.error('刪除事件失敗:', err)
    alert('刪除失敗：' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}

const formatDateTimeLocal = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return year + '-' + month + '-' + day + 'T' + hours + ':' + minutes
}

onMounted(async () => {
  await loadEvents()
})
</script>

<style scoped>
.container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  overflow: hidden;
}

/* .header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  text-align: center;
}

.header h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
} */

.controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: #f8f9fa;
  border-bottom: 2px solid #e0e0e0;
  flex-wrap: wrap;
  gap: 15px;
}

.nav-buttons button,
.view-buttons button {
  background: #667eea;
  color: white;
  border: none;
  padding: 10px 20px;
  margin: 0 5px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.nav-buttons button:hover,
.view-buttons button:hover {
  background: #5568d3;
  transform: translateY(-2px);
}

.view-buttons button.active {
  background: #10b981;
}

.current-period {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.add-event-btn {
  background: #10b981;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.add-event-btn:hover {
  background: #059669;
  transform: translateY(-2px);
}

.loading {
  text-align: center;
  padding: 50px;
  font-size: 18px;
  color: #667eea;
}

.error {
  text-align: center;
  padding: 50px;
  font-size: 18px;
  color: #ef4444;
  background: #fee;
  margin: 20px;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .controls {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>