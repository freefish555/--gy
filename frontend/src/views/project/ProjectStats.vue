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

    <!-- 图表行1：项目类型分布 + 所属行业分布 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never" header="项目类型分布">
          <div ref="typeChartRef" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="所属行业分布">
          <div ref="industryChartRef" style="height:280px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行2：项目地区分布 + 合同金额趋势 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never" header="项目地区分布">
          <div ref="regionChartRef" style="height:280px"></div>
          <div v-if="regionEmpty" style="text-align:center;color:#909399;padding:12px;font-size:13px">
            暂无数据（请先在项目中填写地区信息）
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <span>合同金额趋势</span>
              <span style="font-size:13px;color:#606266">
                年度总额：
                <span style="font-size:16px;font-weight:700;color:#409EFF">
                  {{ amountTotalWan }}
                </span>
                <span style="font-size:12px;color:#909399"> 万元</span>
              </span>
            </div>
          </template>
          <div ref="amountChartRef" style="height:240px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计表格行：编写人员统计 + 项目经理统计 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never" header="编写人员系统数量统计">
          <el-table :data="writerStats" size="small" border style="width:100%" max-height="320">
            <el-table-column type="index" label="序号" width="55" align="center" />
            <el-table-column prop="writerName" label="编写人员" min-width="90" />
            <el-table-column prop="sysTotal" label="系统总数" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="primary">{{ row.sysTotal || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sysL2" label="2级" width="65" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="warning">{{ row.sysL2 || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sysL3" label="3级" width="65" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="danger">{{ row.sysL3 || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="avgQualityScore" label="质量审核均分" min-width="100" align="center">
              <template #default="{ row }">
                <span v-if="row.avgQualityScore !== null && row.avgQualityScore !== undefined">
                  <el-tag size="small"
                    :type="row.avgQualityScore >= 90 ? 'success' : row.avgQualityScore >= 60 ? 'warning' : 'danger'">
                    {{ row.avgQualityScore }}
                  </el-tag>
                </span>
                <span v-else style="color:#C0C4CC">-</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="writerStats.length === 0"
            style="text-align:center;color:#909399;padding:20px;font-size:13px">
            暂无数据（请先在被测系统中配置编写人）
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" header="项目经理负责情况统计">
          <el-table :data="managerDetailStats" size="small" border style="width:100%" max-height="320">
            <el-table-column type="index" label="序号" width="55" align="center" />
            <el-table-column prop="managerName" label="项目经理" min-width="90" />
            <el-table-column prop="projectCnt" label="项目数" width="70" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="primary">{{ row.projectCnt || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sysTotal" label="系统总数" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small">{{ row.sysTotal || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sysL2" label="2级" width="65" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="warning">{{ row.sysL2 || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sysL3" label="3级" width="65" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="danger">{{ row.sysL3 || 0 }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="managerDetailStats.length === 0"
            style="text-align:center;color:#909399;padding:20px;font-size:13px">
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

const typeChartRef    = ref<HTMLElement>()
const industryChartRef = ref<HTMLElement>()
const regionChartRef  = ref<HTMLElement>()
const amountChartRef  = ref<HTMLElement>()
let charts: echarts.ECharts[] = []

const typeYear    = ref('')
const yearOptions = ref<string[]>([])

const writerStats        = ref<any[]>([])
const managerDetailStats = ref<any[]>([])
const regionEmpty        = ref(false)
const amountTotalWan     = ref('0.00')

const summaryCards = ref([
  { label: '项目总数',       value: 0,   icon: 'Folder',       color: '#409EFF' },
  { label: '进行中',         value: 0,   icon: 'Clock',        color: '#E6A23C' },
  { label: '已完成',         value: 0,   icon: 'Check',        color: '#67C23A' },
  { label: '本年合同额(万元)', value: '0', icon: 'Money',        color: '#F56C6C' },
  { label: '2级系统数量',    value: 0,   icon: 'Grid',         color: '#909399' },
  { label: '3级系统数量',    value: 0,   icon: 'DataAnalysis', color: '#6E4AC6' },
])

onMounted(async () => {
  const now = new Date().getFullYear()
  for (let y = now; y >= now - 5; y--) yearOptions.value.push(String(y))
  typeYear.value = String(now)
  await loadAll()
})

async function loadAll() {
  await Promise.all([loadSummary(), loadCharts(), loadTableStats()])
}

// ── 汇总卡片 ──────────────────────────────────────────
async function loadSummary() {
  try {
    const res: any = await projectApi.stats({ year: typeYear.value })
    const d = res.data
    summaryCards.value[0].value = d.total      || 0
    summaryCards.value[1].value = d.inProgress || 0
    summaryCards.value[2].value = d.completed  || 0
    summaryCards.value[3].value = d.totalAmountWan || '0'
    summaryCards.value[4].value = d.sysCountL2 || 0
    summaryCards.value[5].value = d.sysCountL3 || 0
  } catch {}
}

// ── 4个图表 ────────────────────────────────────────────
async function loadCharts() {
  try {
    const [typeRes, indRes, regionRes, amtRes]: any[] = await Promise.all([
      projectApi.statsByType(typeYear.value || undefined),
      projectApi.statsByIndustry(typeYear.value || undefined),
      projectApi.statsByRegion(typeYear.value || undefined),
      projectApi.statsByAmount(typeYear.value || undefined),
    ])

    // 项目类型分布 - 饼图
    renderPieChart(typeChartRef.value!, typeRes.data || [], '项目类型')

    // 所属行业分布 - 横向条形图
    renderBarHChart(industryChartRef.value!, indRes.data || [], '项目数')

    // 项目地区分布 - 饼图
    const regionData = (regionRes.data || []).filter((d: any) => d.name)
    regionEmpty.value = regionData.length === 0
    if (regionData.length > 0) {
      renderPieChart(regionChartRef.value!, regionData, '项目地区')
    }

    // 合同金额趋势 - 柱状图
    const amtData = amtRes.data || {}
    amountTotalWan.value = amtData.totalWan || '0.00'
    renderAmountChart(amountChartRef.value!, amtData.monthly || [])

  } catch {
    // 加载失败时显示空图表
    renderPieChart(typeChartRef.value!, [], '项目类型')
    renderBarHChart(industryChartRef.value!, [], '项目数')
    renderAmountChart(amountChartRef.value!, [])
  }
}

// ── 统计表格 ────────────────────────────────────────────
async function loadTableStats() {
  try {
    const [writerRes, mgrRes]: any[] = await Promise.all([
      projectApi.statsByWriter(typeYear.value || undefined),
      projectApi.statsByManagerDetail(typeYear.value || undefined),
    ])
    writerStats.value        = writerRes.data || []
    managerDetailStats.value = mgrRes.data   || []
  } catch {
    writerStats.value        = []
    managerDetailStats.value = []
  }
}

// ── 图表渲染函数 ────────────────────────────────────────

function getOrInitChart(el: HTMLElement): echarts.ECharts {
  const existing = echarts.getInstanceByDom(el)
  if (existing) return existing
  const c = echarts.init(el)
  charts.push(c)
  return c
}

/** 饼图（项目类型 / 项目地区） */
function renderPieChart(el: HTMLElement, data: any[], seriesName: string) {
  if (!el) return
  const c = getOrInitChart(el)
  c.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      name: seriesName,
      type: 'pie',
      radius: ['38%', '65%'],
      center: ['50%', '44%'],
      data: data.map((d: any) => ({ name: d.name || '未知', value: d.cnt ?? d.value ?? 0 })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.3)' } },
      label: { formatter: '{b}\n{d}%' },
    }]
  }, true)
}

/** 横向条形图（所属行业分布） */
function renderBarHChart(el: HTMLElement, data: any[], seriesName: string) {
  if (!el) return
  const c = getOrInitChart(el)
  const sorted = [...data].sort((a: any, b: any) => (a.cnt ?? a.value ?? 0) - (b.cnt ?? b.value ?? 0))
  c.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: {
      type: 'category',
      data: sorted.map((d: any) => d.name || '未知'),
      axisLabel: { fontSize: 12 }
    },
    series: [{
      name: seriesName,
      type: 'bar',
      data: sorted.map((d: any) => d.cnt ?? d.value ?? 0),
      itemStyle: { color: '#409EFF', borderRadius: [0, 4, 4, 0] },
      label: { show: true, position: 'right', color: '#606266' }
    }]
  }, true)
}

/** 合同金额趋势柱状图（月度，万元） */
function renderAmountChart(el: HTMLElement, monthly: any[]) {
  if (!el) return
  const c = getOrInitChart(el)
  const months = monthly.length > 0
    ? monthly.map((m: any) => m.month)
    : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月']
  const amounts = monthly.map((m: any) => parseFloat(m.amountWan) || 0)

  c.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const p = params[0]
        return `${p.name}<br/>合同金额：<b>${p.value}</b> 万元`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: months, axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', name: '万元', nameTextStyle: { color: '#909399' } },
    series: [{
      type: 'bar',
      data: amounts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0,   color: '#67C23A' },
          { offset: 1,   color: '#b3e19d' },
        ])
      },
      label: {
        show: true,
        position: 'top',
        formatter: (p: any) => p.value > 0 ? p.value : '',
        color: '#606266',
        fontSize: 11
      },
      barMaxWidth: 40,
    }]
  }, true)
}

onUnmounted(() => { charts.forEach(c => c.dispose()) })
</script>

<style scoped>
.page-container {}
.stats-row {}
.stat-card {}
.stat-content { display: flex; align-items: center; gap: 16px; }
.stat-icon {
  width: 52px; height: 52px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-value { font-size: 24px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
