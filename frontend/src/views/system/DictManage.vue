<template>
  <div class="page-container">
    <el-row :gutter="16">
      <!-- 字典类型列表 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>字典类型</span>
              <el-button type="primary" :icon="Plus" size="small" @click="openTypeDialog()">新增</el-button>
            </div>
          </template>
          <el-input v-model="typeSearch" placeholder="搜索字典名称" clearable :prefix-icon="Search"
            style="margin-bottom:8px" @input="filterTypes" />
          <el-table :data="filteredTypes" @row-click="selectType" highlight-current-row
            :row-class-name="({row}) => row.id === selectedType?.id ? 'active-row' : ''">
            <el-table-column label="字典编码" prop="dictCode" />
            <el-table-column label="字典名称" prop="dictName" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="danger" link size="small" :icon="Delete"
                  @click.stop="deleteType(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 字典项列表 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ selectedType ? `【${selectedType.dictName}】字典项` : '字典项（请先选择字典类型）' }}</span>
              <el-button type="primary" :icon="Plus" size="small" :disabled="!selectedType"
                @click="openItemDialog()">新增</el-button>
            </div>
          </template>
          <el-table :data="itemList" border v-loading="itemLoading">
            <el-table-column label="值" prop="itemValue" width="100" />
            <el-table-column label="标签" prop="itemLabel" min-width="150" />
            <el-table-column label="排序" prop="sortOrder" width="70" align="center" />
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status ? 'success' : 'danger'" size="small">{{ row.status ? '启用' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <el-button type="primary" link size="small" :icon="Edit" @click="openItemDialog(row)">编辑</el-button>
                <el-button type="danger" link size="small" :icon="Delete" @click="deleteItem(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 字典类型弹窗 -->
    <el-dialog v-model="typeDialog" title="新增字典类型" width="400px">
      <el-form :model="typeForm" :rules="typeRules" ref="typeFormRef" label-width="90px">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="typeForm.dictCode" placeholder="如: project_type" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="如: 项目类型" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialog=false">取消</el-button>
        <el-button type="primary" @click="saveType">保存</el-button>
      </template>
    </el-dialog>

    <!-- 字典项弹窗 -->
    <el-dialog v-model="itemDialog" :title="itemEditId ? '编辑字典项' : '新增字典项'" width="400px">
      <el-form :model="itemForm" :rules="itemRules" ref="itemFormRef" label-width="80px">
        <el-form-item label="值" prop="itemValue">
          <el-input v-model="itemForm.itemValue" placeholder="存储值" />
        </el-form-item>
        <el-form-item label="标签" prop="itemLabel">
          <el-input v-model="itemForm.itemLabel" placeholder="显示文字" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="itemForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="itemForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialog=false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { dictApi } from '@/api/system'

const typeSearch = ref('')
const typeList = ref<any[]>([])
const filteredTypes = computed(() =>
  typeSearch.value ? typeList.value.filter(t => t.dictName.includes(typeSearch.value) || t.dictCode.includes(typeSearch.value)) : typeList.value
)
const selectedType = ref<any>(null)
const itemList = ref<any[]>([])
const itemLoading = ref(false)

const typeDialog = ref(false)
const typeFormRef = ref()
const typeForm = reactive({ dictCode: '', dictName: '', remark: '' })
const typeRules = { dictCode: [{ required: true }], dictName: [{ required: true }] }

const itemDialog = ref(false)
const itemEditId = ref<number | null>(null)
const itemFormRef = ref()
const itemForm = reactive({ itemValue: '', itemLabel: '', sortOrder: 0, status: 1 })
const itemRules = { itemValue: [{ required: true }], itemLabel: [{ required: true }] }

function filterTypes() {}

onMounted(loadTypes)

async function loadTypes() {
  try { const r: any = await dictApi.listTypes(); typeList.value = r.data?.records || r.data || [] } catch {}
}

function selectType(row: any) {
  selectedType.value = row
  loadItems()
}

async function loadItems() {
  if (!selectedType.value) return
  itemLoading.value = true
  try { const r: any = await dictApi.getItems(selectedType.value.dictCode); itemList.value = r.data || [] }
  finally { itemLoading.value = false }
}

function openTypeDialog() {
  Object.assign(typeForm, { dictCode: '', dictName: '', remark: '' })
  typeDialog.value = true
}

async function saveType() {
  await typeFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    await dictApi.createType(typeForm)
    ElMessage.success('新增成功'); typeDialog.value = false; loadTypes()
  })
}

async function deleteType(row: any) {
  await ElMessageBox.confirm(`确认删除字典【${row.dictName}】及其所有项吗？`, '警告', { type: 'warning' })
  await dictApi.deleteType(row.id)
  ElMessage.success('删除成功')
  if (selectedType.value?.id === row.id) { selectedType.value = null; itemList.value = [] }
  loadTypes()
}

function openItemDialog(row?: any) {
  itemEditId.value = row?.id || null
  Object.assign(itemForm, row || { itemValue: '', itemLabel: '', sortOrder: 0, status: 1 })
  itemDialog.value = true
}

async function saveItem() {
  await itemFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    // include dictCode so createItem can build the correct URL path
    const payload = { ...itemForm, dictId: selectedType.value.id, dictCode: selectedType.value.dictCode }
    if (itemEditId.value) await dictApi.updateItem(itemEditId.value, payload)
    else await dictApi.createItem(payload)
    ElMessage.success('保存成功'); itemDialog.value = false; loadItems()
  })
}

async function deleteItem(row: any) {
  await ElMessageBox.confirm(`确认删除字典项【${row.itemLabel}】？`, '警告', { type: 'warning' })
  await dictApi.deleteItem(row.id)
  ElMessage.success('删除成功'); loadItems()
}
</script>

<style scoped>
.page-container {}
.card-header { display: flex; justify-content: space-between; align-items: center; }
:deep(.active-row) { background: #ecf5ff !important; }
</style>
