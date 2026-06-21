import { request, ApiResponse } from '@/utils/request'

export interface ReportQuery {
  storeId?: number
  materialId?: number
  category?: string
  year?: number
  month?: number
  startDate?: string
  endDate?: string
}

export interface StockReportItem {
  storeId: number
  storeName: string
  materialId: number
  materialName: string
  specification: string
  unit: string
  quantity: number
  lockedQuantity: number
  availableQuantity: number
  avgCost: number
  stockValue: number
  safetyStock?: number
  stockStatus: string
}

export interface ConsumptionReportItem {
  storeId: number
  storeName: string
  materialId: number
  materialName: string
  specification: string
  unit: string
  quantity: number
  totalCost: number
  year: number
  month: number
}

export interface TransferReportItem {
  id: number
  orderNo: string
  sourceStoreName: string
  targetStoreName: string
  totalQuantity: number
  totalCost: number
  statusDesc: string
  applyTime: string
  receiveTime?: string
}

export interface StockAlert {
  materialId: number
  materialCode: string
  materialName: string
  category: string
  specification: string
  unit: string
  purchaseCost: number
  safetyStock?: number
  currentStock?: number
  alertType: number
  alertMessage: string
}

export interface DashboardData {
  totalStores: number
  totalMaterials: number
  totalStockValue: number
  monthlyConsumptionCost: number
  monthlyConsumptionQuantity: number
  monthlyTransferCount: number
  pendingApprovalCount: number
  pendingOutboundCount: number
  pendingReceiveCount: number
  stockAlertCount: number
  ongoingInventoryCount: number
  consumptionTrend: Array<Record<string, any>>
  stockByCategory: Array<Record<string, any>>
  storeStockRanking: Array<Record<string, any>>
  stockAlerts: StockAlert[]
}

export const reportApi = {
  getStockReport(params: ReportQuery): Promise<ApiResponse<StockReportItem[]>> {
    return request.get('/reports/stock', { params })
  },

  getConsumptionReport(params: ReportQuery): Promise<ApiResponse<ConsumptionReportItem[]>> {
    return request.get('/reports/consumption', { params })
  },

  getTransferReport(params: ReportQuery): Promise<ApiResponse<TransferReportItem[]>> {
    return request.get('/reports/transfer', { params })
  },

  getDashboardData(): Promise<ApiResponse<DashboardData>> {
    return request.get('/reports/dashboard')
  }
}

export const getStockStatusText = (status: string): string => {
  const map: Record<string, string> = {
    '正常': '正常',
    '预警': '预警',
    '短缺': '短缺'
  }
  return map[status] || status
}

export const getStockStatusType = (status: string): string => {
  const map: Record<string, string> = {
    '正常': 'success',
    '预警': 'warning',
    '短缺': 'danger'
  }
  return map[status] || 'info'
}
