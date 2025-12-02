<template>
  <div class="login-view">
    <div class="login-wrapper">
      
      <!-- 左側：品牌展示 -->
      <div class="brand-section">
        <div class="brand-content">
          <SkjlLogo layout="vertical" mode="white"/>
          
          <!-- 🔥 修改：使用 i18n 翻譯，並保留換行效果 (pre-line) -->
          <p class="brand-desc">{{ $t('auth.sloganDesc') }}</p>
          
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
        </div>
      </div>

      <!-- 右側：登入表單 -->
      <div class="form-section">
        <div class="form-container">
          <LoginForm />
        </div>
        <div class="footer-links">
          <span>SKJL © 2025</span>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import LoginForm from '../components/auth/LoginForm.vue'
import SkjlLogo from '../components/SkjlLogo.vue'
</script>

<style scoped>
.login-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  padding: 20px;
}

.login-wrapper {
  background: white;
  width: 100%;
  max-width: 1000px;
  height: 600px; /* 固定高度讓畫面更有設計感 */
  display: flex;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 80px rgba(0, 0, 0, 0.1);
}

/* --- 左側品牌區 --- */
.brand-section {
  flex: 1;
   /* 👇 修改背景：灰橄欖綠 -> 淺灰綠 */
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
  /* 👇 文字改米白色，與和紙主題呼應 */
  color: #fdf5e6; 
  font-size: 16px;
  line-height: 1.8;
  letter-spacing: 1px;
  font-weight: 400;
  opacity: 0.8;

  /* 🔥 新增：讓 \n 換行符號生效 */
  white-space: pre-line; 
}

/* 裝飾用的背景圓圈 */
.decoration-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(50px);
  opacity: 0.5; /* 稍微透明一點，融入背景 */
  z-index: 1;
}

.circle-1 {
  width: 200px;
  height: 200px;
  /* 👇 修改：米白色 (象徵紙張) */
  background: #fdf5e6;
  top: -50px;
  left: -50px;
  animation: float 6s ease-in-out infinite;
}

.circle-2 {
  width: 150px;
  height: 150px;
 /* 👇 修改：淺草綠 (象徵自然) */
  background: #aebc9e; 
  bottom: -30px;
  right: -30px;
  animation: float 8s ease-in-out infinite reverse;
}

/* --- 右側表單區 --- */
.form-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: white;
  padding: 40px;
}

.form-container {
  width: 100%;
  max-width: 360px; /* 限制表單寬度 */
}

.footer-links {
  margin-top: 40px;
  font-size: 12px;
  color: #ccc;
}

/* 簡單的漂浮動畫 */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(20px); }
}

/* --- RWD: 手機版堆疊顯示 --- */
@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    height: auto;
    max-width: 450px;
  }

  .brand-section {
    padding: 40px 20px 20px;
    min-height: 200px;
  }

  .brand-desc {
    display: none; /* 手機版隱藏描述，保持簡潔 */
  }

  .form-section {
    padding: 40px 30px;
  }
}
</style>