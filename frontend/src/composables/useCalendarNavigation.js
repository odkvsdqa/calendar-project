// src/composables/useCalendarNavigation.js
import { onMounted, onUnmounted } from 'vue'

export function useCalendarNavigation(currentDate, viewMode) {
  
  // 1. 核心計算邏輯 (從 CalendarApp 搬出來並優化)
  const previousPeriod = () => {
    const d = new Date(currentDate.value)
    if (viewMode.value === 'year') d.setFullYear(d.getFullYear() - 1)
    else if (viewMode.value === 'month') d.setMonth(d.getMonth() - 1)
    else d.setDate(d.getDate() - 1)
    currentDate.value = d
  }

  const nextPeriod = () => {
    const d = new Date(currentDate.value)
    if (viewMode.value === 'year') d.setFullYear(d.getFullYear() + 1)
    else if (viewMode.value === 'month') d.setMonth(d.getMonth() + 1)
    else d.setDate(d.getDate() + 1)
    currentDate.value = d
  }

  const goToToday = () => {
    currentDate.value = new Date()
  }

  // 2. 互動監聽邏輯
  const handleKeydown = (e) => {
    // 避免在輸入框打字時觸發
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return
    if (e.key === 'ArrowLeft') previousPeriod()
    if (e.key === 'ArrowRight') nextPeriod()
  }
  // 🔥 新增：捲動歸零 helper
// ... 前面的程式碼保持不變 ...

  // 🔥 修改：捲動位置控制 helper (支援 top / bottom)
  const setScrollPosition = (position) => {
    // 使用 setTimeout 確保在 Vue 更新 DOM 之後執行
    setTimeout(() => {
      let container = null
      if (viewMode.value === 'year') {
        container = document.querySelector('.year-view')
      } else if (viewMode.value === 'day') {
        container = document.querySelector('.day-view-container')
      }
      
      if (container) {
        if (position === 'top') {
          container.scrollTop = 0 // 回到頂部 (00:00 或 1月)
        } else {
          container.scrollTop = container.scrollHeight // 到底部 (23:00 或 12月)
        }
      }
    }, 10)
  }

  // 滾輪邏輯
  let wheelTimeout = false

  const handleWheel = (e) => {
    // 1. 水平捲動 (Shift + 滾輪)
    // 水平切換通常習慣是回到頂部
    if (Math.abs(e.deltaX) > Math.abs(e.deltaY)) {
      e.preventDefault()
      if (wheelTimeout) return
      
      if (e.deltaX > 0) {
        nextPeriod()
        setScrollPosition('top')
      } else {
        previousPeriod()
        setScrollPosition('top')
      }
      
      wheelTimeout = true
      setTimeout(() => { wheelTimeout = false }, 200)
      return
    }

    // 2. 垂直捲動
    // A. 月視圖 (無捲軸)
    if (viewMode.value === 'month') {
      e.preventDefault()
      if (wheelTimeout) return
      if (e.deltaY > 0) nextPeriod()
      else if (e.deltaY < 0) previousPeriod()
      
      wheelTimeout = true
      setTimeout(() => { wheelTimeout = false }, 50)
      return
    }

    // B. 年/日視圖 (有捲軸)
    let container = null
    if (viewMode.value === 'year') container = document.querySelector('.year-view')
    else if (viewMode.value === 'day') container = document.querySelector('.day-view-container')

    if (!container) return

    const { scrollTop, scrollHeight, clientHeight } = container
    const isAtBottom = Math.ceil(scrollTop + clientHeight) >= scrollHeight - 1
    const isAtTop = scrollTop <= 0

    // 狀況 1: 往下滾 (Next) 且 撞到底部 -> 去下一頁的「頂部」
    if (e.deltaY > 0 && isAtBottom) {
      e.preventDefault()
      if (wheelTimeout) return
      nextPeriod()
      setScrollPosition('top') // 🔥 下一頁從頭開始看
      
      wheelTimeout = true
      setTimeout(() => { wheelTimeout = false }, 300)
    }
    
    // 狀況 2: 往上滾 (Prev) 且 撞到頂部 -> 去上一頁的「底部」
    else if (e.deltaY < 0 && isAtTop) {
      e.preventDefault()
      if (wheelTimeout) return
      previousPeriod()
      setScrollPosition('bottom') // 🔥 上一頁從尾巴接續看
      
      wheelTimeout = true
      setTimeout(() => { wheelTimeout = false }, 300)
    }
  }

  // ... 後面的 onMounted 等保持不變 ...
  

  // 手機滑動 (Touch)
  let touchStartX = 0
  const handleTouchStart = (e) => { touchStartX = e.touches[0].clientX }
  const handleTouchEnd = (e) => {
    const touchEndX = e.changedTouches[0].clientX
    const diff = touchStartX - touchEndX
    if (Math.abs(diff) > 50) { // 滑動超過 50px 才算
      if (diff > 0) nextPeriod() // 向左滑 -> 下一頁
      else previousPeriod()      // 向右滑 -> 上一頁
    }
  }

  // 3. 自動掛載與卸載監聽器
  onMounted(() => {
    window.addEventListener('keydown', handleKeydown)
    // 建議綁定在特定容器上，這裡先綁 window 方便全域使用
    window.addEventListener('touchstart', handleTouchStart)
    window.addEventListener('touchend', handleTouchEnd)
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown)
    window.removeEventListener('touchstart', handleTouchStart)
    window.removeEventListener('touchend', handleTouchEnd)
  })

  // 回傳給組件使用
  return { previousPeriod, nextPeriod, goToToday, handleWheel }
}