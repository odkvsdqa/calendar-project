// src/utils/auth.js

const TokenKey = 'auth-token'
const UserKey = 'user-info'

export function getToken() {
  return localStorage.getItem(TokenKey)
}

export function setToken(token) {
  return localStorage.setItem(TokenKey, token)
}

export function removeToken() {
  return localStorage.removeItem(TokenKey)
}

export function setUserInfo(user) {
  return localStorage.setItem(UserKey, JSON.stringify(user))
}

export function getUserInfo() {
  const user = localStorage.getItem(UserKey)
  return user ? JSON.parse(user) : null
}

export function removeUserInfo() {
  return localStorage.removeItem(UserKey)
}

// 如果您原本有這個函式，請保留
export function clearAuth() {
  removeToken()
  removeUserInfo()
}

export function isAuthenticated() {
  return !!getToken()
}