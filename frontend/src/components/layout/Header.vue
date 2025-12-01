<template>
  <div class="header">
    <div class="header-content">
      <div class="logo-wrapper">
        <SkjlLogo layout="horizontal" :show-tagline="false" mode="default" />
      </div>
      <!-- 右側區塊 -->
      <div class="right-section">
        <!-- 導航 -->
        <div class="nav-links">
          <router-link to="/calendar" class="nav-link">日曆</router-link>
          <router-link v-if="isAdmin" to="/admin" class="nav-link">
            後台
          </router-link>
        </div>

        <div class="divider"></div>

        <!-- 用戶區 -->
        <div class="user-section">
          <div class="user-info">
            <span class="username">{{ username }}</span>
            <span v-if="isAdmin" class="admin-badge">ADMIN</span>
          </div>
          <button class="btn-logout" @click="openLogoutModal">登出</button>
        </div>
      </div>
    </div>

    <!-- 登出確認彈窗 -->
    <Teleport to="body">
      <div
        v-if="showLogoutModal"
        class="modal-overlay"
        @click.self="showLogoutModal = false"
      >
        <div class="confirm-box">
          <!-- ✅ 替換登出確認窗 -->
          <BaseModal
            :show="showLogoutModal"
            title="準備要離開了嗎？"
            width="320px"
            @close="showLogoutModal = false"
          >
            <p style="text-align: center; color: #666; margin-bottom: 20px">
              確定要登出目前的帳號嗎？
            </p>

            <!-- 自定義 Footer 按鈕 -->
            <div style="display: flex; gap: 10px; justify-content: center">
              <button class="btn-cancel" @click="showLogoutModal = false">
                取消
              </button>
              <button class="btn-confirm" @click="confirmLogout">
                確定登出
              </button>
            </div>
          </BaseModal>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import SkjlLogo from "../SkjlLogo.vue";
import BaseModal from "../common/BaseModal.vue";

const props = defineProps({
  username: { type: String, required: true },
  userRole: { type: String, default: "USER" },
});

const emit = defineEmits(["logout"]);

const isAdmin = computed(() => props.userRole === "ADMIN");
const showLogoutModal = ref(false);

const openLogoutModal = () => {
  showLogoutModal.value = true;
};

const confirmLogout = () => {
  showLogoutModal.value = false;
  emit("logout");
};
</script>

<style scoped>
/* 日系極簡 Header */
.header {
  background: #c4c0b4;
  border-bottom: 1px solid #eee;
  position: relative;
  z-index: 100;
  width: 100%;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 60px;
  padding: 0 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
/* 右側區塊 */
.right-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 導航 */
.nav-links {
  display: flex;
  gap: 20px;
}

.nav-link {
  text-decoration: none;
  color: #666;
  font-size: 13px;
  padding: 5px 0;
  letter-spacing: 0.05em;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
  font-weight: 400;
}

.nav-link:hover {
  color: #333;
}

.nav-link.router-link-active {
  color: #557c55;
  border-bottom-color: #667eea;
}

/* 分隔線 */
.divider {
  width: 1px;
  height: 24px;
  background: #ddd;
}

/* 用戶區 */
.user-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  line-height: 1.2;
}

.username {
  font-weight: 500;
  color: #333;
  font-size: 13px;
  letter-spacing: 0.03em;
}
.username,
.nav-link {
  /* 帶一點點綠的深灰，比純黑柔和 */
  color: #4a5d4a;
}
.admin-badge {
  font-size: 10px;
  background: #fef3c7;
  color: #d97706;
  padding: 2px 6px;
  border-radius: 2px;
  font-weight: 500;
  letter-spacing: 0.05em;
}

/* 登出按鈕 */
.btn-logout {
  background: white;
  border-color: #557c55;
  color: #557c55;
  padding: 6px 14px;
  border-radius: 2px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  letter-spacing: 0.05em;
  font-weight: 400;
}

.btn-logout:hover {
  background: #fcfcfc;
  color: #333;
  border-color: #ccc;
}

/* 彈窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease;
}

.confirm-box {
  background: white;
  padding: 30px;
  border-radius: 2px;
  width: 90%;
  max-width: 320px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.confirm-box h3 {
  color: #333;
  margin-bottom: 10px;
  font-size: 20px;
  font-weight: 500;
}

.confirm-box p {
  color: #666;
  margin-bottom: 24px;
  font-size: 15px;
}

.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.confirm-actions button {
  padding: 10px 20px;
  border-radius: 2px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f3f4f6;
  color: #4b5563;
  border: 1px solid #e0e0e0;
}

.btn-cancel:hover {
  background: #e5e7eb;
}

.btn-confirm {
  background: #333;
  color: white;
}

.btn-confirm:hover {
  opacity: 0.9;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* RWD */
@media (max-width: 768px) {
  .header-content {
    padding: 0 20px;
  }

  .logo {
    font-size: 20px;
  }

  .tagline {
    display: none;
  }

  .divider {
    display: none;
  }

  .username {
    display: none;
  }

  .admin-badge {
    display: none;
  }

  .nav-links {
    gap: 15px;
  }

  .nav-link {
    font-size: 12px;
  }
}
</style>
