// @ts-ignore
/* eslint-disable */
import request from '@/request'

// 声明全局API命名空间
declare global {
  namespace API {
    interface AppAddRequest {
      appName?: string
      cover?: string
      initPrompt?: string
      codeGenType?: string
    }

    interface AppDeployRequest {
      appId?: number
    }

    interface AppQueryRequest {
      pageNum?: number
      pageSize?: number
      sortField?: string
      sortOrder?: string
      id?: number
      appName?: string
      codeGenType?: string
      userId?: number
      priority?: number
      initPrompt?: string
      isTeam?: number
    }

    interface AppTeamInviteRequest {
      appId?: number
      userId?: number
    }

    interface AppTeamMemberQueryRequest {
      pageNum?: number
      pageSize?: number
      sortField?: string
      sortOrder?: string
      appId?: number
    }

    interface AppTeamMemberVO {
      userId?: number
      userAccount?: string
      userName?: string
      userAvatar?: string
      userProfile?: string
      userRole?: string
      joinTime?: string
      isCreate?: number
    }

    interface AppTeamQueryRequest {
      pageNum?: number
      pageSize?: number
      sortField?: string
      sortOrder?: string
      appName?: string
      userId?: number
    }

    interface AppTeamRemoveRequest {
      appId?: number
      userId?: number
    }

    interface AppUpdateRequest {
      id?: number
      appName?: string
      cover?: string
      priority?: number
      isTeam?: number
    }

    interface AppVO {
      id?: number
      appName?: string
      cover?: string
      codeGenType?: string
      deployKey?: string
      deployedTime?: string
      priority?: number
      userId?: number
      isTeam?: number
      initPrompt?: string
      createTime?: string
      updateTime?: string
    }

    interface BaseResponseString {
      code?: number
      data?: string
      message?: string
    }

    interface BaseResponseBoolean {
      code?: number
      data?: boolean
      message?: string
    }

    interface BaseResponseAppVO {
      code?: number
      data?: AppVO
      message?: string
    }

    interface BaseResponseApp {
      code?: number
      data?: App
      message?: string
    }

    interface BaseResponsePageAppVO {
      code?: number
      data?: PageAppVO
      message?: string
    }

    interface BaseResponseListAppTeamMemberVO {
      code?: number
      data?: AppTeamMemberVO[]
      message?: string
    }

    interface BaseResponsePageAppTeamMemberVO {
      code?: number
      data?: PageAppTeamMemberVO
      message?: string
    }

    interface App {
      id?: number
      appName?: string
      cover?: string
      initPrompt?: string
      codeGenType?: string
      deployKey?: string
      deployedTime?: string
      priority?: number
      userId?: number
      editTime?: string
      createTime?: string
      updateTime?: string
      isDelete?: number
      isTeam?: number
    }

    interface PageAppVO {
      records?: AppVO[]
      pageNumber?: number
      pageSize?: number
      totalPage?: number
      totalRow?: number
      optimizeCountQuery?: boolean
    }

    interface PageAppTeamMemberVO {
      records?: AppTeamMemberVO[]
      pageNumber?: number
      pageSize?: number
      totalPage?: number
      totalRow?: number
      optimizeCountQuery?: boolean
    }

    interface DeleteRequest {
      id?: number
    }

    interface chatToGenCodeParams {
      appId: number
      message: string
    }

    interface getAppByIdParams {
      id: number
    }

    interface getAppByIdAdminParams {
      id: number
    }

    interface getAppTeamMembersParams {
      appId: number
    }

    interface checkUserInAppParams {
      appId: number
      userId: number
    }

    interface ServerSentEventString {
      data: string
      event?: string
    }
  }
}

/** 此处后端没有提供注释 POST /app/add */
export async function addApp(body: API.AppAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/app/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/chat/gen/code */
export async function chatToGenCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.chatToGenCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ServerSentEventString[]>('/app/chat/gen/code', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/delete */
export async function deleteApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/delete/my */
export async function deleteMyApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/delete/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/deploy */
export async function deployApp(body: API.AppDeployRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/app/deploy', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/get */
export async function getAppById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAppVO>('/app/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/get/admin */
export async function getAppByIdAdmin(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppByIdAdminParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseApp>('/app/get/admin', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/list/featured */
export async function listFeaturedAppByPage(
  body: API.AppQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO>('/app/list/featured', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/list/my */
export async function listMyAppByPage(body: API.AppQueryRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageAppVO>('/app/list/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/list/my/team */
export async function listMyTeamAppByPage(
  body: API.AppTeamQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO>('/app/list/my/team', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/list/page */
export async function listAppByPage(body: API.AppQueryRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageAppVO>('/app/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/team/check */
export async function checkUserInApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.checkUserInAppParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/app/team/check', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/team/invite */
export async function inviteUserToApp(
  body: API.AppTeamInviteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/app/team/invite', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/team/members */
export async function getAppTeamMembers(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppTeamMembersParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListAppTeamMemberVO>('/app/team/members', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/team/members/page */
export async function getAppTeamMembersByPage(
  body: API.AppTeamMemberQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppTeamMemberVO>('/app/team/members/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/team/remove */
export async function removeUserFromApp(
  body: API.AppTeamRemoveRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/app/team/remove', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/update */
export async function updateApp(body: API.AppUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/update/my */
export async function updateMyApp(body: API.AppUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/update/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
