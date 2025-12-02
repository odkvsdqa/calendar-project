<template>
  <div class="admin-dashboard">
    <div class="dashboard-container">
      <h1>📊 {{ $t('admin.dashboard') }}</h1>
      
      <!-- Tab 切換 -->
      <div class="mode-selector">
        <button @click="currentTab = 'DateQuery'" :class="{ active: currentTab === 'DateQuery' }">
          {{ $t('admin.tabs.dateQuery') }}
        </button>
        <button @click="currentTab = 'MonthlyStats'" :class="{ active: currentTab === 'MonthlyStats' }">
          {{ $t('admin.tabs.monthlyStats') }}
        </button>
        <button @click="currentTab = 'TimeRangeStats'" :class="{ active: currentTab === 'TimeRangeStats' }">
          {{ $t('admin.tabs.timeRange') }}
        </button>
        <button @click="currentTab = 'UserSchedule'" :class="{ active: currentTab === 'UserSchedule' }">
          {{ $t('admin.tabs.userSchedule') }}
        </button>
      </div>

      <!-- 動態組件區 -->
      <div class="main-content">
        <KeepAlive>
          <component :is="tabs[currentTab]" />
        </KeepAlive>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref , watch} from 'vue'
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

// 🔥 修改：加入 localStorage 記憶功能
// 1. 嘗試讀取紀錄，如果沒有則預設為 'DateQuery'
const savedTab = localStorage.getItem('adminCurrentTab')
// 為了防止舊的髒資料導致錯誤，確認一下讀到的 key 是否存在於 tabs 中
const defaultTab = (savedTab && tabs[savedTab]) ? savedTab : 'DateQuery'

const currentTab = ref(defaultTab)

// 2. 監聽變化並儲存
watch(currentTab, (newTab) => {
  localStorage.setItem('adminCurrentTab', newTab)
})
</script>

<!-- 您原本引入的 CSS (若有的話保留，若沒有請確認路徑) -->
<!-- <style src="../../assets/css/admin-dashboard.css"></style> -->

<style scoped>
/* 
  ========================================
  您原本的樣式 (AdminDashboard) - 完整保留
  ========================================
*/
.dashboard-container {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}

h1 {
  /* 標題使用森綠色 */
  color: #557c55; 
  font-size: 24px;
  font-weight: 500;
  margin-bottom: 30px;
  letter-spacing: 0.05em;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Tab 切換區 */
.mode-selector {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.mode-selector button {
  padding: 8px 20px;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
  letter-spacing: 0.05em;
  
  /* 預設狀態：白底灰框 */
  background: white;
  border: 1px solid #e0e0e0;
  color: #666;
}

.mode-selector button:hover {
  border-color: #557c55;
  color: #557c55;
}

/* 選中狀態：森綠色實心 */
.mode-selector button.active {
  background: #557c55;
  color: white;
  border-color: #557c55;
  font-weight: 500;
  box-shadow: 0 2px 5px rgba(85, 124, 85, 0.3);
}

/* 內容區塊 */
.main-content {
  background: white;
  border-radius: 8px;
  padding: 30px;
  /* 極淡陰影 */
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.02); 
  border: 1px solid #e5e7eb;
}
</style>