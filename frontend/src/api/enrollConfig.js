import request from '../utils/request'

/** 获取选课配置 */
export function getEnrollConfig() {
  return request.get('/enroll-config')
}
/** 更新选课配置 */
export function updateEnrollConfig(data) {
  return request.put('/enroll-config', data)
}
