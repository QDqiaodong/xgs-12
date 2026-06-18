import { request, ApiResponse, PageResponse } from '@/utils/request'

export interface Consumable {
  id: number
  name: string
  code: string
  category: string
  unit: string
  specification: string
  price: number
  stock: number
  warningStock: number
  createdAt: string
  updatedAt: string
}

export interface ConsumableQuery {
  name?: string
  code?: string
  category?: string
  pageNum: number
  pageSize: number
}

export const consumableApi = {
  getList(params: ConsumableQuery): Promise<ApiResponse<PageResponse<Consumable>>> {
    return request.get('/consumables', { params })
  },
  
  getById(id: number): Promise<ApiResponse<Consumable>> {
    return request.get(`/consumables/${id}`)
  },
  
  create(data: Partial<Consumable>): Promise<ApiResponse<Consumable>> {
    return request.post('/consumables', data)
  },
  
  update(id: number, data: Partial<Consumable>): Promise<ApiResponse<Consumable>> {
    return request.put(`/consumables/${id}`, data)
  },
  
  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/consumables/${id}`)
  }
}