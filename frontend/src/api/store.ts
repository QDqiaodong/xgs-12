import { request, ApiResponse, PageResponse } from '@/utils/request'

export interface Store {
  id: number
  name: string
  code: string
  address: string
  phone: string
  manager: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface StoreQuery {
  name?: string
  code?: string
  status?: number
  pageNum: number
  pageSize: number
}

export const storeApi = {
  getList(params: StoreQuery): Promise<ApiResponse<PageResponse<Store>>> {
    return request.get('/stores', { params })
  },
  
  getById(id: number): Promise<ApiResponse<Store>> {
    return request.get(`/stores/${id}`)
  },
  
  create(data: Partial<Store>): Promise<ApiResponse<Store>> {
    return request.post('/stores', data)
  },
  
  update(id: number, data: Partial<Store>): Promise<ApiResponse<Store>> {
    return request.put(`/stores/${id}`, data)
  },
  
  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/stores/${id}`)
  }
}