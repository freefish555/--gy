<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 硬件测评设备 -->
      <el-tab-pane label="硬件测评设备" name="device">
        <div class="tab-toolbar">
          <div>
            <el-input v-model="deviceQuery.keyword" placeholder="搜索设备名称/型号" clearable style="width:220px" @keyup.enter="loadDevices" />
            <el-button type="primary" :icon="Search" @click="loadDevices" style="margin-left:8px">查询</el-button>
          </div>
          <el-button type="primary" :icon="Plus" @click="openDeviceDialog()">新增设备</el-button>
        </div>

        <el-table :data="deviceList" border v-loading="deviceLoading" style="margin-top:12px">
          <el-table-column label="编号" prop="deviceNo" width="120" />
          <el-table-column label="设备名称" prop="deviceName" min-width="180" />
          <el-table-column label="型号/规格" prop="deviceModel" width="150" />
          <el-table-column label="设备类型" prop="deviceType" width="120" />
          <el-table-column label="负责人" prop="assignedUserName" width="100" />
          <el-table-column label="IP地址" prop="ipAddress" width="140" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status ? 'success' : 'danger'" size="small">{{ row.status ? '正常' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" :icon="Edit" @click="openDeviceDialog(row)">编辑</el-button>
              <el-button type="danger" link size="small" :icon="Delete" @click="deleteDevice(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="deviceQuery.pageNum" v-model:page-size="deviceQuery.pageSize"
          :total="deviceTotal" layout="total, prev, pager, next"
          style="margin-top:12px; justify-content:flex-end; display:flex" @change="loadDevices" />
      </el-tab-pane>

      <!-- 渗透软件工具 -->
      <el-tab-pane label="渗透软件工具" name="pentest">
        <div class="tab-toolbar">
          <div>
            <el-input v-model="pentestQuery.keyword" placeholder="搜索工具名称" clearable style="width:220px" @keyup.enter="loadPentests" />
            <el-button type="primary" :icon="Search" @click="loadPentests" style="margin-left:8px">查询</el-button>
          </div>
          <el-button type="primary" :icon="Plus" @click="openPentestDialog()">新增工具</el-button>
        </div>

        <el-table :data="pentestList" border v-loading="pentestLoading" style="margin-top:12px">
          <el-table-column label="工具名称" prop="toolName" min-width="160" />
          <el-table-column label="版本" prop="toolVersion" width="120" />
          <el-table-column label="用途" prop="toolPurpose" min-width="200" />
          <el-table-column label="官网/来源" prop="toolSource" min-width="200">
            <template #default="{ row }">
              <a v-if="row.toolSource" :href="row.toolSource" target="_blank" class="link">{{ row.toolSource }}</a>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status ? 'success' : 'danger'" size="small">{{ row.status ? '正常' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" :icon="Edit" @click="openPentestDialog(row)">编辑</el-button>
              <el-button type="danger" link size="small" :icon="Delete" @click="deletePentest(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="pentestQuery.pageNum" v-model:page-size="pentestQuery.pageSize"
          :total="pentestTotal" layout="total, prev, pager, next"
          style="margin-top:12px; justify-content:flex-end; display:flex" @change="loadPentests" />
      </el-tab-pane>
    </el-tabs>

    <!-- 设备编辑弹窗 -->
    <el-dialog v-model="deviceDialog" :title="deviceEditId ? '编辑设备' : '新增硬件设备'" width="560px">
      <el-form :model="deviceForm" :rules="deviceRules" ref="deviceFormRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="设备编号" prop="deviceNo">
              <el-input v-model="deviceForm.deviceNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="deviceForm.deviceName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="型号/规格">
              <el-input v-model="deviceForm.deviceModel" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型">
              <el-input v-model="deviceForm.deviceType" placeholder="如: 扫描设备" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-select v-model="deviceForm.assignedUserId" clearable filterable placeholder="请选择" style="width:100%">
                <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="IP地址">
              <el-input v-model="deviceForm.ipAddress" placeholder="192.168.x.x" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="deviceForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deviceDialog=false">取消</el-button>
        <el-button type="primary" :loading="savingDevice" @click="saveDevice">保存</el-button>
      </template>
    </el-dialog>

    <!-- 渗透工具编辑弹窗 -->
    <el-dialog v-model="pentestDialog" :title="pentestEditId ? '编辑工具' : '新增渗透工具'" width="560px">
      <el-form :model="pentestForm" :rules="pentestRules" ref="pentestFormRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工具名称" prop="toolName">
              <el-input v-model="pentestForm.toolName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本">
              <el-input v-model="pentestForm.toolVersion" placeholder="如: 2024.1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="用途说明">
          <el-input v-model="pentestForm.toolPurpose" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="官网/来源">
          <el-input v-model="pentestForm.toolSource" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="pentestForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pentestDialog=false">取消</el-button>
        <el-button type="primary" :loading="savingPentest" @click="savePentest">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { toolApi, staffApi } from '@/api/staff'

const activeTab = ref('device')

// 设备
const deviceLoading = ref(false)
const deviceList = ref<any[]>([])
const deviceTotal = ref(0)
const deviceDialog = ref(false)
const deviceEditId = ref<number | null>(null)
const savingDevice = ref(false)
const deviceFormRef = ref()
const deviceQuery = reactive({ pageNum: 1, pageSize: 20, keyword: '' })
const deviceForm = reactive({ deviceNo: '', deviceName: '', deviceModel: '', deviceType: '', assignedUserId: null as any, ipAddress: '', remark: '' })
const deviceRules = { deviceNo: [{ required: true, message: '请输入设备编号' }], deviceName: [{ required: true, message: '请输入设备名称' }] }

// 渗透工具
const pentestLoading = ref(false)
const pentestList = ref<any[]>([])
const pentestTotal = ref(0)
const pentestDialog = ref(false)
const pentestEditId = ref<number | null>(null)
const savingPentest = ref(false)
const pentestFormRef = ref()
const pentestQuery = reactive({ pageNum: 1, pageSize: 20, keyword: '' })
const pentestForm = reactive({ toolName: '', toolVersion: '', toolPurpose: '', toolSource: '', remark: '' })
const pentestRules = { toolName: [{ required: true, message: '请输入工具名称' }] }

const staffOptions = ref<any[]>([])

onMounted(async () => {
  loadDevices()
  loadPentests()
  try { const r: any = await staffApi.all(); staffOptions.value = r.data || [] } catch {}
})

async function loadDevices() {
  deviceLoading.value = true
  try { const r: any = await toolApi.deviceList(deviceQuery); deviceList.value = r.data?.records || []; deviceTotal.value = r.data?.total || 0 }
  finally { deviceLoading.value = false }
}

async function loadPentests() {
  pentestLoading.value = true
  try { const r: any = await toolApi.pentestList(pentestQuery); pentestList.value = r.data?.records || []; pentestTotal.value = r.data?.total || 0 }
  finally { pentestLoading.value = false }
}

function openDeviceDialog(row?: any) {
  deviceEditId.value = row?.id || null
  Object.assign(deviceForm, row || { deviceNo: '', deviceName: '', deviceModel: '', deviceType: '', assignedUserId: null, ipAddress: '', remark: '' })
  deviceDialog.value = true
}

async function saveDevice() {
  await deviceFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    savingDevice.value = true
    try {
      if (deviceEditId.value) await toolApi.deviceUpdate(deviceEditId.value, deviceForm)
      else await toolApi.deviceCreate(deviceForm)
      ElMessage.success('保存成功'); deviceDialog.value = false; loadDevices()
    } finally { savingDevice.value = false }
  })
}

async function deleteDevice(row: any) {
  await ElMessageBox.confirm(`确认删除设备【${row.deviceName}】？`, '警告', { type: 'warning' })
  await toolApi.deviceDelete(row.id)
  ElMessage.success('删除成功'); loadDevices()
}

function openPentestDialog(row?: any) {
  pentestEditId.value = row?.id || null
  Object.assign(pentestForm, row || { toolName: '', toolVersion: '', toolPurpose: '', toolSource: '', remark: '' })
  pentestDialog.value = true
}

async function savePentest() {
  await pentestFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    savingPentest.value = true
    try {
      if (pentestEditId.value) await toolApi.pentestUpdate(pentestEditId.value, pentestForm)
      else await toolApi.pentestCreate(pentestForm)
      ElMessage.success('保存成功'); pentestDialog.value = false; loadPentests()
    } finally { savingPentest.value = false }
  })
}

async function deletePentest(row: any) {
  await ElMessageBox.confirm(`确认删除工具【${row.toolName}】？`, '警告', { type: 'warning' })
  await toolApi.pentestDelete(row.id)
  ElMessage.success('删除成功'); loadPentests()
}
</script>

<style scoped>
.page-container {}
.tab-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.link { color: #409EFF; text-decoration: none; }
</style>
