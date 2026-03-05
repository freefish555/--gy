<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="title">登录日志</span></template>

      <el-form :model="queryForm" inline class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="登录状态">
          <el-select v-model="queryForm.loginStatus" placeholder="全部" clearable style="width:100px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间" end-placeholder="结束时间" style="width:340px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column label="用户名" prop="username" width="120" />
        <el-table-column label="真实姓名" prop="realName" width="100" />
        <el-table-column label="登录IP" prop="loginIp" width="140" />
        <el-table-column label="登录时间" prop="loginAt" width="170" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.loginStatus ? 'success' : 'danger'" size="small">
              {{ row.loginStatus ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="失败原因" prop="failReason" min-width="200" show-overflow-tooltip />
        <el-table-column label="退出时间" prop="logoutAt" width="170" />
        <el-table-column label="会话时长" width="100" align="center">
          <template #default="{ row }">
            {{ row.sessionDuration ? Math.floor(row.sessionDuration / 60) + '分钟' : '--' }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:12px; justify-content:flex-end; display:flex"
        @change="loadData"
      />
    </el-card>
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
const queryForm = reactive({ pageNum: 1, pageSize: 20, username: '', loginStatus: undefined as any, timeFrom: '', timeTo: '' })

watch(dateRange, (val) => {
  queryForm.timeFrom = val?.[0] || ''
  queryForm.timeTo = val?.[1] || ''
})

onMounted(loadData)

async function loadData() {
  loading.value = true
  try { const r: any = await logApi.loginLogs(queryForm); tableData.value = r.data?.records || []; total.value = r.data?.total || 0 }
  finally { loading.value = false }
}

function resetQuery() {
  Object.assign(queryForm, { pageNum: 1, username: '', loginStatus: undefined, timeFrom: '', timeTo: '' })
  dateRange.value = []
  loadData()
}
</script>

<style scoped>
.page-container {}
.title { font-size: 16px; font-weight: 600; }
.search-form { margin-bottom: 8px; }
</style>
