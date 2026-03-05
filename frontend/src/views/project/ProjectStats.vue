<template>
  <div class="page-container">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6" v-for="card in summaryCards" :key="card.label">
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

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never" header="按项目状态分布">
          <div ref="statusChartRef" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="按项目类型分布">
          <div class="chart-filter">
            <el-select v-model="typeYear" placeholder="选择年份" clearable size="small"
              @change="loadStats" style="width:120px">
              <el-option v-for="y in yearOptions" :key="y" :label="y+'年'" :value="y" />
            </el-select>
          </div>
          <div ref="typeChartRef" style="height:280px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never" header="按所属行业分布">
          <div ref="industryChartRef" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="合同金额月度趋势">
          <div ref="amountChartRef" style="height:280px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { projectApi } from '@/api/project'
import { Folder, Clock, Check, Money } from '@element-plus/icons-vue'

const statusChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
const industryChartRef = ref<HTMLElement>()
const amountChartRef = ref<HTMLElement>()
let charts: echarts.ECharts[] = []

const typeYear = ref('')
const yearOptions = ref<string[]>([])

const summaryCards = ref([
  { label: '项目总数', value: 0, icon: 'Folder', color: '#409EFF' },
  { label: '进行中', value: 0, icon: 'Clock', color: '#E6A23C' },
  { label: '已完成', value: 0, icon: 'Check', color: '#67C23A' },
  { label: '本年合同额(万元)', value: '0', icon: 'Money', color: '#F56C6C' },
])

onMounted(async () => {
  // 生成年份选项
  const now = new Date().getFullYear()
  for (let y = now; y >= now - 5; y--) yearOptions.value.push(String(y))
  typeYear.value = String(now)
  await loadStats()
})

async function loadStats() {
  try {
    const res: any = await projectApi.stats({ year: typeYear.value })
    const d = res.data
    // 汇总卡片
    summaryCards.value[0].value = d.total || 0
    summaryCards.value[1].value = d.inProgress || 0
    summaryCards.value[2].value = d.completed || 0
    summaryCards.value[3].value = d.totalAmountWan || '0'

    renderStatusChart(d.statusDist || [])
    renderTypeChart(d.typeDist || [])
    renderIndustryChart(d.industryDist || [])
    renderAmountChart(d.monthlyAmount || [])
  } catch (e) {
    // 使用模拟数据
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
