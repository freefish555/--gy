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
  projectStatus?: string
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
  batchDelete: (ids: number[]) => request.delete('/project/batch', { data: { ids } }),
  batchUpdateStatus: (ids: number[], status: number) =>
    request.put('/project/batch/status', { ids, status }),
  export: (params: ProjectQuery) =>
    request.post('/project/export', params, { responseType: 'blob' }),
  stats: (params?: any) => request.get('/project/stats', { params }),
  statsByType: (year?: string) => request.get('/project/stats/type', { params: { year } }),
  statsByIndustry: (year?: string) => request.get('/project/stats/industry', { params: { year } }),
}
