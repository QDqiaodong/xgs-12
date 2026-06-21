import { request, ApiResponse, PageResponse } from '@/utils/request'

export interface Store {
  id: number
  storeCode: string
  storeName: string
  storeType: string
  address: string
  contactPerson: string
  contactPhone: string
  status: number
  createTime: string
  updateTime: string
}

export interface StoreQuery {
  storeCode?: string
  storeName?: string
  storeType?: string
  status?: number
  current: number
  size: number
}

export const storeApi = {
  getList(params: StoreQuery): Promise<ApiResponse<PageResponse<Store>>> {
    return request.get('/api/stores', { params })
  },
  
  getById(id: number): Promise<ApiResponse<Store>> {
    return request.get(`/api/stores/${id}`)
  },
  
  create(data: Partial<Store>): Promise<ApiResponse<number>> {
    return request.post('/api/stores', data)
  },
  
  update(id: number, data: Partial<Store>): Promise<ApiResponse<void>> {
    return request.put(`/api/stores/${id}`, data)
  },
  
  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/api/stores/${id}`)
  }
}
