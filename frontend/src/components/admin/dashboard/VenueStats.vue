<template>
  <div class="query-section">
    <!-- 🔥 I18N 修正 -->
    <h2>📊 {{ $t("admin.components.venueStats.title") }}</h2>

    <div class="stats-container">
      <!-- 左側：點擊熱度 -->
      <div class="stats-panel">
        <!-- 🔥 I18N 修正 -->
        <h3>🔥 {{ $t("admin.components.venueStats.popularity") }}</h3>

        <div v-if="interactions.length > 0" class="ranking-list">
          <div
            v-for="(item, index) in sortedInteractions"
            :key="item.venueId"
            class="rank-item"
          >
            <span class="rank-num" :class="'top-' + (index + 1)">{{
              index + 1
            }}</span>
            <span class="rank-name">{{
              $t("venueNames." + item.venueId)
            }}</span>
            <span class="rank-count">{{ item.clickCount }}次</span>
          </div>
        </div>
        <!-- 🔥 I18N 修正：空狀態 -->
        <div v-else class="empty-text">
          {{
            $t("admin.components.monthlyStats.empty").replace(
              "請選擇月份查看統計報表",
              "尚無數據"
            )
          }}
        </div>
        <!-- 這裡我先借用現有 key，如果要完美可以新增 admin.components.venueStats.empty -->
      </div>

      <!-- 右側：許願池 -->
      <div class="stats-panel">
        <!-- 🔥 I18N 修正 -->
        <h3>🌠 {{ $t("admin.components.venueStats.wishlist") }}</h3>

        <div v-if="wishes.length > 0" class="ranking-list">
          <div v-for="(item, index) in wishes" :key="item.id" class="rank-item">
            <span class="rank-num">{{ index + 1 }}</span>
            <span class="rank-name">{{ item.venueName }}</span>
            <span class="rank-count">{{ item.count }}票</span>
          </div>
        </div>
        <div v-else class="empty-text">尚無數據</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import venueApi from "../../../services/venueApi";

const wishes = ref([]);
const interactions = ref([]);

const loadData = async () => {
  try {
    const [wishesRes, interactRes] = await Promise.all([
      venueApi.getWishes(),
      venueApi.getInteractions(),
    ]);
    wishes.value = wishesRes.data;
    interactions.value = interactRes.data;
  } catch (err) {
    console.error("Failed to load stats", err);
  }
};

// 根據點擊數排序
const sortedInteractions = computed(() => {
  return [...interactions.value].sort((a, b) => b.clickCount - a.clickCount);
});

onMounted(loadData);
</script>

<style scoped>
.stats-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.stats-panel {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #eee;
}
h3 {
  margin-top: 0;
  color: #557c55;
  font-size: 16px;
  border-bottom: 2px solid #f3f4f6;
  padding-bottom: 10px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.rank-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px;
  background: #fafbf9;
  border-radius: 6px;
}
.rank-num {
  width: 24px;
  height: 24px;
  background: #ddd;
  color: #666;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  margin-right: 10px;
}
.rank-name {
  flex: 1;
  font-weight: 500;
  color: #333;
}
.rank-count {
  font-weight: bold;
  color: #557c55;
}

/* 前三名顏色 */
.top-1 {
  background: #ffd700;
  color: #fff;
}
.top-2 {
  background: #c0c0c0;
  color: #fff;
}
.top-3 {
  background: #cd7f32;
  color: #fff;
}

.empty-text {
  color: #999;
  text-align: center;
  padding: 20px;
}

@media (max-width: 768px) {
  .stats-container {
    grid-template-columns: 1fr;
  }
}
</style>
