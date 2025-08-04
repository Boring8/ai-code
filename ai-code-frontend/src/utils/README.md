# 请求工具库使用说明

## 概述

本项目使用 Axios 作为 HTTP 请求库，并封装了统一的请求工具，包含以下功能：

- 统一的请求配置
- 全局请求/响应拦截器
- 自动错误处理
- 用户认证状态管理
- 开发环境日志

## 文件结构

```
src/
├── utils/
│   └── request.ts          # Axios 请求工具
├── api/
│   └── index.ts            # API 接口定义
├── stores/
│   └── user.ts             # 用户状态管理
└── config/
    └── index.ts            # 环境配置
```

## 使用方法

### 1. 直接使用 request 实例

```typescript
import request from '@/utils/request'

// GET 请求
const response = await request.get('/api/users')

// POST 请求
const response = await request.post('/api/users', {
  name: 'John',
  email: 'john@example.com'
})

// PUT 请求
const response = await request.put('/api/users/1', {
  name: 'John Updated'
})

// DELETE 请求
const response = await request.delete('/api/users/1')
```

### 2. 使用预定义的 API 接口

```typescript
import { userApi, systemApi } from '@/api'

// 用户相关接口
const userInfo = await userApi.getUserInfo()
const loginResult = await userApi.login({ username: 'admin', password: '123456' })

// 系统相关接口
const config = await systemApi.getConfig()
```

### 3. 使用用户状态管理

```typescript
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 获取用户信息
await userStore.getUserInfo()

// 用户登录
const success = await userStore.login('username', 'password')

// 用户登出
await userStore.logout()

// 检查登录状态
if (userStore.isLoggedIn) {
  console.log('用户已登录:', userStore.userInfo)
}
```

## 配置说明

### 环境变量

在 `.env` 文件中配置：

```env
# API 基础地址
VITE_API_BASE_URL=http://localhost:8123/api

# 其他配置...
```

### 配置文件

在 `src/config/index.ts` 中可以修改：

```typescript
export const config = {
  apiBaseURL: 'http://localhost:8123/api',
  requestTimeout: 60000,
  enableRequestLog: true,  // 开发环境请求日志
  enableResponseLog: true  // 开发环境响应日志
}
```

## 错误处理

### 自动错误处理

- 网络错误：自动显示"网络连接失败"提示
- HTTP 状态码错误：根据状态码显示相应错误信息
- 401 未授权：自动跳转到登录页面

### 自定义错误处理

```typescript
try {
  const response = await request.get('/api/data')
  // 处理成功响应
} catch (error) {
  // 处理错误
  console.error('请求失败:', error)
}
```

## 拦截器功能

### 请求拦截器

- 自动添加认证信息
- 开发环境请求日志
- 请求参数处理

### 响应拦截器

- 统一响应格式处理
- 自动错误处理
- 用户认证状态检查
- 开发环境响应日志

## 类型支持

所有 API 接口都支持 TypeScript 类型：

```typescript
import { ApiResponse, UserInfo } from '@/api'

// 类型安全的 API 调用
const response: ApiResponse<UserInfo> = await userApi.getUserInfo()
const user: UserInfo = response.data
```

## 最佳实践

1. **使用预定义的 API 接口**：避免直接使用 request 实例
2. **统一错误处理**：利用拦截器的自动错误处理
3. **类型安全**：使用 TypeScript 类型定义
4. **状态管理**：使用 Pinia store 管理用户状态
5. **环境配置**：通过配置文件管理不同环境的设置 