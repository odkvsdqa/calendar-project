import api from './api'

export const preferenceApi = {
  getLanguage() {
    return api.get('/preferences/language')
  },
  updateLanguage(language) {
    return api.put('/preferences/language', { language })
  }
}