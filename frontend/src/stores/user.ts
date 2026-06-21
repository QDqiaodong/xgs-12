import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi, transformUserInfo, type LoginUserInfo } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<LoginUserInfo | null>(null)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: LoginUserInfo | null) => {
    userInfo.value = info
    if (info) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  const loadUserInfoFromStorage = () => {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        userInfo.value = JSON.parse(stored)
      } catch (e) {
        userInfo.value = null
      }
    }
  }

  const fetchUserInfo = async () => {
    if (!token.value) {
      userInfo.value = null
      return false
    }
    try {
      const res = await authApi.getUserInfo()
      if (res.code === 200 && res.data) {
        const info = transformUserInfo(res.data)
        setUserInfo(info)
        return true
      }
      return false
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return false
    }
  }

  const initUserInfo = async () => {
    loadUserInfoFromStorage()
    if (token.value && !userInfo.value) {
      await fetchUserInfo()
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    fetchUserInfo,
    initUserInfo,
    loadUserInfoFromStorage,
    logout
  }
})