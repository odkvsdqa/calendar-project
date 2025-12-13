import { ref } from 'vue'
import { categoryApi } from '../services/categoryApi'
import { useI18n } from 'vue-i18n'

export function useCategories() {
  const { t } = useI18n()
  const categories = ref([])
  const isLoading = ref(false)
  
  /**
   * 載入所有類型
   */
  const loadCategories = async () => {
    isLoading.value = true
    try {
      const response = await categoryApi.getAvailableCategories()
      categories.value = response.data
    } catch (error) {
      console.error('載入類型失敗:', error)
    } finally {
      isLoading.value = false
    }
  }
  
  /**
   * 取得類型顯示名稱（支援 i18n）
   */
  const getCategoryDisplayName = (category) => {
    if (category.isSystem && category.nameKey) {
      return t(category.nameKey)
    }
    return category.name
  }
  
  /**
   * 新增自訂類型
   */
  const createCategory = async (categoryData) => {
    try {
      const response = await categoryApi.createCategory(categoryData)
      categories.value.push(response.data)
      return response.data
    } catch (error) {
      throw error
    }
  }
  
  /**
   * 更新類型
   */
  const updateCategory = async (id, categoryData) => {
    try {
      const response = await categoryApi.updateCategory(id, categoryData)
      const index = categories.value.findIndex(c => c.id === id)
      if (index !== -1) {
        categories.value[index] = response.data
      }
      return response.data
    } catch (error) {
      throw error
    }
  }
  
  /**
   * 刪除類型
   */
  const deleteCategory = async (id) => {
    try {
      await categoryApi.deleteCategory(id)
      categories.value = categories.value.filter(c => c.id !== id)
    } catch (error) {
      throw error
    }
  }
  
  return {
    categories,
    isLoading,
    loadCategories,
    getCategoryDisplayName,
    createCategory,
    updateCategory,
    deleteCategory
  }
}