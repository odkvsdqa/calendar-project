/**
 * 使用者偏好設定 API
 * v1.1 擴充：主題、時區管理
 */
import api from './api'

export const preferenceApi = {
  
  // === 原有 API（向後相容）===
  
  /**
   * 獲取使用者語言偏好
   * GET /api/preferences/language
   */
  getLanguage() {
    return api.get('/preferences/language')
  },
  
  /**
   * 更新使用者語言偏好
   * PUT /api/preferences/language
   */
  updateLanguage(language) {
    return api.put('/preferences/language', { language })
  },
  
  // 🔥 v1.1 新增 API
  
  /**
   * 獲取完整偏好設定
   * GET /api/preferences
   * @returns {Promise} { language, theme, timezone }
   */
  getPreferences() {
    return api.get('/preferences')
  },
  
  /**
   * 更新完整偏好設定
   * PUT /api/preferences
   * @param {Object} preferences - { language, theme, timezone }
   */
  updatePreferences(preferences) {
    return api.put('/preferences', preferences)
  },
  
  /**
   * 獲取支援的時區清單
   * GET /api/preferences/timezones
   */
  getSupportedTimezones() {
    return api.get('/preferences/timezones')
  }
}