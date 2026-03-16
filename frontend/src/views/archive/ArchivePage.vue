<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="title">归档材料制作</span></template>

      <el-steps :active="step" align-center style="margin-bottom:30px">
        <el-step title="选择项目" />
        <el-step title="选择模板" />
        <el-step title="生成文件" />
      </el-steps>

      <!-- Step 1: 选择项目 -->
      <div v-if="step === 0">
        <el-form :model="queryForm" inline>
          <el-form-item label="项目编号/名称">
            <el-input v-model="queryForm.keyword" placeholder="输入项目编号或名称搜索" clearable style="width:260px"
              @keyup.enter="searchProjects" />
          </el-form-item>
          <el-form-item label="所属年份">
            <el-date-picker v-model="queryForm.yearBelong" type="year" value-format="YYYY"
              placeholder="选择年份" style="width:120px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="searchProjects">搜索</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="projectList" border @current-change="handleProjectSelect"
          highlight-current-row style="margin-top:12px">
          <el-table-column label="项目编号" prop="projectNo" width="150" />
          <el-table-column label="项目名称" prop="projectName" min-width="200" />
          <el-table-column label="客户名称" prop="customerName" width="150" />
          <el-table-column label="项目状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.projectStatus)" size="small">
                {{ statusLabel(row.projectStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="所属年份" prop="yearBelong" width="100" align="center" />
        </el-table>

        <div class="step-footer">
          <el-button type="primary" :disabled="!selectedProject" @click="step=1">
            下一步 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- Step 2: 选择模板 -->
      <div v-if="step === 1">
        <el-alert type="info" :closable="false" style="margin-bottom:16px">
          已选项目：<strong>{{ selectedProject?.projectName }}</strong>（{{ selectedProject?.projectNo }}）
        </el-alert>

        <el-table :data="templateList" border @selection-change="handleTemplateSelect">
          <el-table-column type="selection" width="55" />
          <el-table-column label="模板名称" prop="templateName" min-width="250" />
          <el-table-column label="文件类型" prop="fileType" width="100" />
          <el-table-column label="版本" prop="version" width="80" align="center" />
          <el-table-column label="更新时间" prop="updatedAt" width="160" />
        </el-table>

        <div class="step-footer">
          <el-button @click="step=0"><el-icon><ArrowLeft /></el-icon> 上一步</el-button>
          <el-button type="primary" :disabled="selectedTemplates.length===0" @click="generateFiles">
            生成归档文件
          </el-button>
        </div>
      </div>

      <!-- Step 3: 生成结果 -->
      <div v-if="step === 2">
        <el-alert v-if="generateSuccess" type="success" :closable="false" title="归档文件生成成功！" style="margin-bottom:16px" />
        <el-alert v-else type="error" :closable="false" :title="'生成失败: ' + generateError" style="margin-bottom:16px" />

        <el-table :data="generatedFiles" border>
          <el-table-column label="文件名" prop="fileName" min-width="300" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.success ? 'success' : 'danger'" size="small">
                {{ row.success ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="说明" prop="message" min-width="200" />
        </el-table>

        <div class="step-footer">
          <el-button @click="step=1"><el-icon><ArrowLeft /></el-icon> 重新选择</el-button>
          <el-button type="primary" :icon="Download" @click="downloadArchive">下载归档包</el-button>
          <el-button @click="resetWizard">重新制作</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, ArrowLeft, ArrowRight, Download } from '@element-plus/icons-vue'
import { projectApi } from '@/api/project'
import { archiveApi } from '@/api/system'

const step = ref(0)
const queryForm = ref({ keyword: '', yearBelong: '' })
const projectList = ref<any[]>([])
const selectedProject = ref<any>(null)
const templateList = ref<any[]>([])
const selectedTemplates = ref<any[]>([])
const generatedFiles = ref<any[]>([])
const generateSuccess = ref(false)
const generateError = ref('')

const statusLabel = (s: number) => ({ 0: '待启动', 1: '进行中', 2: '测评完成', 3: '报告已出', 4: '已归档' }[s] || '未知')
const statusTagType = (s: number) => ({ 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: '' }[s] || '')

onMounted(() => { searchProjects(); loadTemplates() })

async function searchProjects() {
  try {
    const res: any = await projectApi.list({
      pageNum: 1, pageSize: 20,
      projectName: queryForm.value.keyword,
      projectNo: queryForm.value.keyword,
      yearBelong: queryForm.value.yearBelong,
    })
    projectList.value = res.data?.records || []
  } catch {}
}

async function loadTemplates() {
  try {
    const res: any = await archiveApi.templateList()
    templateList.value = res.data || []
  } catch {}
}

function handleProjectSelect(row: any) { selectedProject.value = row }
function handleTemplateSelect(rows: any[]) { selectedTemplates.value = rows }

async function generateFiles() {
  if (!selectedProject.value) return
  step.value = 2
  generateSuccess.value = false
  generateError.value = ''
  generatedFiles.value = []
  try {
    const res: any = await archiveApi.generate(
      selectedProject.value.id,
      selectedTemplates.value.map((t: any) => t.id)
    )
    generateSuccess.value = true
    generatedFiles.value = res.data?.files || []
  } catch (e: any) {
    generateError.value = e.message || '未知错误'
    step.value = 2
  }
}

async function downloadArchive() {
  if (!selectedProject.value) return
  try {
    const res: any = await archiveApi.download(selectedProject.value.id)
    // res is AxiosResponse when responseType is 'blob'
    const blobData = res?.data || res
    const blob = blobData instanceof Blob ? blobData : new Blob([blobData], { type: 'application/zip' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${selectedProject.value.projectNo}-归档材料.zip`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } catch (e: any) {
    ElMessage.error('下载失败: ' + (e.message || '请先生成归档材料'))
  }
}

function resetWizard() {
  step.value = 0
  selectedProject.value = null
  selectedTemplates.value = []
  generatedFiles.value = []
}
</script>

<style scoped>
.page-container {}
.title { font-size: 16px; font-weight: 600; }
.step-footer { display: flex; justify-content: center; gap: 16px; padding: 20px 0; }
</style>
