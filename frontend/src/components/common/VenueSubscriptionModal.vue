<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="close">
    <div class="modal-card">
      <div class="modal-header">
        <h3>探索活動 / 訂閱場館</h3>
        <button class="close-btn" @click="close">×</button>
      </div>

      <div class="modal-body">
        <p class="desc">訂閱後，該場館的行程將自動同步至您的日曆。</p>
        
        <div v-if="isLoading" class="loading">
          載入中...
        </div>

        <div v-else class="venue-list">
          <div 
            v-for="venue in availableVenues" 
            :key="venue.id" 
            class="venue-item"
          >
            <div class="venue-info">
              <span class="venue-name">{{ venue.name }}</span>
              <span class="venue-id">({{ venue.id.toUpperCase() }})</span>
            </div>
            
            <!-- 仿 iOS 風格的 Toggle Switch -->
            <label class="switch">
              <input 
                type="checkbox" 
                :checked="subscribedVenueIds.has(venue.id)"
                @change="toggleVenueSubscription(venue.id)"
              >
              <span class="slider round"></span>
            </label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useVenues } from '../../composables/useVenues';

const props = defineProps({
  isOpen: Boolean
});

const emit = defineEmits(['close']);

const { 
  availableVenues, 
  subscribedVenueIds, 
  toggleVenueSubscription, 
  fetchVenueList, 
  isLoading 
} = useVenues();

const close = () => emit('close');

onMounted(() => {
  fetchVenueList();
});
</script>

<style scoped>
/* 日系極簡 Modal 風格 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.4); /* 輕微遮罩 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(2px); /* 挪威森林霧氣感 */
}

.modal-card {
  background: #ffffff;
  width: 90%;
  max-width: 400px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  padding: 24px;
  animation: slideUp 0.3s ease-out;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.modal-header h3 {
  font-size: 1.2rem;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
}

.desc {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 20px;
}

.venue-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.venue-name {
  font-weight: 500;
  color: #333;
}

.venue-id {
  font-size: 0.8rem;
  color: #999;
  margin-left: 8px;
}

/* Toggle Switch CSS (森綠色 #557c55) */
.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.switch input { opacity: 0; width: 0; height: 0; }

.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #ccc;
  transition: .4s;
}

.slider.round { border-radius: 34px; }
.slider.round:before { border-radius: 50%; }

.slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: .4s;
}

input:checked + .slider {
  background-color: #557c55; /* 主色：森綠色 */
}

input:checked + .slider:before {
  transform: translateX(20px);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>