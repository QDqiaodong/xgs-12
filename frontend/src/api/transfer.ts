import { request, ApiResponse, PageResponse } from '@/utils/request'

export interface TransferOrder {
  id: number
  orderNo: string
  sourceStoreId: number
  sourceStoreName: string
  targetStoreId: number
  targetStoreName: string
  status: TransferStatus
  totalAmount: number
  totalQuantity: number
  operator: string
  approver?: string
  approveTime?: string
  approveRemark?: string
  outboundTime?: string
  receiveTime?: string
  receiver?: string
  receiveRemark?: string
  createdAt: string
  updatedAt: string
  items: TransferItem[]
  history?: ApprovalHistory[]
}

export interface TransferItem {
  id: number
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  quantity: number
  unitPrice: number
  totalPrice: number
  receivedQuantity?: number
  differenceReason?: string
}

export interface ApprovalHistory {
  id: number
  action: string
  operator: string
  remark: string
  createdAt: string
}

export type TransferStatus = 
  | 'pending'
  | 'approved'
  | 'rejected'
  | 'outbound'
  | 'in_transit'
  | 'received'
  | 'cancelled'

export interface TransferQuery {
  orderNo?: string
  sourceStoreId?: number
  targetStoreId?: number
  status?: TransferStatus
  startDate?: string
  endDate?: string
  pageNum: number
  pageSize: number
}

export interface TransferCreateData {
  targetStoreId: number
  items: {
    consumableId: number
    quantity: number
  }[]
  remark?: string
}

export interface TransferApproveData {
  approved: boolean
  remark?: string
}

export interface TransferOutboundData {
  operator: string
}

export interface TransferReceiveData {
  receiver: string
  items: {
    id: number
    receivedQuantity: number
    differenceReason?: string
  }[]
  remark?: string
}

export const transferApi = {
  getTransferList(params: TransferQuery): Promise<ApiResponse<PageResponse<TransferOrder>>> {
    return request.get('/transfers', { params })
  },

  getTransferDetail(id: number): Promise<ApiResponse<TransferOrder>> {
    return request.get(`/transfers/${id}`)
  },

  createTransfer(data: TransferCreateData): Promise<ApiResponse<TransferOrder>> {
    return request.post('/transfers', data)
  },

  approveTransfer(id: number, data: TransferApproveData): Promise<ApiResponse<TransferOrder>> {
    return request.post(`/transfers/${id}/approve`, data)
  },

  outboundTransfer(id: number, data: TransferOutboundData): Promise<ApiResponse<TransferOrder>> {
    return request.post(`/transfers/${id}/outbound`, data)
  },

  receiveTransfer(id: number, data: TransferReceiveData): Promise<ApiResponse<TransferOrder>> {
    return request.post(`/transfers/${id}/receive`, data)
  },

  cancel(id: number): Promise<ApiResponse<void>> {
    return request.post(`/transfers/${id}/cancel`)
  },

  getPendingApproval(params: { pageNum: number; pageSize: number }): Promise<ApiResponse<PageResponse<TransferOrder>>> {
    return request.get('/transfers/pending-approval', { params })
  },

  getPendingOutbound(params: { pageNum: number; pageSize: number }): Promise<ApiResponse<PageResponse<TransferOrder>>> {
    return request.get('/transfers/pending-outbound', { params })
  },

  getPendingReceive(params: { pageNum: number; pageSize: number }): Promise<ApiResponse<PageResponse<TransferOrder>>> {
    return request.get('/transfers/pending-receive', { params })
  },

  printOutboundOrder(id: number): Promise<ApiResponse<string>> {
    return request.get(`/transfers/${id}/print`)
  }
}

export const getStatusText = (status: TransferStatus): string => {
  const map: Record<TransferStatus, string> = {
    pending: '待审批',
    approved: '已审批',
    rejected: '已驳回',
    outbound: '已出库',
    in_transit: '在途',
    received: '已签收',
    cancelled: '已取消'
  }
  return map[status] || status
}

export const getStatusType = (status: TransferStatus): string => {
  const map: Record<TransferStatus, string> = {
    pending: 'warning',
    approved: 'primary',
    rejected: 'danger',
    outbound: 'success',
    in_transit: 'info',
    received: 'success',
    cancelled: 'info'
  }
  return map[status] || 'info'
}