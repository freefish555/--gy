<template>
  <div class="page-container">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="title">项目详情</span>
          <div>
            <el-button
              v-if="canEdit"
              :icon="Edit" type="primary"
              @click="$router.push(`/project/edit/${projectId}`)"
            >编辑</el-button>
            <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- ===== Tab1: 基本信息 ===== -->
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="3" border class="detail-desc">
            <el-descriptions-item label="项目编号">{{ detail.projectNo || '--' }}</el-descriptions-item>
            <el-descriptions-item label="项目名称" :span="2">{{ detail.projectName || '--' }}</el-descriptions-item>

            <el-descriptions-item label="客户名称">{{ detail.customerName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="客户地址" :span="2">{{ detail.customerAddress || '--' }}</el-descriptions-item>

            <el-descriptions-item label="联系人姓名">{{ detail.customerContact || '--' }}</el-descriptions-item>
            <el-descriptions-item label="联系人电话">{{ detail.customerPhone || '--' }}</el-descriptions-item>
            <el-descriptions-item label="项目状态">
              <el-tag :type="statusTagType(detail.projectStatus)">{{ statusLabel(detail.projectStatus) }}</el-tag>
            </el-descriptions-item>

            <el-descriptions-item label="项目类型">{{ detail.projectTypeName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="所属行业">{{ detail.industryName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="项目地区">{{ regionLabel(detail.projectRegion) }}</el-descriptions-item>

            <el-descriptions-item label="合同签订日期">{{ detail.contractDate || '--' }}</el-descriptions-item>
            <el-descriptions-item label="合同金额(元)">
              <span v-if="detail.contractAmount">{{ formatMoney(detail.contractAmount) }}</span>
              <span v-else style="color:#C0C4CC">--</span>
            </el-descriptions-item>
            <el-descriptions-item label="所属年份">{{ detail.yearBelong || '--' }}</el-descriptions-item>

            <el-descriptions-item label="业务人员">{{ detail.businessPerson || '--' }}</el-descriptions-item>
            <el-descriptions-item label="报告邮寄日期">{{ detail.reportMailDate || '--' }}</el-descriptions-item>
            <el-descriptions-item label="报告邮寄单号">{{ detail.reportMailNo || '--' }}</el-descriptions-item>

            <el-descriptions-item label="电子归档">
              <el-tag :type="detail.electronicArchived ? 'success' : 'info'" size="small">
                {{ detail.electronicArchived ? '已归档' : '未归档' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="纸质归档">
              <el-tag :type="detail.paperArchived ? 'success' : 'info'" size="small">
                {{ detail.paperArchived ? '已归档' : '未归档' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="系统名称合并">{{ detail.systemNameMerged || '--' }}</el-descriptions-item>

            <el-descriptions-item label="2级系统数量">
              <el-tag type="warning" size="small">{{ detail.sysCountL2 ?? 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="3级系统数量">
              <el-tag type="danger" size="small">{{ detail.sysCountL3 ?? 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="1">{{ detail.remark || '--' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- ===== Tab2: 被测系统 ===== -->
        <el-tab-pane label="被测系统" name="systems">
          <el-table :data="detail.systems" border stripe>
            <el-table-column label="序号" prop="sysSeq" width="65" align="center" />
            <el-table-column label="系统名称" prop="sysName" min-width="180" />
            <el-table-column label="系统等级" prop="sysLevel" width="90" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.sysLevel" size="small" type="warning">{{ row.sysLevel }}级</el-tag>
                <span v-else style="color:#C0C4CC">--</span>
              </template>
            </el-table-column>
            <el-table-column label="测评指标" prop="evalIndex" min-width="100" />
            <el-table-column label="备案号" prop="recordNo" min-width="140" />
            <el-table-column label="编写人" prop="writerName" min-width="90" align="center">
              <template #default="{ row }">
                <span v-if="row.writerName">{{ row.writerName }}</span>
                <span v-else style="color:#C0C4CC">--</span>
              </template>
            </el-table-column>
            <el-table-column label="审核人员" prop="reviewerName" min-width="90" align="center">
              <template #default="{ row }">
                <span v-if="row.reviewerName">{{ row.reviewerName }}</span>
                <span v-else style="color:#C0C4CC">--</span>
              </template>
            </el-table-column>
            <el-table-column label="报告结论" prop="reportConclusion" width="100" align="center">
              <template #default="{ row }">
                <el-tag
                  v-if="row.reportConclusion"
                  size="small"
                  :type="row.reportConclusion === 'FH' ? 'success' : row.reportConclusion === 'JBFH' ? 'warning' : 'danger'"
                >{{ conclusionLabel(row.reportConclusion) }}</el-tag>
                <span v-else style="color:#C0C4CC">--</span>
              </template>
            </el-table-column>
            <el-table-column label="质量审核得分" prop="qualityScore" width="110" align="center">
              <template #default="{ row }">
                <span v-if="row.qualityScore !== null && row.qualityScore !== undefined">
                  <el-tag
                    size="small"
                    :type="row.qualityScore >= 90 ? 'success' : row.qualityScore >= 60 ? 'warning' : 'danger'"
                  >{{ row.qualityScore }}</el-tag>
                </span>
                <span v-else style="color:#C0C4CC">--</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!detail.systems || detail.systems.length === 0"
            style="text-align:center;color:#909399;padding:24px;font-size:13px">
            暂无被测系统数据
          </div>
        </el-tab-pane>

        <!-- ===== Tab3: 人员配置 ===== -->
        <el-tab-pane label="人员配置" name="members">
          <el-descriptions :column="2" border class="detail-desc">
            <el-descriptions-item label="项目经理">{{ detail.projectManagerName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="项目负责人">{{ detail.projectLeaderName || '--' }}</el-descriptions-item>
          </el-descriptions>
          <el-divider content-position="left" style="margin:16px 0 12px">测评组成员</el-divider>
          <el-table :data="memberTableData" border stripe>
            <el-table-column label="角色" prop="roleLabel" width="180" />
            <el-table-column label="人员" prop="names" min-width="300" />
          </el-table>
        </el-tab-pane>

        <!-- ===== Tab4: 阶段时间 ===== -->
        <el-tab-pane label="阶段时间" name="phases">
          <el-descriptions :column="2" border class="detail-desc">
            <el-descriptions-item label="任务预约日期">{{ detail.taskAppointDate || '--' }}</el-descriptions-item>
            <el-descriptions-item label="准备阶段">{{ detail.phasePrepare || '--' }}</el-descriptions-item>
            <el-descriptions-item label="方案阶段">{{ detail.phasePlan || '--' }}</el-descriptions-item>
            <el-descriptions-item label="现场测评阶段">{{ detail.phaseOnsite || '--' }}</el-descriptions-item>
            <el-descriptions-item label="报告阶段">{{ detail.phaseReport || '--' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="最后更新">{{ formatDateTime(detail.updatedAt) }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit } from '@element-plus/icons-vue'
import { projectApi } from '@/api/project'
import { dictApi } from '@/api/system'
import { useAuthStore } from '@/store/auth'

const route     = useRoute()
const authStore = useAuthStore()
const projectId = computed(() => Number(route.params.id))
const loading   = ref(false)
const activeTab = ref('info')
const detail    = ref<any>({})

// 项目地区字典（itemValue → itemLabel）
const regionMap = ref<Record<string, string>>({})

const canEdit = computed(() => {
  if (authStore.hasPermission('project:update:all')) return true
  if (authStore.hasPermission('project:update:own')) {
    return detail.value.createdBy === authStore.userInfo?.userId
  }
  return false
})

const roleLabels: Record<string, string> = {
  project_manager:      '项目经理',
  project_leader:       '项目负责人',
  registered_evaluator: '登记测评师',
  actual_member:        '实际测评人员',
  survey_editor:        '调研表编制人',
  plan_editor:          '方案编制人',
  report_editor:        '报告编制人',
  network_evaluator:    '网络测评人员',
  host_evaluator:       '主机测评人员',
  physical_evaluator:   '物理测评人员',
  tool_scanner:         '工具扫描人员',
  pentest_member:       '渗透测试人员',
}

const memberTableData = computed(() => {
  if (!detail.value.members) return []
  const grouped: Record<string, string[]> = {}
  detail.value.members.forEach((m: any) => {
    if (!grouped[m.memberRole]) grouped[m.memberRole] = []
    grouped[m.memberRole].push(m.staffName)
  })
  // 按角色顺序输出
  const order = Object.keys(roleLabels)
  const result: any[] = []
  order.forEach(role => {
    if (grouped[role] && grouped[role].length > 0) {
      result.push({ roleLabel: roleLabels[role], names: grouped[role].join('、') })
    }
  })
  return result
})

function statusLabel(s: number) {
  const map: Record<number, string> = { 0: '待启动', 1: '进行中', 2: '测评完成', 3: '报告已出', 4: '已归档' }
  return map[s] ?? '未知'
}

function statusTagType(s: number) {
  const map: Record<number, string> = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: '' }
  return map[s] ?? ''
}

function conclusionLabel(v: string) {
  const map: Record<string, string> = { FH: '符合', JBFH: '基本符合', BFH: '不符合' }
  return map[v] ?? v
}

function regionLabel(v: string) {
  if (!v) return '--'
  return regionMap.value[v] || v
}

function formatMoney(val: number | string) {
  const n = Number(val)
  if (isNaN(n)) return val
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

function formatDateTime(val: any) {
  if (!val) return '--'
  return String(val).replace('T', ' ').substring(0, 19)
}

onMounted(async () => {
  // 加载地区字典
  try {
    const res: any = await dictApi.getAllItems('PROJECT_REGION')
    const items = res.data || []
    items.forEach((d: any) => { regionMap.value[d.itemValue] = d.itemLabel })
  } catch {}

  // 加载项目详情
  loading.value = true
  try {
    const res: any = await projectApi.detail(projectId.value)
    detail.value = res.data
  } catch (e: any) {
    ElMessage.error('加载失败: ' + e.message)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-container { width: 100%; box-sizing: border-box; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
.detail-desc { margin-bottom: 8px; }
</style>
