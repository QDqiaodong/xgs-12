import { request, ApiResponse, PageResponse } from '@/utils/request'

export type InventoryPlanStatus = 'draft' | 'pending' | 'in_progress' | 'completed' | 'cancelled'

export interface InventoryPlan {
  id: number
  planNo: string
  planName: string
  storeId: number
  storeName: string
  inventoryType: 'full' | 'partial' | 'cycle'
  status: InventoryPlanStatus
  planDate: string
  executor?: string
  remark?: string
  operator: string
  createdAt: string
  updatedAt: string
  items: InventoryPlanItem[]
}

export interface InventoryPlanItem {
  id: number
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  categoryId?: number
  categoryName?: string
}

export interface InventoryPlanCreateData {
  planName: string
  storeId: number
  inventoryType: 'full' | 'partial' | 'cycle'
  planDate: string
  items: {
    consumableId: number
  }[]
  remark?: string
}

export interface InventoryPlanUpdateData {
  planName?: string
  storeId?: number
  inventoryType?: 'full' | 'partial' | 'cycle'
  planDate?: string
  items?: {
    id?: number
    consumableId: number
  }[]
  remark?: string
}

export interface InventoryPlanQuery {
  planNo?: string
  planName?: string
  storeId?: number
  status?: InventoryPlanStatus
  inventoryType?: 'full' | 'partial' | 'cycle'
  startDate?: string
  endDate?: string
  pageNum: number
  pageSize: number
}

export interface InventoryRecord {
  id: number
  planId: number
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  systemQuantity: number
  actualQuantity: number
  differenceQuantity: number
  unitPrice: number
  differenceAmount: number
  differenceReason?: string
  operator: string
  createdAt: string
}

export interface InventoryExecuteData {
  planId: number
  items: {
    consumableId: number
    actualQuantity: number
    differenceReason?: string
  }[]
  remark?: string
}

export interface InventoryAnalysis {
  planId: number
  planName: string
  storeName: string
  totalItems: number
  checkedItems: number
  matchedItems: number
  differenceItems: number
  totalSystemQuantity: number
  totalActualQuantity: number
  totalDifferenceQuantity: number
  totalDifferenceAmount: number
  profitAmount: number
  lossAmount: number
  accuracyRate: number
  items: InventoryAnalysisItem[]
}

export interface InventoryAnalysisItem {
  id: number
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  systemQuantity: number
  actualQuantity: number
  differenceQuantity: number
  unitPrice: number
  differenceAmount: number
  differenceType: 'profit' | 'loss' | 'match'
  differenceReason?: string
}

export const inventoryApi = {
  createInventoryPlan(data: InventoryPlanCreateData): Promise<ApiResponse<InventoryPlan>> {
    return request.post('/inventory/plans', data)
  },

  getInventoryPlanList(params: InventoryPlanQuery): Promise<ApiResponse<PageResponse<InventoryPlan>>> {
    return request.get('/inventory/plans', { params })
  },

  getInventoryPlanDetail(id: number): Promise<ApiResponse<InventoryPlan>> {
    return request.get(`/inventory/plans/${id}`)
  },

  updateInventoryPlan(id: number, data: InventoryPlanUpdateData): Promise<ApiResponse<InventoryPlan>> {
    return request.put(`/inventory/plans/${id}`, data)
  },

  deleteInventoryPlan(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/inventory/plans/${id}`)
  },

  executeInventory(data: InventoryExecuteData): Promise<ApiResponse<InventoryRecord[]>> {
    return request.post('/inventory/execute', data)
  },

  getInventoryRecord(planId: number): Promise<ApiResponse<InventoryRecord[]>> {
    return request.get(`/inventory/records/${planId}`)
  },

  getInventoryAnalysis(planId: number): Promise<ApiResponse<InventoryAnalysis>> {
    return request.get(`/inventory/analysis/${planId}`)
  },

  cancelInventoryPlan(id: number): Promise<ApiResponse<InventoryPlan>> {
    return request.post(`/inventory/plans/${id}/cancel`)
  }
}

export const getInventoryStatusText = (status: InventoryPlanStatus): string => {
  const map: Record<InventoryPlanStatus, string> = {
    draft: '草稿',
    pending: '待执行',
    in_progress: '执行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

export const getInventoryStatusType = (status: InventoryPlanStatus): string => {
  const map: Record<InventoryPlanStatus, string> = {
    draft: 'info',
    pending: 'warning',
    in_progress: 'primary',
    completed: 'success',
    cancelled: 'danger'
  }
  return map[status] || 'info'
}

export const getInventoryTypeText = (type: 'full' | 'partial' | 'cycle'): string => {
  const map = {
    full: '全盘',
    partial: '抽盘',
    cycle: '循环盘点'
  }
  return map[type] || type
}
