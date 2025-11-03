<template>
  <div class="global-footer">
    <div class="footer-content">
      <p>陈鸽涛原创项目</p>
    </div>
  </div>
</template>

<script setup lang="ts">
// Footer 组件，固定在底部
</script>

<style scoped>
.global-footer {
  width: 100%;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  text-align: center;
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

.footer-content p {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    padding: 12px 16px;
    font-size: 12px;
  }
}
</style>


