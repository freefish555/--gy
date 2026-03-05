<template>
  <div class="login-container">
    <div class="login-card">
      <!-- Logo区域 -->
      <div class="login-header">
        <div class="logo-icon">
          <el-icon :size="40" color="#409EFF"><Lock /></el-icon>
        </div>
        <h1 class="system-title">网络安全等级保护测评</h1>
        <p class="system-subtitle">项目管理及材料规定管理系统</p>
      </div>

      <!-- 普通登录表单 -->
      <el-form
        v-if="!showTotp"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <!-- 验证码（根据配置显示） -->
        <el-form-item v-if="showCaptcha" prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captchaCode"
              placeholder="请输入验证码"
              style="flex:1"
            />
            <img
              :src="captchaImg"
              class="captcha-img"
              @click="getCaptcha"
              title="点击刷新验证码"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- TOTP二次验证 -->
      <div v-else class="totp-form">
        <div class="totp-tip">
          <el-icon color="#409EFF" :size="20"><Key /></el-icon>
          <span>请输入 Google Authenticator 中的6位动态验证码</span>
        </div>
        <el-input
          v-model="totpCode"
          placeholder="6位动态验证码"
          maxlength="6"
          size="large"
          class="totp-input"
          :prefix-icon="Key"
          @keyup.enter="handleTotpVerify"
        />
        <el-button
          type="primary"
          :loading="loading"
          class="login-btn"
          @click="handleTotpVerify"
          style="margin-top:16px"
        >
          验 证
        </el-button>
        <el-button text @click="showTotp=false" style="margin-top:8px; width:100%">
          返回登录
        </el-button>
      </div>
    </div>

    <!-- 版权信息 -->
    <div class="login-footer">
      <span>南京国云电力有限公司 © 2026 版权所有</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref()
const loading = ref(false)
const showCaptcha = ref(false)
const showTotp = ref(false)
const captchaImg = ref('')
const captchaId = ref('')
const totpCode = ref('')
const tempToken = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captchaCode: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: showCaptcha.value, message: '请输入验证码', trigger: 'blur' }]
}

onMounted(() => {
  getCaptcha()
})

async function getCaptcha() {
  try {
    const res: any = await request.get('/auth/captcha')
    captchaImg.value = res.data.captchaImg
    captchaId.value = res.data.captchaId
    showCaptcha.value = true
  } catch {
    showCaptcha.value = false
  }
}

async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    loading.value = true
    try {
      const result = await authStore.login(
        loginForm.username,
        loginForm.password,
        loginForm.captchaCode,
        captchaId.value
      )
      if (result.requireTotp) {
        tempToken.value = authStore.token
        showTotp.value = true
      } else {
        ElMessage.success('登录成功')
        if (result.firstLogin) {
          ElMessage.warning('首次登录，请先修改密码', { duration: 5000 })
        }
        const redirect = (route.query.redirect as string) || '/'
        router.push(redirect)
      }
    } catch {
      getCaptcha()
    } finally {
      loading.value = false
    }
  })
}

async function handleTotpVerify() {
  if (!totpCode.value || totpCode.value.length !== 6) {
    ElMessage.warning('请输入6位动态验证码')
    return
  }
  loading.value = true
  try {
    await authStore.verifyTotp(tempToken.value, parseInt(totpCode.value))
    ElMessage.success('验证成功，登录中...')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    totpCode.value = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a237e 0%, #0d47a1 50%, #01579b 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 420px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #409EFF, #1a237e);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.logo-icon .el-icon { color: #fff !important; }

.system-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a237e;
  margin-bottom: 6px;
}

.system-subtitle {
  font-size: 13px;
  color: #909399;
}

.login-form { width: 100%; }

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
  align-items: center;
}

.captcha-img {
  width: 110px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  flex-shrink: 0;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 2px;
}

.totp-form {
  text-align: center;
}

.totp-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}

.totp-input { width: 100%; letter-spacing: 4px; }

.login-footer {
  margin-top: 24px;
  color: rgba(255,255,255,0.6);
  font-size: 12px;
}
</style>
