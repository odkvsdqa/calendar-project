<template>
  <div class="skeleton-container">
    <!-- 控制列骨架 -->
    <div class="skeleton-controls">
      <div class="skeleton-btn-group">
        <div class="skeleton-btn"></div>
        <div class="skeleton-btn"></div>
        <div class="skeleton-btn"></div>
      </div>
      <div class="skeleton-title"></div>
      <div class="skeleton-right">
        <div class="skeleton-btn-group">
          <div class="skeleton-btn"></div>
          <div class="skeleton-btn"></div>
          <div class="skeleton-btn"></div>
        </div>
        <div class="skeleton-btn-primary"></div>
      </div>
    </div>

    <!-- 星期標題骨架 -->
    <div class="skeleton-weekdays">
      <div v-for="i in 7" :key="i" class="skeleton-weekday"></div>
    </div>

    <!-- 日曆網格骨架 -->
    <div class="skeleton-grid">
      <div v-for="i in 42" :key="i" class="skeleton-cell"></div>
    </div>
  </div>
</template>

<style scoped>
/* 日系極簡骨架屏 */
.skeleton-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  overflow: hidden;
}

/* 控制列骨架 */
.skeleton-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 40px;
  border-bottom: 1px solid #f5f5f5;
  flex-shrink: 0;
}

.skeleton-btn-group {
  display: flex;
  gap: 10px;
}

.skeleton-btn {
  width: 60px;
  height: 32px;
  background: #f0f0f0;
  border-radius: 2px;
  animation: pulse 1.5s ease-in-out infinite;
}

.skeleton-title {
  width: 150px;
  height: 28px;
  background: #f0f0f0;
  border-radius: 2px;
  animation: pulse 1.5s ease-in-out infinite;
}

.skeleton-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.skeleton-btn-primary {
  width: 80px;
  height: 36px;
  background: #e8e8e8;
  border-radius: 2px;
  animation: pulse 1.5s ease-in-out infinite;
}

/* 星期標題骨架 */
.skeleton-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  border-bottom: 1px solid #f5f5f5;
  flex-shrink: 0;
  padding: 10px 0;
}

.skeleton-weekday {
  height: 20px;
  margin: 0 auto;
  width: 40px;
  background: #f5f5f5;
  border-radius: 2px;
  animation: pulse 1.5s ease-in-out infinite;
}

/* 日曆網格骨架 */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-auto-rows: minmax(90px, auto);
  flex: 1;
  border-left: 1px solid #f5f5f5;
  overflow: hidden;
}

.skeleton-cell {
  border-right: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
  padding: 8px;
  background: white;
  position: relative;
}

.skeleton-cell::before {
  content: '';
  position: absolute;
  top: 8px;
  left: 8px;
  width: 24px;
  height: 24px;
  background: #f5f5f5;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

/* 脈動動畫 */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* RWD */
@media (max-width: 768px) {
  .skeleton-controls {
    padding: 8px 10px;
    flex-wrap: wrap;
  }
  
  .skeleton-title {
    width: 100px;
  }
  
  .skeleton-btn {
    width: 40px;
    height: 28px;
  }
}
</style>