<template>
  <div class="month-view">
    <!-- 滾動容器 -->
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
            <template
              v-for="(event, idx) in getVisibleTracks(dayData.date)"
              :key="idx"
            >
              <!-- 情況 A: 這個軌道有事件 -->
              <div
                v-if="event"
                class="event-bar"
                :style="getEventStyle(event, dayData.date)"
                @click.stop="emit('edit-event', event)"
                :title="getEventTitle(event)"
              >
                <!-- 🔥 修改 1：圖標顯示邏輯同步標題邏輯 -->
                <!-- 只有在「活動開始日」或「週日」才顯示圖標，避免每一格都出現 -->
                <span 
                  v-if="event.category && shouldShowTitle(event, dayData.date)" 
                  class="event-category-icon"
                >
                  {{ event.category.icon }}
                </span>
                
                <span
                  v-if="shouldShowTitle(event, dayData.date)"
                  class="event-title"
                >
                  {{ event.title }}
                </span>
              </div>

              <!-- 情況 B: 隱形佔位符 -->
              <div v-else class="empty-event"></div>
            </template>
          </div>

          <!-- More 按鈕 -->
          <div
            v-if="getEventsForDay(dayData.date).length > 4"
            class="more-events"
            @click.stop="openListModal(dayData.date)"
          >
            {{
              $t("calendar.moreEvents", {
                count: getEventsForDay(dayData.date).length - 4,
              })
            }}
          </div>
        </div>
      </div>
    </div>

    <!-- 列表彈窗 (維持不變) -->
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
import { formatDateTimeLocal } from "../utils/dateFormatter";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const weekdays = computed(() => [
  t("calendar.weekdays.sun"),
  t("calendar.weekdays.mon"),
  t("calendar.weekdays.tue"),
  t("calendar.weekdays.wed"),
  t("calendar.weekdays.thu"),
  t("calendar.weekdays.fri"),
  t("calendar.weekdays.sat"),
]);
const props = defineProps({
  currentDate: { type: Date, required: true },
  events: { type: Array, default: () => [] },
});
const emit = defineEmits(["add-event", "edit-event", "change-view"]);

const eventsRef = toRef(props, "events");
const { getCostLevel } = useCostAnalysis(eventsRef);

const calendarDays = ref([]);
const eventTracks = ref(new Map());

const renderCalendar = () => {
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
  listEvents.value = getEventsForDay(date);
  showListModal.value = true;
};

const closeListModal = () => {
  showListModal.value = false;
};

const handleListEventClick = (event) => {
  closeListModal();
  emit("edit-event", event);
};

const formatTime = (isoString) => {
  const d = new Date(isoString);
  return `${String(d.getHours()).padStart(2, "0")}:${String(
    d.getMinutes()
  ).padStart(2, "0")}`;
};

const getEventStyle = (event, date) => {
  const trackIndex = eventTracks.value.get(event.id) || 0;
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

  const style = {
    position: "relative",
    zIndex: 10 - trackIndex,
    left: "0",
    right: "0",
    width: "100%",
    boxSizing: "border-box",
    backgroundColor: event.color || "#557c55",
    color: "#ffffff",
    fontSize: "11px",
    fontWeight: "500",
    textShadow: "0 0 1px rgba(0,0,0,0.2)",
    lineHeight: "18px",
    height: "18px",
    padding: "0 6px",
    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis",
    boxShadow: "none",
    marginBottom: "1px",
    marginLeft: "0px",
    marginRight: "0px",
    borderRadius: "0px",
  };

  if (isStart && isEnd) {
    style.borderRadius = "3px";
    style.marginLeft = "1px";
    style.marginRight = "1px";
  } else if (isStart) {
    style.borderTopLeftRadius = "3px";
    style.borderBottomLeftRadius = "3px";
    style.marginLeft = "1px";
    style.marginRight = "0px";
  } else if (isEnd) {
    style.borderTopRightRadius = "3px";
    style.borderBottomRightRadius = "3px";
    style.marginLeft = "0px";
    style.marginRight = "1px";
  }

  return style;
};

const getEventTitle = (event) => {
  return event.description
    ? event.title + "\n" + event.description
    : event.title;
};

// 🔥 修改 2：更新標題顯示邏輯
// 當「活動開始日」或「該格是週日(每週第一天)」時，顯示標題
const shouldShowTitle = (event, date) => {
  // 1. 檢查是否為開始日
  const s = new Date(event.startTime);
  const isStartDate = date.getTime() ===
    new Date(s.getFullYear(), s.getMonth(), s.getDate()).getTime();

  // 2. 檢查是否為週日 (每週的開始)
  const isSunday = date.getDay() === 0;

  return isStartDate || isSunday;
};

const getVisibleTracks = (date) => {
  const dayEvents = getEventsForDay(date);
  const slots = Array(4).fill(null);
  dayEvents.forEach((event) => {
    const track = eventTracks.value.get(event.id);
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
/* 維持原有樣式 */
.month-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
  height: 100%;
  background: #fff;
}
.month-scroll-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: hidden;
  min-width: 100%;
}
.weekdays-header,
.calendar-grid {
  min-width: 100%;
  width: 100%;
}
.weekdays-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #fafbf9;
  border-bottom: 1px solid #d1d5db;
  border-left: 1px solid #d1d5db;
  border-right: 1px solid #d1d5db;
  border-top: 1px solid #d1d5db;
  gap: 1px;
  background-color: #d1d5db;
  flex-shrink: 0;
  min-width: 800px;
}
.calendar-grid {
  min-width: 800px;
}
.weekday-cell {
  background-color: #fafbf9;
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
  background-color: #d1d5db;
  border: 1px solid #d1d5db;
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
.day {
  background-color: white;
  padding: 4px 0 0 0;
  cursor: pointer;
  position: relative;
  transition: background 0.2s;
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
.day-header {
  height: 28px;
  min-height: 28px;
  padding: 0 8px;
  margin-bottom: 2px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}
.day-number {
  font-size: 12px;
  color: #444;
  position: relative;
  z-index: 1;
  padding-left: 0;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.day.has-event {
  background-color: #f2f7f2;
}
.day.has-event:hover {
  background-color: #e6efe6;
}
.day.other-month {
  background: #F5F5F5;
}
.day.other-month .day-number {
  color: #d1d5db;
}
.day.today .clickable-num:hover {
  background: #444;
}
.day.today .day-number {
  background: #333;
  color: white;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  font-weight: 500;
  text-decoration: underline;
  margin: 0;
}
.events-wrapper {
  position: relative;
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  overflow: hidden;
  pointer-events: none;
  margin-top: 0;
  padding-top: 1px;
  padding-bottom: 2px;
  flex-shrink: 0;
}
.event-bar {
  display: block;
  pointer-events: auto;
  flex-shrink: 0;
}
.event-bar:hover {
  opacity: 0.85;
  transform: translateY(-1px);
}
.empty-event {
  height: 18px;
  margin-bottom: 1px;
  width: 100%;
  visibility: hidden;
  display: block;
}
.more-events {
  position: relative;
  margin-top: auto;
  height: 18px;
  line-height: 18px;
  flex-shrink: 0;
  text-align: left;
  font-size: 10px;
  color: #557c55;
  padding-left: 6px;
  font-weight: 500;
  background: inherit;
  box-shadow: none;
  border-radius: 0;
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
.list-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
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
@media (max-width: 768px) {
  .month-scroll-wrapper {
    width: 100% !important;
    min-width: 0 !important;
    overflow-x: hidden !important;
  }
  .weekdays-header,
  .calendar-grid {
    width: 100% !important;
    min-width: 0 !important;
    grid-template-columns: repeat(7, minmax(0, 1fr)) !important;
  }
  .weekday-cell {
    font-size: 10px;
    padding: 5px 0;
    overflow: hidden;
  }
  .day {
    min-height: 80px;
    overflow: hidden;
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
  .event-bar {
    padding: 0 1px;
    font-size: 9px;
    line-height: 16px;
    height: 16px;
    margin-bottom: 1px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .more-events {
    font-size: 9px;
    height: 16px;
    line-height: 14px;
    padding-left: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .event-category-icon {
    font-size: 11px;
    margin-right: 3px;
  }
}
</style>