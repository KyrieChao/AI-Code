/* eslint-disable */
import request from '@/requests'

/** 此处后端没有提供注释 GET /health/ */
export async function health(options?: { [key: string]: any }) {
  return request<API.ApiResponseString>('/health/', {
    method: 'GET',
    ...(options || {}),
  })
}
