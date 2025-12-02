// src/utils/errorHandle.js
import i18n from '../i18n' // 引入我們建立的 i18n 實體

// 取得全域翻譯函式
const { t } = i18n.global

export function handleApiError(error, customMessage) {
  if (error.response?.status === 401) {
    return t('errors.sessionExpired')
  } else if (error.response?.status === 403) {
    return t('errors.forbidden')
  } else if (error.response?.status === 404) {
    return t('errors.notFound')
  } else if (error.response?.status >= 500) {
    return t('errors.serverError')
  }
  
  // 如果有後端回傳的 message 就用後端的，否則用預設錯誤，或自訂訊息
  return error.response?.data?.message || customMessage || t('errors.default')
}