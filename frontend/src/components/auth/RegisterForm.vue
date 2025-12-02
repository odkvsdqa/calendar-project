<template>
  <div class="register-form">
    <h2>{{ $t("auth.register") }}</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>{{ $t("auth.username") }}</label>
        <input
          type="text"
          v-model="form.username"
          :placeholder="$t('auth.username')"
          required
          minlength="3"
          maxlength="20"
        />
      </div>

      <div class="form-group">
        <label>{{ $t("auth.email") }}</label>
        <input
          type="email"
          v-model="form.email"
          :placeholder="$t('auth.email')"
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
          minlength="6"
        />
      </div>

      <div class="form-group">
        <label>{{ $t("auth.confirmPassword") }}</label>
        <input
          type="password"
          v-model="form.confirmPassword"
          :placeholder="$t('auth.confirmPassword')"
          required
        />
      </div>

      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>

      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>

      <button type="submit" class="btn-submit" :disabled="loading">
        {{ loading ? $t("common.loading") : $t("auth.registerNow") }}
      </button>

      <div class="form-footer">
        {{ $t("auth.hasAccount") }}
        <router-link to="/login">{{ $t("auth.loginNow") }}</router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { register } from "../../services/authService";

const router = useRouter();
const { t } = useI18n();

const form = ref({
  username: "",
  email: "",
  password: "",
  confirmPassword: "",
});

const loading = ref(false);
const errorMessage = ref("");
const successMessage = ref("");

const handleSubmit = async () => {
  errorMessage.value = "";
  successMessage.value = "";

  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = t("auth.confirmPassword")  // 簡單提示
    return;
  }

  loading.value = true;

  try {
    const registerData = {
      username: form.value.username,
      email: form.value.email,
      password: form.value.password,
    };

    await register(registerData);

    successMessage.value = t('messages.registerSuccess')

    setTimeout(() => {
      router.push("/login");
    }, 3000);
  } catch (error) {
    console.error("Register failed:", error);
    errorMessage.value = error.response?.data?.message || t('auth.errors.registerFailed');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* 樣式保持不變 (請保留您原本的 CSS) */
.register-form {
  max-width: 100%;
  margin: 0 auto;
  padding: 0;
  background: transparent;
  box-shadow: none;
}
h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 24px;
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
  box-sizing: border-box;
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
