<template>
  <div class="modal-overlay" @click.self="handleClose">
    <div class="modal-content">
      <h2>{{ modalTitle }}</h2>
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>標題 *</label>
          <input type="text" v-model="localForm.title" required minlength="2" maxlength="100" placeholder="輸入事件標題">
          <small>{{ localForm.title?.length || 0 }} / 100 字</small>
        </div>

        <!-- 🔥 新增：預計花費 -->
        <div class="form-group">
          <label>預計花費 (選填)</label>
          <div class="cost-input-wrapper">
             <span class="currency">NT$</span>
             <input type="number" v-model="localForm.estimatedCost" min="0" placeholder="0" class="cost-input">
          </div>
        </div>

        <div class="form-group">
          <label>描述</label>
          <textarea v-model="localForm.description" rows="3" placeholder="輸入事件描述"></textarea>
        </div>

        <div class="form-group">
          <label>開始時間 *</label>
          <input type="datetime-local" v-model="localForm.startTime" required>
        </div>
        <div class="form-group">
          <label>結束時間 *</label>
          <input type="datetime-local" v-model="localForm.endTime" required>
        </div>

        <div class="form-group">
          <label>顏色</label>
          <div class="color-picker">
            <div v-for="color in colorOptions" :key="color.value" class="color-option" :class="{ active: localForm.color === color.value }" :style="{ backgroundColor: color.value }" @click="localForm.color = color.value">
              <span v-if="localForm.color === color.value" class="check">✓</span>
            </div>
          </div>
        </div>

        <div class="form-buttons">
          <button type="button" class="btn-cancel" @click="handleClose">取消</button>
          <button v-if="localForm.id" type="button" class="btn-delete" @click="handleDeleteClick">刪除</button>
          <button type="submit" class="btn-save">儲存</button>
        </div>
      </form>

      <!-- 刪除確認 (保留你原有的) -->
      <div v-if="showDeleteConfirm" class="confirm-overlay">
        <div class="confirm-box">
          <h3>⚠️ 警告</h3>
          <p>確定要刪除「{{ localForm.title }}」嗎？<br>此動作無法復原。</p>
          <div class="confirm-actions">
            <button class="btn-cancel-confirm" @click="showDeleteConfirm = false">取消</button>
            <button class="btn-confirm-delete" @click="confirmDelete">確認刪除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useToast } from '../composables/useToast'

const props = defineProps({
  eventForm: Object,
  modalTitle: String
})
const emit = defineEmits(['close', 'save', 'delete'])
const { showToast } = useToast()
const showDeleteConfirm = ref(false)

const colorOptions = [
  { value: '#7c8db5', name: '灰藍' },
  { value: '#bfaac1', name: '藕紫' },
  { value: '#9cb094', name: '抹茶' }
]

const localForm = ref({
  id: null, title: '', description: '', startTime: '', endTime: '', color: '#557c55',
  estimatedCost: null // 🔥
})

const syncFormData = () => {
  if (props.eventForm) {
    localForm.value = {
      ...props.eventForm,
      // 確保即使沒傳也能預設
      estimatedCost: props.eventForm.estimatedCost || null 
    }
  }
}

// 驗證邏輯 (保留你原有的)
const validateForm = () => {
  const title = localForm.value.title?.trim() || ''
  if (title.length < 2 || title.length > 100) return '標題長度必須在 2 到 100 個字元之間'
  if (!localForm.value.startTime || !localForm.value.endTime) return '請完整選擇開始與結束時間'
  if (new Date(localForm.value.startTime) >= new Date(localForm.value.endTime)) return '結束時間必須晚於開始時間'
  return null
}

const handleClose = () => emit('close')
const handleSubmit = () => {
  const error = validateForm()
  if (error) { showToast(error, 'error'); return }
  emit('save', localForm.value)
}
const handleDeleteClick = () => showDeleteConfirm.value = true
const confirmDelete = () => { showDeleteConfirm.value = false; emit('delete', localForm.value.id) }

syncFormData()
watch(() => props.eventForm, syncFormData, { deep: true })
</script>

<style scoped>
/* 繼承你原有的日系 CSS */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 1000; display: flex; align-items: center; justify-content: center; animation: fadeIn 0.2s; }
.modal-content { background: white; padding: 30px; border-radius: 2px; width: 90%; max-width: 500px; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3); max-height: 90vh; overflow-y: auto; }
.modal-content h2 { margin-bottom: 20px; color: #333; font-weight: 500; font-size: 20px; letter-spacing: 0.05em; }

.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; color: #555; font-size: 13px; letter-spacing: 0.03em; }
.form-group input, .form-group textarea { width: 100%; padding: 10px 12px; border: 1px solid #e0e0e0; border-radius: 2px; font-size: 14px; transition: border-color 0.2s; font-family: inherit; }
.form-group input:focus, .form-group textarea:focus { outline: none; border-color: #667eea; }
.form-group small { color: #999; font-size: 12px; display: block; margin-top: 4px; }

/* 🔥 新增：金額輸入框樣式 */
.cost-input-wrapper { position: relative; display: flex; align-items: center; }
.currency { position: absolute; left: 12px; color: #999; font-size: 13px; pointer-events: none; }
.cost-input { padding-left: 42px !important; }

/* 顏色選擇與按鈕 (保留原樣) */
.color-picker { display: flex; gap: 10px; }
.color-option { width: 40px; height: 40px; border-radius: 2px; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; justify-content: center; border: 2px solid transparent; }
.color-option:hover { transform: scale(1.1); }
.color-option.active { border-color: #333; }
.check { color: white; font-weight: bold; font-size: 18px; }

.form-buttons { display: flex; gap: 10px; justify-content: flex-end; margin-top: 24px; }
.form-buttons button { padding: 10px 20px; border: none; border-radius: 2px; cursor: pointer; font-size: 13px; transition: all 0.2s; font-family: inherit; letter-spacing: 0.05em; }
.btn-save { background: #333; color: white; }
.btn-cancel { background: #f3f4f6; color: #666; border: 1px solid #e0e0e0; }
.btn-delete { background: white; color: #ef4444; border: 1px solid #ef4444; }

.confirm-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.2); border-radius: 2px; display: flex; justify-content: center; align-items: center; z-index: 50; }
.confirm-box { background: white; width: 280px; padding: 24px; border-radius: 2px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2); text-align: center; }
.confirm-box h3 { color: #ef4444; font-size: 18px; margin-bottom: 8px; }
.confirm-actions { display: flex; justify-content: center; gap: 12px; }
.btn-cancel-confirm { background: #f3f4f6; color: #666; border: 1px solid #e0e0e0; padding: 8px 16px; border-radius: 2px; border: none; }
.btn-confirm-delete { background: #ef4444; color: white; padding: 8px 16px; border-radius: 2px; border: none; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
</style>