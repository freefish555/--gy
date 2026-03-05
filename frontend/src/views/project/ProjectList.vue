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
        <el-form-item label="项目状态">
          <el-select v-model="queryForm.projectStatus" placeholder="全部" clearable style="width:130px" multiple>
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
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
          <el-button
            :disabled="selectedIds.length === 0"
            @click="showBatchDialog = true"
            v-if="authStore.hasPermission('project:update:all')"
          >
            批量操作 <span v-if="selectedIds.length > 0">({{ selectedIds.length }})</span>
          </el-button>
          <el-button
            :icon="Download"
            v-if="authStore.hasPermission('project:export')"
            @click="handleExport"
          >导出Excel</el-button>
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
            <!-- 项目状态 -->
            <template v-if="col.prop === 'projectStatus'">
              <el-tag :type="getStatusType(row.projectStatus)" size="small">
                {{ getStatusLabel(row.projectStatus) }}
              </el-tag>
            </template>
            <!-- 纸质归档 -->
            <template v-else-if="col.prop === 'paperArchived'">
              <el-tag :type="row.paperArchived ? 'success' : 'info'" size="small">
                {{ row.paperArchived ? '已归档' : '未归档' }}
              </el-tag>
            </template>
            <!-- 系统等级聚合 -->
            <template v-else-if="col.prop === 'sysLevels'">
              <el-tag v-for="(sys, i) in (row.systems || [])" :key="i" size="small"
                style="margin-right:2px">{{ sys.sysLevel }}级</el-tag>
            </template>
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
              v-if="authStore.hasPermission('project:delete') && row.projectStatus !== 4"
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
    <el-drawer v-model="showColumnConfig" title="自定义显示列" size="320px">
      <div class="column-config">
        <div v-for="col in allColumns" :key="col.prop" class="column-item">
          <el-checkbox v-model="col.visible">{{ col.label }}</el-checkbox>
        </div>
      </div>
      <template #footer>
        <el-button @click="resetColumns">恢复默认</el-button>
        <el-button type="primary" @click="saveColumnConfig">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download, Grid, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import request from '@/utils/request'

const authStore = useAuthStore()

// 状态选项
const statusOptions = [
  { value: 0, label: '待启动' },
  { value: 1, label: '已分配' },
  { value: 2, label: '进行中' },
  { value: 3, label: '已完成' },
  { value: 4, label: '电子归档' }
]

function getStatusLabel(status: number) {
  return statusOptions.find(s => s.value === status)?.label || '-'
}

function getStatusType(status: number): '' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<number, any> = { 0: 'info', 1: '', 2: 'warning', 3: 'success', 4: 'success' }
  return map[status] ?? 'info'
}

// 列配置
const allColumns = reactive([
  { prop: 'projectNo',      label: '项目编号',   width: 150,  visible: true },
  { prop: 'projectName',    label: '项目名称',   minWidth: 200, visible: true },
  { prop: 'customerName',   label: '客户名称',   minWidth: 160, visible: true },
  { prop: 'systemNameMerged', label: '被测系统', minWidth: 160, visible: true },
  { prop: 'sysLevels',      label: '系统等级',   width: 120,  visible: true },
  { prop: 'projectTypeName',label: '项目类型',   width: 90,   visible: true },
  { prop: 'projectManagerName', label: '项目经理', width: 100, visible: true },
  { prop: 'projectStatus',  label: '项目状态',   width: 100,  visible: true },
  { prop: 'paperArchived',  label: '纸质归档',   width: 90,   visible: false },
  { prop: 'contractDate',   label: '合同日期',   width: 110,  visible: false },
  { prop: 'yearBelong',     label: '所属年份',   width: 90,   visible: true },
  { prop: 'createdAt',      label: '创建时间',   width: 160,  visible: false }
])

const visibleColumns = computed(() => allColumns.filter(c => c.visible))
const showColumnConfig = ref(false)
const showAdvanced = ref(false)

function resetColumns() {
  allColumns.forEach(c => {
    c.visible = ['projectNo', 'projectName', 'customerName', 'systemNameMerged',
                  'sysLevels', 'projectTypeName', 'projectManagerName',
                  'projectStatus', 'yearBelong'].includes(c.prop)
  })
}

function saveColumnConfig() {
  showColumnConfig.value = false
  ElMessage.success('列配置已保存')
}

// 查询
const queryForm = reactive({
  pageNum: 1, pageSize: 20,
  projectNo: '', projectName: '', customerName: '',
  projectStatus: undefined as any, yearBelong: '',
  recordNo: '', projectManagerId: undefined as any,
  projectTypeId: undefined as any, industryId: undefined as any
})
const dateRange = ref<string[]>([])
const showBatchDialog = ref(false)
const tableRef = ref()
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
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
      projectStatus: queryForm.projectStatus?.join(','),
      contractDateFrom: dateRange.value?.[0],
      contractDateTo: dateRange.value?.[1]
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
      request.get('/dict/items?code=PROJECT_TYPE'),
      request.get('/dict/items?code=INDUSTRY')
    ])
    staffOptions.value = staffRes.data
    projectTypeOptions.value = typeRes.data
    industryOptions.value = industryRes.data
  } catch {}
}

function handleSearch() {
  queryForm.pageNum = 1
  loadList()
}

function handleReset() {
  Object.assign(queryForm, {
    pageNum: 1, projectNo: '', projectName: '', customerName: '',
    projectStatus: undefined, yearBelong: '', recordNo: '',
    projectManagerId: undefined, projectTypeId: undefined, industryId: undefined
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

async function handleExport() {
  ElMessage.info('正在导出，请稍候...')
  // TODO: 下载Excel
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
.toolbar-left, .toolbar-right { display: flex; gap: 8px; }
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.column-config { padding: 8px 0; }
.column-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
</style>
