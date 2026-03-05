import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus, { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './assets/styles/main.css'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(ElementPlus, { locale: zhCn })
app.use(router)

const mountedApp = app.mount('#app')

// ✅ Element Plus 全局消息组件需要手动设置 appContext
// 这样在路由切换过程中 ElMessage/ElMessageBox 能正确获取 Vue app 上下文
// 参考：https://element-plus.org/en-US/guide/quickstart.html#global-configuration
ElMessage._context = app._context
ElMessageBox._context = app._context
ElNotification._context = app._context
