// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /appUser/create */
export async function createAppTeam(
  body: API.AppTeamCreateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/appUser/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /appUser/invite */
export async function inviteUserToApp(
  body: API.AppTeamInviteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/appUser/invite', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /appUser/list/my */
export async function listMyTeamAppByPage(
  body: API.AppTeamQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO>('/appUser/list/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /appUser/members */
export async function getAppTeamMembers(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppTeamMembersParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListAppTeamMemberVO>('/appUser/members', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /appUser/members/page */
export async function getAppTeamMembersByPage(
  body: API.AppTeamMemberQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppTeamMemberVO>('/appUser/members/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /appUser/remove */
export async function removeUserFromApp(
  body: API.AppTeamRemoveRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/appUser/remove', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
