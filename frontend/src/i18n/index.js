// src/i18n/index.js
import { createI18n } from 'vue-i18n'
import zhTW from './zh-TW.json'
import enUS from './en-US.json'
import jaJP from './ja-JP.json'

/**
 * 獲取初始語言設定
 * 優先順序：localStorage > 瀏覽器語言 > 預設值 (zh-TW)
 * 
 * ⚠️ 注意：這裡只讀取 localStorage，不呼叫後端 API
 * 因為 i18n 初始化時還沒有 Token，無法驗證身份
 * 後端同步會在以下時機進行：
 * 1. 登入成功時 (LoginForm.vue)
 * 2. App 啟動時 (CalendarView.vue 或 App.vue)
 */
function getInitialLocale() {
  // 1. 優先從 localStorage 讀取
  const savedLocale = localStorage.getItem('user-locale')
  if (savedLocale && ['zh-TW', 'en-US', 'ja-JP'].includes(savedLocale)) {
    return savedLocale
  }

  // 2. 嘗試根據瀏覽器語言判斷
  const browserLang = navigator.language || navigator.userLanguage
  if (browserLang.startsWith('zh')) return 'zh-TW'
  if (browserLang.startsWith('ja')) return 'ja-JP'
  if (browserLang.startsWith('en')) return 'en-US'

  // 3. 預設繁體中文
  return 'zh-TW'
}

const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: getInitialLocale(),
  fallbackLocale: 'zh-TW',
  messages: {
    'zh-TW': zhTW,
    'en-US': enUS,
    'ja-JP': jaJP
  }
})

export default i18n