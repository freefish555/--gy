<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline class="search-form" label-width="80px">
        <el-form-item label="项目编号">
          <el-input v-model="queryForm.projectNo" placeholder="请输入项目编号" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model="queryForm.projectName" placeholder="请输入项目名称" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="queryForm.customerName" placeholder="请输入客户名称" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="所属年份">
          <el-date-picker v-model="queryForm.yearBelong" type="year" value-format="YYYY"
            placeholder="选择年份" style="width:120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button text type="primary" @click="showAdvanced = !showAdvanced">
            {{ showAdvanced ? '收起' : '高级搜索' }}
            <el-icon><ArrowDown v-if="!showAdvanced" /><ArrowUp v-else /></el-icon>
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 高级搜索（折叠） -->
      <el-collapse-transition>
        <div v-show="showAdvanced">
          <el-divider style="margin:8px 0" />
          <el-form :model="queryForm" inline label-width="90px">
            <el-form-item label="备案编号">
              <el-input v-model="queryForm.recordNo" clearable style="width:150px" />
            </el-form-item>
            <el-form-item label="项目经理">
              <el-select v-model="queryForm.projectManagerId" clearable filterable placeholder="请选择" style="width:150px">
                <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="项目组成员">
              <el-input v-model="queryForm.memberName" placeholder="输入成员姓名" clearable style="width:150px" />
            </el-form-item>
            <el-form-item label="项目负责人">
              <el-input v-model="queryForm.projectLeaderName" placeholder="输入负责人姓名" clearable style="width:150px" />
            </el-form-item>
            <el-form-item label="项目类型">
              <el-select v-model="queryForm.projectTypeId" clearable placeholder="全部" style="width:130px">
                <el-option v-for="d in projectTypeOptions" :key="d.id" :label="d.itemLabel" :value="d.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="所属行业">
              <el-select v-model="queryForm.industryId" clearable placeholder="全部" style="width:130px">
                <el-option v-for="d in industryOptions" :key="d.id" :label="d.itemLabel" :value="d.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="签订日期">
              <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
                start-placeholder="开始日期" end-placeholder="结束日期" style="width:220px" />
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 工具栏 + 表格 -->
    <el-card shadow="never" style="margin-top:12px">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button
            type="primary"
            :icon="Plus"
            v-if="authStore.hasPermission('project:create')"
            @click="$router.push('/project/create')"
          >新增项目</el-button>

          <!-- 导入按钮 -->
          <el-dropdown v-if="authStore.hasPermission('project:create')" @command="handleImportCommand">
            <el-button :icon="Upload">
              导入<el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="download">下载导入模板</el-dropdown-item>
                <el-dropdown-item command="import">上传导入文件</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 导出按钮 -->
          <el-dropdown v-if="authStore.hasPermission('project:export')" @command="handleExportCommand">
            <el-button :icon="Download">
              导出<el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="query">导出当前查询结果</el-dropdown-item>
                <el-dropdown-item command="all">导出全部项目</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-button
            :disabled="selectedIds.length === 0"
            @click="showBatchDialog = true"
            v-if="authStore.hasPermission('project:update:all')"
          >
            批量操作 <span v-if="selectedIds.length > 0">({{ selectedIds.length }})</span>
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-tooltip content="自定义列">
            <el-button :icon="Grid" circle @click="showColumnConfig = true" />
          </el-tooltip>
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="loadList" />
          </el-tooltip>
        </div>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        stripe
        row-key="id"
        @selection-change="handleSelectionChange"
        style="width:100%"
      >
        <el-table-column type="selection" width="40" fixed="left" />
        <el-table-column label="序号" type="index" width="55" fixed="left" />

        <el-table-column
          v-for="col in visibleColumns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :show-overflow-tooltip="true"
        >
          <template #default="{ row }">
            <!-- 被测系统：显示系统名称合并 -->
            <template v-if="col.prop === 'systemNameMerged'">
              <el-tooltip :content="row.systemNameMerged" placement="top" :disabled="!row.systemNameMerged">
                <span>{{ row.systemNameMerged || '-' }}</span>
              </el-tooltip>
            </template>
            <!-- 系统等级：聚合标签 -->
            <template v-else-if="col.prop === 'sysLevels'">
              <span v-if="row.sysCountL2 > 0">
                <el-tag type="info" size="small" style="margin-right:4px">二级×{{ row.sysCountL2 }}</el-tag>
              </span>
              <span v-if="row.sysCountL3 > 0">
                <el-tag type="warning" size="small">三级×{{ row.sysCountL3 }}</el-tag>
              </span>
              <span v-if="!row.sysCountL2 && !row.sysCountL3">-</span>
            </template>
            <!-- 纸质归档 -->
            <template v-else-if="col.prop === 'paperArchived'">
              <el-tag :type="row.paperArchived ? 'success' : 'info'" size="small">
                {{ row.paperArchived ? '已归档' : '未归档' }}
              </el-tag>
            </template>
            <!-- 电子归档 -->
            <template v-else-if="col.prop === 'electronicArchived'">
              <el-tag :type="row.electronicArchived ? 'success' : 'info'" size="small">
                {{ row.electronicArchived ? '已归档' : '未归档' }}
              </el-tag>
            </template>
            <!-- 默认 -->
            <template v-else>{{ row[col.prop] || '-' }}</template>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small"
              @click="$router.push(`/project/detail/${row.id}`)">详情</el-button>
            <el-button
              text type="warning" size="small"
              v-if="canEdit(row)"
              @click="$router.push(`/project/edit/${row.id}`)">编辑</el-button>
            <el-popconfirm
              v-if="authStore.hasPermission('project:delete')"
              title="确认删除该项目吗？"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="loadList"
        />
      </div>
    </el-card>

    <!-- 自定义列配置 -->
    <el-drawer v-model="showColumnConfig" title="自定义显示列" size="340px">
      <div class="column-config">
        <el-text type="info" size="small" style="display:block;margin-bottom:12px">
          勾选需要在列表中显示的字段
        </el-text>
        <el-checkbox-group v-model="visibleProps">
          <div v-for="col in allColumns" :key="col.prop" class="column-item">
            <el-checkbox :label="col.prop">{{ col.label }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="resetColumns">恢复默认</el-button>
        <el-button type="primary" @click="saveColumnConfig">保存</el-button>
      </template>
    </el-drawer>

    <!-- 批量操作对话框 -->
    <el-dialog v-model="showBatchDialog" title="批量操作" width="400px">
      <el-form label-width="100px">
        <el-form-item label="操作类型">
          <el-select v-model="batchAction" style="width:100%">
            <el-option label="批量修改项目经理" value="manager" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="batchAction === 'manager'" label="项目经理">
          <el-select v-model="batchValue" filterable placeholder="请选择" style="width:100%">
            <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBatchUpdate">确认执行</el-button>
      </template>
    </el-dialog>

    <!-- 导入结果对话框 -->
    <el-dialog v-model="showImportResult" title="导入结果" width="600px">
      <el-descriptions :column="3" border size="small" style="margin-bottom:16px">
        <el-descriptions-item label="总计行数">{{ importResult.totalCount }}</el-descriptions-item>
        <el-descriptions-item label="成功导入">
          <el-text type="success">{{ importResult.successCount }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="跳过/失败">
          <el-text :type="importResult.skipCount > 0 ? 'danger' : 'info'">{{ importResult.skipCount }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="importResult.errors && importResult.errors.length > 0">
        <el-text type="warning" size="small">错误/跳过详情：</el-text>
        <div class="import-error-list">
          <div v-for="(err, idx) in importResult.errors" :key="idx" class="import-error-item">
            <el-icon color="#E6A23C"><WarningFilled /></el-icon>
            <span>{{ err }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showImportResult = false; loadList()">确定</el-button>
      </template>
    </el-dialog>

    <!-- 隐藏的文件上传input -->
    <input
      ref="fileInputRef"
      type="file"
      accept=".xlsx,.xls"
      style="display:none"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download, Upload, Grid, ArrowDown, ArrowUp, WarningFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import request from '@/utils/request'

const authStore = useAuthStore()

// ========== 所有可配置列定义 ==========
const DEFAULT_VISIBLE = ['projectNo', 'projectName', 'customerName', 'systemNameMerged',
  'sysLevels', 'projectTypeName', 'projectManagerName', 'yearBelong']

const allColumns = [
  { prop: 'projectNo',           label: '项目编号',      width: 150 },
  { prop: 'projectName',         label: '项目名称',      minWidth: 200 },
  { prop: 'customerName',        label: '客户名称',      minWidth: 160 },
  { prop: 'customerAddress',     label: '客户地址',      minWidth: 160 },
  { prop: 'customerContact',     label: '联系人',        width: 100 },
  { prop: 'customerPhone',       label: '联系电话',      width: 130 },
  { prop: 'systemNameMerged',    label: '被测系统',      minWidth: 200 },
  { prop: 'sysLevels',           label: '系统等级',      width: 140 },
  { prop: 'sysCountL2',          label: '2级系统数',     width: 90 },
  { prop: 'sysCountL3',          label: '3级系统数',     width: 90 },
  { prop: 'sysCount',            label: '系统数量',       width: 80 },
  { prop: 'projectGroupMembers', label: '项目组成员',    minWidth: 150 },
  { prop: 'projectTypeName',     label: '项目类型',      width: 90 },
  { prop: 'industryName',        label: '所属行业',      width: 100 },
  { prop: 'projectManagerName',  label: '项目经理',      width: 100 },
  { prop: 'projectLeaderName',   label: '项目负责人',    width: 110 },
  { prop: 'contractDate',        label: '合同日期',      width: 110 },
  { prop: 'contractAmount',      label: '合同金额(元)',  width: 120 },
  { prop: 'paperArchived',       label: '纸质归档',      width: 90 },
  { prop: 'electronicArchived',  label: '电子归档',      width: 90 },
  { prop: 'yearBelong',          label: '所属年份',      width: 90 },
  { prop: 'businessPerson',      label: '业务人员',      width: 100 },
  { prop: 'projectRegion',       label: '项目地区',      width: 100 },
  { prop: 'phasePrepare',        label: '测评准备阶段',  minWidth: 180 },
  { prop: 'phasePlan',           label: '方案编制阶段',  minWidth: 180 },
  { prop: 'phaseOnsite',         label: '现场测评阶段',  minWidth: 180 },
  { prop: 'phaseReport',         label: '报告编制阶段',  minWidth: 180 },
  { prop: 'taskAppointDate',     label: '任务预约日期',  width: 120 },
  { prop: 'reportMailDate',      label: '报告邮寄日期',  width: 120 },
  { prop: 'reportMailNo',        label: '报告邮寄单号',  width: 130 },
  { prop: 'createdAt',           label: '创建时间',      width: 160 },
]

const visibleProps = ref<string[]>([...DEFAULT_VISIBLE])
const visibleColumns = computed(() =>
  allColumns.filter(c => visibleProps.value.includes(c.prop))
)
const showColumnConfig = ref(false)

function resetColumns() {
  visibleProps.value = [...DEFAULT_VISIBLE]
}

function saveColumnConfig() {
  showColumnConfig.value = false
  ElMessage.success('列配置已保存')
}

// ========== 查询 ==========
const showAdvanced = ref(false)
const queryForm = reactive({
  pageNum: 1, pageSize: 20,
  projectNo: '', projectName: '', customerName: '',
  yearBelong: '', recordNo: '',
  projectManagerId: undefined as any,
  projectTypeId: undefined as any,
  industryId: undefined as any,
  memberName: '',
  projectLeaderName: '',
})
const dateRange = ref<string[]>([])
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const tableRef = ref()
const staffOptions = ref<any[]>([])
const projectTypeOptions = ref<any[]>([])
const industryOptions = ref<any[]>([])

onMounted(() => {
  loadList()
  loadOptions()
})

async function loadList() {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      contractDateFrom: dateRange.value?.[0],
      contractDateTo: dateRange.value?.[1],
    }
    const res: any = await request.get('/project/page', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    const [staffRes, typeRes, industryRes]: any[] = await Promise.all([
      request.get('/staff/list'),
      request.get('/system/dict/PROJECT_TYPE/items'),
      request.get('/system/dict/INDUSTRY/items'),
    ])
    staffOptions.value = staffRes.data || []
    projectTypeOptions.value = typeRes.data || []
    industryOptions.value = industryRes.data || []
  } catch {}
}

function handleSearch() {
  queryForm.pageNum = 1
  loadList()
}

function handleReset() {
  Object.assign(queryForm, {
    pageNum: 1, projectNo: '', projectName: '', customerName: '',
    yearBelong: '', recordNo: '',
    projectManagerId: undefined,
    projectTypeId: undefined,
    industryId: undefined,
    memberName: '',
    projectLeaderName: '',
  })
  dateRange.value = []
  loadList()
}

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}

function canEdit(row: any) {
  if (authStore.hasPermission('project:update:all')) return true
  if (authStore.hasPermission('project:update:own')) {
    return row.createdBy === authStore.userInfo?.userId
  }
  return false
}

async function handleDelete(id: number) {
  await request.delete(`/project/${id}`)
  ElMessage.success('删除成功')
  loadList()
}

// ========== 批量操作 ==========
const showBatchDialog = ref(false)
const batchAction = ref('manager')
const batchValue = ref<any>(null)

async function handleBatchUpdate() {
  if (!batchValue.value) { ElMessage.warning('请选择操作值'); return }
  try {
    await request.post('/project/batch', {
      ids: selectedIds.value,
      action: batchAction.value,
      value: batchValue.value,
    })
    ElMessage.success('批量操作成功')
    showBatchDialog.value = false
    loadList()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

// ========== 导出 ==========
async function handleExportCommand(cmd: string) {
  try {
    ElMessage.info('正在生成Excel，请稍候...')
    const params: any = {
      ...queryForm,
      contractDateFrom: dateRange.value?.[0],
      contractDateTo: dateRange.value?.[1],
      exportAll: cmd === 'all',
    }
    // 移除分页参数
    delete params.pageNum
    delete params.pageSize

    const res = await request.get('/project/export', {
      params,
      responseType: 'blob',
    })

    const blobData = (res as any)?.data || res
    const blob = blobData instanceof Blob ? blobData : new Blob([blobData], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const suffix = cmd === 'all' ? '全部' : '查询结果'
    a.download = `项目列表_${suffix}_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '')}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e: any) {
    ElMessage.error('导出失败: ' + (e.message || '未知错误'))
  }
}

// ========== 导入 ==========
const fileInputRef = ref<HTMLInputElement>()
const importLoading = ref(false)
const showImportResult = ref(false)
const importResult = ref<any>({ successCount: 0, skipCount: 0, totalCount: 0, errors: [] })

function handleImportCommand(cmd: string) {
  if (cmd === 'download') {
    downloadImportTemplate()
  } else if (cmd === 'import') {
    fileInputRef.value?.click()
  }
}

async function downloadImportTemplate() {
  try {
    const res = await request.get('/project/import/template', { responseType: 'blob' })
    const blobData2 = (res as any)?.data || res
    const blob2 = blobData2 instanceof Blob ? blobData2 : new Blob([blobData2], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = URL.createObjectURL(blob2)
    const a = document.createElement('a')
    a.href = url
    a.download = '项目批量导入模板.xlsx'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (e: any) {
    ElMessage.error('模板下载失败')
  }
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return
  const file = input.files[0]
  input.value = '' // 重置，允许重复选择同一文件

  const formData = new FormData()
  formData.append('file', file)
  importLoading.value = true
  try {
    ElMessage.info('正在导入，请稍候...')
    const res: any = await request.post('/project/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    importResult.value = res.data
    showImportResult.value = true
  } catch (e: any) {
    ElMessage.error('导入失败: ' + (e.message || '未知错误'))
  } finally {
    importLoading.value = false
  }
}
</script>

<style scoped>
.search-card { margin-bottom: 0; }
.search-form .el-form-item { margin-bottom: 12px; }
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.toolbar-left { display: flex; gap: 8px; align-items: center; }
.toolbar-right { display: flex; gap: 8px; }
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.column-config { padding: 4px 0; }
.column-item {
  padding: 6px 4px;
  border-bottom: 1px solid #f5f5f5;
}
.import-error-list {
  max-height: 220px;
  overflow-y: auto;
  margin-top: 8px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  padding: 8px;
  background: #fafafa;
}
.import-error-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 4px 0;
  font-size: 13px;
  color: #606266;
  border-bottom: 1px dashed #f0f0f0;
}
.import-error-item:last-child { border-bottom: none; }
</style>
