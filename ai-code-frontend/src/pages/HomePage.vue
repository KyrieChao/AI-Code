<template>
  <div id="homePage">
    <!-- 网站标题 -->
    <div class="header-section">
      <h1 class="main-title">
        一句话 呈所想
        <span class="cat-icon">🐱</span>
      </h1>
      <p class="subtitle">与 AI 对话轻松创建应用和网站</p>
    </div>

    <!-- 用户提示词输入框 -->
    <div class="input-section">
      <a-input
        v-model:value="promptInput"
        :placeholder="'使用 NoCode 创建一个产品原型'"
        size="large"
        class="prompt-input"
        @pressEnter="handleCreateApp"
      >
        <template #prefix>
          <span class="input-prefix">💬</span>
        </template>
        <template #suffix>
          <div class="input-actions">
            <a-button type="text" size="small" @click="handleUpload">
              <template #icon><UploadOutlined /></template>
              上传
            </a-button>
            <a-button type="text" size="small" @click="handleOptimize">
              <template #icon><BulbOutlined /></template>
              优化
            </a-button>
            <a-button type="primary" shape="circle" @click="handleCreateApp">
              <template #icon><ArrowUpOutlined /></template>
            </a-button>
          </div>
        </template>
      </a-input>
    </div>

    <!-- 快捷提示词按钮 -->
    <div class="quick-prompts">
      <a-button
        v-for="(prompt, index) in quickPrompts"
        :key="index"
        class="prompt-btn"
        @click="handleQuickPrompt(prompt)"
      >
        {{ prompt }}
      </a-button>
    </div>

    <!-- 我的应用分页列表 -->
    <div class="app-section">
      <h2 class="section-title">我的作品</h2>
      <a-spin :spinning="myAppsLoading">
        <a-list
          :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 4, xxl: 4 }"
          :data-source="myApps"
          :pagination="myAppsPagination"
        >
          <template #renderItem="{ item }">
            <a-list-item>
              <a-card
                hoverable
                class="app-card"
                @click="handleViewApp(item.id)"
              >
                <template #cover>
                  <div class="app-cover">
                    <img
                      v-if="item.cover"
                      :src="item.cover"
                      alt="应用封面"
                      class="cover-image"
                    />
                    <div v-else class="cover-placeholder">
                      <FileTextOutlined />
                    </div>
                  </div>
                </template>
                <a-card-meta>
                  <template #title>
                    <div class="app-title">{{ item.appName }}</div>
                  </template>
                  <template #description>
                    <div class="app-meta">
                      <span>创建于{{ formatTime(item.createTime) }}</span>
                    </div>
                  </template>
                </a-card-meta>
              </a-card>
            </a-list-item>
          </template>
        </a-list>
      </a-spin>
    </div>

    <!-- 精选应用分页列表 -->
    <div class="app-section">
      <h2 class="section-title">精选案例</h2>
      <a-spin :spinning="featuredAppsLoading">
        <a-list
          :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 4, xxl: 4 }"
          :data-source="featuredApps"
          :pagination="featuredAppsPagination"
        >
          <template #renderItem="{ item }">
            <a-list-item>
              <a-card
                hoverable
                class="app-card"
                @click="handleViewApp(item.id)"
              >
                <template #cover>
                  <div class="app-cover">
                    <img
                      v-if="item.cover"
                      :src="item.cover"
                      alt="应用封面"
                      class="cover-image"
                    />
                    <div v-else class="cover-placeholder">
                      <FileTextOutlined />
                    </div>
                  </div>
                </template>
                <a-card-meta>
                  <template #title>
                    <div class="app-title">{{ item.appName }}</div>
                  </template>
                  <template #description>
                    <div class="app-meta">
                      <span v-if="item.user">{{ item.user.userName }}</span>
                      <a-tag v-if="item.priority === 99" color="gold" class="featured-tag">
                        精选
                      </a-tag>
                    </div>
                  </template>
                </a-card-meta>
              </a-card>
            </a-list-item>
          </template>
        </a-list>
      </a-spin>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UploadOutlined,
  BulbOutlined,
  ArrowUpOutlined,
  FileTextOutlined,
} from '@ant-design/icons-vue'
import { addApp, listMyAppByPage, listFeaturedAppByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import dayjs from 'dayjs'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 提示词输入
const promptInput = ref('')

// 快捷提示词
const quickPrompts = [
  '波普风电商页面',
  '企业网站',
  '电商运营后台',
  '暗黑话题社区',
]

// 我的应用
const myApps = ref<API.AppVO[]>([])
const myAppsLoading = ref(false)
const myAppsSearchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 20,
  appName: '',
})

// 精选应用
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsLoading = ref(false)
const featuredAppsSearchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 20,
  appName: '',
  priority: 99,
})

// 我的应用分页
const myAppsPagination = computed(() => ({
  current: myAppsSearchParams.pageNum ?? 1,
  pageSize: myAppsSearchParams.pageSize ?? 20,
  total: myAppsTotal.value,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  onChange: (page: number, pageSize: number) => {
    myAppsSearchParams.pageNum = page
    myAppsSearchParams.pageSize = pageSize
    fetchMyApps()
  },
}))

const myAppsTotal = ref(0)

// 精选应用分页
const featuredAppsPagination = computed(() => ({
  current: featuredAppsSearchParams.pageNum ?? 1,
  pageSize: featuredAppsSearchParams.pageSize ?? 20,
  total: featuredAppsTotal.value,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  onChange: (page: number, pageSize: number) => {
    featuredAppsSearchParams.pageNum = page
    featuredAppsSearchParams.pageSize = pageSize
    fetchFeaturedApps()
  },
}))

const featuredAppsTotal = ref(0)

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return ''
  const now = dayjs()
  const target = dayjs(time)
  const diffHours = now.diff(target, 'hour')
  const diffDays = now.diff(target, 'day')
  const diffWeeks = now.diff(target, 'week')

  if (diffHours < 24) {
    return `${diffHours}小时前`
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else if (diffWeeks < 4) {
    return `${diffWeeks}周前`
  } else {
    return target.format('YYYY-MM-DD')
  }
}

// 获取我的应用列表
const fetchMyApps = async () => {
  // 只有登录用户才能查看我的应用
  if (!loginUserStore.loginUser.id) {
    myApps.value = []
    return
  }

  myAppsLoading.value = true
  try {
    const res = await listMyAppByPage({
      ...myAppsSearchParams,
    })
    if (res.data.code === 200 && res.data.data) {
      myApps.value = res.data.data.records ?? []
      myAppsTotal.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取我的应用失败：' + res.data.description)
    }
  } catch (error) {
    message.error('获取我的应用失败')
  } finally {
    myAppsLoading.value = false
  }
}

// 获取精选应用列表
const fetchFeaturedApps = async () => {
  featuredAppsLoading.value = true
  try {
    const res = await listFeaturedAppByPage({
      ...featuredAppsSearchParams,
    })
    if (res.data.code === 200 && res.data.data) {
      featuredApps.value = res.data.data.records ?? []
      featuredAppsTotal.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取精选应用失败：' + res.data.description)
    }
  } catch (error) {
    message.error('获取精选应用失败')
  } finally {
    featuredAppsLoading.value = false
  }
}

// 创建应用
const handleCreateApp = async () => {
  if (!promptInput.value.trim()) {
    message.warning('请输入提示词')
    return
  }

  // 检查是否登录
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    router.push('/user/login?redirect=' + encodeURIComponent(router.currentRoute.value.fullPath))
    return
  }

  try {
    const res = await addApp({
      appName: promptInput.value.substring(0, 50) || '未命名应用',
      initPrompt: promptInput.value,
    })

    if (res.data.code === 200 && res.data.data) {
      // 后端返回的id可能是number，转换为string避免精度丢失
      const appId = String(res.data.data)
      message.success('应用创建成功')
      // 跳转到对话页面
      router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建应用失败：' + res.data.description)
    }
  } catch (error) {
    message.error('创建应用失败')
  }
}

// 快捷提示词
const handleQuickPrompt = (prompt: string) => {
  promptInput.value = prompt
  handleCreateApp()
}

// 上传（暂时不实现）
const handleUpload = () => {
  message.info('上传功能暂未实现')
}

// 优化（暂时不实现）
const handleOptimize = () => {
  message.info('优化功能暂未实现')
}

// 查看应用详情
const handleViewApp = (appId?: string | number) => {
  if (!appId) return
  // 转换为string避免精度丢失
  router.push(`/app/chat/${String(appId)}`)
}

// 页面加载时获取数据
onMounted(() => {
  fetchFeaturedApps()
  // 如果已登录，获取我的应用
  if (loginUserStore.loginUser.id) {
    fetchMyApps()
  } else {
    // 尝试获取用户信息
    loginUserStore.fetchLoginUser().then(() => {
      if (loginUserStore.loginUser.id) {
        fetchMyApps()
      }
    })
  }
})
</script>

<style scoped>
#homePage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.header-section {
  text-align: center;
  margin-bottom: 48px;
}

.main-title {
  font-size: 48px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
}

.cat-icon {
  font-size: 36px;
  margin-left: 12px;
}

.subtitle {
  font-size: 18px;
  color: #666;
  margin: 0;
}

.input-section {
  margin-bottom: 32px;
}

.prompt-input {
  border-radius: 8px;
}

.input-prefix {
  margin-right: 8px;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-prompts {
  display: flex;
  gap: 12px;
  margin-bottom: 48px;
  flex-wrap: wrap;
  justify-content: center;
}

.prompt-btn {
  border-radius: 20px;
}

.app-section {
  margin-bottom: 48px;
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 24px;
  color: #333;
}

.app-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.app-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  font-size: 48px;
  color: #ccc;
}

.app-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

.featured-tag {
  margin-left: auto;
}
</style>
