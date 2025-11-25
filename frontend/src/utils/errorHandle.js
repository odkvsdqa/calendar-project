// ✅ 統一錯誤處理
// utils/errorHandler.js
export function handleApiError(error, customMessage) {
  if (error.response?.status === 401) {
    return '登入已過期，請重新登入'
  } else if (error.response?.status === 403) {
    return '沒有權限執行此操作'
  } else if (error.response?.status === 404) {
    return '找不到資源'
  } else if (error.response?.status >= 500) {
    return '伺服器錯誤，請稍後再試'
  }
  return customMessage || error.response?.data?.message || '操作失敗'
}
