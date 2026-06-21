import { request, ApiResponse, PageResponse } from '@/utils/request'

export interface MaterialVO {
  id: number
  materialCode: string
  materialName: string
  category: string
  specification: string
  unit: string
  purchaseCost: number
  shelfLife: number
  shelfLocation: string
  safetyStock: number
  currentStock: number
  status: number
  createTime: string
  updateTime: string
}

export interface MaterialCreateDTO {
  materialCode: string
  materialName: string
  category: string
  specification?: string
  unit: string
  purchaseCost: number
  shelfLife?: number
  shelfLocation?: string
  safetyStock?: number
  currentStock?: number
  status?: number
}

export interface MaterialUpdateDTO {
  materialName?: string
  category?: string
  specification?: string
  unit?: string
  purchaseCost?: number
  shelfLife?: number
  shelfLocation?: string
  safetyStock?: number
  currentStock?: number
  status?: number
}

export interface MaterialQueryDTO {
  materialCode?: string
  materialName?: string
  category?: string
  status?: number
  current?: number
  size?: number
}

export interface Consumable {
  id: number
  name: string
  code: string
  category: string
  unit: string
  specification: string
  price: number
  stock: number
  warningStock: number
  shelfLife?: number
  shelfLocation?: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface ConsumableQuery {
  name?: string
  code?: string
  category?: string
  status?: number
  pageNum: number
  pageSize: number
}

export const toFrontend = (vo: MaterialVO): Consumable => ({
  id: vo.id,
  name: vo.materialName,
  code: vo.materialCode,
  category: vo.category,
  unit: vo.unit,
  specification: vo.specification,
  price: vo.purchaseCost,
  stock: vo.currentStock,
  warningStock: vo.safetyStock,
  shelfLife: vo.shelfLife,
  shelfLocation: vo.shelfLocation,
  status: vo.status,
  createdAt: vo.createTime,
  updatedAt: vo.updateTime
})

export const toCreateDTO = (data: Partial<Consumable>): MaterialCreateDTO => ({
  materialCode: data.code || '',
  materialName: data.name || '',
  category: data.category || '',
  specification: data.specification,
  unit: data.unit || '',
  purchaseCost: data.price || 0,
  shelfLife: data.shelfLife,
  shelfLocation: data.shelfLocation,
  safetyStock: data.warningStock,
  currentStock: data.stock,
  status: data.status
})

export const toUpdateDTO = (data: Partial<Consumable>): MaterialUpdateDTO => ({
  materialName: data.name,
  category: data.category,
  specification: data.specification,
  unit: data.unit,
  purchaseCost: data.price,
  shelfLife: data.shelfLife,
  shelfLocation: data.shelfLocation,
  safetyStock: data.warningStock,
  currentStock: data.stock,
  status: data.status
})

export const toQueryDTO = (query: ConsumableQuery): MaterialQueryDTO => ({
  materialCode: query.code,
  materialName: query.name,
  category: query.category,
  status: query.status,
  current: query.pageNum,
  size: query.pageSize
})

export const consumableApi = {
  getList(params: ConsumableQuery): Promise<ApiResponse<PageResponse<Consumable>>> {
    const queryDTO = toQueryDTO(params)
    return request.get('/materials', { params: queryDTO }).then(res => {
      if (res.data && res.data.records) {
        res.data.records = res.data.records.map(toFrontend)
      }
      return res
    })
  },
  
  getById(id: number): Promise<ApiResponse<Consumable>> {
    return request.get<MaterialVO>(`/materials/${id}`).then(res => {
      if (res.data) {
        res.data = toFrontend(res.data as unknown as MaterialVO) as unknown as Consumable
      }
      return res
    })
  },
  
  create(data: Partial<Consumable>): Promise<ApiResponse<number>> {
    const createDTO = toCreateDTO(data)
    return request.post('/materials', createDTO)
  },
  
  update(id: number, data: Partial<Consumable>): Promise<ApiResponse<void>> {
    const updateDTO = toUpdateDTO(data)
    return request.put(`/materials/${id}`, updateDTO)
  },
  
  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/materials/${id}`)
  }
}