import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import i18n from './i18n'

// 🔥 v1.1 新增：引入 CSS 變數系統
import './assets/css/main.css'

const app = createApp(App)

app.use(router)
app.use(i18n)

app.mount('#app')

// 🔥 v1.1 新增：啟動時套用主題（避免白屏閃爍）
const savedTheme = localStorage.getItem('user-theme') || 'system'
const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches

let initialTheme = savedTheme
if (savedTheme === 'system') {
  initialTheme = prefersDark ? 'dark' : 'light'
}

if (initialTheme === 'dark') {
  document.documentElement.setAttribute('data-theme', 'dark')
} else {
  document.documentElement.removeAttribute('data-theme')
}

console.log('✅ 初始主題已套用:', initialTheme)