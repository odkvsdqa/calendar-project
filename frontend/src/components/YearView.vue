<template>
  <div class="year-view">
    <div class="months-grid">
      <div v-for="monthIndex in 12" :key="monthIndex" class="month-card">
        <h3
          class="month-title clickable"
          @click="emit('change-view', 'month', new Date(currentDate.getFullYear(), monthIndex - 1, 1))"
        >
          {{ getMonthName(monthIndex - 1) }}
        </h3>

        <div class="mini-calendar">
          <div v-for="day in miniWeekdays" :key="day" class="mini-day-header">
            {{ day }}
          </div>

          <div
            v-for="(dayData, index) in getMonthDays(monthIndex - 1)"
            :key="index"
            class="mini-day"
            :class="{
              'other-month': dayData.isOtherMonth,
              'today': dayData.isToday
            }"
            :style="getDayStyle(dayData)"
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
import { toRef, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useCostAnalysis } from "../composables/useCostAnalysis";
import { parseDate } from "../utils/dateFormatter"; 

const props = defineProps({
  currentDate: { type: Date, required: true },
  events: { type: Array, default: () => [] },
});

const emit = defineEmits(["go-to-date", "change-view"]);
const { t, locale } = useI18n();

const getMonthName = (monthIndex) => {
  const date = new Date(props.currentDate.getFullYear(), monthIndex, 1);
  return new Intl.DateTimeFormat(locale.value, { month: "short" }).format(date);
};

const miniWeekdays = computed(() => [
  t("calendar.weekdays.sun"), t("calendar.weekdays.mon"), t("calendar.weekdays.tue"),
  t("calendar.weekdays.wed"), t("calendar.weekdays.thu"), t("calendar.weekdays.fri"),
  t("calendar.weekdays.sat"),
]);

const eventsRef = toRef(props, "events");
const { getDailyCost, formatCost } = useCostAnalysis(eventsRef);

// Grid 邏輯：補上個月/當月/補下個月
const getMonthDays = (monthIndex) => {
  const year = props.currentDate.getFullYear();
  const firstDay = new Date(year, monthIndex, 1);
  const lastDay = new Date(year, monthIndex + 1, 0);
  const prevLastDay = new Date(year, monthIndex, 0);

  const firstDayWeek = firstDay.getDay();
  const lastDayDate = lastDay.getDate();
  const prevLastDayDate = prevLastDay.getDate();

  const days = [];
  const today = new Date();

  // A. 補上個月
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    const d = new Date(year, monthIndex - 1, prevLastDayDate - i);
    days.push({ date: d, isOtherMonth: true, isToday: false });
  }

  // B. 本月
  for (let i = 1; i <= lastDayDate; i++) {
    const d = new Date(year, monthIndex, i);
    const isToday = d.toDateString() === today.toDateString();
    days.push({ date: d, isOtherMonth: false, isToday: isToday });
  }

  // C. 補下個月 (補滿 42 格)
  while (days.length < 42) {
    const nextDate = days.length - (firstDayWeek + lastDayDate) + 1;
    const d = new Date(year, monthIndex + 1, nextDate);
    days.push({ date: d, isOtherMonth: true, isToday: false });
  }

  return days;
};

// 🔥 修正：自己的事件統一用淡綠色，場館事件保持原色
const getDayStyle = (dayData) => {
  if (dayData.isOtherMonth) return {};

  // 將當前格子時間正規化為 00:00:00
  const cellTime = new Date(dayData.date);
  cellTime.setHours(0, 0, 0, 0);
  const cellMs = cellTime.getTime();

  // 找出所有「涵蓋這一天」的事件
  const activeEvents = props.events.filter(e => {
    const start = parseDate(e.startTime);
    const end = parseDate(e.endTime);
    
    const sMs = new Date(start).setHours(0, 0, 0, 0);
    const eMs = new Date(end).setHours(23, 59, 59, 999);

    return cellMs >= sMs && cellMs <= eMs;
  });

  if (activeEvents.length > 0) {
    // 🔥 核心修改：優先找「使用者事件」
    const userEvent = activeEvents.find(e => !e.isExternal);

    if (userEvent) {
      // ✅ 使用者事件：統一用淡綠色 #557c55
      return {
        backgroundColor: '#557c55',
        color: 'white',
        fontWeight: 'bold'
      };
    } else {
      // ✅ 場館事件：保持原本顏色
      return {
        backgroundColor: activeEvents[0].color,
        color: 'white',
        fontWeight: 'bold'
      };
    }
  }

  return {};
};
</script>

<style scoped>
.year-view { padding: 30px; height: 100%; display: flex; flex-direction: column; overflow-y: auto; }
.months-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; padding-bottom: 20px; }
.month-card { background: white; border: 2px solid #e0e0e0; border-radius: 12px; padding: 20px; transition: all 0.3s; display: flex; flex-direction: column; justify-content: flex-start !important; gap: 15px; }
.month-card:hover { border-color: #557c55; box-shadow: 0 4px 12px rgba(85, 124, 85, 0.2); }
.month-title { text-align: center; color: #557c55; margin-bottom: 0; font-size: 18px; }
.mini-calendar { display: grid; grid-template-columns: repeat(7, 1fr); gap: 1px; background-color: #d1d5db; border: 1px solid #d1d5db; border-radius: 4px; overflow: hidden; }
.mini-day-header { text-align: center; font-size: 10px; font-weight: bold; color: #557c55; background-color: #fafbf9; padding: 5px 2px; }
.mini-day { background-color: white; text-align: center; padding: 4px 2px; font-size: 11px; cursor: pointer; transition: all 0.2s; height: 24px; display: flex; align-items: center; justify-content: center; }
.mini-day:hover { background: #f5f7f5; }
.mini-day.other-month { opacity: 0.3; }
.mini-day.today { background: #fbbf24; color: white; font-weight: bold; }
.clickable { cursor: pointer; transition: color 0.2s; }
.clickable:hover { color: #446344; text-decoration: underline; }

@media (max-width: 768px) {
  .year-view { padding: 15px; }
  .months-grid { grid-template-columns: 1fr; gap: 15px; }
  .month-card { padding: 12px; border-radius: 10px; }
  .month-title { font-size: 16px; margin-bottom: 8px; }
  .mini-day-header { font-size: 9px; padding: 4px 1px; }
  .mini-day { padding: 6px 1px; font-size: 11px; }
}
@media (max-width: 480px) {
  .year-view { padding: 10px; }
  .months-grid { grid-template-columns: 1fr; gap: 12px; }
  .month-card { background: white; border: 2px solid #e0e0e0; border-radius: 12px; padding: 20px; transition: all 0.3s; display: flex; flex-direction: column; justify-content: flex-start; gap: 15px; }
  .month-title { font-size: 15px; margin-bottom: 6px; }
  .mini-calendar { gap: 1px; }
  .mini-day-header { font-size: 8px; padding: 3px 0; }
  .mini-day { padding: 5px 0; font-size: 10px; border-radius: 3px; }
}
</style>