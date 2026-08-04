import { createApi } from './base'
export const studentApi = createApi('/student')

/** Excel 批量导入学生 */
export function importStudents(file) {
  const form = new FormData()
  form.append('file', file)
  return import('../utils/request').then(({ default: request }) =>
    request.post('/student/import', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  )
}
