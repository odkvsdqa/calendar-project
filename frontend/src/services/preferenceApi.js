// src/services/preferenceApi.js
import api from './api'

/**
 * 使用者偏好設定 API
 * 用於同步語言偏好至後端資料庫
 */
export const preferenceApi = {
  /**
   * 獲取當前使用者的語言偏好
   * @returns {Promise} { language: 'zh-TW' }
   */
  getLanguage() {
    return api.get('/preferences/language')
  },

  /**
   * 更新當前使用者的語言偏好
   * @param {string} language - 語言代碼 (zh-TW, en-US, ja-JP)
   * @returns {Promise} { language: 'en-US' }
   */
  updateLanguage(language) {
    console.log("📤 送出語言為：", language, typeof language)
    return api.put('/preferences/language', { language})
  }
}