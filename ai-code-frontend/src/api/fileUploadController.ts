// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 DELETE /file/delete/image */
export async function deleteImage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteImageParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/file/delete/image', {
    method: 'DELETE',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /file/upload/image */
export async function uploadImage(body: FormData, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/file/upload/image', {
    method: 'POST',
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /file/upload/images */
export async function uploadImages(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadImagesParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseStringArray>('/file/upload/images', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
