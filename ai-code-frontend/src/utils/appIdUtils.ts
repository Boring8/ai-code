/**
 * AppId 工具函数
 * 用于处理AppId的类型转换，避免JavaScript Number类型的精度限制
 */

/**
 * 将任意类型的AppId转换为字符串格式
 * 用于前端存储和URL路由，避免精度丢失
 */
export function toAppIdString(appId: string | number | undefined): string | undefined {
  if (appId === undefined || appId === null) {
    return undefined
  }
  return String(appId)
}

/**
 * 将字符串AppId转换为number格式
 * 用于API调用，满足后端接口的number类型要求
 * 注意：对于超过Number.MAX_SAFE_INTEGER的数值，此函数会导致精度丢失
 */
export function toAppIdNumber(appId: string | number | undefined): number | undefined {
  if (appId === undefined || appId === null) {
    return undefined
  }
  return Number(appId)
}

/**
 * 检查AppId是否超过JavaScript安全整数范围
 */
export function isLargeAppId(appId: string | number | undefined): boolean {
  if (appId === undefined || appId === null) {
    return false
  }
  const num = Number(appId)
  return num > Number.MAX_SAFE_INTEGER
}

/**
 * 获取AppId的API参数格式
 * 对于大数值，直接使用字符串；对于小数值，转换为number
 */
export function getAppIdForApi(appId: string | number | undefined): string | number | undefined {
  if (appId === undefined || appId === null) {
    return undefined
  }
  
  // 如果是大数值，直接返回字符串
  if (isLargeAppId(appId)) {
    return toAppIdString(appId)
  }
  
  // 如果是小数值，转换为number
  return toAppIdNumber(appId)
}

/**
 * 验证AppId是否为有效的数字字符串
 */
export function isValidAppId(appId: string | number | undefined): boolean {
  if (appId === undefined || appId === null) {
    return false
  }
  const num = Number(appId)
  return !isNaN(num) && isFinite(num) && num > 0
}

/**
 * 安全地获取AppId字符串，如果无效则返回undefined
 */
export function getSafeAppIdString(appId: string | number | undefined): string | undefined {
  if (!isValidAppId(appId)) {
    return undefined
  }
  return toAppIdString(appId)
} 