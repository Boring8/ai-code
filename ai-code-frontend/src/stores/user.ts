import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as userController from '@/api/userController'

// 用户信息类型（使用新的 API 类型）
export interface UserInfo {
  id?: number
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  createTime?: string
  updateTime?: string
}

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref<UserInfo | null>(null)
  
  // 是否已登录
  const isLoggedIn = ref(false)
  
  // 加载状态
  const loading = ref(false)

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      loading.value = true
      console.log(123456)
      const response = await userController.getLoginUser()
      const { code, data } = response.data
      
      if (code === 0 && data) {
        userInfo.value = data
        isLoggedIn.value = true
      }
      
      return data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  // 用户登录
  const login = async (userAccount: string, userPassword: string) => {
    try {
      loading.value = true
      const response = await userController.userLogin({
        userAccount,
        userPassword
      })
      const { code, data } = response.data
      
      if (code === 0 && data) {
        userInfo.value = data
        isLoggedIn.value = true
        return true
      }
      
      return false
    } catch (error) {
      console.error('登录失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 用户登出
  const logout = async () => {
    try {
      await userController.userLogout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      // 无论成功失败都清除本地状态
      userInfo.value = null
      isLoggedIn.value = false
    }
  }

  // 清除用户信息
  const clearUserInfo = () => {
    userInfo.value = null
    isLoggedIn.value = false
  }

  return {
    userInfo,
    isLoggedIn,
    loading,
    getUserInfo,
    login,
    logout,
    clearUserInfo
  }
}) 