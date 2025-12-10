// src/services/adminApi.js
import api from './api'

/**
 * 管理員 API 服務
 */
export const adminApi = {
  /**
   * 獲取所有用戶列表
   */
  getAllUsers() {
    return api.get('/admin/users')
  },

  /**
   * 獲取特定日期有排程的用戶
   * @param {string} date - 日期 (格式: YYYY-MM-DD)
   */
  getUsersByDate(date) {
    return api.get('/admin/users-by-date', {
      params: { date }
    })
  },

  /**
   * 獲取特定月份的統計數據
   * @param {number} year - 年份
   * @param {number} month - 月份
   */
  getMonthlyStats(year, month) {
    return api.get('/admin/monthly-stats', {
      params: { year, month }
    })
  },

  /**
   * 獲取特定時間範圍的統計數據
   * @param {string} start - 開始時間 (ISO 格式)
   * @param {string} end - 結束時間 (ISO 格式)
   */
  getTimeRangeStats(start, end) {
    return api.get('/admin/time-range-stats', {
      params: { start, end }
    })
  },

  /**
   * 獲取特定用戶的所有排程日期
   * @param {string} userId - 用戶ID
   */
  getUserScheduleDates(userId) {
    return api.get(`/admin/user-schedule-dates/${userId}`)
  },

  getExchangeRates() {
    return api.get('/admin/exchange-rates')
  },

  updateExchangeRates() {
    return api.post('/admin/update-exchange-rates')
  }

}

export default adminApi