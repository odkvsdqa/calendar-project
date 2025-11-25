// ✅ 封裝成 Composable
// composables/useAsync.js
export function useAsync(asyncFunction) {
  const loading = ref(false)
  const error = ref(null)
  
  const execute = async (...args) => {
    loading.value = true
    error.value = null
    try {
      return await asyncFunction(...args)
    } catch (err) {
      error.value = handleApiError(err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  return { loading, error, execute }
}