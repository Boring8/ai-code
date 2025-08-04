<script setup lang="ts">
import { Layout } from 'ant-design-vue'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const { Header, Content, Footer } = Layout
</script>

<template>
  <Layout class="basic-layout">
    <Header class="layout-header">
      <GlobalHeader />
    </Header>
    
    <Content class="layout-content">
      <router-view />
    </Content>
    
    <Footer class="layout-footer">
      <GlobalFooter />
    </Footer>
  </Layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
}

.layout-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 0;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.layout-content {
  margin-top: 64px;
  min-height: calc(100vh - 64px - 70px);
  padding: 24px;
  background: #f0f2f5;
}

.layout-footer {
  padding: 0;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-content {
    padding: 16px;
  }
}
</style> 