<template>
  <div class="admin-dashboard">
    <div class="dashboard-container">
      <h1>📊 管理員儀表板</h1>
      
      <!-- Tab 切換 -->
      <div class="mode-selector">
        <button @click="currentTab = 'DateQuery'" :class="{ active: currentTab === 'DateQuery' }">按日期查詢</button>
        <button @click="currentTab = 'MonthlyStats'" :class="{ active: currentTab === 'MonthlyStats' }">按月份統計</button>
        <button @click="currentTab = 'TimeRangeStats'" :class="{ active: currentTab === 'TimeRangeStats' }">按時間範圍</button>
        <button @click="currentTab = 'UserSchedule'" :class="{ active: currentTab === 'UserSchedule' }">用戶排程</button>
      </div>

      <!-- 動態組件區 -->
      <div class="main-content">
        <!-- KeepAlive 會快取組件狀態，切換 Tab 時查詢結果不會消失 (可選) -->
        <KeepAlive>
          <component :is="tabs[currentTab]" />
        </KeepAlive>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 引入子組件
import DateQuery from './dashboard/DateQuery.vue'
import MonthlyStats from './dashboard/MonthlyStats.vue'
import TimeRangeStats from './dashboard/TimeRangeStats.vue'
import UserSchedule from './dashboard/UserSchedule.vue'

const tabs = {
  DateQuery,
  MonthlyStats,
  TimeRangeStats,
  UserSchedule
}

const currentTab = ref('DateQuery')
</script>

<!-- 引入共用 CSS，不要加 scoped，這樣樣式才能穿透到子組件 -->
<style src="../../assets/css/admin-dashboard.css"></style>