<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">日志服务器设置</span>
          <el-button type="primary" :icon="Check" @click="saveConfig" :loading="saving">保存设置</el-button>
        </div>
      </template>
      <el-form :model="form" label-width="150px" style="max-width:600px; padding:16px 0">
        <el-form-item label="启用远程日志">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <template v-if="form.enabled">
          <el-form-item label="服务器地址">
            <el-input v-model="form.host" placeholder="如: 192.168.1.100" />
          </el-form-item>
          <el-form-item label="端口">
            <el-input-number v-model="form.port" :min="1" :max="65535" />
          </el-form-item>
          <el-form-item label="协议">
            <el-select v-model="form.protocol">
              <el-option label="UDP (Syslog)" value="UDP" />
              <el-option label="TCP" value="TCP" />
            </el-select>
          </el-form-item>
          <el-form-item label="日志格式">
            <el-select v-model="form.format">
              <el-option label="Syslog (RFC 5424)" value="syslog" />
              <el-option label="JSON" value="json" />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { logApi } from '@/api/system'

const saving = ref(false)
const form = reactive({ enabled: false, host: '', port: 514, protocol: 'UDP', format: 'syslog' })

onMounted(async () => {
  try { const r: any = await logApi.serverConfig(); Object.assign(form, r.data || {}) } catch {}
})

async function saveConfig() {
  saving.value = true
  try { await logApi.saveServerConfig(form); ElMessage.success('保存成功') }
  finally { saving.value = false }
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
</style>
