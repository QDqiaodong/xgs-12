import { ref, onMounted, onUnmounted } from 'vue'
import { debounce } from 'lodash-es'

export const useWindowSize = () => {
  const width = ref(window.innerWidth)
  const height = ref(window.innerHeight)

  const update = () => {
    width.value = window.innerWidth
    height.value = window.innerHeight
  }

  const debouncedUpdate = debounce(update, 100)

  onMounted(() => {
    window.addEventListener('resize', debouncedUpdate)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', debouncedUpdate)
  })

  return { width, height }
}

export const useLoading = (initialState = false) => {
  const loading = ref(initialState)

  const startLoading = () => {
    loading.value = true
  }

  const stopLoading = () => {
    loading.value = false
  }

  const withLoading = async <T>(fn: () => Promise<T>): Promise<T> => {
    startLoading()
    try {
      return await fn()
    } finally {
      stopLoading()
    }
  }

  return {
    loading,
    startLoading,
    stopLoading,
    withLoading
  }
}

export const useTable = <T extends Record<string, any>>(
  fetchFn: (params: any) => Promise<any>
) => {
  const loading = ref(false)
  const tableData = ref<T[]>([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(10)
  const searchForm = ref<Record<string, any>>({})

  const fetchData = async () => {
    loading.value = true
    try {
      const res = await fetchFn({
        ...searchForm.value,
        current: pageNum.value,
        size: pageSize.value
      })
      tableData.value = res.data.records
      total.value = res.data.total
    } catch (error) {
      console.error('Failed to fetch data:', error)
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    pageNum.value = 1
    fetchData()
  }

  const handleReset = () => {
    searchForm.value = {}
    handleSearch()
  }

  const handlePageChange = (page: number) => {
    pageNum.value = page
    fetchData()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    pageNum.value = 1
    fetchData()
  }

  onMounted(() => {
    fetchData()
  })

  return {
    loading,
    tableData,
    total,
    pageNum,
    pageSize,
    searchForm,
    fetchData,
    handleSearch,
    handleReset,
    handlePageChange,
    handleSizeChange
  }
}