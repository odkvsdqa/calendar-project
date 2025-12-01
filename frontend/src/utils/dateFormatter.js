// src/utils/dateFormatter.js

/**
 * 格式化為 datetime-local 格式 (YYYY-MM-DDTHH:mm)
 */
export function formatDateTimeLocal(date) {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

/**
 * 格式化為日期 (YYYY-MM-DD)
 */
export function formatDate(date) {
  return new Date(date).toISOString().slice(0, 10)
}

/**
 * 格式化為時間 (HH:mm)
 */
export function formatTime(dateTime) {
  const date = new Date(dateTime)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

export function formatToIsoString(dateInput) {
  const d = new Date(dateInput)
  const pad = (num) => String(num).padStart(2, '0')
  
  const year = d.getFullYear()
  const month = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  const hours = pad(d.getHours())
  const minutes = pad(d.getMinutes())
  const seconds = pad(d.getSeconds())
  
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

// ✅ [新增] 判斷兩個日期是否為同一天 (忽略時間)
export function isSameDay(date1, date2) {
  const d1 = new Date(date1)
  const d2 = new Date(date2)
  return d1.getFullYear() === d2.getFullYear() &&
         d1.getMonth() === d2.getMonth() &&
         d1.getDate() === d2.getDate()
}

/**
 * 🔥 [核彈級修復] 安全日期解析 v2
 * 解決 iOS Safari 對字串解析的所有疑難雜症 (UTC/Local 偏移問題)
 * 強制使用本地時間建立 Date 物件
 */
export function parseDate(dateInput) {
  if (!dateInput) return new Date()

  // 如果已經是 Date 物件，直接回傳
  if (dateInput instanceof Date) return dateInput

  // 如果是字串
  if (typeof dateInput === 'string') {
    // 1. 嘗試匹配 ISO 格式 (YYYY-MM-DDTHH:mm:ss 或 YYYY-MM-DD HH:mm:ss)
    // 這種正則會抓出 [Year, Month, Day, Hour, Minute, Second]
    const regex = /^(\d{4})[-/](\d{2})[-/](\d{2})[T\s](\d{2}):(\d{2})(?::(\d{2}))?/
    const match = dateInput.match(regex)

    if (match) {
      // match[1]=Year, match[2]=Month, match[3]=Day...
      // 注意：Month 要減 1 (0-11)
      return new Date(
        parseInt(match[1]),
        parseInt(match[2]) - 1,
        parseInt(match[3]),
        parseInt(match[4]),
        parseInt(match[5]),
        match[6] ? parseInt(match[6]) : 0
      )
    }
  }

  // 如果正則匹配失敗 (例如只有日期沒有時間)，退回一般的 new Date
  // 但要處理 Safari 不吃 '-' 的問題，將 '-' 換成 '/'
  if (typeof dateInput === 'string') {
     return new Date(dateInput.replace(/-/g, '/'))
  }
  
  return new Date(dateInput)
}