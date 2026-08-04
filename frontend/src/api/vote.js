import request from '../utils/request'

/** 分页查询投票活动 */
export function getVotes(params) {
  return request.get('/vote/page', { params })
}
/** 投票活动详情 */
export function getVoteDetail(id) {
  return request.get(`/vote/${id}`)
}
/** 创建投票活动 */
export function createVote(data) {
  return request.post('/vote', data)
}
/** 修改投票活动 */
export function updateVote(data) {
  return request.put('/vote', data)
}
/** 删除投票活动 */
export function deleteVote(id) {
  return request.delete(`/vote/${id}`)
}
/** 学生投票 */
export function castVote(data) {
  return request.post('/vote/cast', data)
}
/** 我的投票 */
export function getMyVotes() {
  return request.get('/vote/my')
}
/** 删除我的投票记录 */
export function deleteMyVote(id) {
  return request.delete(`/vote/my/${id}`)
}
/** 投票结果（饼图） */
export function getVoteResult(voteId) {
  return request.get(`/vote/result/${voteId}`)
}
/** 所有投票统计（管理员/教师） */
export function getVoteDetailStat() {
  return request.get('/vote/detail')
}
