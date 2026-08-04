import request from '../utils/request'

/** 教师提交课程修改申请 */
export function submitChange(data) {
  return request.post('/course-change', data)
}
/** 教师查询自己的申请 */
export function getMyChanges() {
  return request.get('/course-change/my')
}
/** 管理员分页查询申请 */
export function getChangePage(params) {
  return request.get('/course-change/page', { params })
}
/** 管理员审批 */
export function approveChange(id, pass, reason) {
  return request.put(`/course-change/${id}/approve`, null, { params: { pass, reason } })
}
/** 删除申请 */
export function deleteChange(id) {
  return request.delete(`/course-change/${id}`)
}
