<template>
  <div class="query-section">
    <h2>{{ $t('admin.components.userSchedule.title') }}</h2>
    
    <div class="input-group">
      <label>{{ $t('admin.components.userSchedule.label') }}</label>
      <select v-model="selectedUserId" class="input-styled input-select">
        <option value="">{{ $t('admin.components.userSchedule.placeholder') }}</option>
        <option v-for="user in allUsers" :key="user.id" :value="user.id">
          {{ user.username }} ({{ user.email }})
        </option>
      </select>
      <button @click="fetchUserSchedule" :disabled="!selectedUserId || loading" class="btn-query">
        {{ $t('common.query') }}
      </button>
    </div>
    
    <div v-if="result" class="result-box fade-in">
      <h3>{{ result.username }} {{ $t('admin.components.userSchedule.scheduleOf') }}</h3>
      
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">{{ $t('admin.stats.scheduledDays') }}:</div>
          <div class="stat-number">{{ result.totalScheduleDays }}</div>
        </div>
      </div>

      <h4>{{ $t('admin.components.userSchedule.dateList') }}</h4>
      <div class="schedule-dates">
        <span v-for="date in result.scheduleDates" :key="date" class="date-tag">
          {{ date }}
        </span>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">👤</div>
      <p>{{ $t('admin.components.userSchedule.empty') }}</p>
    </div>

    <div v-if="loading" class="loading-overlay"><div class="spinner"></div></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../../services/adminApi'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const allUsers = ref([])
const selectedUserId = ref('')
const result = ref(null)
const loading = ref(false)

const loadAllUsers = async () => {
  try {
    loading.value = true
    const response = await adminApi.getAllUsers()
    allUsers.value = response.data
  } catch (err) {
    console.error('Failed to load users:', err)
  } finally {
    loading.value = false
  }
}

const fetchUserSchedule = async () => {
  if (!selectedUserId.value) return
  try {
    loading.value = true
    const response = await adminApi.getUserScheduleDates(selectedUserId.value)
    result.value = response.data
  } catch (err) {
    console.error('Query failed:', err)
    alert(t('admin.errors.queryFailed'))
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadAllUsers() })
</script>

<style scoped>
/* 共用樣式 */
.query-section { padding: 10px; }
h2 { font-size: 18px; color: #333; margin-bottom: 20px; font-weight: 600; }

.input-group { display: flex; align-items: center; gap: 15px; margin-bottom: 25px; flex-wrap: wrap; }
.input-styled { padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.input-select { min-width: 200px; }

.btn-query { padding: 8px 20px; background-color: #557c55; color: white; border: none; border-radius: 6px; cursor: pointer; transition: background 0.2s; }
.btn-query:hover { background-color: #446344; }
.btn-query:disabled { background-color: #ccc; }

.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
.stat-card { background: white; border: 1px solid #eee; border-radius: 8px; padding: 20px; text-align: center; box-shadow: 0 2px 5px rgba(0,0,0,0.03); display: flex; flex-direction: column; justify-content: center; align-items: center; }

/* 統一字體 */
.stat-label { font-size: 14px; color: #555; font-weight: bold; margin-bottom: 8px; }
.stat-number { font-size: 28px; color: #557c55; font-weight: 500; line-height: 1.2; }

/* 日期標籤 */
.schedule-dates { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 15px; }
.date-tag { display: inline-block; background: #e8f5e9; border: 1px solid #557c55; color: #557c55; padding: 6px 12px; border-radius: 20px; font-size: 13px; font-weight: 500; }

.empty-state { text-align: center; padding: 40px; color: #999; background: #fafbf9; border-radius: 8px; border: 1px dashed #ddd; }
.empty-icon { font-size: 40px; margin-bottom: 10px; }
.loading-overlay { position: absolute; top:0; left:0; width:100%; height:100%; background: rgba(255,255,255,0.7); display: flex; justify-content: center; align-items: center; }
.spinner { width: 30px; height: 30px; border: 3px solid #f3f3f3; border-top: 3px solid #557c55; border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>