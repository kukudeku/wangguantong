import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/login/index.vue'
import Dashboard from '../views/dashboard/index.vue'
import Member from '../views/member/index.vue'
import Computer from '../views/computer/index.vue'
import Recharge from '../views/recharge/index.vue'
import Online from '../views/online/index.vue'
import Food from '../views/food/index.vue'
import Reservation from '../views/reservation/index.vue'
import User from '../views/user/index.vue'
import UserLogin from '../views/user-login/index.vue'

// 路由表决定“某个网址显示哪个 Vue 页面”。
// 根路径默认进入用户端；管理员后台统一放在 /admin 前缀下。
const routes = [
  { path: '/', redirect: '/user' },
  { path: '/user/login', component: UserLogin },
  { path: '/user', component: User },
  { path: '/admin', redirect: '/admin/dashboard' },
  { path: '/admin/login', component: Login },
  { path: '/admin/dashboard', component: Dashboard },
  { path: '/admin/member', component: Member },
  { path: '/admin/computer', component: Computer },
  { path: '/admin/recharge', component: Recharge },
  { path: '/admin/online', component: Online },
  { path: '/admin/food', component: Food },
  { path: '/admin/reservation', component: Reservation },
  // 以下是旧地址兼容跳转，保证以前收藏的链接仍可使用。
  { path: '/login', redirect: '/admin/login' },
  { path: '/dashboard', redirect: '/admin/dashboard' },
  { path: '/member', redirect: '/admin/member' },
  { path: '/computer', redirect: '/admin/computer' },
  { path: '/recharge', redirect: '/admin/recharge' },
  { path: '/online', redirect: '/admin/online' },
  { path: '/food', redirect: '/admin/food' },
  { path: '/reservation', redirect: '/admin/reservation' }
]

const router = createRouter({
  // HTML5 history 模式会生成 /user 这种正常地址，而不是 /#/user。
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  // 全局路由守卫：每次切换页面前都检查浏览器本地是否保存了对应登录用户。
  const admin = localStorage.getItem('admin')
  const user = localStorage.getItem('user')
  if (to.path.startsWith('/admin') && to.path !== '/admin/login' && !admin) {
    // 未登录管理员访问后台业务页时，强制回到管理员登录页。
    next('/admin/login')
  } else if (to.path === '/user' && !user) {
    // 未登录普通用户访问自助终端时，强制回到用户登录页。
    next('/user/login')
  } else {
    // 已满足访问条件，继续完成本次路由跳转。
    next()
  }
})

export default router
