import { request, ApiResponse, PageResponse } from '@/utils/request'

export interface ReportQuery {
  storeId?: number
  consumableId?: number
  categoryId?: number
  startDate?: string
  endDate?: string
  pageNum: number
  pageSize: number
}

export interface StockReportItem {
  id: number
  storeId: number
  storeName: string
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  categoryId: number
  categoryName: string
  quantity: number
  unitPrice: number
  totalAmount: number
  lastInboundDate?: string
  lastOutboundDate?: string
  warningQuantity?: number
  status: 'normal' | 'warning' | 'shortage'
}

export interface ConsumptionReportItem {
  id: number
  storeId: number
  storeName: string
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  categoryId: number
  categoryName: string
  quantity: number
  unitPrice: number
  totalAmount: number
  consumptionMonth: string
}

export interface TransferReportItem {
  id: number
  transferNo: string
  sourceStoreId: number
  sourceStoreName: string
  targetStoreId: number
  targetStoreName: string
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  quantity: number
  unitPrice: number
  totalAmount: number
  status: string
  transferDate: string
}

export interface DashboardData {
  totalStores: number
  totalConsumables: number
  totalStockQuantity: number
  totalStockAmount: number
  todayInboundCount: number
  todayOutboundCount: number
  todayTransferCount: number
  pendingApprovalCount: number
  pendingOutboundCount: number
  pendingReceiveCount: number
  warningStockCount: number
  shortageStockCount: number
  monthlyConsumptionTrend: {
    month: string
    amount: number
    quantity: number
  }[]
  categoryStockRatio: {
    categoryName: string
    amount: number
    ratio: number
  }[]
  storeStockRanking: {
    storeName: string
    amount: number
    quantity: number
  }[]
  recentTransfers: {
    id: number
    transferNo: string
    sourceStoreName: string
    targetStoreName: string
    status: string
    totalAmount: number
    createdAt: string
  }[]
  consumptionRanking: {
    consumableName: string
    quantity: number
    amount: number
  }[]
}

export const reportApi = {
  getStockReport(params: ReportQuery): Promise<ApiResponse<PageResponse<StockReportItem>>> {
    return request.get('/reports/stock', { params })
  },

  getConsumptionReport(params: ReportQuery): Promise<ApiResponse<PageResponse<ConsumptionReportItem>>> {
    return request.get('/reports/consumption', { params })
  },

  getTransferReport(params: ReportQuery): Promise<ApiResponse<PageResponse<TransferReportItem>>> {
    return request.get('/reports/transfer', { params })
  },

  getDashboardData(): Promise<ApiResponse<DashboardData>> {
    return request.get('/reports/dashboard')
  },

  exportStockReport(params: ReportQuery): Promise<ApiResponse<Blob>> {
    return request.get('/reports/stock/export', { params, responseType: 'blob' })
  },

  exportConsumptionReport(params: ReportQuery): Promise<ApiResponse<Blob>> {
    return request.get('/reports/consumption/export', { params, responseType: 'blob' })
  },

  exportTransferReport(params: ReportQuery): Promise<ApiResponse<Blob>> {
    return request.get('/reports/transfer/export', { params, responseType: 'blob' })
  }
}

export const getStockStatusText = (status: 'normal' | 'warning' | 'shortage'): string => {
  const map = {
    normal: '正常',
    warning: '预警',
    shortage: '短缺'
  }
  return map[status] || status
}

export const getStockStatusType = (status: 'normal' | 'warning' | 'shortage'): string => {
  const map = {
    normal: 'success',
    warning: 'warning',
    shortage: 'danger'
  }
  return map[status] || 'info'
}
