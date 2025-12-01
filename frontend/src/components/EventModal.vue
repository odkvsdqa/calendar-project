<!-- src/components/EventModal.vue -->
<template>
  <!-- 
    1. 主編輯視窗 
    因為父組件 (CalendarApp) 已經用 v-if="showModal" 控制這個組件的掛載，
    所以這裡的 show 直接設為 true 即可。
    width="450px" 是為了維持您原本的寬度設定。
  -->
  <BaseModal 
    :show="true" 
    :title="modalTitle" 
    width="450px"
    @close="handleClose"
  >
    <!-- 內容區：直接放原本的 Form -->
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>標題</label>
        <input type="text" v-model="localForm.title" required placeholder="請輸入標題..." class="input-styled">
      </div>

      <div class="form-group">
        <label>預計花費</label>
        <div class="cost-input-wrapper">
          <span class="currency-symbol">NT$</span>
          <input 
            type="number" 
            v-model="localForm.estimatedCost" 
            min="0" 
            placeholder="0"
            class="input-styled cost-input"
          >
        </div>
      </div>

      <div class="form-group">
        <label>描述</label>
        <textarea v-model="localForm.description" rows="3" placeholder="備註..." class="input-styled"></textarea>
      </div>

      <div class="form-row">
        <div class="form-group half">
          <label>開始</label>
          <input type="datetime-local" v-model="localForm.startTime" required class="input-styled">
        </div>
        <div class="form-group half">
          <label>結束</label>
          <input type="datetime-local" v-model="localForm.endTime" required class="input-styled">
        </div>
      </div>

      <div class="form-group">
        <label>標籤顏色</label>
        <div class="color-picker">
          <div v-for="color in colorOptions" :key="color.value" 
            class="color-option" 
            :class="{ active: localForm.color === color.value }"
            :style="{ backgroundColor: color.value }"
            @click="localForm.color = color.value">
            <span v-if="localForm.color === color.value" class="check">✓</span>
          </div>
        </div>
      </div>

      <!-- 底部按鈕區 (維持您原本的排版：刪除在左，操作在右) -->
      <div class="form-footer">
        <!-- 刪除按鈕放左邊 -->
        <button v-if="localForm.id" type="button" class="btn-delete" @click="handleDeleteClick">刪除</button>
        <!-- 右邊佔位 (如果沒有刪除按鈕，flex會把右邊的按鈕推到底嗎？需要 spacer 或 margin-left: auto) -->
        <div v-else></div> 

        <!-- 右邊放操作按鈕 -->
        <div class="action-buttons">
          <button type="button" class="btn-cancel" @click="handleClose">取消</button>
          <button type="submit" class="btn-save">儲存</button>
        </div>
      </div>
    </form>
  </BaseModal>

  <!-- 
    2. 刪除確認視窗 (這是一個獨立的 Modal) 
    這樣寫比原本嵌套在 div 裡更乾淨，Z-index 層級也由 BaseModal 自動處理
  -->
  <BaseModal
    :show="showDeleteConfirm"
    title="確認刪除"
    width="320px"
    @close="showDeleteConfirm = false"
  >
    <div class="confirm-content">
      <p>確定要刪除此事件嗎？</p>
      <div class="confirm-actions">
        <button class="btn-cancel" @click="showDeleteConfirm = false">取消</button>
        <button class="btn-delete-confirm" @click="confirmDelete">確認</button>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseModal from './common/BaseModal.vue' // 記得引入剛剛建立的 BaseModal

const props = defineProps({
  eventForm: Object,
  modalTitle: { type: String, default: '新增事件' }
})
const emit = defineEmits(['close', 'save', 'delete'])
const showDeleteConfirm = ref(false)

const colorOptions = [
  { value: '#557c55', name: '森綠' },
  { value: '#7c8db5', name: '霧藍' },
  { value: '#bfaac1', name: '藕紫' },
  { value: '#b0a496', name: '亞麻' }
]

const localForm = ref({
  id: null, title: '', description: '', startTime: '', endTime: '', 
  color: '#557c55', estimatedCost: null
})

const syncFormData = () => {
  if (props.eventForm) {
    localForm.value = {
      ...props.eventForm,
      color: props.eventForm.color || '#557c55',
      estimatedCost: props.eventForm.estimatedCost || null
    }
  }
}

const handleClose = () => emit('close')
const handleSubmit = () => emit('save', localForm.value)
const handleDeleteClick = () => showDeleteConfirm.value = true
const confirmDelete = () => { showDeleteConfirm.value = false; emit('delete', localForm.value.id) }

syncFormData()
watch(() => props.eventForm, syncFormData, { deep: true })
</script>

<style scoped>
/* 
  ❌ 移除了大量 CSS：
  - .modal-overlay
  - .modal-content
  - .modal-header
  - .btn-close-x
  - .confirm-overlay
  - .confirm-box
  
  ✅ 只保留表單內部的樣式
*/

.form-group { margin-bottom: 18px; }
.form-group label { display: block; margin-bottom: 6px; font-size: 13px; color: #666; font-weight: 500; }

.input-styled {
  width: 100%; 
  padding: 8px; 
  border: 1px solid #e0e0e0; 
  border-radius: 4px;
  font-size: 13px; 
  color: #333; 
  transition: border-color 0.2s;
  box-sizing: border-box; 
  font-family: inherit;
}
.input-styled:focus { outline: none; border-color: #557c55; }

/* 金額輸入框 */
.cost-input-wrapper { position: relative; }
.currency-symbol { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #999; font-size: 13px; }
.cost-input { padding-left: 45px; }

/* 雙欄 */
.form-row { display: flex; gap: 8px; }
.half { flex: 1; }

/* 顏色選擇 */
.color-picker { display: flex; gap: 10px; }
.color-option {
  width: 32px; height: 32px; border-radius: 50%; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  border: 2px solid transparent; transition: transform 0.2s;
}
.color-option:hover { transform: scale(1.1); }
.color-option.active { border-color: #555; }
.check { color: white; font-size: 14px; }

/* 底部按鈕區 */
.form-footer {
  margin-top: 30px; 
  display: flex; 
  justify-content: space-between; 
  align-items: center;
}
.action-buttons { display: flex; gap: 10px; }

button {
  padding: 8px 20px; border-radius: 4px; border: none; cursor: pointer;
  font-size: 13px; letter-spacing: 0.05em; transition: all 0.2s;
}
.btn-save { background: #557c55; color: white; }
.btn-save:hover { background: #446344; }
.btn-cancel { background: #f3f4f6; color: #666; border: 1px solid #e0e0e0; }
.btn-cancel:hover { background: #e5e7eb; }
.btn-delete { color: #d98e8e; background: transparent; padding-left: 0; }
.btn-delete:hover { color: #c06060; text-decoration: underline; }

/* 刪除確認窗內部的排版 */
.confirm-content { text-align: center; }
.confirm-content p { color: #666; margin-bottom: 24px; }
.confirm-actions { display: flex; justify-content: center; gap: 12px; }
.btn-delete-confirm { background: #d98e8e; color: white; }
</style>