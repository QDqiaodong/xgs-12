import { request, ApiResponse } from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  realName: string
  role: string
}

export interface UserInfo {
  userId: number
  username: string
  realName: string
  email: string
  phone: string
  role: string
  storeId?: number
  status: number
}

export interface LoginUserInfo {
  id: number
  username: string
  realName: string
  role: string
  storeId?: number
  email?: string
  phone?: string
  status?: number
}

export const transformLoginResult = (data: LoginResult): { token: string; userInfo: LoginUserInfo } => {
  return {
    token: data.token,
    userInfo: {
      id: data.userId,
      username: data.username,
      realName: data.realName,
      role: data.role,
      storeId: undefined
    }
  }
}

export const transformUserInfo = (data: UserInfo): LoginUserInfo => {
  return {
    id: data.userId,
    username: data.username,
    realName: data.realName,
    role: data.role,
    storeId: data.storeId,
    email: data.email,
    phone: data.phone,
    status: data.status
  }
}

export const authApi = {
  login(params: LoginParams): Promise<ApiResponse<LoginResult>> {
    return request.post('/auth/login', params)
  },
  
  logout(): Promise<ApiResponse<void>> {
    return request.post('/auth/logout')
  },
  
  getUserInfo(): Promise<ApiResponse<UserInfo>> {
    return request.get('/auth/info')
  },
  
  updatePassword(data: { oldPassword: string; newPassword: string }): Promise<ApiResponse<void>> {
    return request.put('/auth/password', data)
  }
}