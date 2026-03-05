import request from '@/utils/request'

export const dictApi = {
  listTypes: (params?: any) => request.get('/system/dict', { params }),
  getItems: (dictCode: string) => request.get(`/system/dict/${dictCode}/items`),
  getAllItems: (dictCode: string) => request.get(`/system/dict/${dictCode}/items`),
  createType: (data: any) => request.post('/system/dict', data),
  updateType: (id: number, data: any) => request.put(`/system/dict/${id}`, data),
  deleteType: (id: number) => request.delete(`/system/dict/${id}`),
  createItem: (data: any) => request.post('/system/dict/item', data),
  updateItem: (id: number, data: any) => request.put(`/system/dict/item/${id}`, data),
  deleteItem: (id: number) => request.delete(`/system/dict/item/${id}`),
}

export const sysConfigApi = {
  list: () => request.get('/system/config'),
  get: (key: string) => request.get(`/system/config/${key}`),
  update: (key: string, value: string) => request.put(`/system/config`, { key, value }),
  batchSave: (configs: any[]) => request.post('/system/config/batch', { configs }),
  // SM4密钥管理
  getSm4Status: () => request.get('/system/config/sm4/status'),
  setSm4Key: (data: any) => request.post('/system/config/sm4/key', data),
}

export const userApi = {
  list: (params?: any) => request.get('/system/user/list', { params }),
  create: (data: any) => request.post('/system/user', data),
  update: (id: number, data: any) => request.put(`/system/user/${id}`, data),
  delete: (id: number) => request.delete(`/system/user/${id}`),
  resetPassword: (id: number, newPassword: string) =>
    request.put(`/system/user/${id}/password`, { newPassword }),
  toggleStatus: (id: number, status: number) =>
    request.put(`/system/user/${id}/status`, { status }),
}

export const archiveApi = {
  // 归档记录
  list: (params?: any) => request.get('/archive/history', { params }),
  // 生成归档文件
  generate: (projectId: number, templateIds: number[]) =>
    request.post('/archive/generate', { projectId, templateIds }),
  // 下载归档包
  download: (projectId: number) =>
    request.get(`/archive/download/${projectId}`, { responseType: 'blob' }),
  // 模板管理
  templateList: () => request.get('/archive/templates'),
  templateUpload: (formData: FormData) =>
    request.post('/archive/template/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  templateDelete: (id: number) => request.delete(`/archive/template/${id}`),
  templateDownload: (id: number) =>
    request.get(`/archive/template/${id}/download`, { responseType: 'blob' }),
}

export const logApi = {
  loginLogs: (params?: any) => request.get('/log/login/page', { params }),
  operationLogs: (params?: any) => request.get('/log/operation/page', { params }),
  serverConfig: () => request.get('/log/server/config'),
  saveServerConfig: (data: any) => request.post('/log/server/config', data),
}
