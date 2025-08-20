// @ts-ignore
/* eslint-disable */
import request from '../request'

/** 分页查询应用，按点赞数从高到低排序 GET /thumb/appThumbPage */
export async function appThumbSet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  options?: { [key: string]: any }
) {
  return request<API.AppThumbVO>('/thumb/appThumbSet', {
    method: 'GET',
    ...(options || {})
  })
}

/** 获取热门应用列表（含名称与点赞数） GET /thumb/appThumbList */
export async function appThumbList(
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAppThumbVOArray>('/thumb/appThumbList', {
    method: 'GET',
    ...(options || {})
  })
}

/** 用户点赞应用 POST /thumb/like/${param0} */
export async function likeApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.likeAppParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/thumb/like/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 用户取消点赞应用 DELETE /thumb/unlike/${param0} */
export async function unlikeApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.unlikeAppParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/thumb/unlike/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 检查用户是否已点赞应用 GET /thumb/isLiked/${param0} */
export async function isUserLikedApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.isUserLikedAppParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/thumb/isLiked/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}
