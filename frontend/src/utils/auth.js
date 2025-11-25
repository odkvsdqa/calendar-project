// Token 相關操作

const TOKEN_KEY = 'auth_token'
const USER_KEY = 'user_info'

/**
 * 保存 Token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 獲取 Token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 移除 Token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 保存用戶資訊
 */
export function setUserInfo(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

/**
 * 獲取用戶資訊
 */
export function getUserInfo() {
  const userStr = localStorage.getItem(USER_KEY)
  return userStr ? JSON.parse(userStr) : null
}

/**
 * 移除用戶資訊
 */
export function removeUserInfo() {
  localStorage.removeItem(USER_KEY)
}

/**
 * 檢查是否已登入
 */
export function isAuthenticated() {
  return !!getToken()
}

/**
 * 清除所有認證資訊（登出）
 */
export function clearAuth() {
  removeToken()
  removeUserInfo()
}

