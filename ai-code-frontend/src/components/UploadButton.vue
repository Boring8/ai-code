<script setup lang="ts">
import { ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import { imageUploadService } from '@/utils/uploadService'

interface Props {
  disabled?: boolean
  multiple?: boolean
  maxCount?: number
  accept?: string
  buttonText?: string
  buttonType?: 'text' | 'default' | 'primary'
  buttonSize?: 'small' | 'middle' | 'large'
  showIcon?: boolean
}

interface Emits {
  (e: 'upload-success', urls: string[]): void
  (e: 'upload-error', error: string): void
  (e: 'upload-start'): void
  (e: 'upload-end'): void
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  multiple: false,
  maxCount: 9,
  accept: 'image/*',
  buttonText: '上传',
  buttonType: 'text',
  buttonSize: 'middle',
  showIcon: true
})

const emit = defineEmits<Emits>()

// 响应式数据
const uploading = ref(false)
const fileInputRef = ref<HTMLInputElement>()

// 处理文件选择
const handleFileSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  
  if (!files || files.length === 0) {
    return
  }

  try {
    uploading.value = true
    emit('upload-start')

    const fileArray = Array.from(files)
    
    // 验证文件数量
    if (fileArray.length > props.maxCount) {
      message.error(`最多只能上传 ${props.maxCount} 个文件`)
      return
    }

    // 上传文件
    const result = await imageUploadService.uploadMultipleFiles(fileArray)
    
    if (result.success && result.data) {
      const urls = Array.isArray(result.data) ? result.data : [result.data]
      emit('upload-success', urls)
      message.success(`成功上传 ${urls.length} 个文件`)
    } else {
      emit('upload-error', result.error || '上传失败')
      message.error(result.error || '上传失败')
    }
  } catch (error) {
    console.error('上传文件失败:', error)
    emit('upload-error', '上传失败，请重试')
    message.error('上传失败，请重试')
  } finally {
    uploading.value = false
    emit('upload-end')
    // 清空文件输入框
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

// 触发文件选择
const triggerFileSelect = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

// 获取按钮图标
const getButtonIcon = () => {
  if (!props.showIcon) return null
  return uploading.value ? null : UploadOutlined
}
</script>

<template>
  <div class="upload-button-wrapper">
    <!-- 隐藏的文件输入框 -->
    <input
      ref="fileInputRef"
      type="file"
      :accept="accept"
      :multiple="multiple"
      :disabled="disabled || uploading"
      @change="handleFileSelect"
      class="hidden-file-input"
    />
    
    <!-- 上传按钮 -->
    <a-button
      :type="buttonType"
      :size="buttonSize"
      :disabled="disabled || uploading"
      :loading="uploading"
      @click="triggerFileSelect"
      class="upload-button"
    >
      <template #icon v-if="getButtonIcon()">
        <component :is="getButtonIcon()" />
      </template>
      {{ uploading ? '上传中...' : buttonText }}
    </a-button>
  </div>
</template>

<style scoped>
.upload-button-wrapper {
  display: inline-block;
}

.hidden-file-input {
  display: none;
}

.upload-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.upload-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
