<script setup lang="ts">
import { Card, Row, Col, Typography } from 'ant-design-vue'

const { Title, Paragraph } = Typography
</script>

<template>
  <div class="home-page">
    <Row :gutter="[24, 24]">
      <Col :span="24">
        <Card>
          <Title level="2">欢迎使用 AI Code</Title>
          <Paragraph>
            这是一个基于 Vue 3 + TypeScript + Ant Design Vue 构建的现代化前端项目。
          </Paragraph>
          <Paragraph>
            项目特点：
          </Paragraph>
          <ul>
            <li>使用 Vue 3 组合式 API</li>
            <li>TypeScript 类型支持</li>
            <li>Ant Design Vue 组件库</li>
            <li>响应式设计</li>
            <li>模块化布局</li>
          </ul>
        </Card>
      </Col>
      
      <Col :xs="24" :sm="12" :md="8">
        <Card title="功能特性">
          <Paragraph>
            支持多种功能模块，可根据需求进行扩展。
          </Paragraph>
        </Card>
      </Col>
      
      <Col :xs="24" :sm="12" :md="8">
        <Card title="技术栈">
          <Paragraph>
            Vue 3, TypeScript, Ant Design Vue, Pinia, Vue Router
          </Paragraph>
        </Card>
      </Col>
      
      <Col :xs="24" :sm="12" :md="8">
        <Card title="开发工具">
          <Paragraph>
            Vite, ESLint, Prettier, TypeScript
          </Paragraph>
        </Card>
      </Col>
    </Row>
  </div>
</template>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
}

ul {
  margin: 16px 0;
  padding-left: 20px;
}

li {
  margin: 8px 0;
}
</style> 