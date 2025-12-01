<template>
  <!-- 🔥 加入 @wheel.stop，這樣滑鼠在彈窗上滾動時，不會觸發底下的換頁 -->
  <div class="modal-overlay" @click.self="handleClose" @wheel.stop>
    <div class="modal-content">
      
      <!-- 🔥 新增：標題列 (包含 X 按鈕) -->
      <div class="modal-header">
        <h2>{{ modalTitle }}</h2>
        <button class="btn-close-x" @click="handleClose">×</button>
      </div>

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

        <div class="form-footer">
          <!-- 刪除按鈕放左邊 -->
          <button v-if="localForm.id" type="button" class="btn-delete" @click="handleDeleteClick">刪除</button>
          
          <!-- 右邊放操作按鈕 -->
          <div class="action-buttons">
            <button type="button" class="btn-cancel" @click="handleClose">取消</button>
            <button type="submit" class="btn-save">儲存</button>
          </div>
        </div>
      </form>

      <!-- 刪除確認彈窗 (樣式微調) -->
      <div v-if="showDeleteConfirm" class="confirm-overlay">
        <div class="confirm-box">
          <h3>確認刪除</h3>
          <p>確定要刪除此事件嗎？</p>
          <div class="confirm-actions">
            <button class="btn-cancel" @click="showDeleteConfirm = false">取消</button>
            <button class="btn-delete-confirm" @click="confirmDelete">確認</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

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
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.4); /* 半透明遮罩 */
  backdrop-filter: blur(2px);
  z-index: 1000; display: flex; align-items: center; justify-content: center;
}

/* 1. 給彈窗更多內部空間 (左右留白改小一點) */
.modal-content {
  background: #fff;
  /* 🔥 修改：原本 padding 是 30px，左右改成 20px，這樣輸入框就有更多空間伸展 */
  padding: 30px 20px; 
  width: 90%; 
  max-width: 450px;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  position: relative;
  max-height: 90vh;
  overflow-y: auto;
  /* 防止橫向捲軸出現 */
  overflow-x: hidden; 
}

/* Header 樣式 */
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 25px; border-bottom: 1px solid #eee; padding-bottom: 15px;
}
.modal-header h2 {
  margin: 0; font-size: 18px; color: #333; font-weight: 500; letter-spacing: 0.05em;
}
/* X 按鈕 */
.btn-close-x {
  background: transparent; border: none; font-size: 24px; color: #999;
  cursor: pointer; line-height: 1; padding: 0 5px;
  transition: color 0.2s;
}
.btn-close-x:hover { color: #333; }

/* 表單樣式優化 */
.form-group { margin-bottom: 18px; }
.form-group label { display: block; margin-bottom: 6px; font-size: 13px; color: #666; font-weight: 500; }

/* 3. 讓輸入框變「瘦」一點，避免太寬 */
.input-styled {
  width: 100%; 
  /* 🔥 修改：原本 padding 是 10px 12px，改成 8px 8px */
  padding: 8px; 
  
  border: 1px solid #e0e0e0; 
  border-radius: 4px;
  
  /* 🔥 修改：字體稍微改小 1px，瀏覽器的日期選單就會自動縮小寬度 */
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

/* 2. 縮小兩個輸入框中間的縫隙 */
.form-row { 
  display: flex; 
  /* 🔥 修改：原本是 15px，改成 8px，讓右邊的框框往左靠 */
  gap: 8px; 
}
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
  margin-top: 30px; display: flex; justify-content: space-between; align-items: center;
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

/* 確認彈窗 */
.confirm-overlay { position: absolute; top:0; left:0; width:100%; height:100%; background: rgba(255,255,255,0.95); display: flex; align-items: center; justify-content: center; z-index: 10; border-radius: 8px;}
.confirm-box { text-align: center; }
.confirm-box h3 { margin: 0 0 10px 0; color: #d98e8e; }
.confirm-box p { color: #666; margin-bottom: 20px; }
.btn-delete-confirm { background: #d98e8e; color: white; }
</style>