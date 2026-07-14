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
import Coupon from '../views/coupon/index.vue'
import Promotion from '../views/promotion/index.vue'
import Service from '../views/service/index.vue'

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
  { path: '/admin/voucher', component: Recharge },
  { path: '/admin/online', component: Online },
  { path: '/admin/food', component: Food },
  { path: '/admin/coupon', component: Coupon },
  { path: '/admin/promotion', component: Promotion },
  { path: '/admin/service', component: Service },
  { path: '/admin/reservation', component: Reservation },
  { path: '/login', redirect: '/admin/login' },
  { path: '/dashboard', redirect: '/admin/dashboard' },
  { path: '/member', redirect: '/admin/member' },
  { path: '/computer', redirect: '/admin/computer' },
  { path: '/recharge', redirect: '/admin/recharge' },
  { path: '/voucher', redirect: '/admin/voucher' },
  { path: '/online', redirect: '/admin/online' },
  { path: '/food', redirect: '/admin/food' },
  { path: '/coupon', redirect: '/admin/coupon' },
  { path: '/promotion', redirect: '/admin/promotion' },
  { path: '/service', redirect: '/admin/service' },
  { path: '/reservation', redirect: '/admin/reservation' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const admin = localStorage.getItem('admin')
  const user = localStorage.getItem('user')
  if (to.path.startsWith('/admin') && to.path !== '/admin/login' && !admin) {
    next('/admin/login')
  } else if (to.path === '/user' && !user) {
    next('/user/login')
  } else {
    next()
  }
})

export default router
