<template>
  <div class="login-form">
    <h2>登入</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>用戶名</label>
        <input 
          type="text" 
          v-model="form.username" 
          placeholder="請輸入用戶名"
          required
        >
      </div>
      
      <div class="form-group">
        <label>密碼</label>
        <input 
          type="password" 
          v-model="form.password" 
          placeholder="請輸入密碼"
          required
        >
      </div>
      
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
      
      <button type="submit" class="btn-submit" :disabled="loading">
        {{ loading ? '登入中...' : '登入' }}
      </button>
      
      <div class="form-footer">
        還沒有帳號？
        <router-link to="/register">立即註冊</router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../../services/authService'
import { setToken, setUserInfo } from '../../utils/auth'

const router = useRouter()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const errorMessage = ref('')

const handleSubmit = async () => {
  errorMessage.value = ''
  loading.value = true
  
  try {
    const response = await login(form.value)
    const { token, user } = response.data
    
    // 保存 Token 和用戶資訊
    setToken(token)
    setUserInfo(user)
    
    // 跳轉到日曆頁面
    router.push('/calendar')
  } catch (error) {
    console.error('登入失敗:', error)
    errorMessage.value = error.response?.data?.message || '登入失敗，請檢查用戶名和密碼'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-form {
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