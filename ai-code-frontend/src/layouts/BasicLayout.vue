<template>
  <a-layout class="basic-layout">
    <a-layout-header class="layout-header">
      <GlobalHeader />
    </a-layout-header>
    <a-layout-content class="layout-content">
      <RouterView />
    </a-layout-content>
    <a-layout-footer class="layout-footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 0;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.layout-content {
  flex: 1;
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 64px - 70px);
}

.layout-footer {
  padding: 0;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}
</style>


