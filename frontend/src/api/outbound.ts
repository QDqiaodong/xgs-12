import { request, ApiResponse, PageResponse } from '@/utils/request'

export interface OutboundOrder {
  id: number
  orderNo: string
  storeId: number
  storeName: string
  targetStoreId?: number
  targetStoreName?: string
  totalAmount: number
  status: number
  operator: string
  createdAt: string
  items: OutboundItem[]
}

export interface OutboundItem {
  id: number
  consumableId: number
  consumableName: string
  quantity: number
  unitPrice: number
  totalPrice: number
}

export interface OutboundQuery {
  orderNo?: string
  storeId?: number
  status?: number
  startDate?: string
  endDate?: string
  pageNum: number
  pageSize: number
}

export const outboundApi = {
  getList(params: OutboundQuery): Promise<ApiResponse<PageResponse<OutboundOrder>>> {
    return request.get('/outbound', { params })
  },
  
  getById(id: number): Promise<ApiResponse<OutboundOrder>> {
    return request.get(`/outbound/${id}`)
  },
  
  create(data: Partial<OutboundOrder>): Promise<ApiResponse<OutboundOrder>> {
    return request.post('/outbound', data)
  },
  
  update(id: number, data: Partial<OutboundOrder>): Promise<ApiResponse<OutboundOrder>> {
    return request.put(`/outbound/${id}`, data)
  },
  
  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/outbound/${id}`)
  }
}