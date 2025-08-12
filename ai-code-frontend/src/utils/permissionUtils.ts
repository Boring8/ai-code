// 权限常量定义
export const PERMISSIONS = {
  APP_USER_MANAGE: 'appUser:manage',
  APP_VIEW: 'app:view',
  APP_DOWNLOAD: 'app:download',
  App_EDIT: 'app:edit',
  APP_UPDATE: 'app:update',
  APP_DELETE: 'app:delete',
  APP_DEPLOY: 'app:deploy'
} as const

// 角色定义
export const ROLES = {
  VIEWER: 'viewer',
  EDITOR: 'editor',
  ADMIN: 'admin'
} as const

// 角色权限映射
export const ROLE_PERMISSIONS = {
  [ROLES.VIEWER]: [
    PERMISSIONS.APP_VIEW,
    PERMISSIONS.APP_DOWNLOAD
  ],
  [ROLES.EDITOR]: [
    PERMISSIONS.APP_VIEW,
    PERMISSIONS.APP_DOWNLOAD,
    PERMISSIONS.App_EDIT
  ],
  [ROLES.ADMIN]: [
    PERMISSIONS.APP_USER_MANAGE,
    PERMISSIONS.APP_VIEW,
    PERMISSIONS.APP_DOWNLOAD,
    PERMISSIONS.App_EDIT,
    PERMISSIONS.APP_UPDATE,
    PERMISSIONS.APP_DELETE,
    PERMISSIONS.APP_DEPLOY
  ]
} as const

// 权限配置
export const PERMISSION_CONFIG = {
  permissions: [
    {
      key: PERMISSIONS.APP_USER_MANAGE,
      name: '成员管理',
      description: '管理app成员，添加或移除成员'
    },
    {
      key: PERMISSIONS.APP_VIEW,
      name: '查看应用',
      description: '查看应用内容'
    },
    {
      key: PERMISSIONS.APP_DOWNLOAD,
      name: '下载代码',
      description: '下载代码到本地'
    },
    {
      key: PERMISSIONS.App_EDIT,
      name: '生成app',
      description: 'ai修改应用'
    },
    {
      key: PERMISSIONS.APP_UPDATE,
      name: '编辑app',
      description: '修改app信息应用'
    },
    {
      key: PERMISSIONS.APP_DELETE,
      name: '删除app',
      description: '删除app'
    },
    {
      key: PERMISSIONS.APP_DEPLOY,
      name: '部署app',
      description: '部署app'
    }
  ],
  roles: [
    {
      key: ROLES.VIEWER,
      name: '浏览者',
      permissions: [
        PERMISSIONS.APP_VIEW,
        PERMISSIONS.APP_DOWNLOAD
      ],
      description: '查看或下载应用'
    },
    {
      key: ROLES.EDITOR,
      name: '编辑者',
      permissions: [
        PERMISSIONS.APP_VIEW,
        PERMISSIONS.APP_DOWNLOAD,
        PERMISSIONS.App_EDIT
      ],
      description: '查看应用、编辑app、下载应用'
    },
    {
      key: ROLES.ADMIN,
      name: '管理员',
      permissions: [
        PERMISSIONS.APP_USER_MANAGE,
        PERMISSIONS.APP_VIEW,
        PERMISSIONS.APP_DOWNLOAD,
        PERMISSIONS.App_EDIT,
        PERMISSIONS.APP_UPDATE,
        PERMISSIONS.APP_DELETE,
        PERMISSIONS.APP_DEPLOY
      ],
      description: '成员管理、查看应用、下载应用、修改应用、删除应用，生成应用'
    }
  ]
}

// 根据角色获取权限列表
export function getPermissionsByRole(role: string): string[] {
  switch (role) {
    case ROLES.VIEWER:
      return [
        PERMISSIONS.APP_VIEW,
        PERMISSIONS.APP_DOWNLOAD
      ]
    case ROLES.EDITOR:
      return [
        PERMISSIONS.APP_VIEW,
        PERMISSIONS.APP_DOWNLOAD,
        PERMISSIONS.App_EDIT
      ]
    case ROLES.ADMIN:
      return [
        PERMISSIONS.APP_USER_MANAGE,
        PERMISSIONS.APP_VIEW,
        PERMISSIONS.APP_DOWNLOAD,
        PERMISSIONS.App_EDIT,
        PERMISSIONS.APP_UPDATE,
        PERMISSIONS.APP_DELETE,
        PERMISSIONS.APP_DEPLOY
      ]
    default:
      return []
  }
}

// 检查用户是否有指定权限
export function hasPermission(userPermissions: string[], permission: string): boolean {
  return userPermissions.includes(permission)
}

// 检查是否可以查看应用
export function canViewApp(userPermissions: string[]): boolean {
  return hasPermission(userPermissions, PERMISSIONS.APP_VIEW)
}

// 检查是否可以编辑应用
export function canEditApp(userPermissions: string[]): boolean {
  return hasPermission(userPermissions, PERMISSIONS.App_EDIT)
}

// 检查是否可以删除应用
export function canDeleteApp(userPermissions: string[]): boolean {
  return hasPermission(userPermissions, PERMISSIONS.APP_DELETE)
}

// 检查是否可以下载应用
export function canDownloadApp(userPermissions: string[]): boolean {
  return hasPermission(userPermissions, PERMISSIONS.APP_DOWNLOAD)
}

// 检查是否可以管理应用用户
export function canManageAppUsers(userPermissions: string[]): boolean {
  return hasPermission(userPermissions, PERMISSIONS.APP_USER_MANAGE)
}

// 检查是否可以邀请用户
export function canInviteUser(userPermissions: string[]): boolean {
  return hasPermission(userPermissions, PERMISSIONS.APP_USER_MANAGE)
}

// 检查是否可以移除用户
export function canRemoveUser(userPermissions: string[]): boolean {
  return hasPermission(userPermissions, PERMISSIONS.APP_USER_MANAGE)
}

// 检查是否可以生成代码
export function canGenCode(userPermissions: string[]): boolean {
  return hasPermission(userPermissions, PERMISSIONS.CHAT_EDIT)
}

// 检查是否可以部署代码
export function canDeployCode(userPermissions: string[]): boolean {
  return hasPermission(userPermissions, PERMISSIONS.APP_DEPLOY)
}

// 获取角色显示名称
export function getRoleDisplayName(role: string): string {
  switch (role) {
    case ROLES.ADMIN:
      return '管理员'
    case ROLES.EDITOR:
      return '编辑者'
    case ROLES.VIEWER:
      return '浏览者'
    default:
      return '成员'
  }
}

// 获取角色标签颜色
export function getRoleColor(role: string): string {
  switch (role) {
    case ROLES.ADMIN:
      return 'red'
    case ROLES.EDITOR:
      return 'orange'
    case ROLES.VIEWER:
      return 'blue'
    default:
      return 'blue'
  }
}

// 检查用户是否是应用创建者
export function isAppCreator(appUserId: number, currentUserId: number): boolean {
  return appUserId === currentUserId
}

// 获取用户的有效权限（结合角色权限和特殊权限）
export function getUserEffectivePermissions(
  userRole: string,
  userPermissions: string[] = [],
  isCreator: boolean = false
): string[] {
  const rolePermissions = getPermissionsByRole(userRole)
  const allPermissions = [...new Set([...rolePermissions, ...userPermissions])]
  
  // 如果是创建者，给予所有权限
  if (isCreator) {
    return Object.values(PERMISSIONS)
  }
  
  return allPermissions
}

// 简化的权限检查 - 基于角色的基本权限
export function getBasicPermissionsByRole(role: string): string[] {
  switch (role) {
    case ROLES.ADMIN:
      return [
        PERMISSIONS.APP_USER_MANAGE,
        PERMISSIONS.APP_VIEW,
        PERMISSIONS.APP_DOWNLOAD,
        PERMISSIONS.App_EDIT,
        PERMISSIONS.APP_UPDATE,
        PERMISSIONS.APP_DELETE,
        PERMISSIONS.APP_DEPLOY
      ]
    case ROLES.EDITOR:
      return [
        PERMISSIONS.APP_VIEW,
        PERMISSIONS.APP_DOWNLOAD,
        PERMISSIONS.App_EDIT
      ]
    case ROLES.VIEWER:
      return [
        PERMISSIONS.APP_VIEW,
        PERMISSIONS.APP_DOWNLOAD
      ]
    default:
      return []
  }
}
