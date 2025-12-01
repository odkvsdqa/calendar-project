<!-- src/components/common/LoadingOverlay.vue -->
<template>
  <div v-if="visible" class="loading-overlay">
    <div class="spinner"></div>
    <div v-if="text" class="loading-text">{{ text }}</div>
  </div>
</template>

<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  text: { type: String, default: '' } // 可選文字，例如 "登入中..."
})
</script>

<style scoped>
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* 統一半透明背景色 */
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(1px); /* 增加一點質感 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 50;
  gap: 10px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #557c55; /* 使用您的森綠色 */
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  color: #557c55;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.05em;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>