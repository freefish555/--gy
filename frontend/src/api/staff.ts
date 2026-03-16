import request from '@/utils/request'

export const staffApi = {
  list: (params?: any) => request.get('/staff/page', { params }),
  all: () => request.get('/staff/list'),
  detail: (id: number) => request.get(`/staff/${id}`),
  create: (data: any) => request.post('/staff', data),
  update: (id: number, data: any) => request.put(`/staff/${id}`, data),
  delete: (id: number) => request.delete(`/staff/${id}`),
  toggleStatus: (id: number, status: number) =>
    request.put(`/staff/${id}/status`, { status }),
  toggle: (id: number) =>
    request.put(`/staff/${id}/toggle`),
  // 导出
  export: (params?: any) => request.get('/staff/export', { params, responseType: 'blob' }),
  // 下载导入模板
  downloadTemplate: () => request.get('/staff/import/template', { responseType: 'blob' }),
  // 导入
  importStaff: (file: File) => {
    const fd = new FormData()
    fd.append('file', file)
    return request.post('/staff/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
}

export const toolApi = {
  // 硬件测评设备
  deviceList: (params?: any) => request.get('/tool/device/list', { params }),
  deviceCreate: (data: any) => request.post('/tool/device', data),
  deviceUpdate: (id: number, data: any) => request.put(`/tool/device/${id}`, data),
  deviceDelete: (id: number) => request.delete(`/tool/device/${id}`),
  deviceAll: () => request.get('/tool/device/list'),

  // 渗透软件工具
  pentestList: (params?: any) => request.get('/tool/pentest/list', { params }),
  pentestCreate: (data: any) => request.post('/tool/pentest', data),
  pentestUpdate: (id: number, data: any) => request.put(`/tool/pentest/${id}`, data),
  pentestDelete: (id: number) => request.delete(`/tool/pentest/${id}`),
  pentestAll: () => request.get('/tool/pentest/list'),
}
