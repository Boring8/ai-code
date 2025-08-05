<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, h } from 'vue'
import { useRouter } from 'vue-router'
import { Avatar, Button, message, Modal } from 'ant-design-vue'
import { UserOutlined, EditOutlined, LogoutOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'

interface Props {
  size?: number
  showName?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 32,
  showName: true
})

const router = useRouter()
const userStore = useUserStore()

// 菜单状态
const isMenuVisible = ref(false)
const menuRef = ref<HTMLDivElement>()
const avatarRef = ref<HTMLDivElement>()
const hideTimeout = ref<NodeJS.Timeout | null>(null)

// 计算用户显示名称
const displayName = computed(() => {
  return userStore.userInfo?.userName || userStore.userInfo?.userAccount || '用户'
})

// 显示菜单
const showMenu = () => {
  if (hideTimeout.value) {
    clearTimeout(hideTimeout.value)
    hideTimeout.value = null
  }
  isMenuVisible.value = true
}

// 隐藏菜单
const hideMenu = (delay = 150) => {
  if (hideTimeout.value) {
    clearTimeout(hideTimeout.value)
  }
  hideTimeout.value = setTimeout(() => {
    isMenuVisible.value = false
  }, delay)
}

// 处理头像鼠标进入
const handleAvatarMouseEnter = () => {
  showMenu()
}

// 处理头像鼠标离开
const handleAvatarMouseLeave = () => {
  hideMenu()
}

// 处理菜单鼠标进入
const handleMenuMouseEnter = () => {
  showMenu()
}

// 处理菜单鼠标离开
const handleMenuMouseLeave = () => {
  hideMenu()
}

// 处理修改用户信息
const handleEditProfile = () => {
  // 跳转到用户编辑页面
  if (userStore.userInfo?.id) {
    router.push(`/user/edit/${userStore.userInfo.id}`)
  } else {
    message.error('用户信息获取失败')
  }
  hideMenu(0)
}

// 处理退出登录
const handleLogout = () => {
  Modal.confirm({
    title: '确认退出登录',
    icon: h(ExclamationCircleOutlined),
    content: '您确定要退出登录吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await userStore.logout()
        message.success('已成功退出登录')
        router.push('/login')
      } catch (error) {
        message.error('退出登录失败')
      }
    }
  })
  hideMenu(0)
}

// 处理键盘事件
const handleKeyDown = (event: KeyboardEvent) => {
  if (!isMenuVisible.value) return
  
  switch (event.key) {
    case 'Escape':
      hideMenu(0)
      break
    case 'Enter':
      if (document.activeElement === avatarRef.value) {
        handleEditProfile()
      }
      break
    case 'Tab':
      // 让Tab键正常工作，不阻止默认行为
      break
  }
}

// 处理点击外部区域
const handleClickOutside = (event: MouseEvent) => {
  if (menuRef.value && !menuRef.value.contains(event.target as Node) &&
      avatarRef.value && !avatarRef.value.contains(event.target as Node)) {
    hideMenu(0)
  }
}

// 组件挂载时添加事件监听
onMounted(() => {
  document.addEventListener('keydown', handleKeyDown)
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
  document.removeEventListener('click', handleClickOutside)
  if (hideTimeout.value) {
    clearTimeout(hideTimeout.value)
  }
})
</script>

<template>
  <div class="user-avatar-menu">
    <!-- 用户头像 -->
         <div 
       ref="avatarRef"
       class="avatar-container"
       @mouseenter="handleAvatarMouseEnter"
       @mouseleave="handleAvatarMouseLeave"
       tabindex="0"
       role="button"
       :aria-label="`${displayName}的头像，悬停显示菜单`"
     >
      <Avatar 
        :src="userStore.userInfo?.userAvatar" 
        :size="props.size"
        class="user-avatar"
      >
        <template #icon><UserOutlined /></template>
      </Avatar>
      <span v-if="props.showName" class="user-name">{{ displayName }}</span>
    </div>

    <!-- 悬停菜单 -->
    <Transition name="menu-fade">
      <div 
        v-if="isMenuVisible"
        ref="menuRef"
        class="menu-container"
        @mouseenter="handleMenuMouseEnter"
        @mouseleave="handleMenuMouseLeave"
      >
        <div class="menu-content">
          <!-- 菜单选项 -->
          <div class="menu-options">
            <button 
              class="menu-option"
              @click="handleEditProfile"
              @keydown.enter="handleEditProfile"
              tabindex="0"
            >
              <EditOutlined class="option-icon" />
              <span class="option-text">修改个人信息</span>
            </button>

            <button 
              class="menu-option"
              @click="handleLogout"
              @keydown.enter="handleLogout"
              tabindex="0"
            >
              <LogoutOutlined class="option-icon" />
              <span class="option-text">退出当前登录</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.user-avatar-menu {
  position: relative;
  display: inline-block;
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
}

.avatar-container:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.avatar-container:focus {
  background-color: rgba(0, 0, 0, 0.04);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.user-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.avatar-container:hover .user-avatar {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-name {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
}

.menu-container {
  position: absolute;
  top:80%;
  right: 0;
  margin-top: 8px;
  z-index: 1000;
  min-width: 140px;
  max-width: 160px;
}

.menu-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 0, 0, 0.06);
  overflow: hidden;
  backdrop-filter: blur(10px);
}


.menu-option {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 0px 14px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  outline: none;
}

.menu-option:hover {
  background-color: #f5f5f5;
}

.menu-option:focus {
  background-color: #f0f8ff;
  box-shadow: inset 0 0 0 1px #1890ff;
}

.option-icon {
  font-size: 14px;
  color: #666;
  transition: color 0.2s ease;
}

.menu-option:hover .option-icon {
  color: #1890ff;
}

.option-text {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

/* 过渡动画 */
.menu-fade-enter-active,
.menu-fade-leave-active {
  transition: all 0.3s ease;
}

.menu-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.menu-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .menu-container {
    right: -50px;
    min-width: 150px;
    max-width: 160px;
  }
  
  .user-name {
    display: none;
  }
  
  .avatar-container {
    padding: 4px;
  }
}

@media (max-width: 576px) {
  .menu-container {
    right: -80px;
    min-width: 140px;
    max-width: 150px;
  }
  
  .menu-option {
    padding: 8px 12px;
  }
}

/* 无障碍支持 */
@media (prefers-reduced-motion: reduce) {
  .menu-fade-enter-active,
  .menu-fade-leave-active {
    transition: none;
  }
  
  .avatar-container,
  .user-avatar,
  .menu-option {
    transition: none;
  }
}
</style> 