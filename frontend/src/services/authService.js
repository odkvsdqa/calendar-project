
// const authApi = axios.create({
//   // baseURL: 'http://172.20.10.3:8080/calendar-web/api/auth',
//   baseURL:'https://genially-impar-mariela.ngrok-free.dev/calendar-web/api/auth',
//   timeout: 10000,
//   headers: {
//     'Content-Type': 'application/json'
//   }
// })

import api from './api'  // ⚠️ 使用 api.js 的配置

/**
 * 用戶註冊
 */
export function register(registerData) {
  return api.post('/auth/register', registerData)
}

/**
 * 用戶登入
 */
export function login(loginData) {
  return api.post('/auth/login', loginData)
}

/**
 * 用戶登出
 */
export function logout() {
  return api.post('/auth/logout')
}

/**
 * 獲取當前用戶資訊
 */
export function getCurrentUser() {
  return api.get('/auth/me')
}

export default {
  register,
  login,
  logout,
  getCurrentUser
}