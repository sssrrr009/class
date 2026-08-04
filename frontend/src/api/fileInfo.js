import request from '../utils/request'

/** 分页查询文件 */
export function getFiles(params) {
  return request.get('/file/page', { params })
}
/** 上传文件 */
export function uploadFile(data) {
  return request.post('/file', data, { headers: { 'Content-Type': 'multipart/form-data' } })
}
/** 修改文件信息 */
export function updateFile(data) {
  return request.put('/file', data)
}
/** 删除文件 */
export function deleteFile(id) {
  return request.delete(`/file/${id}`)
}
/** 文件类型占比统计 */
export function getFileStat() {
  return request.get('/file/stat')
}
/** 下载文件 */
export function downloadFile(id) {
  return request.get(`/file/download/${id}`, { responseType: 'blob' })
}
