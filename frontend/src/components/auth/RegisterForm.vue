<template>
  <div class="register-form">
    <h2>註冊</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>用戶名</label>
        <input 
          type="text" 
          v-model="form.username" 
          placeholder="請輸入用戶名（3-20字元）"
          required
          minlength="3"
          maxlength="20"
        >
      </div>
      
      <div class="form-group">
        <label>電子郵件</label>
        <input 
          type="email" 
          v-model="form.email" 
          placeholder="請輸入電子郵件"
          required
        >
      </div>
      
      <div class="form-group">
        <label>密碼</label>
        <input 
          type="password" 
          v-model="form.password" 
          placeholder="請輸入密碼（至少6位）"
          required
          minlength="6"
        >
      </div>
      
      <div class="form-group">
        <label>確認密碼</label>
        <input 
          type="password" 
          v-model="form.confirmPassword" 
          placeholder="請再次輸入密碼"
          required
        >
      </div>
      
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
      
      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>
      
      <button type="submit" class="btn-submit" :disabled="loading">
        {{ loading ? '註冊中...' : '註冊' }}
      </button>
      
      <div class="form-footer">
        已有帳號？
        <router-link to="/login">立即登入</router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../../services/authService'

const router = useRouter()

const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''
  
  // 驗證密碼是否一致
  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '兩次輸入的密碼不一致'
    return
  }
  
  loading.value = true
  
  try {
    const registerData = {
      username: form.value.username,
      email: form.value.email,
      password: form.value.password
    }
    
    await register(registerData)
    
    successMessage.value = '註冊成功！3秒後跳轉到登入頁面...'
    
    // 3秒後跳轉到登入頁
    setTimeout(() => {
      router.push('/login')
    }, 3000)
    
  } catch (error) {
    console.error('註冊失敗:', error)
    errorMessage.value = error.response?.data?.message || '註冊失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-form {
  max-width: 400px;
  margin: 0 auto;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #667eea;
  margin-bottom: 30px;
  font-size: 28px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.error-message {
  background: #fee;
  color: #e53e3e;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
}

.success-message {
  background: #d4edda;
  color: #155724;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
}

.btn-submit {
  width: 100%;
  padding: 14px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-submit:hover:not(:disabled) {
  background: #5568d3;
  transform: translateY(-2px);
}

.btn-submit:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.form-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: bold;
}

.form-footer a:hover {
  text-decoration: underline;
}
</style>