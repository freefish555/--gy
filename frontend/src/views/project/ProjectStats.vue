<template>
  <div class="page-container">
    <!-- 顶部年份筛选 -->
    <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px">
      <span style="font-size:14px;color:#606266">年份筛选：</span>
      <el-select v-model="typeYear" placeholder="全部年份" clearable size="small"
        @change="loadAll" style="width:120px">
        <el-option v-for="y in yearOptions" :key="y" :label="y+'年'" :value="y" />
      </el-select>
    </div>

    <!-- 汇总卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8" v-for="card in summaryCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon :size="24" color="#fff"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never" header="按项目状态分布">
          <div ref="statusChartRef" style="height:260px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="按项目类型分布">
          <div ref="typeChartRef" style="height:260px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never" header="按所属行业分布">
          <div ref="industryChartRef" style="height:260px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="合同金额月度趋势">
          <div ref="amountChartRef" style="height:260px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增统计表格行 -->
    <el-row :gutter="16" style="margin-top:16px">
      <!-- 编写人员系统数量统计 -->
      <el-col :span="12">
        <el-card shadow="never" header="编写人员系统数量统计">
          <el-table :data="writerStats" size="small" border style="width:100%" max-height="320">
            <el-table-column type="index" label="序号" width="55" align="center" />
            <el-table-column prop="writerName" label="编写人员" min-width="100" />
            <el-table-column prop="sysTotal" label="系统总数" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="primary">{{ row.sysTotal || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sysL2" label="2级系统" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="warning">{{ row.sysL2 || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sysL3" label="3级系统" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="danger">{{ row.sysL3 || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="avgQualityScore" label="质量审核平均得分" min-width="130" align="center">
              <template #default="{ row }">
                <span v-if="row.avgQualityScore !== null && row.avgQualityScore !== undefined">
                  <el-tag size="small" :type="row.avgQualityScore >= 90 ? 'success' : row.avgQualityScore >= 60 ? 'warning' : 'danger'">
                    {{ row.avgQualityScore }}
                  </el-tag>
                </span>
                <span v-else style="color:#C0C4CC">-</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="writerStats.length === 0" style="text-align:center;color:#909399;padding:20px;font-size:13px">
            暂无数据（请先在被测系统中配置编写人）
          </div>
        </el-card>
      </el-col>

      <!-- 项目经理统计 -->
      <el-col :span="12">
        <el-card shadow="never" header="项目经理负责情况统计">
          <el-table :data="managerDetailStats" size="small" border style="width:100%" max-height="320">
            <el-table-column type="index" label="序号" width="55" align="center" />
            <el-table-column prop="managerName" label="项目经理" min-width="100" />
            <el-table-column prop="projectCnt" label="项目数" width="75" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="primary">{{ row.projectCnt || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sysTotal" label="系统总数" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small">{{ row.sysTotal || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sysL2" label="2级系统" width="75" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="warning">{{ row.sysL2 || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sysL3" label="3级系统" width="75" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="danger">{{ row.sysL3 || 0 }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="managerDetailStats.length === 0" style="text-align:center;color:#909399;padding:20px;font-size:13px">
            暂无数据
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { projectApi } from '@/api/project'
import { Folder, Clock, Check, Money, Grid, DataAnalysis } from '@element-plus/icons-vue'

const statusChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
const industryChartRef = ref<HTMLElement>()
const amountChartRef = ref<HTMLElement>()
let charts: echarts.ECharts[] = []

const typeYear = ref('')
const yearOptions = ref<string[]>([])

const writerStats = ref<any[]>([])
const managerDetailStats = ref<any[]>([])

const summaryCards = ref([
  { label: '项目总数', value: 0, icon: 'Folder', color: '#409EFF' },
  { label: '进行中', value: 0, icon: 'Clock', color: '#E6A23C' },
  { label: '已完成', value: 0, icon: 'Check', color: '#67C23A' },
  { label: '本年合同额(万元)', value: '0', icon: 'Money', color: '#F56C6C' },
  { label: '2级系统数量', value: 0, icon: 'Grid', color: '#909399' },
  { label: '3级系统数量', value: 0, icon: 'DataAnalysis', color: '#6E4AC6' },
])

onMounted(async () => {
  const now = new Date().getFullYear()
  for (let y = now; y >= now - 5; y--) yearOptions.value.push(String(y))
  typeYear.value = String(now)
  await loadAll()
})

async function loadAll() {
  await Promise.all([loadStats(), loadTableStats()])
}

async function loadStats() {
  try {
    const res: any = await projectApi.stats({ year: typeYear.value })
    const d = res.data
    summaryCards.value[0].value = d.total || 0
    summaryCards.value[1].value = d.inProgress || 0
    summaryCards.value[2].value = d.completed || 0
    summaryCards.value[3].value = d.totalAmountWan || '0'
    summaryCards.value[4].value = d.sysCountL2 || 0
    summaryCards.value[5].value = d.sysCountL3 || 0

    renderStatusChart(d.statusDist || [])
    renderTypeChart(d.typeDist || [])
    renderIndustryChart(d.industryDist || [])
    renderAmountChart(d.monthlyAmount || [])
  } catch (e) {
    renderStatusChart([
      { name: '待启动', value: 5 }, { name: '进行中', value: 12 },
      { name: '测评完成', value: 8 }, { name: '报告已出', value: 6 }, { name: '已归档', value: 20 }
    ])
    renderTypeChart([
      { name: '等保测评', value: 30 }, { name: '商密测评', value: 10 },
      { name: '专项测评', value: 5 }, { name: '其他', value: 6 }
    ])
    renderIndustryChart([
      { name: '政府', value: 15 }, { name: '电力', value: 12 }, { name: '金融', value: 8 },
      { name: '教育', value: 6 }, { name: '医疗', value: 5 }, { name: '其他', value: 5 }
    ])
    renderAmountChart([
      { month: '1月', amount: 12 }, { month: '2月', amount: 8 }, { month: '3月', amount: 15 },
      { month: '4月', amount: 20 }, { month: '5月', amount: 18 }, { month: '6月', amount: 25 }
    ])
  }
}

async function loadTableStats() {
  try {
    const [writerRes, mgrRes]: any[] = await Promise.all([
      projectApi.statsByWriter(typeYear.value || undefined),
      projectApi.statsByManagerDetail(typeYear.value || undefined),
    ])
    writerStats.value = writerRes.data || []
    managerDetailStats.value = mgrRes.data || []
  } catch (e) {
    writerStats.value = []
    managerDetailStats.value = []
  }
}

function initChart(ref: HTMLElement): echarts.ECharts {
  const c = echarts.init(ref)
  charts.push(c)
  return c
}

function renderStatusChart(data: any[]) {
  if (!statusChartRef.value) return
  const c = echarts.getInstanceByDom(statusChartRef.value) || initChart(statusChartRef.value)
  c.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['40%', '70%'],
      data: data.map(d => ({ name: d.name, value: d.value })),
      emphasis: { itemStyle: { shadowBlur: 10 } }
    }]
  })
}

function renderTypeChart(data: any[]) {
  if (!typeChartRef.value) return
  const c = echarts.getInstanceByDom(typeChartRef.value) || initChart(typeChartRef.value)
  c.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: '60%',
      data: data.map(d => ({ name: d.name, value: d.value }))
    }]
  })
}

function renderIndustryChart(data: any[]) {
  if (!industryChartRef.value) return
  const c = echarts.getInstanceByDom(industryChartRef.value) || initChart(industryChartRef.value)
  c.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: data.map(d => d.value), itemStyle: { color: '#409EFF' } }]
  })
}

function renderAmountChart(data: any[]) {
  if (!amountChartRef.value) return
  const c = echarts.getInstanceByDom(amountChartRef.value) || initChart(amountChartRef.value)
  c.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.month) },
    yAxis: { type: 'value', name: '万元' },
    series: [{
      type: 'line', smooth: true, data: data.map(d => d.amount),
      areaStyle: { opacity: 0.2 }, itemStyle: { color: '#67C23A' }
    }]
  })
}

onUnmounted(() => { charts.forEach(c => c.dispose()) })
</script>

<style scoped>
.page-container {}
.stats-row {}
.stat-card {}
.stat-content { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-value { font-size: 24px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.chart-filter { display: flex; justify-content: flex-end; margin-bottom: 8px; }
</style>
