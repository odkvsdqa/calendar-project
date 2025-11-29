<template>
  <div class="register-view">
    <div class="register-wrapper">
      
      <!-- 左側：品牌展示 (Banner) -->
      <div class="brand-section">
        <div class="brand-content">
          <SkjlLogo layout="vertical" mode="white" />
          <p class="brand-desc">
            立即加入 SKJL<br>
            開啟您的質感生活規劃
          </p>
          <!-- 裝飾圓圈 (稍微改變顏色位置，增加一點點變化) -->
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
        </div>
      </div>

      <!-- 右側：註冊表單 -->
      <div class="form-section">
        <div class="form-container">
          <RegisterForm />
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import RegisterForm from '../components/auth/RegisterForm.vue'
import SkjlLogo from '../components/SkjlLogo.vue'
</script>

<style scoped>
.register-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  padding: 20px;
}

.register-wrapper {
  background: white;
  width: 100%;
  max-width: 1000px;
  min-height: 650px; /* 註冊表單通常比較長，所以高度給多一點 */
  display: flex;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 80px rgba(0, 0, 0, 0.1);
}

/* --- 左側品牌區 --- */
.brand-section {
  flex: 1;
  /* 👇 背景改為和紙漸層 */
  background: linear-gradient(135deg, #556b2f 0%, #8fbc8f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
}

.brand-desc {
  margin-top: 30px;
  color: #fdf5e6; /* 米白字 */
  font-size: 16px;
  line-height: 1.8;
  letter-spacing: 1px;
  font-weight: 400;
  opacity: 0.9;
}

/* 裝飾用的背景圓圈 */
.decoration-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(50px);
  opacity: 0.5;
  z-index: 1;
}

.circle-1 {
  width: 220px;
  height: 220px;
  /* 👇 修改：淺草綠 (稍微換個位置或顏色讓註冊頁有點小變化，也可以維持米白) */
  background: #aebc9e;
  top: -60px;
  left: -40px;
  animation: float 7s ease-in-out infinite;
}

.circle-2 {
  width: 180px;
  height: 180px;
  /* 👇 修改：米白色 */
  background: #fdf5e6;
  bottom: -40px;
  right: -50px;
  animation: float 9s ease-in-out infinite reverse;
}

/* --- 右側表單區 --- */
.form-section {
  flex: 1.2; /* 註冊表單較寬，給它多一點空間 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: white;
  padding: 40px;
}

.form-container {
  width: 100%;
  max-width: 400px; /* 註冊欄位多，寬度稍微放寬 */
}

.footer-links {
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.footer-links a {
  color: #667eea;
  text-decoration: none;
  font-weight: bold;
}

.footer-links a:hover {
  text-decoration: underline;
}

/* 動畫 */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(20px); }
}

/* --- RWD --- */
@media (max-width: 768px) {
  .register-wrapper {
    flex-direction: column;
    min-height: auto;
    max-width: 450px;
  }

  .brand-section {
    padding: 30px 20px;
    min-height: 150px;
  }

  .brand-desc {
    display: none;
  }

  .form-section {
    padding: 40px 30px;
  }
}
</style>