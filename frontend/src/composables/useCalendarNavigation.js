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

  // src/composables/useCalendarNavigation.js

  const handleWheel = (e) => {
    // ------------------------------------------------
    // 0. 全域檢查：如果滑鼠指標下有開啟的 Modal (遮罩)，則完全不處理
    // (雖然我們在 Modal 加了 @wheel.stop，但這是一個雙重保險)
    // ------------------------------------------------
    const target = e.target
    if (target.closest('.modal-overlay') || target.closest('.list-modal-overlay')) {
      return // 在彈窗上，直接忽略導航邏輯
    }

    // ------------------------------------------------
    // 1. 水平捲動 (Shift + 滾輪) - 永遠觸發換頁
    // ------------------------------------------------
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

    // ------------------------------------------------
    // 2. 垂直捲動 (包含 MonthView 的特殊處理)
    // ------------------------------------------------
    
    // 檢查「視窗本身」是否出現了捲動條 (例如因為縮放)
    // document.documentElement.scrollHeight 是整個網頁高度
    // window.innerHeight 是視窗可視高度
    const bodyIsScrollable = document.documentElement.scrollHeight > window.innerHeight
    const scrollTop = window.scrollY || document.documentElement.scrollTop
    
    // 判斷視窗是否撞頂或撞底 (容許 1px 誤差)
    const bodyAtTop = scrollTop <= 0
    const bodyAtBottom = Math.ceil(scrollTop + window.innerHeight) >= document.documentElement.scrollHeight

    // A. 如果是「月」視圖
    if (viewMode.value === 'month') {
      // 🔥 關鍵修正：如果網頁可以捲動，我們必須優先讓它捲動
      if (bodyIsScrollable) {
        // 如果想往下滾，但還沒到底 -> 讓瀏覽器捲動，不換頁
        if (e.deltaY > 0 && !bodyAtBottom) return 
        
        // 如果想往上滾，但還沒到頂 -> 讓瀏覽器捲動，不換頁
        if (e.deltaY < 0 && !bodyAtTop) return
      }
      
      // 如果 (沒捲動條) 或 (已經撞到底/頂了)，才執行換頁
      e.preventDefault()
      if (wheelTimeout) return
      
      if (e.deltaY > 0) nextPeriod()
      else if (e.deltaY < 0) previousPeriod()
      
      wheelTimeout = true
      setTimeout(() => { wheelTimeout = false }, 50)
      return
    }

    // B. 年/日視圖 (Container 內部捲動 + 視窗捲動 雙重判斷)
    let container = null
    if (viewMode.value === 'year') container = document.querySelector('.year-view')
    else if (viewMode.value === 'day') container = document.querySelector('.day-view-container')

    if (!container) return

    const { scrollTop: cTop, scrollHeight: cHeight, clientHeight: cClient } = container
    const containerAtBottom = Math.ceil(cTop + cClient) >= cHeight - 1
    const containerAtTop = cTop <= 0

    // 只有當 (容器到底 且 視窗到底) 時，才切換到下一頁
    if (e.deltaY > 0 && containerAtBottom && bodyAtBottom) {
      e.preventDefault()
      if (wheelTimeout) return
      nextPeriod()
      setScrollPosition('top')
      
      wheelTimeout = true
      setTimeout(() => { wheelTimeout = false }, 300)
    }
    
    // 只有當 (容器到頂 且 視窗到頂) 時，才切換到上一頁
    else if (e.deltaY < 0 && containerAtTop && bodyAtTop) {
      e.preventDefault()
      if (wheelTimeout) return
      previousPeriod()
      setScrollPosition('bottom')
      
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