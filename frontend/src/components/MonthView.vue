<template>
  <div class="month-view">
    <!-- 🔥 新增：滾動容器，包住 Header 和 Grid -->
    <div class="month-scroll-wrapper">
      <!-- 星期標題 -->
      <div class="weekdays-header">
        <div v-for="day in weekdays" :key="day" class="weekday-cell">
          {{ day }}
        </div>
      </div>

      <!-- 日曆網格 -->
      <div class="calendar-grid">
        <div
          v-for="(dayData, index) in calendarDays"
          :key="index"
          class="day"
          :class="{
            'other-month': dayData.isOtherMonth,
            today: dayData.isToday,
            /* 只要有事件就變色 */
            'has-event':
              !dayData.isOtherMonth && getEventsForDay(dayData.date).length > 0,
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
            <!-- 🔥 修改重點：使用 getVisibleTracks 來渲染，包含 null (空氣) -->
            <template
              v-for="(event, idx) in getVisibleTracks(dayData.date)"
              :key="idx"
            >
              <!-- 情況 A: 這個軌道有事件 -> 渲染事件條 -->
              <div
                v-if="event"
                class="event-bar"
                :style="getEventStyle(event, dayData.date)"
                @click.stop="emit('edit-event', event)"
                :title="getEventTitle(event)"
              >
                <span
                  v-if="shouldShowTitle(event, dayData.date)"
                  class="event-title"
                >
                  {{ event.title }}
                </span>
              </div>

              <!-- 情況 B: 這個軌道是空的 -> 渲染隱形佔位符，防止下方事件跳上來 -->
              <div v-else class="empty-event"></div>
            </template>
          </div>

          <!-- More 按鈕 (判斷總數是否大於 4) -->
          <div
            v-if="getEventsForDay(dayData.date).length > 4"
            class="more-events"
            @click.stop="openListModal(dayData.date)"
          >
            {{ $t('calendar.moreEvents', { count: getEventsForDay(dayData.date).length - 4 }) }}
          </div>
        </div>
      </div>
      <!-- Grid 結束 -->
    </div>
    <!-- 滾動容器結束 -->
     
    <!-- 列表彈窗 -->
    <div
      v-if="showListModal"
      class="list-modal-overlay"
      @click.self="closeListModal"
      @wheel.stop
    >
      <div class="list-modal">
        <div class="list-header">
          <h3>{{ listDateTitle }}</h3>
          <button class="btn-close" @click="closeListModal"></button>
        </div>
        <div class="list-content">
          <div
            v-for="event in listEvents"
            :key="event.id"
            class="list-item"
            @click="handleListEventClick(event)"
          >
            <div
              class="item-dot"
              :style="{ background: event.color || '#557c55' }"
            ></div>
            <div class="item-info">
              <span class="item-time">{{ formatTime(event.startTime) }}</span>
              <span class="item-title">{{ event.title }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, watch, toRef, computed } from "vue";

import { useCostAnalysis } from "../composables/useCostAnalysis";
// 如果你的 utils 路徑不同，請確認這行
import { formatDateTimeLocal } from "../utils/dateFormatter";
import { useI18n } from 'vue-i18n'


const { t } = useI18n()
const weekdays = computed(() => [
  t('calendar.weekdays.sun'),
  t('calendar.weekdays.mon'),
  t('calendar.weekdays.tue'),
  t('calendar.weekdays.wed'),
  t('calendar.weekdays.thu'),
  t('calendar.weekdays.fri'),
  t('calendar.weekdays.sat')
])
const props = defineProps({
  currentDate: { type: Date, required: true },
  events: { type: Array, default: () => [] },
});
const emit = defineEmits(["add-event", "edit-event", "change-view"]);

// 🔥 使用 Composable 處理金錢邏輯
const eventsRef = toRef(props, "events");
const { getCostLevel } = useCostAnalysis(eventsRef);

// --- 以下維持你原有的日曆邏輯 ---
const calendarDays = ref([]);
const eventTracks = ref(new Map());

const renderCalendar = () => {
  /* ... 保留你修好的邏輯 ... */
  // 簡化顯示，請貼上你原本的 renderCalendar
  const year = props.currentDate.getFullYear();
  const month = props.currentDate.getMonth();
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const prevLastDay = new Date(year, month, 0);
  const firstDayWeek = firstDay.getDay();
  calendarDays.value = [];
  for (let i = firstDayWeek - 1; i >= 0; i--)
    calendarDays.value.push({
      date: new Date(year, month - 1, prevLastDay.getDate() - i),
      isOtherMonth: true,
      isToday: false,
    });
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const today = new Date();
    calendarDays.value.push({
      date: new Date(year, month, i),
      isOtherMonth: false,
      isToday:
        year === today.getFullYear() &&
        month === today.getMonth() &&
        i === today.getDate(),
    });
  }
  const remainingCells = 42 - calendarDays.value.length;
  for (let i = 1; i <= remainingCells; i++)
    calendarDays.value.push({
      date: new Date(year, month + 1, i),
      isOtherMonth: true,
      isToday: false,
    });
  assignEventTracks();
};

const getEventsForDay = (date) => {
  /* ... 保留你修好的邏輯 ... */
  if (!Array.isArray(props.events)) return [];
  const targetDay = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate()
  );
  return props.events
    .filter((event) => {
      const startDate = new Date(event.startTime);
      const endDate = new Date(event.endTime);
      const startDay = new Date(
        startDate.getFullYear(),
        startDate.getMonth(),
        startDate.getDate()
      );
      const endDay = new Date(
        endDate.getFullYear(),
        endDate.getMonth(),
        endDate.getDate()
      );
      return targetDay >= startDay && targetDay <= endDay;
    })
    .sort((a, b) => {
      const trackA = eventTracks.value.get(a.id) || 0;
      const trackB = eventTracks.value.get(b.id) || 0;
      if (trackA !== trackB) return trackA - trackB;
      return new Date(a.startTime) - new Date(b.startTime);
    });
};

const assignEventTracks = () => {
  /* ... 保留你修好的邏輯 ... */
  eventTracks.value.clear();
  if (!Array.isArray(props.events) || props.events.length === 0) return;
  const sortedEvents = [...props.events].sort(
    (a, b) => new Date(a.startTime) - new Date(b.startTime)
  );
  const tracks = [];
  sortedEvents.forEach((event) => {
    const eventStart = new Date(event.startTime);
    const eventEnd = new Date(event.endTime);
    const startDay = new Date(
      eventStart.getFullYear(),
      eventStart.getMonth(),
      eventStart.getDate()
    );
    const endDay = new Date(
      eventEnd.getFullYear(),
      eventEnd.getMonth(),
      eventEnd.getDate()
    );
    let trackIndex = 0;
    while (trackIndex < tracks.length) {
      if (tracks[trackIndex].endDate < startDay) break;
      trackIndex++;
    }
    eventTracks.value.set(event.id, trackIndex);
    if (trackIndex >= tracks.length)
      tracks.push({ endDate: endDay, eventId: event.id });
    else tracks[trackIndex] = { endDate: endDay, eventId: event.id };
  });
};

// 🔥 處理列表彈窗邏輯
const showListModal = ref(false);
const listEvents = ref([]);
const listDate = ref(null);

const listDateTitle = computed(() => {
  if (!listDate.value) return "";
  return `${
    listDate.value.getMonth() + 1
  }月${listDate.value.getDate()}日 的行程`;
});

const openListModal = (date) => {
  listDate.value = date;
  // 這裡取得當天"所有"事件
  listEvents.value = getEventsForDay(date); // 這裡會自動排序，因為 getEventsForDay 裡有 sort
  showListModal.value = true;
};

const closeListModal = () => {
  showListModal.value = false;
};

const handleListEventClick = (event) => {
  closeListModal();
  emit("edit-event", event); // 打開編輯視窗
};

const formatTime = (isoString) => {
  const d = new Date(isoString);
  return `${String(d.getHours()).padStart(2, "0")}:${String(
    d.getMinutes()
  ).padStart(2, "0")}`;
};

const getEventStyle = (event, date) => {
  // 1. 軌道與位置計算 (保留你的邏輯)
  const trackIndex = eventTracks.value.get(event.id) || 0;

  // 2. 日期計算
  const startDate = new Date(event.startTime);
  const endDate = new Date(event.endTime);
  const startDay = new Date(
    startDate.getFullYear(),
    startDate.getMonth(),
    startDate.getDate()
  );
  const endDay = new Date(
    endDate.getFullYear(),
    endDate.getMonth(),
    endDate.getDate()
  );
  const currentDay = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate()
  );

  const isStart = currentDay.getTime() === startDay.getTime();
  const isEnd = currentDay.getTime() === endDay.getTime();

  // 3. 樣式設定
  const style = {
    // --- 定位 (絕對定位，這是你測試出不會跑版的關鍵) ---
    // 🔥 修改 2：改為 relative，讓它佔據真實空間
    position: "relative",
    zIndex: 10 - trackIndex,
    left: "0",
    right: "0",
    width: "100%",
    boxSizing: "border-box",

    // --- 視覺 (Google 風格：實心、白字、清晰) ---
    backgroundColor: event.color || "#557c55",
    color: "#ffffff",

    // 🔥 修正你的 typo: ontSize -> fontSize
    fontSize: "11px", // 稍微大一點點 (10px -> 11px) 比較清楚
    fontWeight: "500", // 稍微加粗，提升白字可讀性
    textShadow: "0 0 1px rgba(0,0,0,0.2)", // 微微陰影防糊

    lineHeight: "18px",
    height: "18px",
    padding: "0 6px",

    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis",
    boxShadow: "none", // 扁平化

    // --- 預設邊距 (稍後覆蓋) ---
    marginBottom: "1px",
    marginLeft: "0px",
    marginRight: "0px",
    borderRadius: "0px",
  };

  // 4. 邊距與圓角邏輯 (保留你測試成功的數值)
  if (isStart && isEnd) {
    style.borderRadius = "3px";
    style.marginLeft = "1px"; // 左右各留 1px，視覺置中且不黏邊
    style.marginRight = "1px";
  } else if (isStart) {
    style.borderTopLeftRadius = "3px";
    style.borderBottomLeftRadius = "3px";
    style.marginLeft = "1px"; // 起始點留縫隙
    style.marginRight = "0px"; // 連接處貼齊
  } else if (isEnd) {
    style.borderTopRightRadius = "3px";
    style.borderBottomRightRadius = "3px";
    style.marginLeft = "0px"; // 連接處貼齊
    style.marginRight = "1px"; // 結束點留縫隙
  }
  // 中間段維持 margin 0

  return style;
};
const getEventTitle = (event) => {
  return event.description
    ? event.title + "\n" + event.description
    : event.title;
};
const shouldShowTitle = (event, date) => {
  const s = new Date(event.startTime);
  return (
    date.getTime() ===
    new Date(s.getFullYear(), s.getMonth(), s.getDate()).getTime()
  );
};
// 🔥 新增：產生固定 4 個軌道的陣列 (含空位)
const getVisibleTracks = (date) => {
  // 1. 取得當天所有事件
  const dayEvents = getEventsForDay(date);

  // 2. 建立一個固定 4 格的空陣列 [null, null, null, null]
  const slots = Array(4).fill(null);

  // 3. 把事件填入對應的軌道 (Track)
  dayEvents.forEach((event) => {
    const track = eventTracks.value.get(event.id);

    // 只顯示軌道 0~3 的事件
    if (track !== undefined && track < 4) {
      slots[track] = event;
    }
  });

  return slots;
};
renderCalendar();
watch(() => props.currentDate, renderCalendar);
watch(() => props.events, renderCalendar, { deep: true });
</script>

<style scoped>
/* 繼承你原有的 Style + 熱力圖樣式 */
.month-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;  /* 防止最外層出現雙重卷軸 */
  padding: 0;
  height: 100%;
  background: #fff;
}
/* 🔥 2. 新增：內部滾動容器 */
/* 滾動容器與標題：移除 800px 限制，改回 100% */
.month-scroll-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto; /* 只保留垂直捲動 */
  overflow-x: hidden; /* 🔥 禁止水平捲動，強制塞進畫面 */
  min-width: 100%;    /* 🔥 改回 100% */
}

/* 讓 Header 和 Grid 也跟著 RWD 縮放 */
.weekdays-header, .calendar-grid {
  min-width: 100%;    /* 🔥 改回 100% */
  width: 100%;        /* 確保佔滿 */
}

.weekdays-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #fafbf9;

  /* 下底線 */
  border-bottom: 1px solid #d1d5db;

  /* 左右邊框補齊 */
  border-left: 1px solid #d1d5db;
  border-right: 1px solid #d1d5db;
  border-top: 1px solid #d1d5db;

  /* 🔥 微調：因為下面有 gap: 1px，這裡其實很難完美對齊每一條直線
     但在視覺上，只要外框對齊，中間的線稍微錯開 1px 通常是可以接受的。
     如果要完美對齊，Header 也要用 gap: 1px */
  gap: 1px;
  background-color: #d1d5db; /* 讓 Header 的縫隙也變灰 */

  /* 🔥 防止 Header 捲動時黏在上面不動導致錯位，讓它跟著 Grid 一起捲 */
  flex-shrink: 0; 
}

/* 讓 Header 和 Grid 繼承這個最小寬度 */
.weekdays-header, .calendar-grid {
  /* 🔥 關鍵：強制最小寬度，確保不會被壓扁 */
  min-width: 800px; 
}

.weekday-cell {
  background-color: #fafbf9; /* 確保 Header 格子有顏色 */
  text-align: center;
  padding: 12px 0;
  font-weight: 500;
  color: #888;
  font-size: 11px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-auto-rows: 1fr;
  flex: 1;
  gap: 1px;
  background-color: #d1d5db; /* 格線顏色 (統一用這個) */
  border: 1px solid #d1d5db; /* 外框 */
}
.clickable-num {
  cursor: pointer;
  border-radius: 50%;
  transition: background 0.2s;
  padding: 2px;
}
.clickable-num:hover {
  background: #eee;
  color: #333;
}
.day:nth-child(7n + 1) {
  border-left: 1px solid #f5f5f5;
}
/* 2. 修改 .day */
/* 3. 調整 Day 容器：給予足夠的最小高度 */
.day {
  background-color: white;
  padding: 4px 0 0 0;
  cursor: pointer;
  position: relative;
  transition: background 0.2s;

  /* 🔥 關鍵：增加最小高度 */
  /* 計算公式：Header(28) + 4個事件(19*4=76) + Footer(20) = 124px */
  /* 設定 125px 或更大，確保 4 個事件不會被切掉 */
  min-height: 130px;

  height: auto;
  box-sizing: border-box;
  width: 100%;

  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  min-width: 0;
  overflow: hidden;
}
.day:hover {
  background: #f5f7f5;
}
/* Day Header */
/* Day Header: 強制固定高度，確保每一格的起始點完全一致 */
.day-header {
  height: 28px; /* 🔥 關鍵：鎖死高度 (比圓圈 22px 大一點即可) */
  min-height: 28px; /* 雙重保險 */

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

.day.has-event {
  /* 🔥 調整後：使用主色 (#557c55) 但透明度改為 0.12 (12%) */
  background-color: rgba(85, 124, 85, 0.08);
}

/* Hover 時稍微再深一點點，增加互動感 */
.day.has-event:hover {
  background-color: rgba(85, 124, 85, 0.15);
}
.day.other-month {
  background: #fafafa;
}
.day.other-month .day-number {
  color: #ccc;
}
.day.today .clickable-num:hover {
  background: #444;
}
.day.today .day-number {
  background: #333;
  color: white;
  width: 22px; /* 22px < 28px，所以不會撐開 header */
  height: 22px;
  border-radius: 50%;
  font-weight: 500;
  text-decoration: underline;
  /* 確保沒有 margin 干擾 */
  margin: 0;
}

.events-wrapper {
  position: relative;
  flex: 1;
  width: 100%;

  /* 🔥 Flex 排列 */
  display: flex;
  flex-direction: column;

  /* 確保從頂部開始排 */
  justify-content: flex-start;

  overflow: hidden;
  pointer-events: none;
  margin-top: 0;

  /* 稍微留點上方緩衝 */
  padding-top: 1px;
  padding-bottom: 2px;

  flex-shrink: 0;
}

/* 4. 確保事件條也是 Block 元素 (雖然上面 JS 有改，但 CSS 雙重保險) */
.event-bar {
  display: block;
  pointer-events: auto;
  flex-shrink: 0; /* 禁止被壓縮 */
}
.event-bar:hover {
  opacity: 0.85;
  transform: translateY(-1px);
}

/* 2. 調整空白佔位符：這是關鍵！ */
/* 因為我們改用 Flex 堆疊，空的軌道必須要有「實體高度」才能把下面的東西擠下去 */
.empty-event {
  height: 18px; /* 跟事件條一樣高 */
  margin-bottom: 1px; /* 跟事件條一樣的間距 */
  width: 100%;
  visibility: hidden; /* 看不見，但佔空間 */
  display: block; /* 確保它佔據一行 */
}

/* src/components/MonthView.vue */

.more-events {
  /* 定位 */
  position: relative;
  margin-top: auto; /* 自動推到最底 */

  /* 尺寸 */
  height: 18px;
  line-height: 18px;

  /* 🔥 關鍵：防止按鈕被壓縮 */
  /* 告訴瀏覽器：就算空間不夠，也不准壓縮這個按鈕的高度 */
  flex-shrink: 0;

  /* 外觀 */
  text-align: left;
  font-size: 10px;
  color: #557c55;
  padding-left: 6px;
  font-weight: 500;

  /* 背景與邊框：移除之前的 border hack，回歸單純 */
  background: inherit;
  box-shadow: none;
  border-radius: 0;

  /* 其他 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  z-index: 30;
}

.more-events:hover {
  background-color: rgba(85, 124, 85, 0.2) !important;
  color: #333;
}
/* 🔥 列表彈窗樣式 */
.list-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  /* 改用 viewport 單位，確保蓋住全螢幕 */
  width: 100vw;
  height: 100vh;

  /* 🔥 修正 1：移除 backdrop-filter (這是變全黑的元兇) */
  /* backdrop-filter: blur(2px); */

  /* 🔥 修正 2：單純的半透明黑 (0.3 ~ 0.5 即可) */
  background-color: rgba(0, 0, 0, 0.3);

  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;

  /* 確保彈窗不會繼承奇怪的文字設定 */
  text-align: left;
}

.list-modal {
  background: white;
  width: 300px;
  max-height: 80vh;
  border-radius: 4px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.list-header {
  padding: 15px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafbf9;
}
.list-header h3 {
  margin: 0;
  font-size: 16px;
  color: #557c55;
  font-weight: 500;
}
.btn-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #999;
}
.list-content {
  padding: 10px;
  overflow-y: auto;
}
.list-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f9f9f9;
}
.list-item:hover {
  background: #f5f7f5;
}
.item-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.item-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.item-time {
  font-size: 11px;
  color: #999;
}
.item-title {
  font-size: 13px;
  color: #444;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* src/components/MonthView.vue */

/* 🔥 手機版專屬強力排版 */
@media (max-width: 768px) {
  /* 1. 強制容器不准超過螢幕寬度 */
  .month-scroll-wrapper {
    width: 100% !important;
    min-width: 0 !important; /* 覆蓋之前的設定 */
    overflow-x: hidden !important; /* 禁止水平捲動 */
  }

  /* 2. Grid 的魔法設定 */
  .weekdays-header, 
  .calendar-grid {
    width: 100% !important;
    min-width: 0 !important;
    /* 🔥 關鍵：原本是 repeat(7, 1fr)，現在改成 minmax(0, 1fr) */
    /* 這會強制格子忽視內容長度，乖乖平分螢幕寬度 */
    grid-template-columns: repeat(7, minmax(0, 1fr)) !important;
  }

  /* 3. 格子內容設定：溢出隱藏 */
  .weekday-cell {
    font-size: 10px; /* 星期字體縮小 */
    padding: 5px 0;
    overflow: hidden; /* 防止文字撐開 */
  }

  .day {
    min-height: 80px; /* 手機版格子高度 */
    overflow: hidden; /* 🔥 防止事件條撐開格子 */
  }

  .day-header {
    padding: 0 2px;
  }
  
  .day-number {
    font-size: 11px;
    width: 20px;
    height: 20px;
    line-height: 20px;
  }

  /* 4. 事件條極限壓縮 */
  .event-bar {
    padding: 0 1px;
    font-size: 9px; /* 字體極小，仿 Google 日曆 */
    line-height: 16px;
    height: 16px;
    margin-bottom: 1px;
    
    /* 確保文字太長時顯示 ... */
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  /* 5. 底部按鈕 */
  .more-events {
    font-size: 9px;
    height: 16px;
    line-height: 14px;
    padding-left: 2px;
    /* 同樣防止撐開 */
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>