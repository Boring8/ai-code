// @ts-ignore
/* eslint-disable */
import request from '@/request'

/**
 * 查询最新的代码面板文本（无记录返回空字符串）
 * GET /app/codeText/latest
 */
export async function getLatestCodeText(
  params: { appId: string | number },
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString>('/app/codeText/latest', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

