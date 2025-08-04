// 环境配置
export const config = {
  // API 基础地址
  apiBaseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api',
  
  // 应用名称
  appName: 'AI Code',
  
  // 版本号
  version: '1.0.0',
  
  // 是否开启调试模式
  debug: import.meta.env.DEV,
  
  // 请求超时时间（毫秒）
  requestTimeout: 60000,
  
  // 是否开启请求日志
  enableRequestLog: import.meta.env.DEV,
  
  // 是否开启响应日志
  enableResponseLog: import.meta.env.DEV
}

// 导出默认配置
export default config 