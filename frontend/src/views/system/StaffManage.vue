<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">项目人员清单</span>
          <div style="display:flex;gap:8px">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-dropdown @command="handleImportCommand">
              <el-button :icon="Upload">
                导入<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="template">下载导入模板</el-dropdown-item>
                  <el-dropdown-item command="import">上传Excel导入</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="primary" :icon="Plus" @click="openDialog()">新增人员</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索 -->
      <el-form :model="queryForm" inline class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="queryForm.realName" placeholder="请输入姓名" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="queryForm.department" placeholder="请输入部门" clearable style="width:130px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:100px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column label="工号" prop="staffNo" width="100" />
        <el-table-column label="姓名" prop="realName" width="100" />
        <el-table-column label="部门" prop="department" width="120" />
        <el-table-column label="职位" prop="position" width="130" />
        <el-table-column label="证书编号" prop="certNo" width="160" />
        <el-table-column label="证书到期" prop="certExpire" width="120" align="center">
          <template #default="{ row }">
            <span :style="{ color: isExpiringSoon(row.certExpire) ? '#E6A23C' : '' }">
              {{ row.certExpire }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="电话" prop="phone" width="140" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'danger'" size="small">
              {{ row.status ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button :type="row.status ? 'warning' : 'success'" link size="small"
              @click="toggleStatus(row)">{{ row.status ? '禁用' : '启用' }}</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:12px; justify-content:flex-end; display:flex"
        @change="loadData"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑人员' : '新增人员'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工号" prop="staffNo">
              <el-input v-model="form.staffNo" placeholder="如: S001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门">
              <el-input v-model="form.department" placeholder="请输入部门" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位">
              <el-input v-model="form.position" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="证书编号">
              <el-input v-model="form.certNo" placeholder="请输入证书编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="证书到期">
              <el-date-picker v-model="form.certExpire" type="date" value-format="YYYY-MM-DD"
                placeholder="选择日期" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="职级">
              <el-select v-model="form.roleLevel" style="width:100%">
                <el-option label="初级" value="junior" />
                <el-option label="中级" value="middle" />
                <el-option label="高级" value="senior" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 导入结果 -->
    <el-dialog v-model="importResultVisible" title="导入结果" width="500px">
      <el-descriptions :column="3" border size="small" style="margin-bottom:12px">
        <el-descriptions-item label="总计">{{ importResult.totalCount }}</el-descriptions-item>
        <el-descriptions-item label="成功"><el-text type="success">{{ importResult.successCount }}</el-text></el-descriptions-item>
        <el-descriptions-item label="跳过"><el-text :type="importResult.skipCount > 0 ? 'danger' : 'info'">{{ importResult.skipCount }}</el-text></el-descriptions-item>
      </el-descriptions>
      <div v-if="importResult.errors && importResult.errors.length > 0" style="max-height:200px;overflow-y:auto">
        <div v-for="(err,i) in importResult.errors" :key="i" style="color:#E6A23C;font-size:13px;padding:4px 0">{{ err }}</div>
      </div>
      <template #footer><el-button type="primary" @click="importResultVisible=false">确定</el-button></template>
    </el-dialog>

    <!-- 隐藏文件输入 -->
    <input ref="fileInputRef" type="file" accept=".xlsx,.xls" style="display:none" @change="handleFileChange" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Download, Upload, ArrowDown } from '@element-plus/icons-vue'
import { staffApi } from '@/api/staff'
import dayjs from 'dayjs'

const loading = ref(false)
const saving = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()

const queryForm = reactive({ pageNum: 1, pageSize: 20, realName: '', department: '', status: undefined as any })
const form = reactive({ staffNo: '', realName: '', department: '', position: '', roleLevel: 'middle', certNo: '', certExpire: '', phone: '', email: '', status: 1 })

const rules = {
  staffNo: [{ required: true, message: '请输入工号' }],
  realName: [{ required: true, message: '请输入姓名' }],
}

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res: any = await staffApi.list(queryForm)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.assign(queryForm, { pageNum: 1, realName: '', department: '', status: undefined })
  loadData()
}

function isExpiringSoon(date: string): boolean {
  if (!date) return false
  return dayjs(date).diff(dayjs(), 'day') < 90
}

function openDialog(row?: any) {
  if (row) {
    editId.value = row.id
    Object.assign(form, row)
  } else {
    editId.value = null
    Object.assign(form, { staffNo: '', realName: '', department: '', position: '', roleLevel: 'middle', certNo: '', certExpire: '', phone: '', email: '', status: 1 })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    saving.value = true
    try {
      if (editId.value) {
        await staffApi.update(editId.value, form)
        ElMessage.success('修改成功')
      } else {
        await staffApi.create(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally { saving.value = false }
  })
}

async function toggleStatus(row: any) {
  const newStatus = row.status ? 0 : 1
  await staffApi.toggleStatus(row.id, newStatus)
  row.status = newStatus
  ElMessage.success(`${newStatus ? '启用' : '禁用'}成功`)
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确认删除人员【${row.realName}】？`, '警告', { type: 'warning' })
  await staffApi.delete(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleExport() {
  try {
    ElMessage.info('正在导出，请稍候...')
    const res = await staffApi.export(queryForm)
    const blobData = (res as any)?.data || res
    const blob = blobData instanceof Blob ? blobData : new Blob([blobData], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `人员清单_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '')}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e: any) {
    ElMessage.error('导出失败: ' + (e.message || '未知错误'))
  }
}

const fileInputRef = ref<HTMLInputElement>()
const importResultVisible = ref(false)
const importResult = ref<any>({})

function handleImportCommand(cmd: string) {
  if (cmd === 'template') downloadImportTemplate()
  else if (cmd === 'import') fileInputRef.value?.click()
}

async function downloadImportTemplate() {
  try {
    const res = await staffApi.downloadTemplate()
    const blobData = (res as any)?.data || res
    const blob = blobData instanceof Blob ? blobData : new Blob([blobData], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '人员导入模板.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e: any) {
    ElMessage.error('下载模板失败')
  }
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return
  const file = input.files[0]
  input.value = ''
  try {
    ElMessage.info('正在导入，请稍候...')
    const res: any = await staffApi.importStaff(file)
    importResult.value = res.data
    importResultVisible.value = true
    loadData()
  } catch (e: any) {
    ElMessage.error('导入失败: ' + (e.message || '未知错误'))
  }
}
</script>

<style scoped>
.page-container {}
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
.search-form { margin-bottom: 4px; }
</style>
