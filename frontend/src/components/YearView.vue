<template>
  <div class="year-view">
    <div class="months-grid">
      <div v-for="monthIndex in 12" :key="monthIndex" class="month-card">
        <!-- 🔥 修改：使用多語言函式顯示月份標題 -->
        <h3
          class="month-title clickable"
          @click="
            emit(
              'change-view',
              'month',
              new Date(currentDate.getFullYear(), monthIndex - 1, 1)
            )
          "
        >
          {{ getMonthName(monthIndex - 1) }}
        </h3>

        <div class="mini-calendar">
          <!-- 🔥 修改：使用多語言星期標題 -->
          <div v-for="day in miniWeekdays" :key="day" class="mini-day-header">
            {{ day }}
          </div>

          <div
            v-for="(dayData, index) in getMonthDays(monthIndex - 1)"
            :key="index"
            class="mini-day"
            :class="{
              'other-month': dayData.isOtherMonth,
              today: dayData.isToday,
              'has-events': !dayData.isOtherMonth && hasEvents(dayData.date),
            }"
            :title="
              getDailyCost(dayData.date) > 0
                ? `$${formatCost(getDailyCost(dayData.date))}`
                : ''
            "
            @click.stop="emit('change-view', 'day', dayData.date)"
          >
            <span v-if="!dayData.isOtherMonth">{{
              dayData.date.getDate()
            }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { toRef, computed } from "vue";
import { useI18n } from "vue-i18n"; // 🔥 引入
import { useCostAnalysis } from "../composables/useCostAnalysis";

const props = defineProps({
  currentDate: { type: Date, required: true },
  events: { type: Array, default: () => [] },
});

const emit = defineEmits(["go-to-date", "change-view"]);

// i18n
const { t, locale } = useI18n();

// 🔥 新增：取得月份名稱 (依據當前語言)
const getMonthName = (monthIndex) => {
  const date = new Date(props.currentDate.getFullYear(), monthIndex, 1);
  return new Intl.DateTimeFormat(locale.value, { month: "short" }).format(date);
};

// 🔥 新增：取得星期簡寫 (日, 一... / Sun, Mon...)
const miniWeekdays = computed(() => [
  t("calendar.weekdays.sun"),
  t("calendar.weekdays.mon"),
  t("calendar.weekdays.tue"),
  t("calendar.weekdays.wed"),
  t("calendar.weekdays.thu"),
  t("calendar.weekdays.fri"),
  t("calendar.weekdays.sat"),
]);

const eventsRef = toRef(props, "events");
const { getCostLevel, getDailyCost, formatCost } = useCostAnalysis(eventsRef);

const getMonthDays = (monthIndex) => {
  const year = props.currentDate.getFullYear();
  const firstDay = new Date(year, monthIndex, 1);
  const lastDay = new Date(year, monthIndex + 1, 0);
  const prevLastDay = new Date(year, monthIndex, 0);

  const firstDayWeek = firstDay.getDay();
  const lastDayDate = lastDay.getDate();

  const days = [];

  // 補上個月
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    days.push({
      date: new Date(year, monthIndex - 1, prevLastDay.getDate() - i),
      isOtherMonth: true,
      isToday: false,
    });
  }

  // 當月
  const today = new Date();
  for (let i = 1; i <= lastDayDate; i++) {
    const isToday =
      year === today.getFullYear() &&
      monthIndex === today.getMonth() &&
      i === today.getDate();
    days.push({
      date: new Date(year, monthIndex, i),
      isOtherMonth: false,
      isToday: isToday,
    });
  }

  // 補下個月
 // 🔥 3. 修改這裡：強制補滿 42 格 (6週)
  // 原本是：while (days.length % 7 !== 0)
  // 改為：while (days.length < 42)
  while (days.length < 42) {
    const nextDate = days.length - (firstDayWeek + lastDayDate) + 1
    days.push({
      date: new Date(year, monthIndex + 1, nextDate),
      isOtherMonth: true,
      isToday: false
    })
  }

  return days;
};

// 判斷當天是否有事件 (支援跨日事件)
const hasEvents = (date) => {
  const cellTime = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate()
  ).getTime();

  return props.events.some((event) => {
    // 這裡我們簡單用 new Date，因為只是判斷有沒有，不用精確到毫秒
    // 如果要跟 DayView 一樣嚴謹，也可以用 parseDate，但年視圖只需判斷日期重疊
    const start = new Date(event.startTime);
    const end = new Date(event.endTime);

    const sTime = new Date(
      start.getFullYear(),
      start.getMonth(),
      start.getDate()
    ).getTime();
    const eTime = new Date(
      end.getFullYear(),
      end.getMonth(),
      end.getDate()
    ).getTime();

    return cellTime >= sTime && cellTime <= eTime;
  });
};
</script>

<style scoped>
/* 
  ========================================
  您原本的樣式 (保留日系極簡風格)
  ========================================
*/
.year-view {
  padding: 30px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.months-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  padding-bottom: 20px;
}

.month-card {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
  
  display: flex;
  flex-direction: column;
  
  /* 🔥 關鍵：強制靠上對齊 */
  justify-content: flex-start !important;
  
  /* 🔥 關鍵：標題與月曆的距離固定為 15px */
  gap: 15px; 
}

.month-card:hover {
  /* 森綠色 Hover 效果 */
  border-color: #557c55;
  box-shadow: 0 4px 12px rgba(85, 124, 85, 0.2);
}

.month-title {
  text-align: center;
  color: #557c55;
  
  /* 🔥 關鍵：移除 margin，改由 parent 的 gap 控制 */
  margin-bottom: 0; 
  
  font-size: 18px;
}

.mini-calendar {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  /* 使用 gap 作為網格線 */
  gap: 1px;
  background-color: #d1d5db; /* 冷灰線條 */
  border: 1px solid #d1d5db;

  border-radius: 4px;
  overflow: hidden;
}

.mini-day-header {
  text-align: center;
  font-size: 10px;
  font-weight: bold;
  color: #557c55;
  background-color: #fafbf9; /* 標題底色，讓它跟格子區分開 */
  padding: 5px 2px;
}

.mini-day {
  background-color: white; /* 格子底色 */
  text-align: center;
  padding: 4px 2px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s;
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
  background: #fbbf24; /* 今天：黃色 */
  color: white;
  font-weight: bold;
}

.mini-day.has-events {
  background: #557c55; /* 有事件：森綠色 */
  color: white;
}

.clickable {
  cursor: pointer;
  transition: color 0.2s;
}
.clickable:hover {
  color: #446344;
  text-decoration: underline;
}

/* RWD - 平板 */
@media (max-width: 768px) {
  .year-view {
    padding: 15px;
  }
  .months-grid {
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

/* RWD - 手機 */
@media (max-width: 480px) {
  .year-view {
    padding: 10px;
  }
  .months-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  /* src/components/YearView.vue */

  .month-card {
    background: white;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    padding: 20px;
    transition: all 0.3s;

    display: flex;
    flex-direction: column;

    /* ❌ 移除這行：不要分散對齊 */
    /* justify-content: space-between; */

    /* ✅ 改為這行：靠上對齊，確保標題跟月曆緊鄰 */
    justify-content: flex-start;

    /* ✅ 新增：設定標題跟月曆之間固定的距離 */
    gap: 15px;
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
</style>
