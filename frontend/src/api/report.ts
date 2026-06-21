import { request, ApiResponse } from '@/utils/request'

export type StockStatusType = 'normal' | 'warning' | 'shortage' | 'negative'

export interface StockStatusConfig {
  status: StockStatusType
  label: string
  color: string
  bgColor: string
  tagType: '' | 'success' | 'warning' | 'danger' | 'info'
  iconName: string
}

export const STOCK_STATUS_CONFIG: Record<StockStatusType, StockStatusConfig> = {
  normal: {
    status: 'normal',
    label: '正常',
    color: '#059669',
    bgColor: '#ECFDF5',
    tagType: 'success',
    iconName: 'CircleCheck'
  },
  warning: {
    status: 'warning',
    label: '预警',
    color: '#F59E0B',
    bgColor: '#FFFBEB',
    tagType: 'warning',
    iconName: 'Warning'
  },
  shortage: {
    status: 'shortage',
    label: '短缺',
    color: '#DC2626',
    bgColor: '#FEF2F2',
    tagType: 'danger',
    iconName: 'WarningFilled'
  },
  negative: {
    status: 'negative',
    label: '负库存',
    color: '#7C3AED',
    bgColor: '#F5F3FF',
    tagType: 'danger',
    iconName: 'CircleCloseFilled'
  }
}

export const getStockStatusConfig = (status: string | StockStatusType): StockStatusConfig => {
  const normalized = String(status).toLowerCase()
  const keyMap: Record<string, StockStatusType> = {
    'normal': 'normal',
    'warning': 'warning',
    'shortage': 'shortage',
    'negative': 'negative',
    '正常': 'normal',
    '预警': 'warning',
    '短缺': 'shortage',
    '负库存': 'negative'
  }
  const key = keyMap[normalized] || 'normal'
  return STOCK_STATUS_CONFIG[key]
}

export const getStockStatusText = (status: string | StockStatusType): string => {
  return getStockStatusConfig(status).label
}

export const getStockStatusType = (status: string | StockStatusType): string => {
  return getStockStatusConfig(status).tagType
}

export const getStockStatusColor = (status: string | StockStatusType): string => {
  return getStockStatusConfig(status).color
}

export const getStockStatusBgColor = (status: string | StockStatusType): string => {
  return getStockStatusConfig(status).bgColor
}

export const getStockStatusIcon = (status: string | StockStatusType): string => {
  return getStockStatusConfig(status).iconName
}

export const calcStockStatus = (
  quantity: number,
  safetyStock: number = 0,
  availableQuantity?: number
): StockStatusType => {
  const qty = availableQuantity !== undefined ? availableQuantity : quantity
  if (qty < 0) return 'negative'
  if (qty === 0) return 'shortage'
  if (safetyStock > 0 && qty <= safetyStock) return 'warning'
  return 'normal'
}

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
