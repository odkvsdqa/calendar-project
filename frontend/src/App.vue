<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Microsoft JhengHei', 'Segoe UI', Arial, sans-serif;
  background: #f5f5f5;
  min-height: 100vh;
}

#app {
  min-height: 100vh;
}

/* 確保根元素佔滿畫面，背景色延伸 */
html, body, #app {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  /* 🔥 統一背景色，避免縮小時出現白邊 */
  background-color: #f3f4f6; 
}
</style>