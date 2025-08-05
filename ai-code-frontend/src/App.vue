<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
/* 全局样式优化 */
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell',
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 50%, #f5f9ff 100%);
  color: #333;
}

html {
  overflow-x: hidden;
}

/* 全局按钮样式 */
.ant-btn {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.ant-btn-primary {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.ant-btn-primary:hover {
  background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

/* 全局卡片样式 */
.ant-card {
  border-radius: 12px;
  border: 1px solid #e8f4fd;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.ant-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 全局输入框样式 */
.ant-input, .ant-input-affix-wrapper {
  border-radius: 8px;
  border: 1px solid #d9e7f5;
  transition: all 0.3s ease;
}

.ant-input:focus, .ant-input-affix-wrapper:focus,
.ant-input-focused, .ant-input-affix-wrapper-focused {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 全局表格样式 */
.ant-table {
  border-radius: 12px;
  overflow: hidden;
}

.ant-table-thead > tr > th {
  background: #f8fbff;
  border-bottom: 1px solid #e8f4fd;
  color: #1890ff;
  font-weight: 600;
}

/* 全局分页样式 */
.ant-pagination-item {
  border-radius: 6px;
  border: 1px solid #d9e7f5;
}

.ant-pagination-item-active {
  background: #1890ff;
  border-color: #1890ff;
}

/* 全局模态框样式 */
.ant-modal-content {
  border-radius: 12px;
  border: 1px solid #e8f4fd;
}

.ant-modal-header {
  border-bottom: 1px solid #e8f4fd;
  border-radius: 12px 12px 0 0;
}

/* 全局下拉菜单样式 */
.ant-dropdown-menu {
  border-radius: 8px;
  border: 1px solid #e8f4fd;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

/* 全局选择器样式 */
.ant-select-selector {
  border-radius: 8px !important;
  border: 1px solid #d9e7f5 !important;
}

.ant-select-focused .ant-select-selector {
  border-color: #1890ff !important;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}
</style>
