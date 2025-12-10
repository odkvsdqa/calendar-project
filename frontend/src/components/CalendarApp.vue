<template>
  <!-- 1. 在最外層容器加上 @wheel 監聽滾輪 -->
  <div class="container" @wheel="handleWheel">
    <!-- 控制列 -->
    <div class="controls">
      <!-- 左側: 導航按鈕 -->
      <div class="left-group">
        <button class="btn-today" @click="goToToday">
          {{ $t("calendar.view.today") }}
        </button>
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
          <button
            @click="viewMode = 'year'"
            :class="{ active: viewMode === 'year' }"
          >
            {{ $t("calendar.view.year") }}
          </button>
          <button
            @click="viewMode = 'month'"
            :class="{ active: viewMode === 'month' }"
          >
            {{ $t("calendar.view.month") }}
          </button>
          <button
            @click="viewMode = 'day'"
            :class="{ active: viewMode === 'day' }"
          >
            {{ $t("calendar.view.day") }}
          </button>
        </div>
        <button class="btn-primary" @click="openEventModal()">
          + {{ $t("calendar.nav.add") }}
        </button>
      </div>
    </div>

    <!-- 後續載入時的 Loading 遮罩 -->
    <LoadingOverlay :visible="loading" />

    <div v-if="error" class="error">{{ error }}</div>

    <!-- 🔥 關鍵修改：傳入 :events="displayEvents" (合併後的資料) -->

    <YearView
      v-if="viewMode === 'year'"
      :current-date="currentDate"
      :events="displayEvents"
      @go-to-date="goToDate"
      @change-view="changeViewMode"
    />

    <MonthView
      v-else-if="viewMode === 'month'"
      :current-date="currentDate"
      :events="displayEvents"
      @add-event="openEventModal"
      @edit-event="editEvent"
      @change-view="changeViewMode"
    />

    <DayView
      v-else-if="viewMode === 'day'"
      :current-date="currentDate"
      :events="displayEvents"
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
import { useI18n } from "vue-i18n";
import YearView from "./YearView.vue";
import MonthView from "./MonthView.vue";
import DayView from "./DayView.vue";
import EventModal from "./EventModal.vue";
import { eventApi } from "../services/api";
import { handleApiError } from "../utils/errorHandle";
import { useToast } from "../composables/useToast";
import { useCalendarNavigation } from "../composables/useCalendarNavigation";
import LoadingOverlay from "./common/LoadingOverlay.vue";
import { useVenues } from "../composables/useVenues"; // 🔥 新增引用

const { t, locale } = useI18n();
const { showToast } = useToast();
const currentDate = ref(new Date());

// 1. 初始化時，嘗試從 localStorage 讀取
const savedViewMode = localStorage.getItem("calendarViewMode");
const viewMode = ref(savedViewMode || "month");

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
  estimatedCost: null,
  currency: "TWD", // 🔥 加上預設值
});

const { previousPeriod, nextPeriod, goToToday, handleWheel } =
  useCalendarNavigation(currentDate, viewMode);

// 🔥 新增：訂閱場館邏輯
const { allExternalEvents, restoreSubscriptions } = useVenues(); // 取得外部事件

// 處理子組件請求切換視圖
const changeViewMode = (mode, date) => {
  if (date) currentDate.value = new Date(date);
  viewMode.value = mode;
};

// 監聽 viewMode 變化，存入 localStorage
watch(viewMode, (newMode) => {
  localStorage.setItem("calendarViewMode", newMode);
});

const events = ref([]); // 這只是使用者的私人事件

// 🔥 核心邏輯：合併「私人事件」與「外部訂閱事件」
const displayEvents = computed(() => {
  // 展開合併兩個陣列
  // 如果 events 是 null/undefined 則給空陣列
  const userEvents = Array.isArray(events.value) ? events.value : [];
  const external = Array.isArray(allExternalEvents.value)
    ? allExternalEvents.value
    : [];

  return [...userEvents, ...external];
});

const initEvents = async () => {
  try {
    const response = await eventApi.getAllEvents();
    events.value = Array.isArray(response.data) ? response.data : [];
  } catch (err) {
    console.error(err);
    events.value = [];
  }
};
initEvents();
import { onMounted } from "vue";
onMounted(async () => {
  // 1. 初始化使用者自己的事件
  await initEvents();

  // 2. 🔥 自動還原外部場館訂閱
  await restoreSubscriptions();
});
// 根據當前語言格式化標題
const currentPeriodText = computed(() => {
  const date = currentDate.value;

  if (viewMode.value === "year") {
    return (
      date.getFullYear() +
      (locale.value === "en-US" ? "" : t("calendar.view.year"))
    );
  } else if (viewMode.value === "month") {
    return new Intl.DateTimeFormat(locale.value, {
      year: "numeric",
      month: "long",
    }).format(date);
  } else {
    // 日視圖：顯示完整日期
    return new Intl.DateTimeFormat(locale.value, {
      year: "numeric",
      month: "long",
      day: "numeric",
    }).format(date);
  }
});

// 後續的重新載入函數
const loadEvents = async () => {
  try {
    loading.value = true;
    error.value = null;
    const response = await eventApi.getAllEvents();

    if (Array.isArray(response.data)) {
      events.value = response.data;
    } else {
      events.value = [];
      error.value = t("errors.apiFormat");
    }
  } catch (err) {
    error.value = handleApiError(err, t("errors.loadEventsFailed"));
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
    currency: "TWD", // 🔥 確保新事件有預設幣別
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
    currency: "TWD", // 🔥 加上預設幣別
  };
};

const editEvent = (event) => {
  // 🔥 防止使用者編輯外部訂閱事件
  if (event.isExternal) {
    showToast("無法編輯外部來源的活動", "info");
    return;
  }

  showModal.value = true;
  eventForm.value = {
    id: event.id,
    title: event.title,
    description: event.description || "",
    startTime: formatDateTimeLocal(new Date(event.startTime)),
    endTime: formatDateTimeLocal(new Date(event.endTime)),
    color: event.color || "#7c8db5",
    estimatedCost: event.estimatedCost || null,
    currency: event.currency || "TWD", // 🔥 如果舊資料沒有，給預設值
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
    showToast(t("messages.saveSuccess"), "success");
  } catch (err) {
    const msg = err.response?.data?.message || err.message;
    showToast(t("errors.operationFailed") + msg, "error");
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
    showToast(t("messages.deleteSuccess"), "success");
  } catch (err) {
    const msg = err.response?.data?.message || err.message;
    showToast(t("errors.deleteFailed") + msg, "error");
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
  grid-template-columns: 1fr max-content 1fr;
  justify-content: space-between;
  align-items: center;
  padding: 15px 40px;
  background: white;
  border-bottom: 1px solid #f5f5f5;
  flex-shrink: 0;
  gap: 15px;
}

.left-group {
  justify-self: start;
}

.center-group {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: auto;
  min-width: 0;
}

.date-title {
  font-size: 20px;
  font-weight: 400;
  letter-spacing: 0.05em;
  color: #222;
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
  white-space: nowrap;
  flex-shrink: 0;
}

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
  background: #446344;
  opacity: 1;
}

.nav-arrow {
  background: transparent;
  border: none;
  font-size: 14px;
  color: #999;
  cursor: pointer;
  padding: 4px;
  margin: 0;
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
    display: flex;
  }

  .right-group {
    justify-content: space-between;
    width: 100%;
  }

  .date-title {
    font-size: 16px;
  }

  .center-group {
    order: -1;
    width: 100%;
    justify-content: center;
    margin-bottom: 5px;
    gap: 15px;
  }
}
</style>
