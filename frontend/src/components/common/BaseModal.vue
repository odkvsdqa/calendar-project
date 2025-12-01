<!-- src/components/common/BaseModal.vue -->
<template>
  <Teleport to="body">
    <div 
      v-if="show" 
      class="modal-overlay" 
      @click.self="handleClose"
      @wheel.stop
    >
      <div class="modal-container" :style="{ maxWidth: width }">
        <!-- 標題區 (可選) -->
        <div v-if="title" class="modal-header">
          <h3>{{ title }}</h3>
          <button class="btn-close" @click="handleClose">×</button>
        </div>
        
        <!-- 內容區 -->
        <div class="modal-body">
          <slot></slot>
        </div>

        <!-- 底部區 (可選) -->
        <div v-if="$slots.footer" class="modal-footer">
          <slot name="footer"></slot>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  show: { type: Boolean, required: true },
  title: { type: String, default: '' },
  width: { type: String, default: '450px' } // 預設寬度
})

const emit = defineEmits(['close'])

const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  /* 統一遮罩風格：深色半透明 */
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease-out;
}

.modal-container {
  background: white;
  width: 90%;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  /* 讓內部可以捲動 */
  overflow: hidden; 
}

.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
  font-weight: 500;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}
.btn-close:hover { color: #333; }

.modal-body {
  padding: 20px;
  overflow-y: auto;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background: #fafbf9;
  flex-shrink: 0;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>