import { computed } from 'vue'
import { useLoginUserStore } from '../stores/loginUser'
import { 
  hasPermission, 
  canViewApp, 
  canEditApp, 
  canDeleteApp, 
  canDownloadApp, 
  canManageAppUsers, 
  canGenCode, 
  canDeployCode,
  getUserEffectivePermissions,
  isAppCreator
} from './permissionUtils'
import type { AppVO } from '../types/api'

/**
 * 权限组合式函数
 * @param appInfo 应用信息
 * @param userPermissions 用户权限列表（可选，如果不提供则从应用信息中获取）
 */
export function usePermissions(appInfo?: AppVO, userPermissions?: string[]) {
  const loginUserStore = useLoginUserStore()
  
  // 获取当前用户权限
  const currentUserPermissions = computed(() => {
    if (userPermissions) {
      return userPermissions
    }
    
    if (!appInfo) {
      return []
    }
    
    const currentUser = loginUserStore.loginUser
    if (!currentUser) return []
    
    // 检查是否是应用创建者
    const isCreator = isAppCreator(appInfo.userId || 0, currentUser.id || 0)
    
    // 获取用户在当前应用中的角色和权限
    const userRole = '' // 这里需要从团队成员信息中获取
    return getUserEffectivePermissions(userRole, appInfo.permissionList || [], isCreator)
  })
  
  // 权限检查函数
  const canView = computed(() => canViewApp(currentUserPermissions.value))
  const canEdit = computed(() => canEditApp(currentUserPermissions.value))
  const canDelete = computed(() => canDeleteApp(currentUserPermissions.value))
  const canDownload = computed(() => canDownloadApp(currentUserPermissions.value))
  const canManageUsers = computed(() => canManageAppUsers(currentUserPermissions.value))
  const canGenerateCode = computed(() => canGenCode(currentUserPermissions.value))
  const canDeploy = computed(() => canDeployCode(currentUserPermissions.value))
  
  // 通用权限检查函数
  const hasPermission = (permission: string) => {
    return hasPermission(currentUserPermissions.value, permission)
  }
  
  // 检查多个权限中的任意一个
  const hasAnyPermission = (permissions: string[]) => {
    return permissions.some(permission => hasPermission(currentUserPermissions.value, permission))
  }
  
  // 检查多个权限中的所有
  const hasAllPermissions = (permissions: string[]) => {
    return permissions.every(permission => hasPermission(currentUserPermissions.value, permission))
  }
  
  // 检查是否是应用创建者
  const isCreator = computed(() => {
    if (!appInfo) return false
    const currentUser = loginUserStore.loginUser
    if (!currentUser) return false
    return isAppCreator(appInfo.userId || 0, currentUser.id || 0)
  })
  
  return {
    // 权限状态
    permissions: currentUserPermissions,
    canView,
    canEdit,
    canDelete,
    canDownload,
    canManageUsers,
    canGenerateCode,
    canDeploy,
    isCreator,
    
    // 权限检查函数
    hasPermission,
    hasAnyPermission,
    hasAllPermissions
  }
}

/**
 * 团队权限组合式函数
 * @param appInfo 应用信息
 * @param teamMembers 团队成员列表
 */
export function useTeamPermissions(appInfo: AppVO, teamMembers: any[] = []) {
  const loginUserStore = useLoginUserStore()
  
  // 获取当前用户在团队中的信息
  const currentMember = computed(() => {
    const currentUser = loginUserStore.loginUser
    if (!currentUser) return null
    return teamMembers.find(member => member.userId === currentUser.id)
  })
  
  // 获取当前用户权限
  const currentUserPermissions = computed(() => {
    const currentUser = loginUserStore.loginUser
    if (!currentUser) return []
    
    // 检查是否是应用创建者
    const isCreator = isAppCreator(appInfo.userId || 0, currentUser.id || 0)
    
    // 获取用户在当前应用中的角色和权限
    const userRole = currentMember.value?.appRole || ''
    return getUserEffectivePermissions(userRole, appInfo.permissionList || [], isCreator)
  })
  
  // 权限检查函数
  const canView = computed(() => canViewApp(currentUserPermissions.value))
  const canEdit = computed(() => canEditApp(currentUserPermissions.value))
  const canDelete = computed(() => canDeleteApp(currentUserPermissions.value))
  const canDownload = computed(() => canDownloadApp(currentUserPermissions.value))
  const canManageUsers = computed(() => canManageAppUsers(currentUserPermissions.value))
  const canGenerateCode = computed(() => canGenCode(currentUserPermissions.value))
  const canDeploy = computed(() => canDeployCode(currentUserPermissions.value))
  
  // 通用权限检查函数
  const hasPermission = (permission: string) => {
    return hasPermission(currentUserPermissions.value, permission)
  }
  
  // 检查多个权限中的任意一个
  const hasAnyPermission = (permissions: string[]) => {
    return permissions.some(permission => hasPermission(currentUserPermissions.value, permission))
  }
  
  // 检查多个权限中的所有
  const hasAllPermissions = (permissions: string[]) => {
    return permissions.every(permission => hasPermission(currentUserPermissions.value, permission))
  }
  
  // 检查是否是应用创建者
  const isCreator = computed(() => {
    const currentUser = loginUserStore.loginUser
    if (!currentUser) return false
    return isAppCreator(appInfo.userId || 0, currentUser.id || 0)
  })
  
  // 检查是否可以管理指定用户
  const canManageUser = (targetUserId: number) => {
    const currentUser = loginUserStore.loginUser
    if (!currentUser) return false
    
    // 不能管理自己
    if (targetUserId === currentUser.id) return false
    
    // 创建者可以管理所有人
    if (isCreator.value) return true
    
    // 管理员可以管理其他人
    return canManageUsers.value
  }
  
  return {
    // 用户信息
    currentMember,
    
    // 权限状态
    permissions: currentUserPermissions,
    canView,
    canEdit,
    canDelete,
    canDownload,
    canManageUsers,
    canGenerateCode,
    canDeploy,
    isCreator,
    
    // 权限检查函数
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    canManageUser
  }
}
