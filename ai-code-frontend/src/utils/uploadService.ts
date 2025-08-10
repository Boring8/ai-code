import { message } from 'ant-design-vue'
import { uploadImage, uploadImages, deleteImage } from '@/api/fileUploadController'

export interface UploadOptions {
  maxSize?: number // 最大文件大小，单位MB
  accept?: string // 接受的文件类型
  multiple?: boolean // 是否支持多文件上传
}

export interface UploadResult {
  success: boolean
  data?: string | string[]
  error?: string
}

export class UploadService {
  private options: UploadOptions

  constructor(options: UploadOptions = {}) {
    this.options = {
      maxSize: 5,
      accept: 'image/*',
      multiple: false,
      ...options
    }
  }

  /**
   * 验证文件
   */
  private validateFile(file: File): boolean {
    // 检查文件类型
    if (this.options.accept && this.options.accept !== '*') {
      const acceptTypes = this.options.accept.split(',')
      const isValidType = acceptTypes.some(type => {
        if (type.endsWith('/*')) {
          const baseType = type.replace('/*', '')
          return file.type.startsWith(baseType)
        }
        return file.type === type
      })
      
      if (!isValidType) {
        message.error(`文件类型不支持，请上传 ${this.options.accept} 格式的文件`)
        return false
      }
    }

    // 检查文件大小
    if (this.options.maxSize) {
      const maxSizeBytes = this.options.maxSize * 1024 * 1024
      if (file.size > maxSizeBytes) {
        message.error(`文件大小不能超过 ${this.options.maxSize}MB`)
        return false
      }
    }

    return true
  }

  /**
   * 上传单个文件
   */
  async uploadSingleFile(file: File): Promise<UploadResult> {
    try {
      // 验证文件
      if (!this.validateFile(file)) {
        return { success: false, error: '文件验证失败' }
      }

      // 创建FormData
      const formData = new FormData()
      formData.append('file', file)

      // 调用上传接口
      const response = await uploadImage(formData)

      if (response.data.code === 0 && response.data.data) {
        return {
          success: true,
          data: response.data.data
        }
      } else {
        return {
          success: false,
          error: response.data.message || '上传失败'
        }
      }
    } catch (error) {
      console.error('上传文件失败:', error)
      return {
        success: false,
        error: '上传失败，请重试'
      }
    }
  }

  /**
   * 上传多个文件
   */
  async uploadMultipleFiles(files: File[]): Promise<UploadResult> {
    try {
      // 验证所有文件
      for (const file of files) {
        if (!this.validateFile(file)) {
          return { success: false, error: '文件验证失败' }
        }
      }

      // 逐个上传文件
      const uploadPromises = files.map(file => this.uploadSingleFile(file))
      const results = await Promise.all(uploadPromises)

      // 检查是否有失败的
      const failedResults = results.filter(result => !result.success)
      if (failedResults.length > 0) {
        return {
          success: false,
          error: `有 ${failedResults.length} 个文件上传失败`
        }
      }

      // 提取成功上传的URL
      const urls = results
        .filter(result => result.success)
        .map(result => result.data as string)

      return {
        success: true,
        data: urls
      }
    } catch (error) {
      console.error('批量上传文件失败:', error)
      return {
        success: false,
        error: '批量上传失败，请重试'
      }
    }
  }

  /**
   * 删除文件
   */
  async deleteFile(fileUrl: string): Promise<boolean> {
    try {
      const response = await deleteImage({ imageUrl: fileUrl })
      return response.data.code === 0
    } catch (error) {
      console.error('删除文件失败:', error)
      return false
    }
  }

  /**
   * 获取文件扩展名
   */
  getFileExtension(filename: string): string {
    return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
  }

  /**
   * 格式化文件大小
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B'
    
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  /**
   * 检查是否为图片文件
   */
  isImageFile(file: File): boolean {
    return file.type.startsWith('image/')
  }

  /**
   * 创建文件预览URL
   */
  createPreviewUrl(file: File): string {
    return URL.createObjectURL(file)
  }

  /**
   * 释放预览URL
   */
  revokePreviewUrl(url: string): void {
    URL.revokeObjectURL(url)
  }
}

// 创建默认的上传服务实例
export const defaultUploadService = new UploadService()

// 创建图片上传服务实例
export const imageUploadService = new UploadService({
  accept: 'image/*',
  maxSize: 5,
  multiple: true
})

// 创建文档上传服务实例
export const documentUploadService = new UploadService({
  accept: '.pdf,.doc,.docx,.txt',
  maxSize: 10,
  multiple: false
})
