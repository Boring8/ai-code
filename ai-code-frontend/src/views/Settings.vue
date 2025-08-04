<script setup lang="ts">
import { ref } from 'vue'
import { Card, Typography, Form, Switch, Input, Button, Space } from 'ant-design-vue'

const { Title } = Typography
const { Item } = Form

// 表单数据
const formData = ref({
  notifications: true,
  theme: 'light',
  language: 'zh-CN'
})

// 处理表单提交
const handleSubmit = (values: any) => {
  console.log('设置已保存:', values)
}
</script>

<template>
  <div class="settings-page">
    <Card>
              <Title :level="2">系统设置</Title>
      
      <Form
        :model="formData"
        layout="vertical"
        @finish="handleSubmit"
        style="max-width: 600px;"
      >
        <Item label="通知设置" name="notifications">
          <Switch v-model:checked="formData.notifications" />
        </Item>
        
        <Item label="主题设置" name="theme">
          <Input v-model:value="formData.theme" placeholder="请输入主题" />
        </Item>
        
        <Item label="语言设置" name="language">
          <Input v-model:value="formData.language" placeholder="请输入语言" />
        </Item>
        
        <Item>
          <Space>
            <Button type="primary" html-type="submit">
              保存设置
            </Button>
            <Button>
              重置
            </Button>
          </Space>
        </Item>
      </Form>
    </Card>
  </div>
</template>

<style scoped>
.settings-page {
  max-width: 800px;
  margin: 0 auto;
}
</style> 