/* eslint-disable */
import request from '@/requests'

/** /app/add */
export async function addApp(body: API.AppAddRequest, options?: { [key: string]: any }) {
  return request<API.ApiResponseLong>('/app/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** /app/update/my */
export async function updateMyApp(body: API.AppUpdateRequest, options?: { [key: string]: any }) {
  return request<API.ApiResponseBoolean>('/app/update/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** /app/list/my/page/vo */
export async function listMyAppByPage(body: API.AppQueryRequest, options?: { [key: string]: any }) {
  return request<API.ApiResponsePageAppVO>('/app/list/my/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** /app/list/featured/page/vo */
export async function listFeaturedAppByPage(
  body: API.AppQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.ApiResponsePageAppVO>('/app/list/featured/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** /app/deploy */
export async function deployApp(body: API.AppDeployRequest, options?: { [key: string]: any }) {
  return request<API.ApiResponseString>('/app/deploy', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** /app/delete/my */
export async function deleteMyApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.ApiResponseBoolean>('/app/delete/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** /app/admin/update */
export async function updateAppByAdmin(
  body: API.AppAdminUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.ApiResponseBoolean>('/app/admin/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** /app/admin/list/page/vo */
export async function listAppByPageForAdmin(
  body: API.AppQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.ApiResponsePageAppVO>('/app/admin/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** /app/admin/delete */
export async function deleteApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.ApiResponseBoolean>('/app/admin/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** /app/get/vo */
export async function getAppVOById(
  params: API.getAppVOByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.ApiResponseAppVO>('/app/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** /app/admin/get */
export async function getAppById(params: API.getAppByIdParams, options?: { [key: string]: any }) {
  return request<API.ApiResponseAppVO>('/app/admin/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** /app/chat/gen/code - SSE流式接口 */
export function chatGenCode(
  params: API.chatGenCodeParams,
  onMessage?: (data: string) => void,
  onError?: (error: Event) => void,
  onComplete?: () => void,
): EventSource {
  const baseURL = 'http://localhost:9000/api'
  // 确保appId转换为string避免精度丢失
  const appIdStr = String(params.appId || '')
  // const url = `${baseURL}/app/chat/gen/code?appId=${appIdStr}&message=${encodeURIComponent(params.message)}`
  const url = `${baseURL}/app/chat/gen/code?appId=${appIdStr}&message=${encodeURIComponent(params.message || '')}`
  // EventSource 默认会发送 cookies（同源情况下）
  const eventSource = new EventSource(url)
  // todo
  console.log('SSE URL:', url) // 调试日志
  eventSource.onmessage = (event) => {
    console.log('SSE Message:', event.data)
    if (onMessage) {
      onMessage(event.data)
    }
  }

  eventSource.onerror = (error) => {
    if (onError) {
      onError(error)
    }
    eventSource.close()
    if (onComplete) {
      onComplete()
    }
  }

  // 监听完成事件
  eventSource.addEventListener('done', () => {
    eventSource.close()
    if (onComplete) {
      onComplete()
    }
  })

  return eventSource
}
