// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /team/check */
export async function checkUserInApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.checkUserInAppParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/team/check', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /team/invite */
export async function inviteUserToApp(
  body: API.AppTeamInviteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/team/invite', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /team/list/my */
export async function listMyTeamAppByPage(
  body: API.AppTeamQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO>('/team/list/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /team/members/page */
export async function getAppTeamMembersByPage(
  body: API.AppTeamMemberQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppTeamMemberVO>('/team/members/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /team/remove */
export async function removeUserFromApp(
  body: API.AppTeamRemoveRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/team/remove', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /team/team/members */
export async function getAppTeamMembers(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppTeamMembersParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListAppTeamMemberVO>('/team/team/members', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
