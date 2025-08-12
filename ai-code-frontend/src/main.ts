import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import '@/access'
import { registerPermissionDirectives } from './utils/permissionDirective'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

// 注册权限指令
registerPermissionDirectives(app)

app.mount('#app')
