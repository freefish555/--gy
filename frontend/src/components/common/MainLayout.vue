<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="layout-aside">
      <!-- Logo -->
      <div class="sidebar-logo">
        <img src="/new_logo_2.png" alt="logo" class="sidebar-logo-full" />
      </div>

      <!-- 菜单 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        background-color="#001529"
        text-color="#b0b7c0"
        active-text-color="#ffffff"
        @select="handleMenuSelect"
        class="sidebar-menu"
      >
        <!-- 项目管理 -->
        <el-sub-menu index="project">
          <template #title>
            <el-icon><Folder /></el-icon>
            <span>项目管理</span>
          </template>
          <el-menu-item index="/project/list">
            <el-icon><List /></el-icon>
            <span>项目总览</span>
          </el-menu-item>
          <el-menu-item
            index="/project/create"
            v-if="authStore.hasPermission('project:create')"
          >
            <el-icon><Plus /></el-icon>
            <span>项目录入</span>
          </el-menu-item>
          <el-menu-item
            index="/project/stats"
            v-if="authStore.hasPermission('project:stats')"
          >
            <el-icon><TrendCharts /></el-icon>
            <span>项目统计</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 归档管理 -->
        <el-sub-menu
          index="archive"
          v-if="authStore.hasPermission('archive:create')"
        >
          <template #title>
            <el-icon><Document /></el-icon>
            <span>归档管理</span>
          </template>
          <el-menu-item index="/archive">
            <el-icon><Files /></el-icon>
            <span>归档材料制作</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 系统设置 -->
        <el-sub-menu
          index="system"
          v-if="authStore.hasAnyPermission('system:config','system:user','system:staff','system:dict','archive:template')"
        >
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item v-if="authStore.hasPermission('system:config')" index="/system/config">
            <el-icon><Tools /></el-icon>
            <span>系统参数设置</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.hasPermission('system:user')" index="/system/user">
            <el-icon><UserFilled /></el-icon>
            <span>管理员设置</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.hasPermission('system:staff')" index="/system/staff">
            <el-icon><Avatar /></el-icon>
            <span>项目人员清单</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.hasPermission('system:dict')" index="/system/dict">
            <el-icon><Menu /></el-icon>
            <span>字典管理</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.hasPermission('archive:template')" index="/system/template">
            <el-icon><CopyDocument /></el-icon>
            <span>归档模板管理</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 日志管理 -->
        <el-sub-menu
          index="log"
          v-if="authStore.hasAnyPermission('log:login:view','log:operation:view','log:server:config')"
        >
          <template #title>
            <el-icon><Document /></el-icon>
            <span>日志管理</span>
          </template>
          <el-menu-item v-if="authStore.hasPermission('log:login:view')" index="/log/login">
            <el-icon><Key /></el-icon>
            <span>登录日志</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.hasPermission('log:operation:view')" index="/log/operation">
            <el-icon><Tickets /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.hasPermission('log:server:config')" index="/log/server">
            <el-icon><Connection /></el-icon>
            <span>日志服务器设置</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon
            :size="20"
            class="collapse-btn"
            @click="isCollapsed = !isCollapsed"
          >
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 用户信息 -->
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" style="background:#409EFF">
                {{ authStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ authStore.userInfo?.realName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item command="totp">
                  <el-icon><Key /></el-icon>绑定双因子认证
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" style="color:#f56c6c">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>

  <!-- 个人信息弹窗 -->
  <el-dialog v-model="showProfile" title="个人信息" width="420px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="用户名">{{ authStore.userInfo?.username }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ authStore.userInfo?.realName }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ authStore.userInfo?.roleName }}</el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button type="primary" @click="showProfile=false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 双因子认证弹窗 -->
  <el-dialog v-model="showTotpDialog" title="绑定双因子认证" width="480px" @close="resetTotpDialog">
    <div v-if="totpStep === 1" style="text-align:center">
      <el-alert type="info" :closable="false" style="margin-bottom:16px;text-align:left">
        请使用 Google Authenticator 或 Authy 扫描以下二维码，然后输入验证码完成绑定。
      </el-alert>
      <img :src="totpQrCode" style="width:200px;height:200px;border:1px solid #eee" />
      <p style="margin-top:8px;font-size:12px;color:#909399">手动输入密钥：{{ totpSecret }}</p>
      <el-form style="margin-top:16px">
        <el-form-item label="验证码" label-width="70px">
          <el-input v-model="totpConfirmCode" placeholder="请输入6位验证码" maxlength="6" style="width:200px" />
        </el-form-item>
      </el-form>
    </div>
    <div v-else-if="totpStep === 0" style="text-align:center;color:#909399;padding:20px">
      <p>正在加载二维码...</p>
    </div>
    <template #footer>
      <el-button @click="showTotpDialog=false">取消</el-button>
      <el-button type="primary" :loading="totpLoading" @click="confirmTotpBind" :disabled="!totpConfirmCode || totpConfirmCode.length !== 6">
        确认绑定
      </el-button>
    </template>
  </el-dialog>

  <!-- 修改密码弹窗 -->
  <el-dialog v-model="showChangePassword" title="修改密码" width="420px">
    <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showChangePassword=false">取消</el-button>
      <el-button type="primary" :loading="pwdLoading" @click="submitChangePassword">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Folder, List, Plus, TrendCharts, Document, Files, Setting, Tools,
  UserFilled, Avatar, Menu, CopyDocument, Key, Tickets,
  Connection, Fold, Expand, ArrowDown, User, Lock, SwitchButton
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import request from '@/utils/request'
import QRCode from 'qrcode'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isCollapsed = ref(false)

// 菜单导航（替代 el-menu 的 router prop，避免注入问题）
function handleMenuSelect(index: string) {
  if (index && index.startsWith('/')) {
    router.push(index)
  }
}

// 当前激活菜单
const activeMenu = computed(() => route.path)

// 面包屑
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(r => r.meta?.title)
  return matched.map(r => ({ path: r.path, title: r.meta?.title }))
})

// 用户操作
const showChangePassword = ref(false)
const pwdLoading = ref(false)
const passwordFormRef = ref()

// 首次登录自动弹出修改密码
onMounted(() => {
  if (authStore.userInfo?.firstLogin) {
    setTimeout(() => {
      ElMessage.warning({ message: '首次登录，请先修改密码', duration: 4000 })
      showChangePassword.value = true
    }, 800)
  }
})
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码' }],
  newPassword: [
    { required: true, message: '请输入新密码' },
    { min: 8, message: '密码长度不少于8位' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次密码不一致'))
        } else callback()
      }
    }
  ]
}

// 个人信息弹窗
const showProfile = ref(false)
const showTotpDialog = ref(false)
const totpQrCode = ref('')
const totpSecret = ref('')
const totpStep = ref(0)
const totpConfirmCode = ref('')
const totpLoading = ref(false)

function resetTotpDialog() {
  totpQrCode.value = ''
  totpSecret.value = ''
  totpStep.value = 0
  totpConfirmCode.value = ''
}

function handleUserCommand(cmd: string) {
  switch (cmd) {
    case 'profile':
      showProfile.value = true
      break
    case 'password':
      showChangePassword.value = true
      break
    case 'totp':
      loadTotpQr()
      break
    case 'logout':
      ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
        .then(async () => {
          await request.post('/auth/logout').catch(() => {})
          authStore.logout()
          router.push('/login')
        })
      break
  }
}

async function loadTotpQr() {
  resetTotpDialog()
  showTotpDialog.value = true
  try {
    const res: any = await request.post('/auth/totp/bind')
    const qrUrl = res.data?.qrUrl || res.data?.qrCode || ''
    totpSecret.value = res.data?.secret || ''
    // 使用 qrcode 库将 otpauth:// URL 生成 base64 图片
    if (qrUrl) {
      totpQrCode.value = await QRCode.toDataURL(qrUrl, { width: 200, margin: 1 })
    } else {
      totpQrCode.value = ''
    }
    totpStep.value = 1
  } catch (e: any) {
    showTotpDialog.value = false
    ElMessage.error('获取双因子认证信息失败: ' + (e.message || '请联系管理员'))
  }
}

async function confirmTotpBind() {
  if (!totpConfirmCode.value || totpConfirmCode.value.length !== 6) {
    ElMessage.warning('请输入6位验证码')
    return
  }
  totpLoading.value = true
  try {
    await request.post('/auth/totp/confirm', {
      secret: totpSecret.value,
      code: parseInt(totpConfirmCode.value)
    })
    ElMessage.success('双因子认证绑定成功')
    showTotpDialog.value = false
  } catch (e: any) {
    ElMessage.error('绑定失败: ' + (e.message || '验证码错误，请重试'))
  } finally {
    totpLoading.value = false
  }
}

async function submitChangePassword() {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    pwdLoading.value = true
    try {
      await request.post('/auth/change-password', {
        oldPassword: passwordForm.value.oldPassword,
        newPassword: passwordForm.value.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      showChangePassword.value = false
      authStore.logout()
      router.push('/login')
    } finally {
      pwdLoading.value = false
    }
  })
}
</script>

<style scoped>
.layout-container { height: 100vh; overflow: hidden; }

.layout-aside {
  background: #001529;
  transition: width 0.3s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: #ffffff;
  flex-shrink: 0;
  padding: 8px 12px;
}

.sidebar-logo-full {
  max-height: 44px;
  max-width: 180px;
  width: auto;
  object-fit: contain;
}

.sidebar-logo-img {
  max-height: 44px;
  max-width: 44px;
  width: auto;
  object-fit: contain;
  border-radius: 6px;
}

.sidebar-logo-icon {
  width: 36px;
  height: 36px;
  object-fit: contain;
  border-radius: 4px;
}

.logo-text {
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.layout-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e8ecf0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}

.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { cursor: pointer; color: #606266; }
.collapse-btn:hover { color: #409EFF; }

.header-right { display: flex; align-items: center; gap: 16px; }

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.user-info:hover { background: #f5f7fa; }
.username { font-size: 14px; color: #333; }

.layout-main {
  background: #f0f2f5;
  padding: 16px;
  overflow-y: auto;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
