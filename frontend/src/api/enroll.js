import request from '../utils/request'

/** 可选课程类别（专业计划内） */
export function getEnrollCategories() {
  return request.get('/enroll/categories')
}
/** 某类别下的可选课程 */
export function getCandidates(categoryId) {
  return request.get('/enroll/candidates', { params: { categoryId } })
}
/** 我的选课 */
export function getMyCourses() {
  return request.get('/enroll/my')
}
/** 选课 */
export function enroll(courseId) {
  return request.post(`/enroll/${courseId}`)
}
/** 退课 */
export function drop(courseId) {
  return request.delete(`/enroll/${courseId}`)
}
