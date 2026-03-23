import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export interface UserInfo {
  userId: number
  username: string
  realName: string
  roleCode: string
  roleName: string
  permissions: string[]
  firstLogin: boolean
  staffId?: number | null
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(
    localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')!) : null
  )

  const isLoggedIn = computed(() => !!token.value)
  const permissions = computed(() => userInfo.value?.permissions || [])

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function hasPermission(perm: string): boolean {
    return permissions.value.includes(perm) ||
           userInfo.value?.roleCode === 'SUPER_ADMIN'
  }

  function hasAnyPermission(...perms: string[]): boolean {
    return perms.some(p => hasPermission(p))
  }

  // 登录
  async function login(username: string, password: string, captchaCode?: string, captchaId?: string) {
    const res: any = await request.post('/auth/login', {
      username, password, captchaCode, captchaId
    })
    const data = res.data
    if (data.requireTotp) {
      // 需要TOTP验证，返回临时token
      setToken(data.token)
      return { requireTotp: true, token: data.token }
    }
    setToken(data.token)
    setUserInfo({
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      roleCode: data.roleCode,
      roleName: data.roleName,
      permissions: data.permissions,
      firstLogin: data.firstLogin,
      staffId: data.staffId ?? null
    })
    return { requireTotp: false, firstLogin: data.firstLogin }
  }

  // TOTP验证
  async function verifyTotp(tempToken: string, totpCode: number) {
    const res: any = await request.post('/auth/totp/verify', { tempToken, totpCode })
    const data = res.data
    setToken(data.token)
    setUserInfo({
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      roleCode: data.roleCode,
      roleName: data.roleName,
      permissions: data.permissions,
      firstLogin: data.firstLogin,
      staffId: data.staffId ?? null
    })
    return data
  }

  return {
    token, userInfo, isLoggedIn, permissions,
    setToken, setUserInfo, logout,
    hasPermission, hasAnyPermission,
    login, verifyTotp
  }
})
