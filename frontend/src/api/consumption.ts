import { request, ApiResponse, PageResponse } from '@/utils/request'

export interface ConsumptionRecord {
  id: number
  storeId: number
  storeName: string
  materialId: number
  materialName: string
  specification: string
  unit: string
  year: number
  month: number
  quantity: number
  totalCost: number
  remark?: string
  createTime: string
}

export interface ConsumptionCreateData {
  storeId: number
  materialId: number
  quantity: number
  unitCost?: number
  remark?: string
}

export interface ConsumptionQuery {
  storeId?: number
  materialId?: number
  year?: number
  month?: number
  current: number
  size: number
}

export interface MonthlyConsumptionSummary {
  storeId: number
  storeName: string
  year: number
  month: number
  totalQuantity: number
  totalCost: number
  details: ConsumptionRecord[]
}

export const consumptionApi = {
  createConsumption(data: ConsumptionCreateData): Promise<ApiResponse<number>> {
    return request.post('/consumptions', data)
  },

  getMonthlyConsumption(storeId: number, year: number, month: number): Promise<ApiResponse<MonthlyConsumptionSummary>> {
    return request.get('/consumptions/monthly', { params: { storeId, year, month } })
  },

  getStoreConsumption(storeId: number): Promise<ApiResponse<ConsumptionRecord[]>> {
    return request.get(`/consumptions/store/${storeId}`)
  },

  updateConsumption(id: number, data: ConsumptionCreateData): Promise<ApiResponse<void>> {
    return request.put(`/consumptions/${id}`, data)
  },

  deleteConsumption(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/consumptions/${id}`)
  },

  getConsumptionList(params: ConsumptionQuery): Promise<ApiResponse<PageResponse<ConsumptionRecord>>> {
    return request.get('/consumptions', { params })
  }
}
