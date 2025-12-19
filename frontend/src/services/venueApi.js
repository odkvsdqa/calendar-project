import api from './api'; // 引用原本的 axios 封裝

export default {
  // 取得可用場館列表 (例如: TMC, 大巨蛋)
  getVenueList() {
    return api.get('/venues/list');
  },

  // 取得特定場館的活動 (觸發後端爬蟲)
  getVenueEvents(venueId) {
    return api.get(`/venues/${venueId}/events`);
  },

  submitWish(name) {
    return api.post('/venues/wish', { name });
  },
  trackClick(venueId) {
    return api.post(`/venues/${venueId}/click`);
  },
  // Admin 用
  getWishes() {
    return api.get('/venues/admin/wishes');
  },
  getInteractions() {
    return api.get('/venues/admin/interactions');
  }
};