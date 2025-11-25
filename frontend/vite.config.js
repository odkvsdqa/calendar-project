import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  // base: '/calendar-web/', // <--- 加入這一行 (注意前後有斜線)
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,

     // ⚠️ 加上這個（新增）
    allowedHosts: [
      '.loca.lt',
      '.trycloudflare.com',
      '.ngrok-free.app',
      '.ngrok.io',
      'localhost',
      'split-mario-highway-disclaimer.trycloudflare.com'
    ],
    // proxy: {
    //   '/calendar-web/api': {
    //     target: 'http://localhost:8080',
    //     changeOrigin: true,
    //     secure: false
    //   }
    // }
  }
})