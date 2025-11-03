<template>
  <div class="global-header">
    <div class="header-left">
      <div class="logo-container">
        <img v-if="logoExists" src="../assets/logo.svg" alt="Logo" class="logo" />
        <span class="site-title">{{ siteTitle }}</span>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        :items="menuItems"
        class="header-menu"
        @click="handleMenuClick"
      />
    </div>
    <div class="header-right">
      <a-button type="primary" @click="handleLogin">
        登录
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()

const siteTitle = ref('AI Code 项目')

// 菜单配置
const menuItems = ref<MenuProps['items']>([
  {
    key: '/',
    label: '首页',
  },
  {
    key: '/about',
    label: '关于',
  },
])

const selectedKeys = ref<string[]>([route.path])

// 监听路由变化，更新选中的菜单项
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath]
  },
  { immediate: true }
)

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (info) => {
  router.push(info.key as string)
}

// 处理登录按钮点击
const handleLogin = () => {
  // TODO: 实现登录逻辑
  console.log('点击登录')
}

// 检查 logo 是否存在（这里简化处理，实际可以通过 API 或静态资源检测）
const logoExists = ref(true)
</script>

<style scoped>
.global-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.logo-container {
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.logo {
  height: 32px;
  width: auto;
  margin-right: 12px;
}

.site-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  border-bottom: none;
  line-height: 62px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .global-header {
    padding: 0 16px;
  }

  .logo-container {
    margin-right: 16px;
  }

  .site-title {
    font-size: 16px;
  }

  .header-menu {
    display: none;
  }
}

@media (max-width: 576px) {
  .site-title {
    font-size: 14px;
  }

  .logo {
    height: 24px;
    margin-right: 8px;
  }
}
</style>

