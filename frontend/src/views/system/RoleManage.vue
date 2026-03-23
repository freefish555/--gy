<template>
  <div class="page-container">
    <el-row :gutter="16">
      <!-- 左侧：角色列表 -->
      <el-col :span="7">
        <el-card shadow="never" header="角色列表">
          <el-table
            :data="roles"
            highlight-current-row
            @current-change="handleRoleSelect"
            style="width:100%"
            size="small"
          >
            <el-table-column prop="roleName" label="角色名称" min-width="100" />
            <el-table-column prop="roleDesc" label="描述" min-width="120" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：权限配置 -->
      <el-col :span="17">
        <el-card shadow="never">
          <template #header>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <span>
                权限配置
                <el-tag v-if="currentRole" size="small" type="primary" style="margin-left:8px">
                  {{ currentRole.roleName }}
                </el-tag>
              </span>
              <el-button
                type="primary"
                size="small"
                :disabled="!currentRole || currentRole.roleCode === 'SUPER_ADMIN'"
                :loading="saving"
                @click="savePermissions"
              >保存权限</el-button>
            </div>
          </template>

          <div v-if="!currentRole" style="color:#909399;padding:20px;text-align:center">
            请在左侧选择一个角色
          </div>
          <div v-else-if="currentRole.roleCode === 'SUPER_ADMIN'" style="color:#909399;padding:20px;text-align:center">
            超级管理员拥有所有权限，不可修改
          </div>
          <div v-else>
            <div v-for="mod in permissionModules" :key="mod.module" style="margin-bottom:16px">
              <div class="module-header">
                <el-checkbox
                  :model-value="isModuleAllChecked(mod.module)"
                  :indeterminate="isModuleIndeterminate(mod.module)"
                  @change="(v: boolean) => toggleModule(mod.module, v)"
                >
                  <span style="font-weight:600;font-size:14px">{{ mod.label }}</span>
                </el-checkbox>
              </div>
              <div class="perm-list">
                <el-checkbox
                  v-for="p in mod.perms"
                  :key="p.id"
                  :label="p.perm_name || p.permName"
                  :model-value="selectedPermIds.includes(p.id)"
                  @change="(v: boolean) => togglePerm(p.id, v)"
                >
                  {{ p.perm_name || p.permName }}
                  <span style="color:#909399;font-size:11px"> ({{ p.perm_code || p.permCode }})</span>
                </el-checkbox>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const roles = ref<any[]>([])
const allPermissions = ref<any[]>([])
const currentRole = ref<any>(null)
const selectedPermIds = ref<number[]>([])
const saving = ref(false)

// 模块名称映射
const moduleLabels: Record<string, string> = {
  project: '项目管理',
  archive: '归档管理',
  system: '系统设置',
  log: '日志管理',
}

// 按模块分组权限
const permissionModules = computed(() => {
  const grouped: Record<string, any[]> = {}
  for (const p of allPermissions.value) {
    const mod = p.module || 'other'
    if (!grouped[mod]) grouped[mod] = []
    grouped[mod].push(p)
  }
  return Object.keys(grouped).map(mod => ({
    module: mod,
    label: moduleLabels[mod] || mod,
    perms: grouped[mod],
  }))
})

function isModuleAllChecked(module: string) {
  const perms = allPermissions.value.filter(p => p.module === module)
  return perms.length > 0 && perms.every(p => selectedPermIds.value.includes(p.id))
}

function isModuleIndeterminate(module: string) {
  const perms = allPermissions.value.filter(p => p.module === module)
  const checked = perms.filter(p => selectedPermIds.value.includes(p.id))
  return checked.length > 0 && checked.length < perms.length
}

function toggleModule(module: string, checked: boolean) {
  const perms = allPermissions.value.filter(p => p.module === module)
  if (checked) {
    const toAdd = perms.map(p => p.id).filter(id => !selectedPermIds.value.includes(id))
    selectedPermIds.value.push(...toAdd)
  } else {
    const toRemove = new Set(perms.map(p => p.id))
    selectedPermIds.value = selectedPermIds.value.filter(id => !toRemove.has(id))
  }
}

function togglePerm(permId: number, checked: boolean) {
  if (checked) {
    if (!selectedPermIds.value.includes(permId)) {
      selectedPermIds.value.push(permId)
    }
  } else {
    selectedPermIds.value = selectedPermIds.value.filter(id => id !== permId)
  }
}

function handleRoleSelect(role: any) {
  if (!role) return
  currentRole.value = role
  selectedPermIds.value = [...(role.permissionIds || [])]
}

async function savePermissions() {
  if (!currentRole.value) return
  saving.value = true
  try {
    await request.put(`/system/role/${currentRole.value.id}/permissions`, {
      permissionIds: selectedPermIds.value
    })
    ElMessage.success('权限保存成功')
    // 刷新角色列表以同步最新权限
    await loadData()
    // 重新选中当前角色
    const updated = roles.value.find(r => r.id === currentRole.value.id)
    if (updated) {
      currentRole.value = updated
      selectedPermIds.value = [...(updated.permissionIds || [])]
    }
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function loadData() {
  try {
    const [rolesRes, permsRes]: any[] = await Promise.all([
      request.get('/system/role/list'),
      request.get('/system/role/permissions'),
    ])
    roles.value = rolesRes.data || []
    allPermissions.value = permsRes.data || []
  } catch (e: any) {
    ElMessage.error('加载数据失败: ' + e.message)
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-container { padding: 0; }
.module-header {
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 8px;
  border-left: 3px solid #409EFF;
}
.perm-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 12px 8px;
}
.perm-list .el-checkbox {
  margin-right: 0;
  min-width: 220px;
}
</style>
