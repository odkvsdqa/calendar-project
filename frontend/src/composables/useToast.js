// src/composables/useToast.js
export function useToast() {
  const showToast = (message, type = 'success') => {
    // 1. 創建元素
    const toast = document.createElement('div')

    // 👇 [新增] 檢查傳進來的 message 是什麼類型
    let textContent = ''
    if (typeof message === 'string') {
      textContent = message
    } else if (message && typeof message === 'object') {
      // 如果是物件，嘗試抓取常見的錯誤訊息欄位，或者轉成 JSON
      textContent = message.message || message.msg || JSON.stringify(message)
    } else {
      textContent = String(message)
    }
    
    // 2. 設定樣式 (漂亮的漸層與陰影)
    const colors = {
      success: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
      error: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
      warning: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)'
    }
  
    toast.textContent = textContent
    toast.style.position = 'fixed'
    toast.style.top = '20px'
    toast.style.left = '50%'
    toast.style.transform = 'translateX(-50%) translateY(-20px)'
    toast.style.background = colors[type] || colors.success
    toast.style.color = 'white'
    toast.style.padding = '12px 24px'
    toast.style.borderRadius = '8px'
    toast.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)'
    toast.style.zIndex = '9999'
    toast.style.fontSize = '14px'
    toast.style.fontWeight = 'bold'
    toast.style.opacity = '0'
    toast.style.transition = 'all 0.3s ease'

    // 3. 加入 DOM
    document.body.appendChild(toast)

    // 4. 動畫效果 (淡入)
    requestAnimationFrame(() => {
      toast.style.opacity = '1'
      toast.style.transform = 'translateX(-50%) translateY(0)'
    })

    // 5. 自動移除 (3秒後淡出)
    setTimeout(() => {
      toast.style.opacity = '0'
      toast.style.transform = 'translateX(-50%) translateY(-20px)'
      setTimeout(() => {
        document.body.removeChild(toast)
      }, 300)
    }, 3000)
  }

  return { showToast }
}