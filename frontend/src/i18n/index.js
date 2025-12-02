// src/i18n/index.js
import { createI18n } from 'vue-i18n'
import zhTW from './zh-TW.json'
import enUS from './en-US.json'
import jaJP from './ja-JP.json'

// 🔥 1. 嘗試從 localStorage 讀取使用者上次選擇的語言
const savedLocale = localStorage.getItem('user-locale')

// 🔥 2. 決定預設語言：有紀錄用紀錄，沒紀錄用 'zh-TW'
const defaultLocale = savedLocale || 'zh-TW'

const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: defaultLocale, // 🔥 使用讀取到的語言
  fallbackLocale: 'en-US',
  globalInjection: true,
  messages: {
    'zh-TW': zhTW,
    'en-US': enUS,
    'ja-JP': jaJP
  }
})

export default i18n