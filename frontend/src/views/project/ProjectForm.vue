<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">{{ isEdit ? '编辑项目' : '新增项目' }}</span>
          <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="form-tabs">
        <!-- Tab1: 基本信息 -->
        <el-tab-pane label="基本信息" name="tab1">
          <el-form :model="form" :rules="rules" ref="formRef1" label-width="120px" class="form-body">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目编号" prop="projectNo">
                  <el-input v-model="form.projectNo" placeholder="如: 2025-DJBH-001" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="任务书编号" prop="taskNo">
                  <el-input v-model="form.taskNo" placeholder="请输入任务书编号" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="项目名称" prop="projectName">
                  <el-input v-model="form.projectName" placeholder="请输入项目名称" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="客户名称" prop="customerName">
                  <el-input v-model="form.customerName" placeholder="请输入客户名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户地址">
                  <el-input v-model="form.customerAddress" placeholder="请输入客户地址" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系人姓名">
                  <el-input v-model="form.customerContact" placeholder="请输入联系人姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系人电话">
                  <el-input v-model="form.customerPhone" placeholder="请输入联系人电话" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目类型" prop="projectTypeId">
                  <el-select v-model="form.projectTypeId" placeholder="请选择" style="width:100%">
                    <el-option v-for="d in projectTypeOptions" :key="d.id" :label="d.itemLabel" :value="d.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="所属行业">
                  <el-select v-model="form.industryId" placeholder="请选择" style="width:100%" clearable>
                    <el-option v-for="d in industryOptions" :key="d.id" :label="d.itemLabel" :value="d.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目状态">
                  <el-select v-model="form.projectStatus" style="width:100%">
                    <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="纸质归档">
                  <el-switch v-model="form.paperArchived" :active-value="1" :inactive-value="0"
                    active-text="已归档" inactive-text="未归档" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="合同签订日期">
                  <el-date-picker v-model="form.contractDate" type="date" value-format="YYYY-MM-DD"
                    placeholder="选择日期" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="合同金额(元)">
                  <el-input-number v-model="form.contractAmount" :precision="2" :min="0"
                    :controls="false" placeholder="请输入合同金额" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="所属年份">
                  <el-date-picker v-model="form.yearBelong" type="year" value-format="YYYY"
                    placeholder="选择年份" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="业务人员">
                  <el-select v-model="form.businessPerson" placeholder="请输入或选择" clearable
                    filterable allow-create style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.realName" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="报告邮寄日期">
                  <el-date-picker v-model="form.reportMailDate" type="date" value-format="YYYY-MM-DD"
                    placeholder="选择日期" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="报告邮寄单号">
                  <el-input v-model="form.reportMailNo" placeholder="请输入快递单号" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目地区">
                  <el-input v-model="form.projectRegion" placeholder="如: 华东地区" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="备注">
                  <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- Tab2: 被测系统 -->
        <el-tab-pane label="被测系统" name="tab2">
          <div class="tab-header">
            <el-button type="primary" :icon="Plus" size="small" @click="addSystem">添加系统</el-button>
          </div>
          <el-table :data="form.systems" border style="margin-top:12px">
            <el-table-column label="序号" prop="sysSeq" width="80">
              <template #default="{ row, $index }">
                <el-input-number v-model="row.sysSeq" :min="1" :controls="false" size="small" style="width:60px" />
              </template>
            </el-table-column>
            <el-table-column label="系统名称" prop="sysName" min-width="180">
              <template #default="{ row }">
                <el-input v-model="row.sysName" placeholder="请输入系统名称" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="系统等级" prop="sysLevel" width="120">
              <template #default="{ row }">
                <el-select v-model="row.sysLevel" placeholder="选择等级" size="small">
                  <el-option label="一级" value="1" />
                  <el-option label="二级" value="2" />
                  <el-option label="三级" value="3" />
                  <el-option label="四级" value="4" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="测评指标" prop="evalIndex" min-width="150">
              <template #default="{ row }">
                <el-input v-model="row.evalIndex" placeholder="测评指标" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="备案号" prop="recordNo" width="150">
              <template #default="{ row }">
                <el-input v-model="row.recordNo" placeholder="备案号" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ $index }">
                <el-button type="danger" link :icon="Delete" size="small" @click="removeSystem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab3: 人员配置 -->
        <el-tab-pane label="人员配置" name="tab3">
          <el-form :model="form" label-width="130px" class="form-body">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目经理">
                  <el-select v-model="form.projectManagerId" clearable filterable placeholder="请选择或搜索" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="项目负责人">
                  <el-select v-model="form.projectLeaderId" clearable filterable placeholder="请选择或搜索" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="登记测评师">
                  <el-select v-model="form.registeredEvaluatorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="实际测评人员">
                  <el-select v-model="form.actualMemberIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="调研表编制人">
                  <el-select v-model="form.surveyEditorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="方案编制人">
                  <el-select v-model="form.planEditorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="报告编制人">
                  <el-select v-model="form.reportEditorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="网络测评人员">
                  <el-select v-model="form.networkEvaluatorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="主机测评人员">
                  <el-select v-model="form.hostEvaluatorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="物理测评人员">
                  <el-select v-model="form.physicalEvaluatorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="工具扫描人员">
                  <el-select v-model="form.toolScannerIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="渗透测试人员">
                  <el-select v-model="form.pentestMemberIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- Tab4: 阶段时间 -->
        <el-tab-pane label="阶段时间" name="tab4">
          <el-form :model="form" label-width="130px" class="form-body">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="任务预约日期">
                  <el-date-picker v-model="form.taskAppointDate" type="date" value-format="YYYY-MM-DD"
                    placeholder="选择日期" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-divider content-position="left">阶段时间段</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="准备阶段">
                  <el-date-picker v-model="form.phasePrepare" type="daterange" value-format="YYYY-MM-DD"
                    start-placeholder="开始" end-placeholder="结束" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="方案阶段">
                  <el-date-picker v-model="form.phasePlan" type="daterange" value-format="YYYY-MM-DD"
                    start-placeholder="开始" end-placeholder="结束" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="现场测评阶段">
                  <el-date-picker v-model="form.phaseOnsite" type="daterange" value-format="YYYY-MM-DD"
                    start-placeholder="开始" end-placeholder="结束" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="报告阶段">
                  <el-date-picker v-model="form.phaseReport" type="daterange" value-format="YYYY-MM-DD"
                    start-placeholder="开始" end-placeholder="结束" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- 底部操作按钮 -->
      <div class="form-footer">
        <el-button @click="$router.back()">取消</el-button>
        <el-button type="primary" :loading="saving" :icon="Check" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建项目' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Delete, Check } from '@element-plus/icons-vue'
import { projectApi } from '@/api/project'
import { staffApi } from '@/api/staff'
import { dictApi } from '@/api/system'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const activeTab = ref('tab1')
const saving = ref(false)
const formRef1 = ref()

const form = reactive<any>({
  projectNo: '',
  projectName: '',
  customerName: '',
  customerAddress: '',
  customerContact: '',
  customerPhone: '',
  taskNo: '',
  projectTypeId: null,
  industryId: null,
  projectStatus: 0,
  paperArchived: 0,
  contractDate: '',
  contractAmount: null,
  yearBelong: '',
  businessPerson: '',
  projectRegion: '',
  remark: '',
  reportMailDate: '',
  reportMailNo: '',
  // Tab2
  systems: [],
  // Tab3
  projectManagerId: null,
  projectLeaderId: null,
  registeredEvaluatorIds: [],
  actualMemberIds: [],
  surveyEditorIds: [],
  planEditorIds: [],
  reportEditorIds: [],
  networkEvaluatorIds: [],
  hostEvaluatorIds: [],
  physicalEvaluatorIds: [],
  toolScannerIds: [],
  pentestMemberIds: [],
  // Tab4
  taskAppointDate: '',
  phasePrepare: [],
  phasePlan: [],
  phaseOnsite: [],
  phaseReport: [],
})

const rules = {
  projectNo: [{ required: true, message: '请输入项目编号' }],
  projectName: [{ required: true, message: '请输入项目名称' }],
  customerName: [{ required: true, message: '请输入客户名称' }],
  projectTypeId: [{ required: true, message: '请选择项目类型' }],
}

const staffOptions = ref<any[]>([])
const projectTypeOptions = ref<any[]>([])
const industryOptions = ref<any[]>([])

const statusOptions = [
  { value: 0, label: '待启动' },
  { value: 1, label: '进行中' },
  { value: 2, label: '测评完成' },
  { value: 3, label: '报告已出' },
  { value: 4, label: '已归档' },
]

onMounted(async () => {
  // 加载员工列表
  try {
    const res: any = await staffApi.all()
    staffOptions.value = res.data || []
  } catch {}

  // 加载字典
  try {
    const ptRes: any = await dictApi.getAllItems('project_type')
    projectTypeOptions.value = ptRes.data || []
    const indRes: any = await dictApi.getAllItems('industry')
    industryOptions.value = indRes.data || []
  } catch {}

  // 编辑时加载数据
  if (isEdit.value) {
    loadDetail()
  }
})

async function loadDetail() {
  try {
    const res: any = await projectApi.detail(Number(route.params.id))
    const d = res.data
    Object.assign(form, d)
    // 阶段时间转数组
    if (d.phasePrepare) form.phasePrepare = d.phasePrepare.split('~')
    if (d.phasePlan) form.phasePlan = d.phasePlan.split('~')
    if (d.phaseOnsite) form.phaseOnsite = d.phaseOnsite.split('~')
    if (d.phaseReport) form.phaseReport = d.phaseReport.split('~')
    // 人员IDs
    if (d.members) {
      const byRole = (role: string) => d.members.filter((m: any) => m.memberRole === role).map((m: any) => m.staffId)
      form.registeredEvaluatorIds = byRole('registered_evaluator')
      form.actualMemberIds = byRole('actual_member')
      form.surveyEditorIds = byRole('survey_editor')
      form.planEditorIds = byRole('plan_editor')
      form.reportEditorIds = byRole('report_editor')
      form.networkEvaluatorIds = byRole('network_evaluator')
      form.hostEvaluatorIds = byRole('host_evaluator')
      form.physicalEvaluatorIds = byRole('physical_evaluator')
      form.toolScannerIds = byRole('tool_scanner')
      form.pentestMemberIds = byRole('pentest_member')
    }
  } catch (e: any) {
    ElMessage.error('加载项目详情失败: ' + e.message)
  }
}

function addSystem() {
  form.systems.push({ sysSeq: form.systems.length + 1, sysName: '', sysLevel: '3', evalIndex: '', recordNo: '' })
}

function removeSystem(index: number) {
  form.systems.splice(index, 1)
}

function formatPhaseRange(val: string[]): string {
  if (!val || val.length < 2) return ''
  return val[0] + '~' + val[1]
}

async function handleSubmit() {
  if (!formRef1.value) return
  await formRef1.value.validate(async (valid: boolean) => {
    if (!valid) { activeTab.value = 'tab1'; return }
    saving.value = true
    try {
      const payload = {
        ...form,
        phasePrepare: formatPhaseRange(form.phasePrepare),
        phasePlan: formatPhaseRange(form.phasePlan),
        phaseOnsite: formatPhaseRange(form.phaseOnsite),
        phaseReport: formatPhaseRange(form.phaseReport),
      }
      if (isEdit.value) {
        await projectApi.update(Number(route.params.id), payload)
        ElMessage.success('项目修改成功')
      } else {
        await projectApi.create(payload)
        ElMessage.success('项目创建成功')
      }
      router.push('/project/list')
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    } finally {
      saving.value = false
    }
  })
}
</script>

<style scoped>
.page-container { max-width: 1200px; margin: 0 auto; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
.form-body { padding: 16px 0; }
.tab-header { display: flex; justify-content: flex-end; padding: 8px 0; }
.form-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 0 4px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}
</style>
