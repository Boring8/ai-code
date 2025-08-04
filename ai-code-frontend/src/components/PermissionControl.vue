<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

interface Props {
  // 需要的权限角色
  requireRole?: 'admin' | 'user' | 'any'
  // 是否显示内容
  show?: boolean
  // 是否隐藏内容
  hide?: boolean
  // 自定义权限检查函数
  checkPermission?: () => boolean
}

const props = withDefaults(defineProps<Props>(), {
  requireRole: 'any',
  show: true,
  hide: false
})

const userStore = useUserStore()

// 权限检查逻辑
const hasPermission = computed(() => {
  // 如果设置了自定义权限检查函数，优先使用
  if (props.checkPermission) {
    return props.checkPermission()
  }
  
  // 如果设置了隐藏，则不显示
  if (props.hide) {
    return false
  }
  
  // 如果设置了显示，则显示
  if (props.show) {
    // 检查角色权限
    switch (props.requireRole) {
      case 'admin':
        return userStore.isAdmin
      case 'user':
        return userStore.isUser
      case 'any':
        return userStore.isLoggedIn
      default:
        return true
    }
  }
  
  return false
})
</script>

<template>
  <div v-if="hasPermission">
    <slot />
  </div>
</template> 