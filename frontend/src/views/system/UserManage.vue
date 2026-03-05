<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">管理员设置</span>
          <el-button type="primary" :icon="Plus" @click="openDialog()">新增管理员</el-button>
        </div>
      </template>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column label="用户名" prop="username" width="130" />
        <el-table-column label="真实姓名" prop="realName" width="120" />
        <el-table-column label="角色" prop="roleName" width="130" />
        <el-table-column label="电话" prop="phone" width="140" />
        <el-table-column label="双因子认证" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.totpEnabled ? 'success' : 'info'" size="small">{{ row.totpEnabled ? '已启用' : '未启用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'danger'" size="small">{{ row.status ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" prop="lastLoginAt" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="resetPwd(row)">重置密码</el-button>
            <el-button :type="row.status ? 'warning' : 'success'" link size="small"
              @click="toggleStatus(row)">{{ row.status ? '禁用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑管理员' : '新增管理员'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!editId" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" style="width:100%">
            <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editId" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit } from '@element-plus/icons-vue'
import { userApi } from '@/api/system'
import request from '@/utils/request'

const loading = ref(false)
const saving = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()
const roleOptions = ref<any[]>([])

const form = reactive({ username: '', realName: '', roleId: null as any, password: '', phone: '', email: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  realName: [{ required: true, message: '请输入真实姓名' }],
  roleId: [{ required: true, message: '请选择角色' }],
  password: [{ required: true, message: '请输入初始密码' }],
}

onMounted(async () => {
  loadData()
  try { const r: any = await request.get('/system/role/list'); roleOptions.value = r.data || [] } catch {}
})

async function loadData() {
  loading.value = true
  try { const r: any = await userApi.list(); tableData.value = r.data || [] }
  finally { loading.value = false }
}

function openDialog(row?: any) {
  editId.value = row?.id || null
  Object.assign(form, row ? { ...row, password: '' } : { username: '', realName: '', roleId: null, password: '', phone: '', email: '' })
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    saving.value = true
    try {
      if (editId.value) await userApi.update(editId.value, form)
      else await userApi.create(form)
      ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
    } finally { saving.value = false }
  })
}

async function resetPwd(row: any) {
  const { value } = await ElMessageBox.prompt(`请输入用户【${row.realName}】的新密码`, '重置密码', {
    inputType: 'password', inputPattern: /^.{8,}$/, inputErrorMessage: '密码至少8位'
  })
  await userApi.resetPassword(row.id, value)
  ElMessage.success('密码重置成功')
}

async function toggleStatus(row: any) {
  const newStatus = row.status ? 0 : 1
  await userApi.toggleStatus(row.id, newStatus)
  row.status = newStatus
  ElMessage.success(`${newStatus ? '启用' : '禁用'}成功`)
}
</script>

<style scoped>
.page-container {}
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
</style>
