import { request, ApiResponse, PageResponse } from '@/utils/request'

export interface InboundOrder {
  id: number
  orderNo: string
  storeId: number
  storeName: string
  supplierId: number
  supplierName: string
  totalAmount: number
  status: number
  operator: string
  createdAt: string
  items: InboundItem[]
}

export interface InboundItem {
  id: number
  consumableId: number
  consumableName: string
  quantity: number
  unitPrice: number
  totalPrice: number
}

export interface InboundQuery {
  orderNo?: string
  storeId?: number
  status?: number
  startDate?: string
  endDate?: string
  pageNum: number
  pageSize: number
}

export const inboundApi = {
  getList(params: InboundQuery): Promise<ApiResponse<PageResponse<InboundOrder>>> {
    return request.get('/inbound', { params })
  },
  
  getById(id: number): Promise<ApiResponse<InboundOrder>> {
    return request.get(`/inbound/${id}`)
  },
  
  create(data: Partial<InboundOrder>): Promise<ApiResponse<InboundOrder>> {
    return request.post('/inbound', data)
  },
  
  update(id: number, data: Partial<InboundOrder>): Promise<ApiResponse<InboundOrder>> {
    return request.put(`/inbound/${id}`, data)
  },
  
  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/inbound/${id}`)
  }
}