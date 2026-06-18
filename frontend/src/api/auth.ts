import { request, ApiResponse } from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userInfo: {
    id: number
    username: string
    realName: string
    role: string
    storeId?: number
  }
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  role: string
  storeId?: number
  createdAt: string
}

export const authApi = {
  login(params: LoginParams): Promise<ApiResponse<LoginResult>> {
    return request.post('/auth/login', params)
  },
  
  logout(): Promise<ApiResponse<void>> {
    return request.post('/auth/logout')
  },
  
  getUserInfo(): Promise<ApiResponse<UserInfo>> {
    return request.get('/auth/user-info')
  },
  
  updatePassword(data: { oldPassword: string; newPassword: string }): Promise<ApiResponse<void>> {
    return request.put('/auth/password', data)
  }
}