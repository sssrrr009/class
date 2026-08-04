import request from '../utils/request'
import { createApi } from './base'
export const noticeApi = createApi('/notice')

/** 发布公告 */
export function publishNotice(data) {
  return request.post('/notice', data)
}
