import { request, ApiResponse, PageResponse } from '@/utils/request'

export interface ConsumptionRecord {
  id: number
  storeId: number
  storeName: string
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  quantity: number
  unitPrice: number
  totalAmount: number
  consumptionMonth: string
  remark?: string
  operator: string
  createdAt: string
  updatedAt: string
}

export interface ConsumptionCreateData {
  storeId: number
  consumptionMonth: string
  items: {
    consumableId: number
    quantity: number
    remark?: string
  }[]
  remark?: string
}

export interface ConsumptionUpdateData {
  items: {
    id?: number
    consumableId: number
    quantity: number
    remark?: string
  }[]
  remark?: string
}

export interface ConsumptionQuery {
  storeId?: number
  consumableId?: number
  consumptionMonth?: string
  startMonth?: string
  endMonth?: string
  pageNum: number
  pageSize: number
}

export interface MonthlyConsumptionSummary {
  consumptionMonth: string
  totalAmount: number
  totalQuantity: number
  storeCount: number
  itemCount: number
}

export interface StoreConsumptionSummary {
  storeId: number
  storeName: string
  totalAmount: number
  totalQuantity: number
  itemCount: number
}

export const consumptionApi = {
  createConsumption(data: ConsumptionCreateData): Promise<ApiResponse<ConsumptionRecord[]>> {
    return request.post('/consumption', data)
  },

  getMonthlyConsumption(params: ConsumptionQuery): Promise<ApiResponse<PageResponse<MonthlyConsumptionSummary>>> {
    return request.get('/consumption/monthly', { params })
  },

  getStoreConsumption(storeId: number, params: ConsumptionQuery): Promise<ApiResponse<PageResponse<ConsumptionRecord>>> {
    return request.get(`/consumption/store/${storeId}`, { params })
  },

  updateConsumption(id: number, data: ConsumptionUpdateData): Promise<ApiResponse<ConsumptionRecord>> {
    return request.put(`/consumption/${id}`, data)
  },

  deleteConsumption(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/consumption/${id}`)
  },

  getConsumptionDetail(id: number): Promise<ApiResponse<ConsumptionRecord>> {
    return request.get(`/consumption/${id}`)
  },

  getConsumptionList(params: ConsumptionQuery): Promise<ApiResponse<PageResponse<ConsumptionRecord>>> {
    return request.get('/consumption', { params })
  }
}
