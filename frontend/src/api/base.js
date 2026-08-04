import request from '../utils/request'

/**
 * 通用 CRUD API 工厂
 */
export function createApi(basePath) {
  return {
    page(params) {
      return request.get(`${basePath}/page`, { params })
    },
    list(params) {
      return request.get(`${basePath}/list`, { params })
    },
    add(data) {
      return request.post(`${basePath}`, data)
    },
    update(data) {
      return request.put(`${basePath}`, data)
    },
    remove(id) {
      return request.delete(`${basePath}/${id}`)
    }
  }
}
