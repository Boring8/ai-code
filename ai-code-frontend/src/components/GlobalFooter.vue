<script setup lang="ts">
// 全局底部组件
</script>

<template>
  <div class="global-footer">
    <div class="footer-content">
      <p class="copyright">by easen</p>
    </div>
  </div>
</template>

<style scoped>
.global-footer {
  height: 70px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}

.footer-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 0 24px;
}

.copyright {
  margin: 0;
  font-size: 14px;
  color: #666;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    padding: 0 16px;
  }
  
  .copyright {
    font-size: 12px;
  }
}
</style> 