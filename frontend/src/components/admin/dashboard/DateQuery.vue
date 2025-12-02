<template>
  <div class="query-section">
    <h2>{{ $t('admin.components.dateQuery.title') }}</h2>
    
    <div class="input-group">
      <label>{{ $t('admin.components.dateQuery.label') }}</label>
      <input type="date" v-model="selectedDate" class="input-styled" />
      <button @click="fetchUsersByDate" :disabled="loading" class="btn-query">
        {{ $t('common.query') }}
      </button>
    </div>
    
    <!-- 結果區 -->
    <div v-if="result" class="result-box fade-in">
      <h3>{{ $t('admin.components.dateQuery.resultTitle') }} ({{ result.date }})</h3>
      
      <div class="stats-grid">
        <div class="stat-card">
          <!-- 🔥 修改：標題在上，加粗，加冒號 -->
          <div class="stat-label">{{ $t('admin.stats.scheduledUsers') }}:</div>
          <!-- 🔥 修改：數字在下，顏色統一 -->
          <div class="stat-number">{{ result.userCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">{{ $t('admin.stats.totalEvents') }}:</div>
          <div class="stat-number">{{ result.eventCount }}</div>
        </div>
      </div>

      <h4>{{ $t('admin.components.dateQuery.userList') }}</h4>
      <div class="user-list">
        <div v-for="user in result.users" :key="user.id" class="user-item">
          <span class="user-icon">👤</span>
          <div class="user-info">
            <span class="user-name">{{ user.username }}</span>
            <span class="user-email">{{ user.email }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空狀態 -->
    <div v-else class="empty-state">
      <div class="empty-icon">📅</div>
      <p>{{ $t('admin.components.dateQuery.empty') }}</p>
    </div>

    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { adminApi } from '../../../services/adminApi'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const today = new Date().toISOString().split('T')[0]
const selectedDate = ref(today)
const result = ref(null)
const loading = ref(false)

const fetchUsersByDate = async () => {
  if (!selectedDate.value) {
    alert(t('admin.errors.selectDate'))
    return
  }
  try {
    loading.value = true
    const response = await adminApi.getUsersByDate(selectedDate.value)
    result.value = response.data
  } catch (err) {
    console.error('Query failed:', err)
    alert(t('admin.errors.queryFailed'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 共用排版修正 */
.query-section {
  padding: 10px;
}

h2 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 600;
}

/* 輸入群組：修正間距 */
.input-group {
  display: flex;
  align-items: center;
  gap: 15px; /* 拉開間距 */
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.input-group label {
  font-weight: 500;
  color: #555;
}

.input-styled {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.btn-query {
  padding: 8px 20px;
  background-color: #557c55;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-query:hover { background-color: #446344; }
.btn-query:disabled { background-color: #ccc; }

/* 統計卡片：統一置中與字體 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 5px rgba(0,0,0,0.03);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* 🔥 統一字體設定 */
.stat-label {
  font-size: 14px;
  color: #555;
  font-weight: bold; /* 標題粗體 */
  margin-bottom: 8px;
}

.stat-number {
  font-size: 28px;
  color: #557c55; /* 主色 */
  font-weight: 500; /* 數字正常稍粗 */
  line-height: 1.2;
}

/* 用戶列表 */
.user-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.user-item {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.user-icon {
  font-size: 20px;
  background: #f3f4f6;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  margin-right: 15px;
}

.user-info { display: flex; flex-direction: column; }
.user-name { font-weight: bold; color: #333; font-size: 14px; }
.user-email { color: #888; font-size: 12px; }

/* 空狀態 */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  background: #fafbf9;
  border-radius: 8px;
  border: 1px dashed #ddd;
}
.empty-icon { font-size: 40px; margin-bottom: 10px; }

.loading-overlay {
  position: absolute; top:0; left:0; width:100%; height:100%;
  background: rgba(255,255,255,0.7); display: flex; justify-content: center; align-items: center;
}
.spinner {
  width: 30px; height: 30px; border: 3px solid #f3f3f3; border-top: 3px solid #557c55; border-radius: 50%; animation: spin 1s linear infinite;
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>