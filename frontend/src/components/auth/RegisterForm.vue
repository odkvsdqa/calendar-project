<template>
  <div class="register-form">
    <h2>{{ $t("auth.register") }}</h2>

    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>{{ $t("auth.username") }}</label>
        <input
          type="text"
          v-model="form.username"
          required
          minlength="3"
          maxlength="20"
          :placeholder="$t('auth.placeholder.username')"
        />
      </div>

      <div class="form-group">
        <label>{{ $t("auth.email") }}</label>
        <div class="input-with-button">
          <input
            type="email"
            v-model="form.email"
            required
            :placeholder="$t('auth.placeholder.email')"
            :disabled="codeSent"
          />
          <button
            type="button"
            class="btn-verify"
            @click="handleSendCode"
            :disabled="!canResend || loading || verifyLoading"
          >
            <span v-if="verifyLoading">發送中...</span>
            <span v-else>{{
              countdown > 0 ? `${countdown}s` : $t("verification.sendCode")
            }}</span>
          </button>
        </div>
      </div>

      <div class="form-group">
        <label>{{ $t("verification.code") }}</label>
        <input
          type="text"
          v-model="form.code"
          maxlength="6"
          :placeholder="$t('verification.placeholder.code')"
          required
        />
      </div>

      <div class="form-group">
        <label>{{ $t("auth.password") }}</label>
        <input
          type="password"
          v-model="form.password"
          required
          minlength="6"
          :placeholder="$t('auth.placeholder.password')"
        />
      </div>

      <div class="form-group">
        <label>{{ $t("auth.confirmPassword") }}</label>
        <input
          type="password"
          v-model="form.confirmPassword"
          required
          :placeholder="$t('auth.placeholder.confirmPassword')"
        />
      </div>

      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
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
import { useVerification } from "../../composables/useVerification";
import api from "../../services/api";
import { useToast } from "../../composables/useToast";

const { t } = useI18n();
const router = useRouter();
const { showToast } = useToast();
const {
  loading: verifyLoading,
  countdown,
  canResend,
  sendCode,
} = useVerification();

const form = ref({
  username: "",
  email: "",
  password: "",
  confirmPassword: "",
  code: "",
});
const loading = ref(false);
const codeSent = ref(false);
const errorMessage = ref("");

const handleSendCode = async () => {
  const success = await sendCode(form.value.email, "register");
  if (success) {
    codeSent.value = true;
  }
};

const handleSubmit = async () => {
  errorMessage.value = "";

  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = t("auth.errors.passwordMismatch");
    return;
  }
  if (!form.value.code) {
    errorMessage.value = t("verification.codeRequired");
    return;
  }

  loading.value = true;
  try {
    await api.post("/auth/register", form.value);
    showToast(t("messages.registerSuccess"), "success");
    setTimeout(() => router.push("/login"), 2000);
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || t("auth.errors.registerFailed");
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.input-with-button {
  display: flex;
  gap: 10px;
}
.input-with-button input {
  flex: 1;
}
.btn-verify {
  width: 120px;
  padding: 0;
  font-size: 13px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
}
.btn-verify:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.btn-verify:hover:not(:disabled) {
  background: #5a67d8;
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
  box-sizing: border-box;
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
}
.error-message {
  background: #fee;
  color: #e53e3e;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
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
</style>
