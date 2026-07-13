import { createApp } from 'vue'
import ArcoVue from '@arco-design/web-vue'
import '@arco-design/web-vue/dist/arco.css'
import './style.css'
import App from './App.vue'
import router from './router'

// 前端入口：创建 Vue 应用，安装 Arco Design 和路由插件，最后挂载到 index.html 的 #app 节点。
// 链式调用的顺序是：createApp 创建应用 -> use 安装插件 -> mount 渲染页面。
createApp(App).use(ArcoVue).use(router).mount('#app')
