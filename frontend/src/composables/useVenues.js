import { ref, computed } from 'vue';
import venueApi from '../services/venueApi'; // ⚠️ 注意這裡引用了 venueApi
import { useToast } from './useToast';

// 全域狀態 (Global State)，確保切換頁面後訂閱狀態還在
const availableVenues = ref([]); // 後端回傳的場館清單
const subscribedVenueIds = ref(new Set()); // 使用者目前打勾的場館 ID
const externalEventsMap = ref({}); // 快取下載下來的活動 { 'tmc': [events...] }
const isLoading = ref(false);

export function useVenues() {
  const { showToast } = useToast();

  // 初始化：取得可用場館
  const fetchVenueList = async () => {
    try {
      const res = await venueApi.getVenueList();
      availableVenues.value = res.data;
    } catch (error) {
      console.error('無法取得場館列表', error);
    }
  };

  // 切換訂閱狀態
  const toggleVenueSubscription = async (venueId) => {
    // 1. 如果已經訂閱，則取消
    if (subscribedVenueIds.value.has(venueId)) {
      subscribedVenueIds.value.delete(venueId);
      return;
    }

    // 2. 如果尚未訂閱，則加入並撈取資料
    subscribedVenueIds.value.add(venueId);

    // 如果快取沒有資料，才去後端爬
    if (!externalEventsMap.value[venueId]) {
      isLoading.value = true;
      try {
        const res = await venueApi.getVenueEvents(venueId);
        // 標記這些是外部事件，方便前端顯示不同樣式
        const formattedEvents = res.data.map(evt => ({
          ...evt,
          isExternal: true,
          source: venueId
        }));
        externalEventsMap.value[venueId] = formattedEvents;
        showToast(`已載入 ${formattedEvents.length} 筆活動`, 'success');
      } catch (error) {
        showToast('無法載入活動，請稍後再試', 'error');
        subscribedVenueIds.value.delete(venueId); // 失敗就取消勾選
      } finally {
        isLoading.value = false;
      }
    }
  };

  // 計算出所有「外部事件」給日曆顯示
  const allExternalEvents = computed(() => {
    let all = [];
    subscribedVenueIds.value.forEach(id => {
      if (externalEventsMap.value[id]) {
        all = all.concat(externalEventsMap.value[id]);
      }
    });
    return all;
  });

  return {
    availableVenues,
    subscribedVenueIds,
    isLoading,
    fetchVenueList,
    toggleVenueSubscription,
    allExternalEvents
  };
}