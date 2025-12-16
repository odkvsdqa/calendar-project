import { ref } from 'vue'
import api from '../services/api'
import { useToast } from './useToast'
import { useI18n } from 'vue-i18n'

export function useVerification() {
  const { showToast } = useToast()
  const { t } = useI18n()
  
  const loading = ref(false)
  const countdown = ref(0)
  const canResend = ref(true)
  let timer = null

  const sendCode = async (email, purpose) => {
    // 使用 i18n 錯誤訊息
    if (!email || !/^\S+@\S+\.\S+$/.test(email)) {
      showToast(t('verification.emailRequired'), 'warning')
      return false
    }

    if (!canResend.value) return false

    loading.value = true
    try {
      await api.post('/auth/send-code', { email, purpose })
      
      // 使用 i18n 成功訊息
      showToast(t('verification.codeSent'), 'success')
      
      startCountdown(60)
      return true
    } catch (error) {
      // 優先顯示後端訊息，若無則顯示 i18n 預設錯誤
      const msg = error.response?.data?.message || t('verification.sendFailed')
      showToast(msg, 'error')
      return false
    } finally {
      loading.value = false
    }
  }

  const startCountdown = (seconds) => {
    countdown.value = seconds
    canResend.value = false
    
    if (timer) clearInterval(timer)

    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        canResend.value = true
        countdown.value = 0
      }
    }, 1000)
  }

  return { loading, countdown, canResend, sendCode }
}