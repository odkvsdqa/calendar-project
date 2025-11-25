<template>
  <div class="modal-overlay" @click.self="handleClose">
    <div class="modal-content">
      <h2>{{ modalTitle }}</h2>
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>標題 *</label>
          <input 
            type="text" 
            v-model="localForm.title" 
            required 
            placeholder="輸入事件標題"
          >
        </div>
        <div class="form-group">
          <label>描述</label>
          <textarea 
            v-model="localForm.description" 
            rows="3" 
            placeholder="輸入事件描述（選填）"
          ></textarea>
        </div>
        <div class="form-group">
          <label>開始時間 *</label>
          <input 
            type="datetime-local" 
            v-model="localForm.startTime" 
            required
          >
        </div>
        <div class="form-group">
          <label>結束時間 *</label>
          <input 
            type="datetime-local" 
            v-model="localForm.endTime" 
            required
          >
        </div>
        <div class="form-buttons">
          <button type="button" class="btn-cancel" @click="handleClose">
            取消
          </button>
          <button 
            v-if="localForm.id" 
            type="button" 
            class="btn-delete" 
            @click="handleDeleteClick"
          >
            刪除
          </button>
          <button type="submit" class="btn-save">
            儲存
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  eventForm: {
    type: Object,
    default: () => ({
      id: null,
      title: '',
      description: '',
      startTime: '',
      endTime: '',
      color: '#667eea'
    })
  },
  modalTitle: {
    type: String,
    default: '新增事件'
  }
})

const emit = defineEmits(['close', 'save', 'delete'])

const localForm = ref({
  id: null,
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  color: '#667eea'
})

const syncFormData = () => {
  if (props.eventForm) {
    localForm.value = {
      id: props.eventForm.id || null,
      title: props.eventForm.title || '',
      description: props.eventForm.description || '',
      startTime: props.eventForm.startTime || '',
      endTime: props.eventForm.endTime || '',
      color: props.eventForm.color || '#667eea'
    }
  }
}

// 初始化時同步
syncFormData()

// 監聽 props 變化
watch(() => props.eventForm, () => {
  syncFormData()
}, { deep: true })

const handleClose = () => {
  emit('close')
}

const handleSubmit = () => {
  emit('save', localForm.value)
}

const handleDeleteClick = () => {
  emit('delete', localForm.value.id)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #555;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.form-buttons button {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
  font-family: inherit;
}

.btn-save {
  background: #10b981;
  color: white;
}

.btn-save:hover {
  background: #059669;
}

.btn-cancel {
  background: #6b7280;
  color: white;
}

.btn-cancel:hover {
  background: #4b5563;
}

.btn-delete {
  background: #ef4444;
  color: white;
}

.btn-delete:hover {
  background: #dc2626;
}
</style>