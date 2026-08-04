import request from '../utils/request'

/** 当前用户信息 */
export function getUserInfo() {
  return request.get('/user/info')
}

/** 修改密码 */
export function changePassword(data) {
  return request.put('/user/password', data)
}

/** 修改个人资料 */
export function updateProfile(data) {
  return request.put('/user/profile', data)
}
