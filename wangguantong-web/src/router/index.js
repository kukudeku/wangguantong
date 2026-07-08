import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/login/index.vue'
import Dashboard from '../views/dashboard/index.vue'
import Member from '../views/member/index.vue'
import Computer from '../views/computer/index.vue'
import Recharge from '../views/recharge/index.vue'
import Online from '../views/online/index.vue'
import Food from '../views/food/index.vue'
import Reservation from '../views/reservation/index.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', component: Login },
  { path: '/dashboard', component: Dashboard },
  { path: '/member', component: Member },
  { path: '/computer', component: Computer },
  { path: '/recharge', component: Recharge },
  { path: '/online', component: Online },
  { path: '/food', component: Food },
  { path: '/reservation', component: Reservation }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const admin = localStorage.getItem('admin')
  if (to.path !== '/login' && !admin) {
    next('/login')
  } else {
    next()
  }
})

export default router
