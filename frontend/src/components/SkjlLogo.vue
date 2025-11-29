<template>
  <div class="logo-container" :class="{ 'vertical': layout === 'vertical' }">
    <!-- SVG 圖標 -->
    <svg 
      class="logo-icon" 
      viewBox="0 0 100 100" 
      xmlns="http://www.w3.org/2000/svg"
    >
      <defs>
        <linearGradient id="skjl-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
      <!-- 方案 A: 抹茶綠 -->
      <stop offset="0%" style="stop-color:#3a5335; stop-opacity:1" />
      <stop offset="100%" style="stop-color:#7a9e60; stop-opacity:1" />
      <!-- 備用方案 B: 藍染 -->
      <!-- <stop offset="0%" style="stop-color:#2c3e50; stop-opacity:1" /> -->
      <!-- <stop offset="100%" style="stop-color:#4ca1af; stop-opacity:1" /> -->
        </linearGradient>
      </defs>

      <!-- 👇 修改: 判斷 mode，如果是 white 就用白色，否則用漸層 -->
      <path 
        d="M20 25 H80 L65 45" 
        :stroke="mode === 'white' ? 'white' : 'url(#skjl-gradient)'" 
        stroke-width="8" 
        stroke-linecap="round" 
        stroke-linejoin="round"
        fill="none"
      />

      <path 
        d="M45 45 V65 C45 80 30 80 25 70" 
        :stroke="mode === 'white' ? 'white' : 'url(#skjl-gradient)'" 
        stroke-width="8" 
        stroke-linecap="round" 
        fill="none"
      />

      <!-- 圓點：如果是白色模式，也改白色，或者保持粉色跳色？建議改白色比較清楚 -->
      <circle cx="75" cy="75" r="6" :fill="mode === 'white' ? 'white' : '#7a9e60'" />
    </svg>

    <!-- 文字部分 -->
    <div class="logo-text">
      <!-- 👇 修改: 加入 class 綁定 -->
      <h1 class="brand-name" :class="{ 'text-white': mode === 'white' }">SKJL</h1>
      <span v-if="showTagline" class="brand-tagline" :class="{ 'text-white': mode === 'white' }">予定</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  layout: {
    type: String,
    default: 'horizontal'
  },
  showTagline: {
    type: Boolean,
    default: true
  },
  // 👇 新增這個屬性
  mode: {
    type: String,
    default: 'default' // 'default' (漸層) 或 'white' (全白)
  }
})
</script>

<style scoped>
/* ... 原有樣式保持不變 ... */

.logo-container {
  display: flex;
  align-items: center;
  gap: 15px;
  user-select: none;
}

.logo-container.vertical {
  flex-direction: column;
  text-align: center;
}

.logo-icon {
  width: 60px;
  height: 60px;
  filter: drop-shadow(0 4px 6px rgba(102, 126, 234, 0.25));
  transition: transform 0.3s ease;
}

.logo-container:hover .logo-icon {
  transform: scale(1.05) rotate(-2deg);
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1;
}

.brand-name {
  font-family: 'Montserrat', 'Helvetica Neue', sans-serif;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 4px;
  
   /* 👇 CSS 這裡的漸層也要同步修改，文字才會變色 */
  background: linear-gradient(135deg, #3a5335 0%, #7a9e60 100%);
  
  /* 讓背景只顯示在文字形狀上 */
  -webkit-background-clip: text;
  background-clip: text;
  
  /* 讓文字本身變透明，透出後面的背景 */
  -webkit-text-fill-color: transparent;
  color: transparent;
  
  margin: 0;
  
  /* 建議加入這一行，確保 block 渲染正常 */
  display: inline-block; 
}

/* 👇 修改這裡：白色模式 - 強制重置所有漸層屬性 */
.brand-name.text-white {
  background: none !important; /* 移除漸層背景 */
  -webkit-background-clip: unset !important; /* 取消裁剪 */
  background-clip: unset !important;
  -webkit-text-fill-color: white !important; /* 強制填滿白色 */
  color: white !important; /* 設定字體顏色 */
}

.brand-tagline {
  font-size: 14px;
  color: #888;
  letter-spacing: 8px;
  margin-top: 4px;
  font-weight: 500;
  margin-left: 4px;
}

/* 副標題也要確保變白 */
.brand-tagline.text-white {
  color: rgba(255, 255, 255, 0.8) !important;
}
</style>