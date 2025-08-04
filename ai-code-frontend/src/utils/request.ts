import axios from 'axios'
import { message } from 'ant-design-vue'
import config from '@/config'
import JSONbig from 'json-bigint'

// 创建 Axios 实例
const myAxios = axios.create({
  baseURL: config.apiBaseURL,
  timeout: config.requestTimeout,
  withCredentials: true,
  // 使用 json-bigint 处理大整数
  transformResponse: [function (data) {
    try {
      // 使用 json-bigint 解析响应数据，将大整数转换为字符串
      return JSONbig.parse(data)
    } catch (err) {
      // 如果解析失败，返回原始数据
      return data
    }
  }]
})

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // 开发环境下打印请求日志
    if (process.env.NODE_ENV === 'development') {
      console.log('🚀 Request:', {
        method: config.method?.toUpperCase(),
        url: config.url,
        data: config.data,
        params: config.params
      })
    }
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    // 开发环境下打印响应日志
    if (process.env.NODE_ENV === 'development') {
      console.log('✅ Response:', {
        status: response.status,
        url: response.config.url,
        data: response.data
      })
    }
    
    const { data } = response
    // 未登录
    if (data.code === 40100) {
      // 不是获取用户信息的请求，并且用户目前不是已经在用户登录页面，则跳转到登录页面
      if (
        !response.config.url?.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // 开发环境下打印错误日志
    if (process.env.NODE_ENV === 'development') {
      console.error('❌ Response Error:', {
        status: error.response?.status,
        url: error.config?.url,
        message: error.message,
        data: error.response?.data
      })
    }
    
    // 处理网络错误
    if (!error.response) {
      message.error('网络连接失败，请检查网络设置')
    } else {
      // 处理 HTTP 错误
      const { status } = error.response
      switch (status) {
        case 401:
          message.error('未授权，请重新登录')
          break
        case 403:
          message.error('禁止访问')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(`请求失败 (${status})`)
      }
    }
    
    return Promise.reject(error)
  },
)

export default myAxios

// 工具函数：安全地将字符串ID转换为数字（用于比较等操作）
export const safeParseInt = (value: string | number | undefined): number => {
  if (typeof value === 'number') return value
  if (typeof value === 'string') {
    const parsed = parseInt(value, 10)
    return isNaN(parsed) ? 0 : parsed
  }
  return 0
}

// 工具函数：检查是否为有效的大整数字符串
export const isValidBigInt = (value: string): boolean => {
  return /^\d+$/.test(value) && value.length > 0
}

// 工具函数：格式化大整数显示（添加千分位分隔符）
export const formatBigInt = (value: string | number): string => {
  const str = String(value)
  return str.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
} 