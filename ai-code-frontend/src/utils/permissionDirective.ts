import type { Directive, DirectiveBinding } from 'vue'
import { hasPermission, canViewApp, canEditApp, canDeleteApp, canDownloadApp, canManageAppUsers, canGenCode, canDeployCode } from './permissionUtils'

// 权限指令类型
type PermissionDirectiveValue = {
  permission: string
  permissions?: string[]
  fallback?: boolean
}

// 权限检查指令
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<PermissionDirectiveValue>) {
    const { permission, permissions = [], fallback = false } = binding.value || {}
    
    if (!permission && permissions.length === 0) {
      console.warn('权限指令需要指定permission或permissions')
      return
    }
    
    // 这里应该从全局状态或props中获取用户权限
    // 暂时使用空数组，实际使用时需要传入用户权限
    const userPermissions: string[] = []
    
    const hasAccess = permission 
      ? hasPermission(userPermissions, permission)
      : permissions.some(p => hasPermission(userPermissions, p))
    
    if (!hasAccess && !fallback) {
      el.style.display = 'none'
    }
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding<PermissionDirectiveValue>) {
    const { permission, permissions = [], fallback = false } = binding.value || {}
    
    if (!permission && permissions.length === 0) {
      return
    }
    
    // 这里应该从全局状态或props中获取用户权限
    const userPermissions: string[] = []
    
    const hasAccess = permission 
      ? hasPermission(userPermissions, permission)
      : permissions.some(p => hasPermission(userPermissions, p))
    
    if (!hasAccess && !fallback) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

// 具体权限检查指令
export const canView: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canViewApp(userPermissions)) {
      el.style.display = 'none'
    }
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canViewApp(userPermissions)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

export const canEdit: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canEditApp(userPermissions)) {
      el.style.display = 'none'
    }
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canEditApp(userPermissions)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

export const canDelete: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canDeleteApp(userPermissions)) {
      el.style.display = 'none'
    }
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canDeleteApp(userPermissions)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

export const canDownload: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canDownloadApp(userPermissions)) {
      el.style.display = 'none'
    }
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canDownloadApp(userPermissions)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

export const canManageUsers: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canManageAppUsers(userPermissions)) {
      el.style.display = 'none'
    }
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canManageAppUsers(userPermissions)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

export const canGenCode: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canGenCode(userPermissions)) {
      el.style.display = 'none'
    }
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canGenCode(userPermissions)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

export const canDeploy: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canDeployCode(userPermissions)) {
      el.style.display = 'none'
    }
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const userPermissions: string[] = binding.value || []
    if (!canDeployCode(userPermissions)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

// 注册所有权限指令
export function registerPermissionDirectives(app: any) {
  app.directive('permission', permission)
  app.directive('can-view', canView)
  app.directive('can-edit', canEdit)
  app.directive('can-delete', canDelete)
  app.directive('can-download', canDownload)
  app.directive('can-manage-users', canManageUsers)
  app.directive('can-gen-code', canGenCode)
  app.directive('can-deploy', canDeploy)
}
