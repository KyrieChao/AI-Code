<template>
  <BasicLayout />
</template>
<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
useLoginUserStore().fetchLoginUser();
</script>
