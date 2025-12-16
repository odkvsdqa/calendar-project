<template>
  <div class="forgot-password-view">
    <div class="forgot-wrapper">
      <div class="brand-section">
        <div class="brand-content">
          <SkjlLogo layout="vertical" mode="white" />
          <p class="brand-desc">{{ $t("auth.sloganDesc") }}</p>
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
        </div>
      </div>

      <div class="form-section">
        <div class="form-container">
          <!-- Step 1 -->
          <div v-if="step === 1">
            <h2>{{ $t("auth.forgotPassword") }}</h2>
            <p class="hint">{{ $t("auth.forgotPasswordHint") }}</p>

            <div class="form-group">
              <label>{{ $t("auth.email") }}</label>
              <input
                type="email"
                v-model="email"
                :placeholder="$t('auth.placeholder.email')"
                required
              />
            </div>

            <button
              class="btn-submit"
              @click="handleSendCode"
              :disabled="loading"
            >
              {{ loading ? $t("common.loading") : $t("verification.sendCode") }}
            </button>
          </div>

          <!-- Step 2 -->
          <div v-else-if="step === 2">
            <h2>{{ $t("auth.resetPassword") }}</h2>

            <div class="form-group">
              <label>{{ $t("verification.code") }}</label>
              <div class="input-with-button">
                <input
                  type="text"
                  v-model="code"
                  maxlength="6"
                  :placeholder="$t('verification.placeholder.code')"
                  required
                />
                <button
                  type="button"
                  class="btn-verify"
                  @click="handleResendCode"
                  :disabled="!canResend"
                >
                  {{
                    countdown > 0 ? `${countdown}s` : $t("verification.resend")
                  }}
                </button>
              </div>
            </div>

            <div class="form-group">
              <label>{{ $t("auth.newPassword") }}</label>
              <input
                type="password"
                v-model="newPassword"
                minlength="6"
                :placeholder="$t('auth.placeholder.newPassword')"
                required
              />
            </div>

            <div class="form-group">
              <label>{{ $t("auth.confirmPassword") }}</label>
              <input
                type="password"
                v-model="confirmPassword"
                :placeholder="$t('auth.placeholder.confirmPassword')"
                required
              />
            </div>

            <button
              class="btn-submit"
              @click="handleResetPassword"
              :disabled="submitting"
            >
              {{ submitting ? $t("common.loading") : $t("auth.confirmReset") }}
            </button>
          </div>

          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <div class="form-footer">
            <router-link to="/login">{{ $t("auth.backToLogin") }}</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import SkjlLogo from "../components/SkjlLogo.vue";
import { useVerification } from "../composables/useVerification";
import api from "../services/api";
import { useToast } from "../composables/useToast";

const { t } = useI18n();
const router = useRouter();
const { showToast } = useToast();
const { loading, countdown, canResend, sendCode } = useVerification();

const step = ref(1);
const email = ref("");
const code = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const errorMessage = ref("");

const handleSendCode = async () => {
  errorMessage.value = "";
  if (!email.value) {
    errorMessage.value = t("verification.emailRequired");
    return;
  }
  const success = await sendCode(email.value, "reset");
  if (success) step.value = 2;
};

const handleResendCode = async () => {
  await sendCode(email.value, "reset");
};

const submitting = ref(false)
const handleResetPassword = async () => {
  errorMessage.value = "";
  if (!code.value) {
    errorMessage.value = t("verification.codeRequired");
    return;
  }
  if (newPassword.value !== confirmPassword.value) {
    errorMessage.value = t("auth.errors.passwordMismatch");
    return;
  }
  if (newPassword.value.length < 6) {
    errorMessage.value = t("auth.errors.passwordTooShort");
    return;
  }

  // 這裡手動設置 loading，因為 sendCode 結束了
  const localLoading = ref(true); // 用這個來控制按鈕
  submitting.value = true; // 🔥 使用這個變數
  try {
    await api.post("/auth/reset-password", {
      email: email.value,
      code: code.value,
      newPassword: newPassword.value,
    });
    showToast(t("auth.resetSuccess"), "success");
    setTimeout(() => router.push("/login"), 1500);
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || t("auth.errors.resetFailed");
  } finally {
    localLoading.value = false;
  }
};
</script>

<style scoped>
.forgot-password-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  padding: 20px;
}
.forgot-wrapper {
  background: white;
  width: 100%;
  max-width: 1000px;
  height: 600px;
  display: flex;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 80px rgba(0, 0, 0, 0.1);
}
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #556b2f 0%, #8fbc8f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.brand-content {
  text-align: center;
  position: relative;
  z-index: 2;
}
.brand-desc {
  margin-top: 30px;
  color: #fdf5e6;
  font-size: 16px;
  opacity: 0.9;
  white-space: pre-line;
}
.form-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
}
.form-container {
  width: 100%;
  max-width: 360px;
}
h2 {
  text-align: center;
  color: #667eea;
  margin-bottom: 10px;
}
.hint {
  text-align: center;
  color: #666;
  font-size: 13px;
  margin-bottom: 24px;
}
.form-group {
  margin-bottom: 18px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
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
.input-with-button {
  display: flex;
  gap: 8px;
}
.input-with-button input {
  flex: 1;
}
.btn-verify {
  width: 100px;
  padding: 0;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.btn-verify:disabled {
  background: #ccc;
  cursor: not-allowed;
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
  margin-top: 10px;
}
.btn-submit:disabled {
  background: #ccc;
}
.error-message {
  background: #fee;
  color: #e53e3e;
  padding: 12px;
  border-radius: 8px;
  margin-top: 15px;
}
.form-footer {
  text-align: center;
  margin-top: 20px;
}
.form-footer a {
  color: #667eea;
  font-weight: bold;
  text-decoration: none;
}
/* 圓圈裝飾 */
.decoration-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(50px);
  opacity: 0.5;
}
.circle-1 {
  width: 200px;
  height: 200px;
  background: #fdf5e6;
  top: -50px;
  left: -50px;
  animation: float 6s infinite;
}
.circle-2 {
  width: 150px;
  height: 150px;
  background: #aebc9e;
  bottom: -30px;
  right: -30px;
  animation: float 8s infinite reverse;
}
@keyframes float {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(20px);
  }
}
@media (max-width: 768px) {
  .forgot-wrapper {
    flex-direction: column;
    height: auto;
    max-width: 450px;
  }
  .brand-section {
    padding: 30px;
    min-height: 150px;
  }
  .brand-desc {
    display: none;
  }
}
</style>
