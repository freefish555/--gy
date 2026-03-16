import request from '@/utils/request'

export interface ProjectQuery {
  pageNum?: number
  pageSize?: number
  projectNo?: string
  projectName?: string
  recordNo?: string
  customerName?: string
  customerContact?: string
  projectManagerId?: number
  registeredEvaluatorId?: number
  projectLeaderId?: number
  projectTypeId?: number
  industryId?: number
  businessPerson?: string
  contractDateFrom?: string
  contractDateTo?: string
  yearBelong?: string
  sortField?: string
  sortOrder?: string
}

export const projectApi = {
  list: (params: ProjectQuery) => request.get('/project/page', { params }),
  detail: (id: number) => request.get(`/project/${id}`),
  create: (data: any) => request.post('/project', data),
  update: (id: number, data: any) => request.put(`/project/${id}`, data),
  delete: (id: number) => request.delete(`/project/${id}`),
  batchUpdate: (body: any) => request.post('/project/batch', body),

  /** 导出Excel */
  exportExcel: (params: ProjectQuery & { exportAll?: boolean }) =>
    request.get('/project/export', { params, responseType: 'blob' }),

  /** 下载导入模板 */
  downloadImportTemplate: () =>
    request.get('/project/import/template', { responseType: 'blob' }),

  /** 批量导入 */
  importProjects: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/project/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  // 统计
  stats: (params?: any) => request.get('/project/stats', { params }),
  statsByType: (year?: string) => request.get('/project/stats/type', { params: { year } }),
  statsByIndustry: (year?: string) => request.get('/project/stats/industry', { params: { year } }),
  statsByManager: (year?: string) => request.get('/project/stats/manager', { params: { year } }),
}
