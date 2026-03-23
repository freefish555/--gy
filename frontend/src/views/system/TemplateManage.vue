<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">归档模板管理</span>
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="onUploadSuccess"
            :on-error="onUploadError"
            :show-file-list="false"
            accept=".doc,.docx,.xls,.xlsx"
          >
            <el-button type="primary" :icon="Upload">上传新模板</el-button>
          </el-upload>
        </div>
      </template>

      <el-table :data="templateList" border v-loading="loading">
        <el-table-column label="模板名称" prop="templateName" min-width="280" />
        <el-table-column label="文件类型" prop="fileType" width="100" align="center" />
        <el-table-column label="版本" prop="version" width="80" align="center" />
        <el-table-column label="文件大小" width="100" align="center">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="上传时间" prop="updatedAt" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Download" @click="download(row)">下载</el-button>
            <el-upload
              :action="`/api/archive/templates/${row.id}/replace`"
              :headers="uploadHeaders"
              :on-success="() => { ElMessage.success('替换成功'); loadData() }"
              :show-file-list="false"
              accept=".doc,.docx,.xls,.xlsx"
            >
              <el-button type="warning" link size="small" :icon="RefreshRight">替换</el-button>
            </el-upload>
            <el-button type="danger" link size="small" :icon="Delete" @click="deleteTemplate(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Download, Delete, RefreshRight } from '@element-plus/icons-vue'
import { archiveApi } from '@/api/system'

const loading = ref(false)
const templateList = ref<any[]>([])

const uploadUrl = '/api/archive/templates/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

onMounted(loadData)

async function loadData() {
  loading.value = true
  try { const r: any = await archiveApi.templateList(); templateList.value = r.data || [] }
  finally { loading.value = false }
}

function onUploadSuccess() { ElMessage.success('上传成功'); loadData() }
function onUploadError() { ElMessage.error('上传失败') }

function formatSize(bytes: number) {
  if (!bytes) return '--'
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / 1048576).toFixed(1) + 'MB'
}

async function download(row: any) {
  try {
    const res: any = await archiveApi.templateDownload(row.id)
    // res is AxiosResponse when responseType is 'blob'
    const blobData = res?.data || res
    const blob = blobData instanceof Blob ? blobData : new Blob([blobData])
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href = url; a.download = row.fileOriginalName || row.templateName
    a.click(); URL.revokeObjectURL(url)
  } catch (e: any) { ElMessage.error('下载失败') }
}

async function deleteTemplate(row: any) {
  await ElMessageBox.confirm(`确认删除模板【${row.templateName}】？`, '警告', { type: 'warning' })
  await archiveApi.templateDelete(row.id)
  ElMessage.success('删除成功'); loadData()
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
</style>
