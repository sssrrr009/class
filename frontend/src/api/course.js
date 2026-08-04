import request from '../utils/request'
import { createApi } from './base'
export const courseApi = createApi('/course')

/** 教师查看自己授课的课程 */
export function getMyTeachingCourses() {
  return request.get('/course/my')
}
