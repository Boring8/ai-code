# AI Code 用户权限管理系统

这是一个基于 Vue 3 + TypeScript + Ant Design Vue 构建的现代化用户权限管理前端系统。

## 功能特性

### 🎯 核心功能
- **角色权限区分**：根据用户角色字段 "userRole" 判断用户权限
- **用户管理**：管理员可以管理所有用户信息
- **个人信息编辑**：用户可编辑自己的信息
- **实时权限控制**：权限切换时实时更新页面元素可见性

### 🔐 权限管理
- **普通用户 (userRole: "user")**
  - 隐藏用户管理页面入口
  - 只能编辑自己的个人信息
  - 无法访问用户管理功能

- **管理员 (userRole: "admin")**
  - 显示用户管理页面入口
  - 可以管理所有用户信息
  - 拥有完整的系统权限

### 👥 用户管理功能
- **用户列表展示**：分页显示用户基本信息
- **用户添加**：新增用户功能
- **用户删除**：删除用户功能（带确认提示）
- **用户信息编辑**：修改用户信息
- **用户查询筛选**：支持按用户名、账号、角色、简介搜索

### ✏️ 用户信息编辑
- **头像点击跳转**：点击任意用户头像跳转到编辑页面
- **权限控制**：普通用户只能编辑自己，管理员可编辑所有用户
- **表单验证**：完整的表单验证和错误提示
- **实时更新**：修改后自动更新相关页面数据

## 技术栈

- **前端框架**：Vue 3 (Composition API)
- **类型系统**：TypeScript
- **UI 组件库**：Ant Design Vue 4.x
- **状态管理**：Pinia
- **路由管理**：Vue Router 4
- **构建工具**：Vite
- **代码规范**：ESLint + Prettier

## 项目结构

```
src/
├── api/                    # API 接口
│   ├── userController.ts   # 用户相关 API
│   └── typings.d.ts       # API 类型定义
├── components/             # 公共组件
│   ├── GlobalHeader.vue   # 全局头部
│   ├── GlobalFooter.vue   # 全局底部
│   └── PermissionControl.vue # 权限控制组件
├── stores/                 # 状态管理
│   └── user.ts            # 用户状态管理
├── views/                  # 页面组件
│   ├── Home.vue           # 首页
│   ├── Login.vue          # 登录页
│   ├── Register.vue       # 注册页
│   ├── Settings.vue       # 设置页
│   ├── UserManagement.vue # 用户管理页
│   └── UserEdit.vue       # 用户编辑页
├── router/                 # 路由配置
│   └── index.ts           # 路由定义
└── utils/                  # 工具函数
    └── request.ts         # HTTP 请求封装
```

## 快速开始

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run dev
```

### 构建生产版本
```bash
npm run build
```

## 使用说明

### 1. 用户登录
- 访问登录页面 `/login`
- 输入账号和密码
- 系统会根据用户角色自动设置权限

### 2. 权限体验
- **普通用户登录后**：
  - 首页显示用户信息和个人编辑按钮
  - 导航栏不显示"用户管理"菜单
  - 只能编辑自己的个人信息

- **管理员登录后**：
  - 首页显示用户管理按钮
  - 导航栏显示"用户管理"菜单
  - 可以管理所有用户信息

### 3. 用户管理功能
- 访问 `/user-management` 页面（仅管理员）
- 支持用户列表查看、搜索、筛选
- 支持添加、编辑、删除用户
- 点击用户头像可直接跳转到编辑页面

### 4. 个人信息编辑
- 点击任意用户头像或"编辑个人信息"按钮
- 跳转到 `/user/edit/:id` 页面
- 根据权限显示不同的编辑选项

## 权限控制实现

### 1. 路由级权限控制
```typescript
// 在路由配置中添加 meta 信息
{
  path: '/user-management',
  name: 'userManagement',
  component: () => import('@/views/UserManagement.vue'),
  meta: { requiresAdmin: true }
}
```

### 2. 组件级权限控制
```vue
<!-- 使用 PermissionControl 组件 -->
<PermissionControl require-role="admin">
  <Button>管理员专属按钮</Button>
</PermissionControl>
```

### 3. 计算属性权限控制
```typescript
// 在组件中使用计算属性
const canManageUsers = computed(() => userStore.canManageUsers)
```

## API 接口

系统使用以下主要 API 接口：

- `GET /user/get/login` - 获取当前登录用户信息
- `POST /user/login` - 用户登录
- `POST /user/logout` - 用户登出
- `POST /user/list/page/vo` - 分页获取用户列表
- `GET /user/get/vo` - 根据ID获取用户信息
- `POST /user/add` - 添加用户
- `POST /user/update` - 更新用户信息
- `POST /user/delete` - 删除用户

## 开发指南

### 添加新的权限控制
1. 在 `stores/user.ts` 中添加新的计算属性
2. 使用 `PermissionControl` 组件包装需要权限控制的元素
3. 在组件中使用计算属性进行条件渲染

### 自定义权限检查
```vue
<PermissionControl :check-permission="customPermissionCheck">
  <div>自定义权限内容</div>
</PermissionControl>
```

### 响应式设计
系统采用响应式设计，支持移动端和桌面端：
- 使用 CSS Grid 和 Flexbox 布局
- 媒体查询适配不同屏幕尺寸
- 移动端优化的交互体验

## 注意事项

1. **权限验证**：所有权限检查都在前端进行，实际项目中需要在后端也进行相应的权限验证
2. **数据安全**：敏感操作（如删除用户）都有确认提示
3. **用户体验**：所有操作都有加载状态和成功/失败提示
4. **表单验证**：所有表单都有完整的验证规则

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

MIT License
