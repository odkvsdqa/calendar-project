<template>
  <form @submit.prevent="handleSubmit" class="change-pwd-form">
    <div v-if="step === 1">
      <p class="hint">{{ $t('auth.changePasswordHint') }}</p>
      <p class="email-display">📧 {{ currentUser?.email }}</p>
      
      <button
        type="button"
        class="btn-send-full"
        @click="handleSendCode"
        :disabled="loading || !canResend"
      >
        {{ countdown > 0 ? `${countdown}s` : $t('verification.sendCodeToEmail') }}
      </button>
    </div>

    <div v-else>
      <div class="form-group">
        <label>{{ $t('verification.code') }}</label>
        <input
          type="text"
          v-model="code"
          maxlength="6"
          :placeholder="$t('verification.placeholder.code')"
          required
        />
      </div>

      <div class="form-group">
        <label>{{ $t('auth.newPassword') }}</label>
        <input
          type="password"
          v-model="newPassword"
          minlength="6"
          :placeholder="$t('auth.placeholder.newPassword')"
          required
        />
      </div>

      <div class="form-group">
        <label>{{ $t('auth.confirmPassword') }}</label>
        <input
          type="password"
          v-model="confirmPassword"
          :placeholder="$t('auth.placeholder.confirmPassword')"
          required
        />
      </div>

      <div class="button-group">
        <button type="button" class="btn-cancel" @click="step = 1">
          {{ $t('common.cancel') }}
        </button>
        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? $t('common.loading') : $t('common.confirm') }}
        </button>
      </div>
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
  </form>
</template>

<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuth } from '../../composables/useAuth'
import { useVerification } from '../../composables/useVerification'
import api from '../../services/api'
import { useToast } from '../../composables/useToast'

const { t } = useI18n()
const { currentUser } = useAuth()
const { showToast } = useToast()
const { loading, countdown, canResend, sendCode } = useVerification()
const emit = defineEmits(['success'])

const step = ref(1)
const code = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const errorMessage = ref('')

const handleSendCode = async () => {
  if (!currentUser.value?.email) {
    errorMessage.value = 'Email not found'
    return
  }
  const success = await sendCode(currentUser.value.email, 'change')
  if (success) {
    step.value = 2
    errorMessage.value = ''
  }
}

const handleSubmit = async () => {
  if (isSubmitting.value) return // 防重複提交
  
  errorMessage.value = ''
  
  // 前端驗證
  if (newPassword.value !== confirmPassword.value) {
    errorMessage.value = t('auth.errors.passwordMismatch')
    return
  }
  if (newPassword.value.length < 6) {
    errorMessage.value = t('auth.errors.passwordTooShort')
    return
  }

  isSubmitting.value = true
  loading.value = true // 使用 useVerification 的 loading
  
  try {
    await api.post('/auth/change-password', {
      email: currentUser.value.email,
      code: code.value,
      newPassword: newPassword.value
    })
    showToast(t('auth.changePasswordSuccess'), 'success')
    emit('success')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || t('auth.errors.operationFailed')
  } finally {
    loading.value = false
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.hint { text-align: center; color: #666; font-size: 13px; margin-bottom: 15px; }
.email-display { text-align: center; font-weight: bold; color: #667eea; margin-bottom: 20px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; font-size: 13px; }
.form-group input { width: 100%; padding: 10px; border: 1px solid #e0e0e0; border-radius: 6px; box-sizing: border-box; }
.btn-send-full { width: 100%; padding: 12px; background: #667eea; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; }
.btn-send-full:disabled { background: #ccc; cursor: not-allowed; }
.button-group { display: flex; gap: 10px; margin-top: 20px; }
.btn-cancel { flex: 1; padding: 10px; background: #f3f4f6; color: #666; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; }
.btn-submit { flex: 1; padding: 10px; background: #667eea; color: white; border: none; border-radius: 6px; font-weight: bold; cursor: pointer; }
.btn-submit:disabled { background: #ccc; }
.error-message { background: #fee; color: #e53e3e; padding: 10px; border-radius: 6px; margin-top: 15px; text-align: center; }
</style>