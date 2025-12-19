import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import venueApi from "../services/venueApi";
import { useToast } from "./useToast";

const venueList = ref([]); // 純場館
const holidayList = ref([]); // 純假日
// 定義 LocalStorage 的 Key
const STORAGE_KEY = "skjl_subscribed_venues";

// 1. 初始化時，嘗試從 LocalStorage 讀取舊的訂閱紀錄
const savedIds = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");

// 全域狀態
const availableVenues = ref([]);
const subscribedVenueIds = ref(new Set(savedIds)); // 使用讀取到的紀錄初始化
const externalEventsMap = ref({});
const isLoading = ref(false);

// 建議在 useVenues.js 加入快取機制
const cachedData = ref(new Map());

const loadVenueData = async (venueId) => {
  if (cachedData.value.has(venueId)) {
    return cachedData.value.get(venueId);
  }
  // 爬取新資料
  cachedData.value.set(venueId, data);
};

export function useVenues() {
  const { showToast } = useToast();
  const { locale } = useI18n();

  // 取得場館列表 (含多語言)
  const fetchVenueList = async () => {
    try {
      const res = await venueApi.getVenueList(locale.value);
      const allItems = res.data;

      // 🔥 關鍵邏輯：拆分清單
      // 假設後端回傳的 ID 是 holiday-tw, holiday-jp
      holidayList.value = allItems.filter((v) => v.id.startsWith("holiday-"));
      venueList.value = allItems.filter((v) => !v.id.startsWith("holiday-"));

      // availableVenues 保持原樣，用於邏輯查找 (如果其他地方有用到)
      availableVenues.value = allItems;
    } catch (error) {
      console.error("無法取得列表", error);
    }
  };

  // 監聽語言改變
  watch(locale, () => {
    fetchVenueList();
  });

  // 獨立出的抓取邏輯 (供 toggle 和 restore 共用)
  const loadVenueData = async (venueId) => {
    // 如果快取已經有了，就不重抓
    if (externalEventsMap.value[venueId]) return;

    isLoading.value = true;
    try {
      const res = await venueApi.getVenueEvents(venueId);
      const formattedEvents = res.data.map((evt) => ({
        ...evt,
        isExternal: true,
        source: venueId,
      }));
      externalEventsMap.value[venueId] = formattedEvents;
      // 這裡不顯示 Toast，避免重整時跳出一堆通知
    } catch (error) {
      console.error(`無法載入場館資料: ${venueId}`, error);
      // 如果抓取失敗，暫時不取消訂閱，讓使用者稍後重試
    } finally {
      isLoading.value = false;
    }
  };

  // 切換訂閱狀態
  const toggleVenueSubscription = async (venueId) => {
    if (subscribedVenueIds.value.has(venueId)) {
      // 取消訂閱
      subscribedVenueIds.value.delete(venueId);
    } else {
      // 新增訂閱
      subscribedVenueIds.value.add(venueId);
      await loadVenueData(venueId);
      showToast("已載入活動", "success");
    }
    // 2. 狀態改變時，同步存入 LocalStorage
    saveToStorage();
  };

  // 存檔 helper
  const saveToStorage = () => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify([...subscribedVenueIds.value])
    );
  };

  // 3. 🔥 新增：還原訂閱資料 (給 App 啟動時呼叫)
  const restoreSubscriptions = async () => {
    if (subscribedVenueIds.value.size === 0) return;

    console.log("正在還原訂閱場館資料...");
    const promises = [];
    for (const venueId of subscribedVenueIds.value) {
      promises.push(loadVenueData(venueId));
    }
    await Promise.all(promises);
  };

  const allExternalEvents = computed(() => {
    let all = [];
    subscribedVenueIds.value.forEach((id) => {
      if (externalEventsMap.value[id]) {
        all = all.concat(externalEventsMap.value[id]);
      }
    });
    return all;
  });

  return {
    venueList, // 匯出這兩個
    holidayList, // 匯出這兩個
    availableVenues,
    subscribedVenueIds,
    isLoading,
    fetchVenueList,
    toggleVenueSubscription,
    restoreSubscriptions, // 記得匯出這個
    allExternalEvents,
  };
}
