import request from '../utils/request'
import { createApi } from './base'

/** 成绩管理 CRUD（管理员/教师） */
export const scoreApi = createApi('/score')

/** 学生本人成绩 */
export function getMyScores() {
  return request.get('/score/my')
}
