import api from './api'

export const categoryApi = {
  /**
   * 取得所有可用類型（系統 + 自訂）
   */
  getAvailableCategories() {
    return api.get('/categories')
  },
  
  /**
   * 新增自訂類型
   */
  createCategory(categoryData) {
    return api.post('/categories', categoryData)
  },
  
  /**
   * 更新自訂類型
   */
  updateCategory(id, categoryData) {
    return api.put(`/categories/${id}`, categoryData)
  },
  
  /**
   * 刪除自訂類型
   */
  deleteCategory(id) {
    return api.delete(`/categories/${id}`)
  }
}