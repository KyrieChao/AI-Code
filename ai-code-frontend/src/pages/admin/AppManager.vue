<template>
  <div id="appManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用ID">
        <a-input-number
          v-model:value="searchParams.id"
          placeholder="输入应用ID"
          style="width: 150px"
        />
      </a-form-item>
      <a-form-item label="应用名称">
        <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" />
      </a-form-item>
      <a-form-item label="用户ID">
        <a-input-number
          v-model:value="searchParams.userId"
          placeholder="输入用户ID"
          style="width: 150px"
        />
      </a-form-item>
      <a-form-item label="优先级">
        <a-input-number
          v-model:value="searchParams.priority"
          placeholder="输入优先级"
          style="width: 150px"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
        <a-button style="margin-left: 8px" @click="doReset">重置</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'cover'">
          <a-image
            v-if="record.cover"
            :src="record.cover"
            :width="60"
            :height="60"
            :preview="false"
          />
          <span v-else>无封面</span>
        </template>
        <template v-else-if="column.dataIndex === 'priority'">
          <a-tag v-if="record.priority === 99" color="gold">精选</a-tag>
          <span v-else>{{ record.priority || 0 }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'updateTime'">
          {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'user'">
          <span v-if="record.user">{{ record.user.userName || record.user.userAccount }}</span>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="doEdit(record.id)">编辑</a-button>
            <a-button type="link" danger @click="doDelete(record.id)">删除</a-button>
            <a-button
              :type="record.priority === 99 ? 'default' : 'primary'"
              @click="doToggleFeatured(record)"
            >
              {{ record.priority === 99 ? '取消精选' : '精选' }}
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { deleteApp, listAppByPageForAdmin, updateAppByAdmin } from '@/api/appController'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    width: 200,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 100,
  },
  {
    title: '用户',
    dataIndex: 'user',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 250,
  },
]

// 展示的数据
const data = ref<API.AppVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  const res = await listAppByPageForAdmin({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.description)
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 重置搜索
const doReset = () => {
  Object.assign(searchParams, {
    pageNum: 1,
    pageSize: 10,
    id: undefined,
    appName: undefined,
    userId: undefined,
    priority: undefined,
  })
  fetchData()
}

// 编辑
const doEdit = (id?: string | number) => {
  if (!id) return
  // 新开页面跳转到应用信息修改页，转换为string避免精度丢失
  const routeData = router.resolve(`/app/edit/${String(id)}`)
  window.open(routeData.href, '_blank')
}

// 删除数据
const doDelete = async (id?: string | number) => {
  if (!id) {
    return
  }
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个应用吗？此操作不可恢复。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      // 转换为string避免精度丢失
      const res = await deleteApp({ id: String(id) })
      if (res.data.code === 200) {
        message.success('删除成功')
        // 刷新数据
        await fetchData()
      } else {
        message.error('删除失败：' + res.data.description)
      }
    },
  })
}

// 切换精选
const doToggleFeatured = async (record: API.AppVO) => {
  if (!record.id) return

  const newPriority = record.priority === 99 ? 0 : 99

  // 转换为string避免精度丢失，但API可能接受number，使用as any绕过类型检查
  const res = await updateAppByAdmin({
    id: String(record.id),
    priority: newPriority,
  })

  if (res.data.code === 200) {
    message.success(newPriority === 99 ? '已设置为精选' : '已取消精选')
    // 刷新数据
    await fetchData()
  } else {
    message.error('操作失败：' + res.data.description)
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#appManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}
</style>
