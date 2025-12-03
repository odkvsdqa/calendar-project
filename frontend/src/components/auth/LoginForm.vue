<template>
  <div class="login-form">
    <h2>{{ $t("auth.login") }}</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>{{ $t("auth.username") }}</label>
        <input
          type="text"
          v-model="form.username"
          :placeholder="$t('auth.username')"
          required
        />
      </div>

      <div class="form-group">
        <label>{{ $t("auth.password") }}</label>
        <input
          type="password"
          v-model="form.password"
          :placeholder="$t('auth.password')"
          required
        />
      </div>

      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>

      <button type="submit" class="btn-submit" :disabled="loading">
        {{ loading ? $t("common.loading") : $t("auth.loginNow") }}
      </button>

      <div class="form-footer">
        {{ $t("auth.noAccount") }}
        <router-link to="/register"> {{ $t("auth.registerNow") }}</router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n"; 
import { login } from "../../services/authService";
import { setToken, setUserInfo } from "../../utils/auth";
import { preferenceApi } from "../../services/preferenceApi"; // 🔥 新增

const router = useRouter();
const { t, locale } = useI18n(); // 🔥 補上 locale

const form = ref({ username: "", password: "" });
const loading = ref(false);
const errorMessage = ref("");

const handleSubmit = async () => {
  errorMessage.value = ''
  loading.value = true
  
  try {
    const response = await login(form.value)
    const { token, user } = response.data
    
    setToken(token)
    setUserInfo(user)
    
    // 🔥 新增：登入成功後，從後端同步語言偏好
    try {
      const langResponse = await preferenceApi.getLanguage()
      const serverLanguage = langResponse.data.language
      
      // 更新前端 i18n 與 localStorage
      locale.value = serverLanguage
      localStorage.setItem('user-locale', serverLanguage)
      
      console.log('✅ 已同步後端語言設定:', serverLanguage)
    } catch (langError) {
      // 若語言同步失敗，不阻擋登入流程，僅 log
      console.warn('⚠️ 語言同步失敗 (使用預設語言):', langError)
    }
    
    // 跳轉至日曆頁面
    router.push('/calendar')
  } catch (error) {
    console.error('Login failed:', error)
    errorMessage.value = error.response?.data?.message || t('auth.errors.loginFailed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 樣式保持不變 */
.login-form { max-width: 100%; margin: 0 auto; padding: 0; background: transparent; box-shadow: none; }
h2 { text-align: center; color: #667eea; margin-bottom: 30px; font-size: 28px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: bold; color: #555; }
.form-group input { width: 100%; padding: 12px; border: 2px solid #e0e0e0; border-radius: 8px; font-size: 14px; transition: border-color 0.3s; box-sizing: border-box; }
.form-group input:focus { outline: none; border-color: #667eea; }
.error-message { background: #fee; color: #e53e3e; padding: 12px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; }
.btn-submit { width: 100%; padding: 14px; background: #667eea; color: white; border: none; border-radius: 8px; font-size: 16px; font-weight: bold; cursor: pointer; transition: all 0.3s; }
.btn-submit:hover:not(:disabled) { background: #5568d3; transform: translateY(-2px); }
.btn-submit:disabled { background: #ccc; cursor: not-allowed; }
.form-footer { text-align: center; margin-top: 20px; color: #666; }
.form-footer a { color: #667eea; text-decoration: none; font-weight: bold; }
.form-footer a:hover { text-decoration: underline; }
</style>