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
        <!-- ===== Tab1: 基本信息 ===== -->
        <el-tab-pane label="基本信息" name="tab1">
          <el-form :model="form" :rules="rules" ref="formRef1" label-width="120px" class="form-body">

            <!-- 第一行：项目编号 + 2级系统数量 + 3级系统数量 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目编号" prop="projectNo">
                  <el-input v-model="form.projectNo" placeholder="如: 2025-DJBH-001" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="2级系统数量" label-width="110px">
                  <el-input-number
                    v-model="form.sysCountL2"
                    :min="0" :controls="false"
                    placeholder="自动统计"
                    style="width:100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="3级系统数量" label-width="110px">
                  <el-input-number
                    v-model="form.sysCountL3"
                    :min="0" :controls="false"
                    placeholder="自动统计"
                    style="width:100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 系统名称合并（自动生成+可手动编辑） -->
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="系统名称合并">
                  <el-input
                    v-model="form.systemNameMerged"
                    placeholder="根据被测系统自动生成，如：xxx系统、xxx2系统（可手动修改）"
                  >
                    <template #append>
                      <el-tooltip content="根据被测系统Tab中填写的系统名称自动拼接，也可在此手动修改">
                        <el-icon><InfoFilled /></el-icon>
                      </el-tooltip>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 项目名称 -->
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="项目名称" prop="projectName">
                  <el-input v-model="form.projectName" placeholder="请输入项目名称" />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 客户名称 + 客户地址 -->
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

            <!-- 联系人 + 联系电话 -->
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

            <!-- 项目类型 + 所属行业 -->
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

            <!-- 合同签订日期 + 合同金额 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="合同签订日期">
                  <el-date-picker v-model="form.contractDate" type="date" value-format="YYYY-MM-DD"
                    placeholder="选择日期" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="合同金额(元)">
                  <el-input v-model="form.contractAmount" placeholder="请输入合同金额" />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 所属年份 + 业务人员 -->
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
                    <el-option v-for="s in businessOptions" :key="s.id" :label="s.realName" :value="s.realName" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 报告邮寄日期 + 报告邮寄单号 -->
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

            <!-- 项目地区 + 归档状态 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目地区">
                  <el-select v-model="form.projectRegion" placeholder="请选择地区" clearable filterable style="width:100%">
                    <el-option v-for="d in projectRegionOptions" :key="d.itemValue" :label="d.itemLabel" :value="d.itemValue" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="归档状态">
                  <div class="archive-status-row">
                    <span class="archive-label">电子归档</span>
                    <el-switch
                      v-model="form.electronicArchived"
                      :active-value="1" :inactive-value="0"
                      active-text="已归档" inactive-text="未归档"
                    />
                    <el-divider direction="vertical" />
                    <span class="archive-label">纸质归档</span>
                    <el-switch
                      v-model="form.paperArchived"
                      :active-value="1" :inactive-value="0"
                      active-text="已归档" inactive-text="未归档"
                    />
                  </div>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 备注 -->
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="备注">
                  <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- ===== Tab2: 被测系统 ===== -->
        <el-tab-pane label="被测系统" name="tab2">
          <div class="tab-header">
            <el-button type="primary" :icon="Plus" size="small" @click="addSystem">+ 添加系统</el-button>
          </div>
          <el-table :data="form.systems" border style="margin-top:12px">
            <el-table-column label="序号" prop="sysSeq" width="80">
              <template #default="{ row, $index }">
                <el-input-number v-model="row.sysSeq" :min="1" :controls="false" size="small" style="width:60px" />
              </template>
            </el-table-column>
            <el-table-column label="系统名称" prop="sysName" min-width="200">
              <template #default="{ row, $index }">
                <el-input
                  v-model="row.sysName"
                  :placeholder="$index === 0 ? '例：xxx系统' : '请输入系统名称'"
                  size="small"
                  @input="onSystemNameChange"
                />
              </template>
            </el-table-column>
            <el-table-column label="系统等级" prop="sysLevel" width="130">
              <template #default="{ row, $index }">
                <el-select
                  v-model="row.sysLevel"
                  :placeholder="$index === 0 ? '例：1/2/3/4/5 级' : '选择等级'"
                  size="small"
                  @change="onSystemLevelChange"
                >
                  <el-option label="一级" :value="1" />
                  <el-option label="二级" :value="2" />
                  <el-option label="三级" :value="3" />
                  <el-option label="四级" :value="4" />
                  <el-option label="五级" :value="5" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="测评指标" prop="evalIndex" min-width="150">
              <template #default="{ row, $index }">
                <el-input
                  v-model="row.evalIndex"
                  :placeholder="$index === 0 ? '例：S3A2' : '测评指标'"
                  size="small"
                />
              </template>
            </el-table-column>
            <el-table-column label="备案号" prop="recordNo" min-width="180">
              <template #default="{ row, $index }">
                <el-input
                  v-model="row.recordNo"
                  :placeholder="$index === 0 ? '例：32001546213-26001' : '备案号'"
                  size="small"
                />
              </template>
            </el-table-column>
            <el-table-column label="编写人" prop="writerId" min-width="140">
              <template #default="{ row }">
                <el-select v-model="row.writerId" clearable filterable placeholder="请选择" size="small" style="width:100%">
                  <el-option v-for="s in writerOptions" :key="s.id" :label="s.realName" :value="s.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="审核人员" prop="reviewerId" min-width="140">
              <template #default="{ row }">
                <el-select v-model="row.reviewerId" clearable filterable placeholder="请选择" size="small" style="width:100%">
                  <el-option v-for="s in reviewerOptions" :key="s.id" :label="s.realName" :value="s.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="报告结论" prop="reportConclusion" min-width="140">
              <template #default="{ row }">
                <el-select v-model="row.reportConclusion" clearable placeholder="请选择" size="small" style="width:100%">
                  <el-option v-for="d in reportConclusionOptions" :key="d.itemValue" :label="d.itemLabel" :value="d.itemValue" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="质量审核得分" prop="qualityScore" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.qualityScore" :min="0" :max="100" :controls="false" size="small" style="width:90px" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ $index }">
                <el-button type="danger" link :icon="Delete" size="small" @click="removeSystem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="system-hint">
            <el-text type="info" size="small">提示：系统名称合并和等级数量将根据此处填写内容自动更新到基本信息Tab</el-text>
          </div>
        </el-tab-pane>

        <!-- ===== Tab3: 人员配置 ===== -->
        <el-tab-pane label="人员配置" name="tab3">
          <el-form :model="form" label-width="140px" class="form-body">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目经理">
                  <el-select v-model="form.projectManagerId" clearable filterable placeholder="请选择或搜索" style="width:100%">
                    <el-option v-for="s in managerOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="项目负责人">
                  <el-select v-model="form.projectLeaderId" clearable filterable placeholder="请选择或搜索" style="width:100%">
                    <el-option v-for="s in managerOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="登记测评师">
                  <el-select v-model="form.registeredEvaluatorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in evaluatorOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="实际测评人员">
                  <el-select v-model="form.actualMemberIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in evaluatorOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="调研表编制人">
                  <el-select v-model="form.surveyEditorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in evaluatorOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="方案编制人">
                  <el-select v-model="form.planEditorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in evaluatorOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="报告编制人">
                  <el-select v-model="form.reportEditorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in evaluatorOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="网络测评人员">
                  <el-select v-model="form.networkEvaluatorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in evaluatorOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="主机测评人员">
                  <el-select v-model="form.hostEvaluatorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in evaluatorOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="物理测评人员">
                  <el-select v-model="form.physicalEvaluatorIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in evaluatorOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="工具扫描人员">
                  <el-select v-model="form.toolScannerIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in evaluatorOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="渗透测试人员">
                  <el-select v-model="form.pentestMemberIds" multiple filterable placeholder="可多选" style="width:100%">
                    <el-option v-for="s in evaluatorOptions" :key="s.id" :label="s.realName" :value="s.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- ===== Tab4: 阶段时间 ===== -->
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
            <el-divider content-position="left">各阶段时间段</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="测评准备阶段">
                  <el-date-picker v-model="form.phasePrepare" type="daterange" value-format="YYYY-MM-DD"
                    start-placeholder="开始" end-placeholder="结束" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="方案编制阶段">
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
                <el-form-item label="报告编制阶段">
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
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Delete, Check, InfoFilled } from '@element-plus/icons-vue'
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
  systemNameMerged: '',
  sysCountL2: 0,
  sysCountL3: 0,
  projectTypeId: null,
  industryId: null,
  paperArchived: 0,
  electronicArchived: 0,
  contractDate: '',
  contractAmount: '',
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
  projectNo: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  projectTypeId: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
}

const staffOptions = ref<any[]>([])
const managerOptions = ref<any[]>([])     // position=项目经理
const evaluatorOptions = ref<any[]>([])   // position=测评师
const reviewerOptions = ref<any[]>([])    // position=技术审核
const writerOptions = ref<any[]>([])      // position=测评师,项目经理,项目负责人
const businessOptions = ref<any[]>([])    // position=业务人员
const reportConclusionOptions = ref<any[]>([])
const projectTypeOptions = ref<any[]>([])
const industryOptions = ref<any[]>([])
const projectRegionOptions = ref<any[]>([])

// ===== 监听被测系统变化，自动更新合并名称和等级数量 =====
function onSystemNameChange() {
  autoUpdateMergedAndCount()
}

function onSystemLevelChange() {
  autoUpdateMergedAndCount()
}

// 用 watch 监听 systems 深度变化
watch(() => form.systems, () => {
  autoUpdateMergedAndCount()
}, { deep: true })

function autoUpdateMergedAndCount() {
  const validSystems = form.systems.filter((s: any) => s.sysName && s.sysName.trim())

  // 自动拼接系统名称（顿号分隔）
  const merged = validSystems.map((s: any) => s.sysName.trim()).join('、')
  // 只有当用户没有手动修改过（即当前值与上次自动生成的值相同）时才自动更新
  // 简化处理：始终自动更新（用户若需手动可直接改基本信息Tab）
  if (merged) {
    form.systemNameMerged = merged
  }

  // 统计等级数量
  const l2 = validSystems.filter((s: any) => s.sysLevel === 2).length
  const l3 = validSystems.filter((s: any) => s.sysLevel === 3).length
  form.sysCountL2 = l2
  form.sysCountL3 = l3
}

function addSystem() {
  const nextSeq = form.systems.length + 1
  form.systems.push({ sysSeq: nextSeq, sysName: '', sysLevel: null, evalIndex: '', recordNo: '', writerId: null, reviewerId: null, reportConclusion: null, qualityScore: null })
}

function removeSystem(index: number) {
  form.systems.splice(index, 1)
  // 重新编号
  form.systems.forEach((s: any, i: number) => { s.sysSeq = i + 1 })
  autoUpdateMergedAndCount()
}

function formatPhaseRange(val: string[]): string {
  if (!val || val.length < 2) return ''
  return val[0] + '~' + val[1]
}

onMounted(async () => {
  try {
    const [allRes, mgrRes, evalRes, revRes, writerRes, bizRes]: any[] = await Promise.all([
      staffApi.all(),
      staffApi.listByPosition('项目经理'),
      staffApi.listByPosition('测评师'),
      staffApi.listByPosition('技术审核'),
      staffApi.listByPosition('测评师,项目经理,项目负责人'),
      staffApi.listByPosition('业务人员'),
    ])
    staffOptions.value = allRes.data || []
    managerOptions.value = mgrRes.data || []
    evaluatorOptions.value = evalRes.data || []
    reviewerOptions.value = revRes.data || []
    writerOptions.value = writerRes.data || []
    businessOptions.value = bizRes.data || []
  } catch {}

  try {
    const ptRes: any = await dictApi.getAllItems('PROJECT_TYPE')
    projectTypeOptions.value = ptRes.data || []
    const indRes: any = await dictApi.getAllItems('INDUSTRY')
    industryOptions.value = indRes.data || []
    const rcRes: any = await dictApi.getAllItems('REPORT_CONCLUSION')
    reportConclusionOptions.value = (rcRes.data || []).map((d: any) => ({ itemValue: d.itemValue, itemLabel: d.itemLabel }))
    const regionRes: any = await dictApi.getAllItems('PROJECT_REGION')
    projectRegionOptions.value = (regionRes.data || []).map((d: any) => ({ itemValue: d.itemValue, itemLabel: d.itemLabel }))
  } catch {}


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
.page-container { width: 100%; box-sizing: border-box; }
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
.archive-status-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.archive-label {
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}
.system-hint {
  margin-top: 8px;
  padding: 4px 0;
}
</style>
