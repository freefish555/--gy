<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="title">操作日志</span></template>

      <el-form :model="queryForm" inline class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable style="width:130px" />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-input v-model="queryForm.module" placeholder="请输入模块" clearable style="width:120px" />
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="queryForm.opResult" placeholder="全部" clearable style="width:90px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始" end-placeholder="结束" style="width:320px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column label="用户名" prop="username" width="110" />
        <el-table-column label="操作模块" prop="module" width="110" />
        <el-table-column label="操作类型" prop="actionType" width="100" />
        <el-table-column label="操作描述" prop="actionDesc" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作IP" prop="opIp" width="130" />
        <el-table-column label="操作时间" prop="opAt" width="170" />
        <el-table-column label="结果" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.opResult ? 'success' : 'danger'" size="small">
              {{ row.opResult ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求参数" width="80" align="center">
          <template #default="{ row }">
            <el-button v-if="row.requestParams" type="primary" link size="small" @click="showParams(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
        :total="total" :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next"
        style="margin-top:12px; justify-content:flex-end; display:flex" @change="loadData" />
    </el-card>

    <el-dialog v-model="paramsDialog" title="请求参数" width="600px">
      <el-input v-model="paramsContent" type="textarea" :rows="15" readonly />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { logApi } from '@/api/system'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])
const paramsDialog = ref(false)
const paramsContent = ref('')
const queryForm = reactive({ pageNum: 1, pageSize: 20, username: '', module: '', opResult: undefined as any, timeFrom: '', timeTo: '' })

watch(dateRange, (val) => {
  queryForm.timeFrom = val?.[0] || ''
  queryForm.timeTo = val?.[1] || ''
})

onMounted(loadData)

async function loadData() {
  loading.value = true
  try { const r: any = await logApi.operationLogs(queryForm); tableData.value = r.data?.records || []; total.value = r.data?.total || 0 }
  finally { loading.value = false }
}

function resetQuery() {
  Object.assign(queryForm, { pageNum: 1, username: '', module: '', opResult: undefined, timeFrom: '', timeTo: '' })
  dateRange.value = []
  loadData()
}

function showParams(row: any) {
  try { paramsContent.value = JSON.stringify(JSON.parse(row.requestParams), null, 2) }
  catch { paramsContent.value = row.requestParams }
  paramsDialog.value = true
}
</script>

<style scoped>
.page-container {}
.title { font-size: 16px; font-weight: 600; }
.search-form { margin-bottom: 8px; }
</style>
