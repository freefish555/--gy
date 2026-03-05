import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { ElMessage } from 'element-plus'

NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginPage.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/common/MainLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/project/list',
    children: [
      // ===== 项目管理 =====
      {
        path: '/project/list',
        name: 'ProjectList',
        component: () => import('@/views/project/ProjectList.vue'),
        meta: { title: '项目总览', icon: 'Folder', module: 'project' }
      },
      {
        path: '/project/create',
        name: 'ProjectCreate',
        component: () => import('@/views/project/ProjectForm.vue'),
        meta: { title: '项目新增', icon: 'Plus', module: 'project', perm: 'project:create' }
      },
      {
        path: '/project/edit/:id',
        name: 'ProjectEdit',
        component: () => import('@/views/project/ProjectForm.vue'),
        meta: { title: '项目编辑', module: 'project', hidden: true }
      },
      {
        path: '/project/detail/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/ProjectDetail.vue'),
        meta: { title: '项目详情', module: 'project', hidden: true }
      },
      {
        path: '/project/stats',
        name: 'ProjectStats',
        component: () => import('@/views/project/ProjectStats.vue'),
        meta: { title: '项目统计', icon: 'TrendCharts', module: 'project', perm: 'project:stats' }
      },
      // ===== 归档管理 =====
      {
        path: '/archive',
        name: 'Archive',
        component: () => import('@/views/archive/ArchivePage.vue'),
        meta: { title: '归档材料制作', icon: 'Document', module: 'archive', perm: 'archive:create' }
      },
      // ===== 系统设置 =====
      {
        path: '/system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/SystemConfig.vue'),
        meta: { title: '系统参数设置', icon: 'Setting', module: 'system', perm: 'system:config' }
      },
      {
        path: '/system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/UserManage.vue'),
        meta: { title: '管理员设置', icon: 'UserFilled', module: 'system', perm: 'system:user' }
      },
      {
        path: '/system/staff',
        name: 'SystemStaff',
        component: () => import('@/views/system/StaffManage.vue'),
        meta: { title: '项目人员清单', icon: 'Avatar', module: 'system', perm: 'system:staff' }
      },
      {
        path: '/system/device',
        name: 'SystemDevice',
        component: () => import('@/views/system/DeviceManage.vue'),
        meta: { title: '测评工具清单', icon: 'Monitor', module: 'system', perm: 'system:device' }
      },
      {
        path: '/system/dict',
        name: 'SystemDict',
        component: () => import('@/views/system/DictManage.vue'),
        meta: { title: '字典管理', icon: 'List', module: 'system', perm: 'system:dict' }
      },
      {
        path: '/system/template',
        name: 'ArchiveTemplate',
        component: () => import('@/views/system/TemplateManage.vue'),
        meta: { title: '归档模板管理', icon: 'Files', module: 'system', perm: 'archive:template' }
      },
      // ===== 日志管理 =====
      {
        path: '/log/login',
        name: 'LoginLog',
        component: () => import('@/views/log/LoginLog.vue'),
        meta: { title: '登录日志', icon: 'Key', module: 'log', perm: 'log:login:view' }
      },
      {
        path: '/log/operation',
        name: 'OperationLog',
        component: () => import('@/views/log/OperationLog.vue'),
        meta: { title: '操作日志', icon: 'Document', module: 'log', perm: 'log:operation:view' }
      },
      {
        path: '/log/server',
        name: 'LogServer',
        component: () => import('@/views/log/LogServer.vue'),
        meta: { title: '日志服务器设置', icon: 'Connection', module: 'log', perm: 'log:server:config' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/project/list'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  const authStore = useAuthStore()

  if (to.meta.requiresAuth === false) {
    if (authStore.isLoggedIn && to.name === 'Login') {
      next('/')
    } else {
      next()
    }
    return
  }

  if (!authStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 权限检查
  const perm = to.meta.perm as string
  if (perm && !authStore.hasPermission(perm)) {
    ElMessage.error('您没有访问该页面的权限')
    next('/')
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
