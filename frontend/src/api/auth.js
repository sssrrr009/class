import request from '../utils/request'

/**
 * 登录
 * @param {Object} data { username, password, role }
 */
export function login(data) {
  return request.post('/auth/login', data)
}
