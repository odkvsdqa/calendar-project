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

// ... 保留原有的 formatDateTimeLocal, formatDate, formatTime ...

// ✅ [新增] 判斷兩個日期是否為同一天 (忽略時間)
export function isSameDay(date1, date2) {
  const d1 = new Date(date1)
  const d2 = new Date(date2)
  return d1.getFullYear() === d2.getFullYear() &&
         d1.getMonth() === d2.getMonth() &&
         d1.getDate() === d2.getDate()
}