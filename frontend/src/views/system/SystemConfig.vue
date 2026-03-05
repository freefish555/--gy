<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">系统参数设置</span>
          <div>
            <el-button type="primary" :icon="Key" @click="showSm4Dialog = true">SM4密钥管理</el-button>
            <el-button type="success" :icon="Check" @click="batchSave" :loading="saving">保存设置</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="configTab">
        <el-tab-pane label="基础设置" name="basic">
          <el-form label-width="160px" style="max-width:700px; padding:16px 0">
            <el-form-item label="系统名称">
              <el-input v-model="configs.SYS_NAME" />
            </el-form-item>
            <el-form-item label="会话超时(分钟)">
              <el-input-number v-model="configs.SESSION_TIMEOUT" :min="5" :max="480" />
            </el-form-item>
            <el-form-item label="日志保留天数">
              <el-input-number v-model="configs.LOG_RETENTION_DAYS" :min="7" :max="365" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="密码策略" name="password">
          <el-form label-width="160px" style="max-width:700px; padding:16px 0">
            <el-form-item label="最小密码长度">
              <el-input-number v-model="configs.PWD_MIN_LENGTH" :min="6" :max="32" />
            </el-form-item>
            <el-form-item label="要求大写字母">
              <el-switch v-model="configs.PWD_REQUIRE_UPPER" :active-value="'true'" :inactive-value="'false'" />
            </el-form-item>
            <el-form-item label="要求小写字母">
              <el-switch v-model="configs.PWD_REQUIRE_LOWER" :active-value="'true'" :inactive-value="'false'" />
            </el-form-item>
            <el-form-item label="要求数字">
              <el-switch v-model="configs.PWD_REQUIRE_NUMBER" :active-value="'true'" :inactive-value="'false'" />
            </el-form-item>
            <el-form-item label="要求特殊字符">
              <el-switch v-model="configs.PWD_REQUIRE_SPECIAL" :active-value="'true'" :inactive-value="'false'" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="登录策略" name="login">
          <el-form label-width="160px" style="max-width:700px; padding:16px 0">
            <el-form-item label="最大失败次数">
              <el-input-number v-model="configs.LOGIN_MAX_FAIL" :min="3" :max="20" />
              <span class="form-hint">超过此次数账号将被锁定</span>
            </el-form-item>
            <el-form-item label="锁定时长(分钟)">
              <el-input-number v-model="configs.LOGIN_LOCK_MINUTES" :min="5" :max="1440" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- SM4密钥管理弹窗 -->
    <el-dialog v-model="showSm4Dialog" title="SM4加密密钥管理" width="500px">
      <el-alert type="warning" :closable="false" style="margin-bottom:16px"
        title="警告：修改SM4密钥将导致已加密的历史数据无法解密，请谨慎操作！" />
      <el-form :model="sm4Form" label-width="110px">
        <el-form-item label="当前密钥状态">
          <el-tag :type="sm4Configured ? 'success' : 'warning'">
            {{ sm4Configured ? '已配置自定义密钥' : '使用默认开发密钥' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新SM4密钥">
          <el-input v-model="sm4Form.key" placeholder="请输入32位十六进制密钥" maxlength="32" show-word-limit />
          <div class="form-hint">例: 0123456789abcdef0123456789abcdef（32位十六进制）</div>
        </el-form-item>
        <el-form-item label="确认密钥">
          <el-input v-model="sm4Form.keyConfirm" placeholder="再次输入密钥以确认" maxlength="32" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSm4Dialog=false">取消</el-button>
        <el-button type="primary" :loading="savingSm4" @click="saveSm4Key">确认设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Key, Check } from '@element-plus/icons-vue'
import { sysConfigApi } from '@/api/system'

const configTab = ref('basic')
const saving = ref(false)
const showSm4Dialog = ref(false)
const savingSm4 = ref(false)
const sm4Configured = ref(false)

const configs = reactive<Record<string, any>>({
  SYS_NAME: '网络安全等级保护测评管理系统',
  SESSION_TIMEOUT: 480,
  LOG_RETENTION_DAYS: 90,
  PWD_MIN_LENGTH: 8,
  PWD_REQUIRE_UPPER: 'true',
  PWD_REQUIRE_LOWER: 'true',
  PWD_REQUIRE_NUMBER: 'true',
  PWD_REQUIRE_SPECIAL: 'false',
  LOGIN_MAX_FAIL: 5,
  LOGIN_LOCK_MINUTES: 30,
})

const sm4Form = reactive({ key: '', keyConfirm: '' })

onMounted(async () => {
  try {
    const res: any = await sysConfigApi.list()
    const list: any[] = res.data || []
    list.forEach((item: any) => {
      if (item.configKey in configs) {
        configs[item.configKey] = item.configValue
      }
    })
  } catch {}
  try {
    const r: any = await sysConfigApi.getSm4Status()
    sm4Configured.value = r.data?.configured || false
  } catch {}
})

async function batchSave() {
  saving.value = true
  try {
    const list = Object.entries(configs).map(([key, value]) => ({ key, value: String(value) }))
    await sysConfigApi.batchSave(list)
    ElMessage.success('设置已保存')
  } finally { saving.value = false }
}

async function saveSm4Key() {
  if (!sm4Form.key || sm4Form.key.length !== 32) {
    ElMessage.warning('SM4密钥必须为32位十六进制字符串')
    return
  }
  if (sm4Form.key !== sm4Form.keyConfirm) {
    ElMessage.warning('两次输入的密钥不一致')
    return
  }
  await ElMessageBox.confirm('确认要更新SM4加密密钥吗？此操作不可逆！', '高危操作', { type: 'error' })
  savingSm4.value = true
  try {
    await sysConfigApi.setSm4Key({ key: sm4Form.key })
    ElMessage.success('SM4密钥设置成功')
    sm4Configured.value = true
    showSm4Dialog.value = false
    Object.assign(sm4Form, { key: '', keyConfirm: '' })
  } finally { savingSm4.value = false }
}
</script>

<style scoped>
.page-container {}
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
.form-hint { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
