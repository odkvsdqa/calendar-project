import { onMounted, onUnmounted, nextTick } from 'vue'

export function useCalendarNavigation(currentDate, viewMode) {
  let touchStartX = 0
  let touchStartY = 0
  let isThrottled = false 
  let isZooming = false 

  // --- 滾動位置銜接邏輯 (配合上下換頁的連續感) ---
  const restoreScrollPosition = (direction) => {
    nextTick(() => {
      // 找出目前畫面上的滾動容器
      const container = document.querySelector('.day-view-container, .year-view, .month-scroll-wrapper')
      
      if (container) {
        if (direction === 'prev') {
          // 往回翻 (昨天/去年/上個月)
          // 因為是「由上往下拉」，邏輯上我們是想看上面的內容
          // 所以換頁後，應該停留在新頁面的「最底部」，模擬無縫連接
          container.scrollTop = container.scrollHeight
        } else {
          // 往後翻 (明天/明年/下個月)
          // 因為是「由下往上推」，邏輯上我們是想看下面的內容
          // 所以換頁後，應該停留在新頁面的「最頂部」
          container.scrollTop = 0
        }
      }
    })
  }

  // --- 換頁核心邏輯 ---
  const changePage = (direction) => {
    if (isThrottled) return
    isThrottled = true
    setTimeout(() => isThrottled = false, 400) 

    const date = new Date(currentDate.value)
    const offset = direction === 'next' ? 1 : -1

    if (viewMode.value === 'year') {
      date.setFullYear(date.getFullYear() + offset)
    } else if (viewMode.value === 'month') {
      date.setMonth(date.getMonth() + offset)
    } else {
      date.setDate(date.getDate() + offset)
    }
    
    currentDate.value = date

    // 呼叫滾動銜接
    restoreScrollPosition(direction)
  }

  const previousPeriod = () => changePage('prev')
  const nextPeriod = () => changePage('next')
  const goToToday = () => currentDate.value = new Date()

  // --- 滾輪智慧判斷 (桌機版維持不變) ---
  const handleWheel = (e) => {
    const container = e.target.closest('.day-view-container, .year-view, .month-scroll-wrapper, .modal-content, .list-content')

    if (container) {
      const { scrollTop, scrollHeight, clientHeight } = container
      const canScrollVertically = scrollHeight > clientHeight

      if (canScrollVertically) {
        if (e.deltaY < 0 && scrollTop > 0) return
        if (e.deltaY > 0 && Math.abs(scrollHeight - clientHeight - scrollTop) > 1) return
      }
    }

    if (e.deltaY < 0) previousPeriod()
    else if (e.deltaY > 0) nextPeriod()
  }

  // --- 觸控邏輯 (🔥 修改為上下換頁 + 邊界檢測) ---
  const handleTouchStart = (e) => {
    if (e.touches.length > 1) {
      isZooming = true
      return
    }
    isZooming = false
    touchStartX = e.changedTouches[0].screenX
    touchStartY = e.changedTouches[0].screenY
  }

  const handleTouchEnd = (e) => {
    // 如果是縮放中，不處理滑動
    if (isZooming || e.touches.length > 0) {
      isZooming = false
      return
    }

    const touchEndX = e.changedTouches[0].screenX
    const touchEndY = e.changedTouches[0].screenY
    
    const diffX = touchEndX - touchStartX
    const diffY = touchEndY - touchStartY // 正數代表向下滑(Pull Down)，負數代表向上滑(Push Up)

    // 🔥 邏輯修改 1: 如果水平移動大於垂直移動，視為左右滑，這裡「不處理」(或是保留左右換頁亦可，依您需求)
    // 您的需求是改成上下換頁，所以這裡我們忽略左右滑動，避免誤觸
    if (Math.abs(diffX) > Math.abs(diffY)) return

    // 🔥 邏輯修改 2: 邊界檢測 (Overscroll Detection)
    const scrollContainer = e.target.closest('.day-view-container, .year-view, .month-scroll-wrapper')
    
    let isAtTop = true
    let isAtBottom = true

    if (scrollContainer) {
      const { scrollTop, scrollHeight, clientHeight } = scrollContainer
      // 容許 2px 誤差
      isAtTop = scrollTop <= 2
      isAtBottom = Math.abs(scrollHeight - clientHeight - scrollTop) <= 2
    }

    const SWIPE_THRESHOLD = 80 // 滑動門檻

    // 判定滑動方向與動作
    if (Math.abs(diffY) > SWIPE_THRESHOLD) {
      // ⬇️ 向下滑動 (Pull Down) -> 預期是看「上面」的內容 (上一頁)
      if (diffY > 0) {
        // 只有在「已經捲動到最頂端」時，才觸發上一頁
        if (isAtTop) {
           previousPeriod()
        }
      } 
      // ⬆️ 向上滑動 (Push Up) -> 預期是看「下面」的內容 (下一頁)
      else {
        // 只有在「已經捲動到最底端」時，才觸發下一頁
        if (isAtBottom) {
           nextPeriod()
        }
      }
    }
  }

  onMounted(() => {
    window.addEventListener('touchstart', handleTouchStart)
    window.addEventListener('touchend', handleTouchEnd)
  })

  onUnmounted(() => {
    window.removeEventListener('touchstart', handleTouchStart)
    window.removeEventListener('touchend', handleTouchEnd)
  })

  return { previousPeriod, nextPeriod, goToToday, handleWheel }
}