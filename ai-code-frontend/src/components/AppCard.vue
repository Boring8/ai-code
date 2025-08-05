<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <div class="placeholder-icon">
          <div class="ai-icon">
            <div class="ai-head">
              <div class="ai-eyes">
                <div class="ai-eye left"></div>
                <div class="ai-eye right"></div>
              </div>
              <div class="ai-mouth"></div>
            </div>
            <div class="ai-antenna left-antenna"></div>
            <div class="ai-antenna right-antenna"></div>
          </div>
        </div>
        <span class="placeholder-text">AI 生成</span>
      </div>
      <div class="app-overlay">
        <div class="overlay-content">
          <a-button type="primary" size="large" @click="handleViewChat" class="overlay-btn">
            查看对话
          </a-button>
          <a-button 
            v-if="app.deployKey" 
            type="default" 
            size="large" 
            @click="handleViewWork"
            class="overlay-btn"
          >
            查看作品
          </a-button>
        </div>
      </div>
    </div>
    <div class="app-info">
      <div class="app-header">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <div v-if="featured" class="featured-badge">
          <span class="badge-icon">⭐</span>
          精选
        </div>
      </div>
      <div class="app-meta">
        <div class="app-author">
          <a-avatar :size="24" class="author-avatar">
            U
          </a-avatar>
          <span class="author-name">{{ featured ? '官方' : '未知用户' }}</span>
        </div>
        <div class="app-actions">
          <a-button type="text" size="small" @click="handleViewChat" class="action-btn">
            对话
          </a-button>
          <a-button 
            v-if="app.deployKey" 
            type="text" 
            size="small" 
            @click="handleViewWork"
            class="action-btn"
          >
            预览
          </a-button>
        </div>
      </div>
      <!-- 添加时间显示 -->
      <div class="app-time">
        <span class="time-label">{{ timeLabel }}</span>
        <span class="time-value">{{ displayTime }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatRelativeTime } from '@/utils/time'

interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

// 计算显示的时间
const displayTime = computed(() => {
  // 优先显示修改时间，如果没有就显示创建时间
  const time = props.app.updateTime || props.app.createTime
  return formatRelativeTime(time)
})

// 计算时间标签
const timeLabel = computed(() => {
  return props.app.updateTime ? '修改于' : '创建于'
})

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}
</script>

<style scoped>
.app-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(24, 144, 255, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  backdrop-filter: blur(10px);
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(24, 144, 255, 0.15);
  border-color: rgba(24, 144, 255, 0.2);
}

.app-card--featured {
  border-color: rgba(255, 193, 7, 0.3);
  box-shadow: 0 4px 20px rgba(255, 193, 7, 0.1);
}

.app-card--featured:hover {
  border-color: rgba(255, 193, 7, 0.5);
  box-shadow: 0 8px 32px rgba(255, 193, 7, 0.2);
}

.app-preview {
  height: 160px;
  background: linear-gradient(135deg, #f8f9ff 0%, #e6f3ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #999;
}

.placeholder-icon {
  font-size: 32px;
}

/* AI图标样式 */
.ai-icon {
  position: relative;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-head {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%);
  border-radius: 50%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 154, 158, 0.3);
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.ai-eyes {
  display: flex;
  gap: 10px;
  margin-bottom: 6px;
}

.ai-eye {
  width: 8px;
  height: 8px;
  background: #4a90e2;
  border-radius: 50%;
  position: relative;
  animation: cuteBlink 4s infinite;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.ai-eye::after {
  content: '';
  position: absolute;
  top: 1px;
  left: 1px;
  width: 3px;
  height: 3px;
  background: white;
  border-radius: 50%;
}

.ai-eye::before {
  content: '';
  position: absolute;
  top: 0.5px;
  left: 0.5px;
  width: 2px;
  height: 2px;
  background: #2c5aa0;
  border-radius: 50%;
}

.ai-mouth {
  width: 6px;
  height: 3px;
  background: #ff6b9d;
  border-radius: 2px;
  margin-top: 2px;
  position: relative;
}

.ai-mouth::after {
  content: '';
  position: absolute;
  top: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 2px;
  height: 1px;
  background: #ff6b9d;
  border-radius: 1px;
}

.ai-antenna {
  position: absolute;
  width: 3px;
  height: 14px;
  background: linear-gradient(to bottom, #ffb6c1, #ff69b4);
  border-radius: 2px;
  top: -8px;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.left-antenna {
  left: 6px;
  transform: rotate(-20deg);
}

.right-antenna {
  right: 6px;
  transform: rotate(20deg);
}

.left-antenna::after,
.right-antenna::after {
  content: '';
  position: absolute;
  top: -3px;
  left: -2px;
  width: 7px;
  height: 7px;
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 4px rgba(255, 215, 0, 0.3);
}

/* 添加可爱的腮红 */
.ai-head::before,
.ai-head::after {
  content: '';
  position: absolute;
  width: 6px;
  height: 6px;
  background: rgba(255, 182, 193, 0.6);
  border-radius: 50%;
  top: 50%;
  transform: translateY(-50%);
}

.ai-head::before {
  left: -2px;
}

.ai-head::after {
  right: -2px;
}

@keyframes cuteBlink {
  0%, 85%, 100% {
    transform: scaleY(1);
  }
  90% {
    transform: scaleY(0.1);
  }
  95% {
    transform: scaleY(1);
  }
}

/* 添加轻微的浮动动画 */
.ai-icon {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-3px);
  }
}

.placeholder-text {
  font-size: 14px;
  font-weight: 500;
}

.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
  backdrop-filter: blur(4px);
}

.app-card:hover .app-overlay {
  opacity: 1;
}

.overlay-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.overlay-btn {
  min-width: 120px;
  border-radius: 8px;
  font-weight: 500;
}

.app-info {
  padding: 16px;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.app-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #1a1a1a;
  line-height: 1.4;
  flex: 1;
  min-width: 0;
}

.featured-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #ffc107 0%, #ffb300 100%);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
  margin-left: 8px;
}

.badge-icon {
  font-size: 12px;
}

.app-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.app-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  border: 2px solid rgba(24, 144, 255, 0.2);
}

.author-name {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.app-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  color: #666;
  border: none;
  background: transparent;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

/* 新增时间样式 */
.app-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.time-label {
  font-weight: 500;
}

.time-value {
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-preview {
    height: 140px;
  }
  
  .app-info {
    padding: 12px;
  }
  
  .app-title {
    font-size: 14px;
  }
  
  .author-name {
    font-size: 12px;
  }
  
  .app-time {
    font-size: 11px;
  }
}
</style>
