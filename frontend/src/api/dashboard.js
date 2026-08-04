import request from '../utils/request'

/** 学生下一节课 */
export function getNextClass() {
  return request.get('/dashboard/next-class')
}
