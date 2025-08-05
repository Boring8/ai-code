<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <div class="footer-copyright">
        <p class="copyright-text">
          © 2024 AI Code. All rights reserved.
        </p>
      </div>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的脚本逻辑
</script>

<style scoped>
.footer {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(24, 144, 255, 0.1);
  padding: 24px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  text-align: center;
}

.footer-copyright {
  text-align: center;
}

.copyright-text {
  color: #999;
  font-size: 14px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer {
    padding: 16px 0;
  }
  
  .footer-content {
    padding: 0 16px;
  }
  
  .footer-links {
    gap: 12px;
  }
  
  .footer-link {
    font-size: 13px;
  }
}
</style>
