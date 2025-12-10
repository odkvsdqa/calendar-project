<template>
  <div class="header">
    <div class="header-content">   
      <!-- 左側：漢堡選單 + Logo -->
      <div class="left-section">
        <button class="menu-btn" @click="isDrawerOpen = true">
          <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
            <line x1="3" y1="12" x2="21" y2="12"></line>
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <line x1="3" y1="18" x2="21" y2="18"></line>
          </svg>
        </button>
        <div class="logo-wrapper">
          <SkjlLogo layout="horizontal" :show-tagline="false" mode="default" />
        </div>
      </div>
      
      <!-- 右側：導航 + 用戶資訊 -->
      <div class="right-section">
        <div class="nav-links">
          <router-link to="/calendar" class="nav-link">{{ $t('header.calendar') }}</router-link>
          <router-link v-if="isAdmin" to="/admin" class="nav-link">{{ $t('header.admin') }}</router-link>
        </div>
        <div class="divider"></div>
        <div class="user-section">
          <div class="user-info">
            <span class="username">{{ username }}</span>
            <span v-if="isAdmin" class="admin-badge">ADMIN</span>
          </div>
          <button class="btn-logout" @click="openLogoutModal">
            {{ $t('auth.logout') }}
          </button>
        </div>
      </div>
    </div>

    <!-- 設定側邊欄 (Drawer) -->
    <Teleport to="body">
      <div v-if="isDrawerOpen" class="drawer-overlay" @click="isDrawerOpen = false"></div>

      <div class="settings-drawer" :class="{ 'open': isDrawerOpen }">
        <div class="drawer-header">
          <h3>{{ $t('header.settings') }}</h3>
          <button class="btn-close-drawer" @click="isDrawerOpen = false">×</button>
        </div>
        
        <div class="drawer-content">
          
          <!-- 1. 導航 (僅手機版顯示) -->
          <div class="accordion-item mobile-only-nav">
            <div class="accordion-header" @click="toggleNav">
              <span class="accordion-title">📅 {{ $t('header.navigation') }}</span>
              <span class="arrow" :class="{ rotated: isNavExpanded }">▼</span>
            </div>
            <div v-show="isNavExpanded" class="accordion-body">
              <div class="drawer-nav-links">
                <router-link to="/calendar" class="drawer-link" @click="isDrawerOpen = false">
                  {{ $t('header.calendar') }}
                </router-link>
                <router-link v-if="isAdmin" to="/admin" class="drawer-link" @click="isDrawerOpen = false">
                  {{ $t('header.admin') }}
                </router-link>
              </div>
            </div>
          </div>
          <div class="drawer-divider mobile-only-nav"></div>

          <!-- 2. 場館訂閱 -->
          <div class="accordion-item">
            <div class="accordion-header" @click="toggleVenues">
              <span class="accordion-title">🎫 {{ $t('header.venues') }}</span>
              <span class="arrow" :class="{ rotated: isVenuesExpanded }">▼</span>
            </div>
            
            <div v-show="isVenuesExpanded" class="accordion-body">
              <div class="lang-options">
                <div 
                  v-for="venue in availableVenues" 
                  :key="venue.id" 
                  class="lang-item" 
                  :class="{ active: subscribedVenueIds.has(venue.id) }" 
                  @click="handleVenueClick(venue)"
                >
                  <span>{{ $t('venueNames.' + venue.id) }}</span>
                  <span v-if="subscribedVenueIds.has(venue.id)" class="check">✓</span>
                </div>
                
                <div v-if="isLoading" class="drawer-link" style="color:#999; font-size:12px;">
                  {{ $t('common.loading') }}
                </div>
              </div>
            </div>
          </div>

          <div class="drawer-divider"></div>

          <!-- 3. 主題切換 -->
          <div class="accordion-item">
            <div class="accordion-header" @click="toggleTheme">
              <span class="accordion-title">🎨 {{ $t('header.theme') }}</span>
              <span class="arrow" :class="{ rotated: isThemeExpanded }">▼</span>
            </div>
            <div v-show="isThemeExpanded" class="accordion-body">
              <div class="lang-options">
                <div class="lang-item" :class="{ active: currentTheme === 'light' }" @click="changeTheme('light')">
                  <span>{{ $t('theme.light') }}</span>
                  <span v-if="currentTheme === 'light'" class="check">✓</span>
                </div>
                <div class="lang-item" :class="{ active: currentTheme === 'dark' }" @click="changeTheme('dark')">
                  <span>{{ $t('theme.dark') }}</span>
                  <span v-if="currentTheme === 'dark'" class="check">✓</span>
                </div>
                <div class="lang-item" :class="{ active: currentTheme === 'system' }" @click="changeTheme('system')">
                  <span>{{ $t('theme.system') }}</span>
                  <span v-if="currentTheme === 'system'" class="check">✓</span>
                </div>
              </div>
            </div>
          </div>

          <div class="drawer-divider"></div>

          <!-- 4. 語言設定 -->
          <div class="accordion-item">
            <div class="accordion-header" @click="toggleLang">
              <span class="accordion-title">🌐 {{ $t('header.language') }}</span>
              <span class="arrow" :class="{ rotated: isLangExpanded }">▼</span>
            </div>
            <div v-show="isLangExpanded" class="accordion-body">
              <div class="lang-options">
                <div class="lang-item" :class="{ active: locale === 'zh-TW' }" @click="changeLocale('zh-TW')">
                  <span>繁體中文</span>
                  <span v-if="locale === 'zh-TW'" class="check">✓</span>
                </div>
                <div class="lang-item" :class="{ active: locale === 'en-US' }" @click="changeLocale('en-US')">
                  <span>English</span>
                  <span v-if="locale === 'en-US'" class="check">✓</span>
                </div>
                <div class="lang-item" :class="{ active: locale === 'ja-JP' }" @click="changeLocale('ja-JP')">
                  <span>日本語</span>
                  <span v-if="locale === 'ja-JP'" class="check">✓</span>
                </div>
              </div>
            </div>
          </div>

        </div>
        <div class="drawer-footer">
          <span class="version-text">SKJL Calendar v1.2</span>
        </div>
      </div>
    </Teleport>

    <!-- 登出確認 -->
    <BaseModal 
      :show="showLogoutModal" 
      :title="$t('auth.logoutConfirmTitle')"
      width="320px"
      @close="showLogoutModal = false"
    >
      <p style="text-align: center; color: #666; margin-bottom: 20px;">
        {{ $t('auth.logoutConfirmText') }}
      </p>
      <div class="confirm-actions">
        <button class="btn-cancel" @click="showLogoutModal = false">{{ $t('common.cancel') }}</button>
        <button class="btn-confirm" @click="confirmLogout">{{ $t('auth.confirmLogout') }}</button>
      </div>
    </BaseModal>

    <!-- 場館訂閱確認 -->
    <BaseModal 
      :show="showVenueConfirmModal" 
      :title="venueModalTitle"
      width="320px"
      @close="showVenueConfirmModal = false"
    >
      <p style="text-align: center; color: #666; margin-bottom: 20px; white-space: pre-line;">
        {{ venueModalText }}
      </p>
      <div class="confirm-actions">
        <button class="btn-cancel" @click="showVenueConfirmModal = false">{{ $t('common.cancel') }}</button>
        <button class="btn-confirm" @click="confirmSubscribeVenue">
           {{ isSubscribingAction ? $t('venue.subscribe') : $t('venue.unsubscribe') }}
        </button>
      </div>
    </BaseModal>

  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import SkjlLogo from '../SkjlLogo.vue'
import BaseModal from '../../components/common/BaseModal.vue'
import { preferenceApi } from '../../services/preferenceApi'
import { useVenues } from '../../composables/useVenues'
import { useTheme } from '../../composables/useTheme'

const props = defineProps({
  username: { type: String, required: true },
  userRole: { type: String, default: 'USER' }
})
const emit = defineEmits(['logout'])

const { t, locale } = useI18n()
const isAdmin = computed(() => props.userRole === 'ADMIN')

// 主題管理
const { currentTheme, setTheme } = useTheme()
const isThemeExpanded = ref(false)

// 狀態控制
const showLogoutModal = ref(false)
const isDrawerOpen = ref(false)

const isNavExpanded = ref(false)
const isLangExpanded = ref(false)
const isVenuesExpanded = ref(true)

// 場館訂閱
const { 
  availableVenues, 
  subscribedVenueIds, 
  toggleVenueSubscription, 
  fetchVenueList, 
  isLoading 
} = useVenues()

// 場館確認視窗
const showVenueConfirmModal = ref(false)
const targetVenue = ref(null)
const isSubscribingAction = ref(true)

const venueModalTitle = computed(() => 
  isSubscribingAction.value ? t('venue.subscribeConfirmTitle') : t('venue.unsubscribeConfirmTitle')
)
const venueModalText = computed(() => {
  if (!targetVenue.value) return ''
  const translatedName = t(`venueNames.${targetVenue.value.id}`)
  
  return isSubscribingAction.value 
    ? t('venue.subscribeConfirmText', { name: translatedName })
    : t('venue.unsubscribeConfirmText', { name: translatedName })
})

// Toggle 控制
const toggleNav = () => { isNavExpanded.value = !isNavExpanded.value }
const toggleLang = () => { isLangExpanded.value = !isLangExpanded.value }
const toggleVenues = () => { isVenuesExpanded.value = !isVenuesExpanded.value }
const toggleTheme = () => { isThemeExpanded.value = !isThemeExpanded.value }

// 場館訂閱處理
const handleVenueClick = (venue) => {
  const isSubscribed = subscribedVenueIds.value.has(venue.id)
  targetVenue.value = venue

  if (!isSubscribed) {
    isSubscribingAction.value = true
    showVenueConfirmModal.value = true
  } else {
    toggleVenueSubscription(venue.id)
  }
}

const confirmSubscribeVenue = async () => {
  if (targetVenue.value) {
    await toggleVenueSubscription(targetVenue.value.id)
  }
  showVenueConfirmModal.value = false
}

// 主題切換
const changeTheme = async (theme) => {
  await setTheme(theme)
}

// 語言切換（帶淡入淡出動畫）
const changeLocale = async (lang) => {
  const header = document.querySelector('.header')
  
  // 淡出
  header.style.opacity = '0'
  
  setTimeout(async () => {
    locale.value = lang
    localStorage.setItem('user-locale', lang)
    
    try {
      await preferenceApi.updateLanguage(lang)
    } catch (error) {
      console.warn('⚠️ 語言同步後端失敗:', error)
    }
    
    // 淡入
    setTimeout(() => {
      header.style.opacity = '1'
    }, 50)
  }, 200)
}

const openLogoutModal = () => showLogoutModal.value = true
const confirmLogout = () => {
  showLogoutModal.value = false
  emit('logout')
}

// 初始化
onMounted(async () => {
  // 載入場館列表
  fetchVenueList()
})
</script>

<style scoped>
.header { background: var(--header-bg); border-bottom: 1px solid var(--border-color); position: relative; z-index: 100; width: 100%; transition: opacity 0.3s ease; }
.header-content { width: 100%; height: 60px; padding: 0 20px; display: flex; justify-content: space-between; align-items: center; }
.left-section { display: flex; align-items: center; gap: 15px; }
.menu-btn { background: transparent; border: none; cursor: pointer; padding: 5px; color: #4a5d4a; display: flex; align-items: center; justify-content: center; transition: opacity 0.2s; }
.menu-btn:hover { opacity: 0.7; }
.right-section { display: flex; align-items: center; gap: 20px; }
.nav-links { display: flex; gap: 20px; }
.nav-link { text-decoration: none; color: #4a5d4a; font-size: 13px; padding: 5px 0; letter-spacing: 0.05em; transition: all 0.2s; border-bottom: 2px solid transparent; font-weight: 400; }
.nav-link:hover { color: #333; }
.nav-link.router-link-active { color: #557c55; border-bottom-color: #557c55; }
.divider { width: 1px; height: 24px; background: var(--divider-color); }
.user-section { display: flex; align-items: center; gap: 15px; }
.user-info { display: flex; flex-direction: column; align-items: flex-end; line-height: 1.2; }
.username { font-weight: 500; color: var(--text-primary); font-size: 13px; letter-spacing: 0.03em; }
.admin-badge { font-size: 10px; background: #fef3c7; color: #d97706; padding: 2px 6px; border-radius: 2px; font-weight: 500; letter-spacing: 0.05em; }
.btn-logout { background: white; border: 1px solid #557c55; color: #557c55; padding: 6px 14px; border-radius: 2px; cursor: pointer; font-size: 13px; transition: all 0.2s; letter-spacing: 0.05em; font-weight: 400; }
.btn-logout:hover { background: #fcfcfc; color: #333; border-color: #ccc; }
.confirm-actions { display: flex; gap: 12px; justify-content: center; }
.confirm-actions button { padding: 10px 20px; border-radius: 2px; border: none; font-size: 14px; cursor: pointer; transition: all 0.2s; }
.btn-cancel { background: var(--bg-hover); color: var(--text-secondary); border: 1px solid var(--border-color); }
.btn-cancel:hover { background: var(--bg-secondary); }
.btn-confirm { background: #333; color: white; }
.btn-confirm:hover { opacity: 0.9; }
.drawer-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.3); z-index: 2000; backdrop-filter: blur(2px); }
.settings-drawer { position: fixed; top: 0; left: 0; bottom: 0; width: 280px; background: var(--bg-secondary); z-index: 2001; box-shadow: 4px 0 16px rgba(0,0,0,0.1); display: flex; flex-direction: column; transform: translateX(-100%); transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.settings-drawer.open { transform: translateX(0); }
.drawer-header { padding: 15px 20px; border-bottom: 1px solid var(--divider-color); display: flex; justify-content: space-between; align-items: center; }
.drawer-header h3 { margin: 0; font-size: 18px; color: var(--text-primary); font-weight: 500; }
.btn-close-drawer { background: transparent; border: none; font-size: 24px; color: var(--text-muted); cursor: pointer; }
.drawer-content { flex: 1; padding: 20px; overflow-y: auto; }
.accordion-item { margin-bottom: 15px; }
.accordion-header { display: flex; justify-content: space-between; align-items: center; padding: 10px 5px; cursor: pointer; border-radius: 4px; transition: background 0.2s; }
.accordion-header:hover { background: var(--bg-hover); }
.accordion-title { font-size: 14px; font-weight: 500; color: var(--text-primary); }
.arrow { font-size: 10px; color: var(--text-muted); transition: transform 0.3s; }
.arrow.rotated { transform: rotate(180deg); }
.accordion-body { padding-left: 15px; padding-top: 5px; animation: fadeIn 0.2s ease-out; }
.lang-options { display: flex; flex-direction: column; gap: 2px; }
.lang-item { display: flex; justify-content: space-between; align-items: center; padding: 10px; cursor: pointer; border-radius: 4px; color: var(--text-secondary); font-size: 14px; transition: all 0.2s; }
.lang-item:hover { background: var(--bg-hover); }
.lang-item.active { background: #e8f5e9; color: #557c55; font-weight: 500; }
.check { font-weight: bold; }
.drawer-nav-links { display: flex; flex-direction: column; gap: 5px; }
.drawer-link { text-decoration: none; color: var(--text-secondary); padding: 10px; border-radius: 4px; font-size: 14px; transition: background 0.2s; }
.drawer-link:hover { background: var(--bg-hover); color: #557c55; }
.drawer-divider { border: 0; border-top: 1px solid var(--divider-color); margin: 15px 0; }
.mobile-only-nav { display: none; }
.drawer-footer { padding: 15px; border-top: 1px solid var(--divider-color); text-align: center; }
.version-text { font-size: 11px; color: var(--text-muted); }
@media (max-width: 600px) { .header-content { padding: 0 15px; } .tagline, .divider, .username, .admin-badge { display: none; } .nav-links { display: none; } .mobile-only-nav { display: block; } }
@keyframes fadeIn { from { opacity: 0; transform: translateY(-5px); } to { opacity: 1; transform: translateY(0); } }
</style>