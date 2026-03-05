<template>
  <div class="page-container">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="title">项目详情</span>
          <div>
            <el-button :icon="Edit" type="primary" @click="$router.push(`/project/edit/${projectId}`)">编辑</el-button>
            <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="项目编号">{{ detail.projectNo }}</el-descriptions-item>
            <el-descriptions-item label="项目名称" :span="2">{{ detail.projectName }}</el-descriptions-item>
            <el-descriptions-item label="客户名称">{{ detail.customerName }}</el-descriptions-item>
            <el-descriptions-item label="客户地址">{{ detail.customerAddress }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.customerContact }} {{ detail.customerPhone }}</el-descriptions-item>
            <el-descriptions-item label="项目类型">{{ detail.projectTypeName }}</el-descriptions-item>
            <el-descriptions-item label="所属行业">{{ detail.industryName }}</el-descriptions-item>
            <el-descriptions-item label="项目状态">
              <el-tag :type="statusTagType(detail.projectStatus)">{{ statusLabel(detail.projectStatus) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="合同签订日期">{{ detail.contractDate }}</el-descriptions-item>
            <el-descriptions-item label="合同金额(元)">
              <span v-if="detail.contractAmountDecrypted">{{ formatMoney(detail.contractAmountDecrypted) }}</span>
              <span v-else>--</span>
            </el-descriptions-item>
            <el-descriptions-item label="所属年份">{{ detail.yearBelong }}</el-descriptions-item>
            <el-descriptions-item label="任务书编号">{{ detail.taskNo }}</el-descriptions-item>
            <el-descriptions-item label="业务人员">{{ detail.businessPerson }}</el-descriptions-item>
            <el-descriptions-item label="项目地区">{{ detail.projectRegion }}</el-descriptions-item>
            <el-descriptions-item label="纸质归档">
              <el-tag :type="detail.paperArchived ? 'success' : 'info'">
                {{ detail.paperArchived ? '已归档' : '未归档' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="报告邮寄日期">{{ detail.reportMailDate }}</el-descriptions-item>
            <el-descriptions-item label="报告邮寄单号">{{ detail.reportMailNo }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="3">{{ detail.remark }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="被测系统" name="systems">
          <el-table :data="detail.systems" border>
            <el-table-column label="序号" prop="sysSeq" width="70" align="center" />
            <el-table-column label="系统名称" prop="sysName" min-width="200" />
            <el-table-column label="系统等级" prop="sysLevel" width="100" align="center">
              <template #default="{ row }">{{ row.sysLevel }}级</template>
            </el-table-column>
            <el-table-column label="测评指标" prop="evalIndex" min-width="150" />
            <el-table-column label="备案号" prop="recordNo" width="160" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="人员配置" name="members">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="项目经理">{{ detail.projectManagerName }}</el-descriptions-item>
            <el-descriptions-item label="项目负责人">{{ detail.projectLeaderName }}</el-descriptions-item>
          </el-descriptions>
          <el-divider content-position="left">测评组成员</el-divider>
          <el-table :data="memberTableData" border>
            <el-table-column label="角色" prop="roleLabel" width="160" />
            <el-table-column label="人员" prop="names" min-width="300" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="阶段时间" name="phases">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="任务预约日期">{{ detail.taskAppointDate }}</el-descriptions-item>
            <el-descriptions-item label="准备阶段">{{ detail.phasePrepare }}</el-descriptions-item>
            <el-descriptions-item label="方案阶段">{{ detail.phasePlan }}</el-descriptions-item>
            <el-descriptions-item label="现场测评阶段">{{ detail.phaseOnsite }}</el-descriptions-item>
            <el-descriptions-item label="报告阶段">{{ detail.phaseReport }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit } from '@element-plus/icons-vue'
import { projectApi } from '@/api/project'

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.id))
const loading = ref(false)
const activeTab = ref('info')
const detail = ref<any>({})

const roleLabels: Record<string, string> = {
  registered_evaluator: '登记测评师',
  actual_member: '实际测评人员',
  survey_editor: '调研表编制人',
  plan_editor: '方案编制人',
  report_editor: '报告编制人',
  network_evaluator: '网络测评人员',
  host_evaluator: '主机测评人员',
  physical_evaluator: '物理测评人员',
  tool_scanner: '工具扫描人员',
  pentest_member: '渗透测试人员',
}

const memberTableData = computed(() => {
  if (!detail.value.members) return []
  const grouped: Record<string, string[]> = {}
  detail.value.members.forEach((m: any) => {
    if (!grouped[m.memberRole]) grouped[m.memberRole] = []
    grouped[m.memberRole].push(m.staffName)
  })
  return Object.entries(grouped).map(([role, names]) => ({
    roleLabel: roleLabels[role] || role,
    names: names.join('、'),
  }))
})

function statusLabel(s: number) {
  const map: Record<number, string> = { 0: '待启动', 1: '进行中', 2: '测评完成', 3: '报告已出', 4: '已归档' }
  return map[s] || '未知'
}

function statusTagType(s: number) {
  const map: Record<number, string> = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: '' }
  return map[s] || ''
}

function formatMoney(val: number | string) {
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

onMounted(async () => {
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
.page-container { max-width: 1200px; margin: 0 auto; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
</style>
