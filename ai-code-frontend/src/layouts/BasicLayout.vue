<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader />
    <!-- 主要内容区域 -->
    <a-layout-content class="main-content">
      <router-view />
    </a-layout-content>
    <!-- 底部版权信息 -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 50%, #f5f9ff 100%);
  min-height: 100vh;
}

.main-content {
  width: 100%;
  padding: 0;
  background: transparent;
  margin: 0;
  position: relative;
}

/* 添加背景装饰 */
.main-content::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(24, 144, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(64, 169, 255, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(105, 192, 255, 0.06) 0%, transparent 50%);
  pointer-events: none;
  z-index: -1;
}
</style>
