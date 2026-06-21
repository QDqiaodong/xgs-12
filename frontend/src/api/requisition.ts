import { request, ApiResponse, PageResponse } from '@/utils/request'

export type RequisitionStatus =
  | 'draft'
  | 'submitted'
  | 'approved'
  | 'rejected'
  | 'in_transit'
  | 'received'
  | 'cancelled'

export interface RequisitionItem {
  id: number
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  unitPrice: number
  quantity: number
  totalPrice: number
  currentStock: number
  warningStock: number
  monthlyConsumption: number
  expectedArrivalDate?: string
  purpose?: string
}

export interface RequisitionOrder {
  id: number
  orderNo: string
  storeId: number
  storeName: string
  status: RequisitionStatus
  totalQuantity: number
  totalAmount: number
  expectedArrivalDate?: string
  purpose?: string
  applicant: string
  applicantPhone?: string
  approver?: string
  approveTime?: string
  approveRemark?: string
  receiveTime?: string
  receiver?: string
  receiveRemark?: string
  createdAt: string
  updatedAt: string
  items: RequisitionItem[]
  history?: ApprovalHistoryItem[]
}

export interface ApprovalHistoryItem {
  id: number
  action: string
  operator: string
  remark?: string
  createdAt: string
}

export interface RequisitionCreateData {
  storeId: number
  expectedArrivalDate?: string
  purpose?: string
  applicantPhone?: string
  items: {
    consumableId: number
    quantity: number
    expectedArrivalDate?: string
    purpose?: string
  }[]
}

export interface RequisitionQuery {
  orderNo?: string
  storeId?: number
  status?: RequisitionStatus
  startDate?: string
  endDate?: string
  pageNum: number
  pageSize: number
}

export interface RequisitionApproveData {
  approved: boolean
  remark?: string
}

export interface RequisitionReceiveData {
  receiver: string
  items: {
    id: number
    receivedQuantity: number
    differenceReason?: string
  }[]
  remark?: string
}

export interface ConsumableStockInfo {
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  currentStock: number
  warningStock: number
  monthlyConsumption: number
  unitPrice: number
}

export const requisitionApi = {
  getList(params: RequisitionQuery): Promise<ApiResponse<PageResponse<RequisitionOrder>>> {
    return request.get('/requisitions', { params })
  },

  getDetail(id: number): Promise<ApiResponse<RequisitionOrder>> {
    return request.get(`/requisitions/${id}`)
  },

  create(data: RequisitionCreateData): Promise<ApiResponse<RequisitionOrder>> {
    return request.post('/requisitions', data)
  },

  update(id: number, data: Partial<RequisitionCreateData>): Promise<ApiResponse<RequisitionOrder>> {
    return request.put(`/requisitions/${id}`, data)
  },

  submit(id: number): Promise<ApiResponse<RequisitionOrder>> {
    return request.post(`/requisitions/${id}/submit`)
  },

  approve(id: number, data: RequisitionApproveData): Promise<ApiResponse<RequisitionOrder>> {
    return request.post(`/requisitions/${id}/approve`, data)
  },

  receive(id: number, data: RequisitionReceiveData): Promise<ApiResponse<RequisitionOrder>> {
    return request.post(`/requisitions/${id}/receive`, data)
  },

  cancel(id: number): Promise<ApiResponse<void>> {
    return request.post(`/requisitions/${id}/cancel`)
  },

  getConsumableStockInfo(storeId: number, consumableId: number): Promise<ApiResponse<ConsumableStockInfo>> {
    return request.get(`/requisitions/stock-info`, { params: { storeId, consumableId } })
  },

  getBatchStockInfo(storeId: number, consumableIds: number[]): Promise<ApiResponse<ConsumableStockInfo[]>> {
    return request.post('/requisitions/batch-stock-info', { storeId, consumableIds })
  }
}

export const getRequisitionStatusText = (status: RequisitionStatus): string => {
  const map: Record<RequisitionStatus, string> = {
    draft: '草稿',
    submitted: '已提交待审批',
    approved: '已审批',
    rejected: '已驳回',
    in_transit: '配送中',
    received: '已签收',
    cancelled: '已取消'
  }
  return map[status] || status
}

export const getRequisitionStatusType = (status: RequisitionStatus): string => {
  const map: Record<RequisitionStatus, string> = {
    draft: 'info',
    submitted: 'warning',
    approved: 'primary',
    rejected: 'danger',
    in_transit: 'warning',
    received: 'success',
    cancelled: 'info'
  }
  return map[status] || 'info'
}

export const getPurposeText = (purpose: string): string => {
  const map: Record<string, string> = {
    daily: '日常办公',
    operation: '门店运营',
    service: '客户服务',
    maintenance: '设备维护',
    promotion: '促销活动',
    new_store: '新店筹备',
    other: '其他'
  }
  return map[purpose] || purpose
}
