// src/services/api.js
import axios from 'axios'
import { getToken } from '../utils/auth'


// // 🔧 動態獲取後端 API 位址
// const getBaseURL = () => {
//   // 如果是 localhost，使用 localhost
//   if (window.location.hostname === 'localhost') {
//     return 'http://localhost:8080/calendar-web/api'
//   }
//   // 否則使用當前主機的 IP
//   return `http://${window.location.hostname}:8080/calendar-web/api`
// }

// // 創建 axios 實例
// const api = axios.create({
//   baseURL: getBaseURL(),
//   timeout: 10000,
//   headers: {
//     'Content-Type': 'application/json'
//   }
// })

// ⚠️ 使用 ngrok 的後端 URL
const api = axios.create({
  baseURL: 'https://label-saints-dental-assisted.trycloudflare.com/calendar-web/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})


// 請求攔截器 - 自動添加 Token
api.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    //  // ⚠️ 確認有這兩行
    // config.headers['ngrok-skip-browser-warning'] = 'true'
    // config.headers['User-Agent'] = 'MyApp'  // ⚠️ 加上這行

    console.log('發送請求:', config.method.toUpperCase(), config.url)
    return config
  },
  error => {
    console.error('請求錯誤:', error)
    return Promise.reject(error)
  }
)

// 響應攔截器 - 處理 Token 過期
api.interceptors.response.use(
  response => {
    console.log('收到響應:', response.status, response.config.url)
    return response
  },
  error => {
    console.error('響應錯誤:', error.response?.status, error.message)
    
    // Token 過期或未授權
    if (error.response?.status === 401) {
      // 👇 [修改重點] 加入這個判斷！
      // 如果當前網址不包含 '/login'，才執行強制登出跳轉。
      // 這樣當你在登入頁打錯密碼時，就不會被強制刷新了。
      if (!window.location.pathname.includes('/login')) {
        localStorage.removeItem('auth_token')
        localStorage.removeItem('user_info')
        window.location.href = '/login'
      }
    }
    
    return Promise.reject(error)
  }
)

// API 方法
export const eventApi = {
  // 獲取所有事件
  getAllEvents() {
    return api.get('/events')
  },
  
  // 根據ID獲取事件
  getEventById(id) {
    return api.get(`/events/${id}`)
  },
  
  // 根據日期範圍獲取事件
  getEventsByDateRange(startDate, endDate) {
    return api.get('/events/range', {
      params: {
        start: startDate,
        end: endDate
      }
    })
  },
  
  // 根據年月獲取事件
  getEventsByMonth(year, month) {
    return api.get('/events/month', {
      params: { year, month }
    })
  },
  
  // 根據年份獲取事件
  getEventsByYear(year) {
    return api.get('/events/year', {
      params: { year }
    })
  },
  
  // 創建新事件
  createEvent(eventData) {
    return api.post('/events', eventData)
  },
  
  // 更新事件
  updateEvent(id, eventData) {
    return api.put(`/events/${id}`, eventData)
  },
  
  // 刪除事件
  deleteEvent(id) {
    return api.delete(`/events/${id}`)
  }
}

export default api