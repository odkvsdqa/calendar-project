<template>
  <!-- 1. 在最外層容器加上 @wheel 監聽滾輪 -->
  <div class="container" @wheel="handleWheel">
    <!-- 控制列 -->
    <div class="controls">
      <!-- 左側: 導航按鈕 -->
      <div class="left-group">
        <button class="btn-today" @click="goToToday">{{ $t('calendar.view.today') }}</button>
      </div>

      <!-- 3. 中間：左右箭頭包夾標題 -->
      <div class="center-group">
        <button class="nav-arrow" @click="previousPeriod">◀</button>
        <div class="date-title">{{ currentPeriodText }}</div>
        <button class="nav-arrow" @click="nextPeriod">▶</button>
      </div>

      <!-- 右側: 視圖切換 + 新增按鈕 -->
      <div class="right-group">
        <div class="btn-group">
          <button @click="viewMode = 'year'" :class="{ active: viewMode === 'year' }">{{ $t('calendar.view.year') }}</button>
          <button @click="viewMode = 'month'" :class="{ active: viewMode === 'month' }">{{ $t('calendar.view.month') }}</button>
          <button @click="viewMode = 'day'" :class="{ active: viewMode === 'day' }">{{ $t('calendar.view.day') }}</button>
        </div>
        <button class="btn-primary" @click="openEventModal()">+ {{ $t('calendar.nav.add') }}</button>
      </div>
    </div>

    <!-- 後續載入時的 Loading 遮罩 -->
    <!-- ✅ 替換為共用組件 -->
    <LoadingOverlay :visible="loading" />

    <div v-if="error" class="error">{{ error }}</div>

    <YearView
      v-if="viewMode === 'year'"
      :current-date="currentDate"
      :events="events"
      @go-to-date="goToDate"
      @change-view="changeViewMode"
    />

    <MonthView
      v-else-if="viewMode === 'month'"
      :current-date="currentDate"
      :events="events"
      @add-event="openEventModal"
      @edit-event="editEvent"
      @change-view="changeViewMode"
    />

    <DayView
      v-else-if="viewMode === 'day'"
      :current-date="currentDate"
      :events="events"
      @add-event-at-time="openEventModalAtTime"
      @edit-event="editEvent"
      @change-view="changeViewMode"
    />

    <EventModal
      v-if="showModal"
      :event-form="eventForm"
      @close="closeEventModal"
      @save="saveEvent"
      @delete="deleteEvent"
    />
  </div>
</template>

<script setup>
import { formatDateTimeLocal, formatToIsoString } from "../utils/dateFormatter";
import { ref, computed, watch } from "vue";
import { useI18n } from 'vue-i18n'; // 🔥 引入
import YearView from "./YearView.vue";
import MonthView from "./MonthView.vue";
import DayView from "./DayView.vue";
import EventModal from "./EventModal.vue";
import { eventApi } from "../services/api";
import { handleApiError } from "../utils/errorHandle";
import { useToast } from "../composables/useToast";
import { useCalendarNavigation } from "../composables/useCalendarNavigation"; // 🔥 引入
import LoadingOverlay from "./common/LoadingOverlay.vue";

const { t, locale } = useI18n() // 🔥 補上 locale
const { showToast } = useToast();
const currentDate = ref(new Date());

// 1. 初始化時，嘗試從 localStorage 讀取
const savedViewMode = localStorage.getItem('calendarViewMode')
const viewMode = ref(savedViewMode || 'month')

const showModal = ref(false);
const loading = ref(false);
const error = ref(null);
const eventForm = ref({
  id: null,
  title: "",
  description: "",
  startTime: "",
  endTime: "",
  color: "#7c8db5",
  estimatedCost: null
});

const { previousPeriod, nextPeriod, goToToday, handleWheel } =
  useCalendarNavigation(currentDate, viewMode);

// 🔥 新增：處理子組件請求切換視圖 (給 Task 3 用)
const changeViewMode = (mode, date) => {
  if (date) currentDate.value = new Date(date);
  viewMode.value = mode;
};

// 🔥 監聽 viewMode 變化，存入 localStorage
watch(viewMode, (newMode) => {
  localStorage.setItem('calendarViewMode', newMode)
})

// 🔥 關鍵改動：使用 await 來初始化資料
const events = ref([]);
// 由於 setup 中不能直接用 top-level await (除非配置 suspense)，這裡改回傳統 async 函數呼叫
const initEvents = async () => {
  try {
    const response = await eventApi.getAllEvents();
    events.value = Array.isArray(response.data) ? response.data : [];
  } catch (err) {
    // error.value = handleApiError(err, t('errors.loadEventsFailed'));
    // 初始化失敗暫時不阻擋渲染，僅 log
    console.error(err)
    events.value = [];
  }
}
initEvents()

// 🔥 修改：根據當前語言格式化標題
const currentPeriodText = computed(() => {
  const date = currentDate.value
  
  if (viewMode.value === 'year') {
    return date.getFullYear() + (locale.value === 'en-US' ? '' : t('calendar.view.year'))
  } else if (viewMode.value === 'month') {
    return new Intl.DateTimeFormat(locale.value, { year: 'numeric', month: 'long' }).format(date)
  } else {
    // 日視圖：顯示完整日期
    return new Intl.DateTimeFormat(locale.value, { year: 'numeric', month: 'long', day: 'numeric' }).format(date)
  }
});

// 後續的重新載入函數（用於 CRUD 操作後）
const loadEvents = async () => {
  try {
    loading.value = true;
    error.value = null;
    const response = await eventApi.getAllEvents();

    if (Array.isArray(response.data)) {
      events.value = response.data;
    } else {
      events.value = [];
      error.value = t('errors.apiFormat');
    }
  } catch (err) {
    error.value = handleApiError(err, t('errors.loadEventsFailed'));
    events.value = [];
  } finally {
    loading.value = false;
  }
};

const goToDate = (date) => {
  currentDate.value = new Date(date);
  viewMode.value = "day";
};

const openEventModal = (date) => {
  showModal.value = true;
  eventForm.value = {
    id: null,
    title: "",
    description: "",
    startTime: "",
    endTime: "",
    color: "#7c8db5",
    estimatedCost: null, 
  };

  if (date) {
    const startDate = new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      9,
      0
    );
    const endDate = new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      10,
      0
    );
    eventForm.value.startTime = formatDateTimeLocal(startDate);
    eventForm.value.endTime = formatDateTimeLocal(endDate);
  }
};

const openEventModalAtTime = (hour) => {
  const date = currentDate.value;
  const startDate = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate(),
    hour,
    0
  );
  const endDate = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate(),
    hour + 1,
    0
  );

  showModal.value = true;
  eventForm.value = {
    id: null,
    title: "",
    description: "",
    startTime: formatDateTimeLocal(startDate),
    endTime: formatDateTimeLocal(endDate),
    color: "#7c8db5",
    estimatedCost: null, 
  };
};

const editEvent = (event) => {
  showModal.value = true;
  eventForm.value = {
    id: event.id,
    title: event.title,
    description: event.description || "",
    startTime: formatDateTimeLocal(new Date(event.startTime)),
    endTime: formatDateTimeLocal(new Date(event.endTime)),
    color: event.color || "#7c8db5",
    estimatedCost: event.estimatedCost || null,
  };
};

const closeEventModal = () => {
  showModal.value = false;
};

const saveEvent = async (eventData) => {
  try {
    loading.value = true;

    const eventToSave = {
      ...eventData,
      startTime: formatToIsoString(eventData.startTime),
      endTime: formatToIsoString(eventData.endTime),
    };

    if (eventData.id) {
      await eventApi.updateEvent(eventData.id, eventToSave);
    } else {
      await eventApi.createEvent(eventToSave);
    }

    await loadEvents();
    closeEventModal();
    showToast(t('messages.saveSuccess'), "success");
  } catch (err) {
    const msg = err.response?.data?.message || err.message
    showToast(t('errors.operationFailed') + msg, 'error');
  } finally {
    loading.value = false;
  }
};

const deleteEvent = async (eventId) => {
  try {
    loading.value = true;
    await eventApi.deleteEvent(eventId);
    await loadEvents();
    closeEventModal();
    showToast(t('messages.deleteSuccess'), "success");
  } catch (err) {
    const msg = err.response?.data?.message || err.message
    showToast(t('errors.deleteFailed') + msg, "error");
  } finally {
    loading.value = false;
  }
};
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

/* 最外層控制列 Grid */
.controls {
  display: grid;
  /* 🔥 修改 1：中間改用 max-content，強迫格子寬度只包住內容，不准拉伸 */
  grid-template-columns: 1fr max-content 1fr; 
  justify-content: space-between;
  align-items: center;
  padding: 15px 40px;
  background: white;
  border-bottom: 1px solid #f5f5f5;
  flex-shrink: 0;
  gap: 15px;
}

/* 左側 */
.left-group {
  justify-self: start;
}

/* 中間區塊：包含 左箭頭 + 日期 + 右箭頭 */
.center-group { 
  /* 🔥 修改 2：改用 inline-flex，這會讓盒子像文字一樣緊湊 */
  display: inline-flex; 
  align-items: center; 
  justify-content: center; 
  
  /* 🔥 修改 3：設定固定間距，不管螢幕多大，它們距離永遠是 10px */
  gap: 10px; 
  
  /* 確保寬度自動 */
  width: auto;
  min-width: 0;
}

/* 日期文字 */
.date-title {
  font-size: 20px;
  font-weight: 400;
  letter-spacing: 0.05em;
  color: #222;
  
  /* 🔥 修改 4：【核彈級設定】 flex: none */
  /* 這告訴瀏覽器：「這個文字不准放大、不准縮小、寬度自動」 */
  /* 這樣它就絕對不會去推擠旁邊的箭頭 */
  flex: none; 
  
  text-align: center;
  white-space: nowrap;
  line-height: 1;
  margin: 0;
  padding: 0;
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

/* 右側組合 */
.right-group {
  display: flex;
  justify-self: end;
  gap: 10px;
}

.btn-today {
  background: white;
  border: 1px solid #e0e0e0;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  font-size: 13px;

  /* 🔥 新增以下兩行：強制不換行，且不允許被壓縮 */
  white-space: nowrap;
  flex-shrink: 0;
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

/* 左右箭頭 */
.nav-arrow {
  background: transparent; 
  border: none; 
  font-size: 14px; 
  color: #999; 
  cursor: pointer; 
  
  /* 🔥 修正 5：減少內距，讓它靠得更近 */
  padding: 4px;
  margin: 0; /* 清除所有外距 */
  
  /* 確保按鈕不會變形 */
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  width: 24px;
}

.nav-arrow:hover { 
  color: #557c55; 
  transform: scale(1.1); 
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
    /* 手機版改回 Flex，方便換行 */
    display: flex;
  }

  .right-group {
    justify-content: space-between;
    width: 100%;
  }

  .date-title {
    font-size: 16px;
  }

  /* 手機版讓中間區塊置中 */
  .center-group {
    order: -1; /* 讓日期跑到最上面 */
    width: 100%; 
    justify-content: center;
    margin-bottom: 5px;
    gap: 15px; /* 手機版可以稍微寬一點 */
  }
}
</style>