<template>
  <!-- 登录页和用户端属于公开页面，直接显示当前路由组件，不套用管理员侧边栏。 -->
  <router-view v-if="publicPage" />
  <!-- 后台页面统一使用左侧菜单 + 顶部栏 + 内容区的管理系统布局。 -->
  <a-layout v-else class="admin-layout">
    <a-layout-sider class="sider">
      <div class="logo">网管通</div>
      <a-menu :selected-keys="[$route.path]" @menu-item-click="goPage">
        <a-menu-item key="/admin/dashboard">首页</a-menu-item>
        <a-menu-item key="/admin/member">会员管理</a-menu-item>
        <a-menu-item key="/admin/computer">电脑管理</a-menu-item>
        <a-menu-item key="/admin/reservation">预约管理</a-menu-item>
        <a-menu-item key="/admin/recharge">充值管理</a-menu-item>
        <a-menu-item key="/admin/online">上机管理</a-menu-item>
        <a-menu-item key="/admin/food">网吧点餐</a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="header">
        <div class="title">网管通 —— 电竞网吧运营管理系统</div>
        <div class="admin-info">
          <span>{{ admin?.realName || '管理员' }}</span>
          <a-button size="small" @click="logout">退出登录</a-button>
        </div>
      </a-layout-header>
      <a-layout-content class="content">
        <!-- router-view 会根据当前网址显示对应的后台页面组件。 -->
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// /admin 下除登录页以外都使用后台布局；用户端和两个登录页只显示自身页面。
const publicPage = computed(() => !router.currentRoute.value.path.startsWith('/admin') || router.currentRoute.value.path === '/admin/login')

// 管理员登录成功后会把对象转成 JSON 保存；computed 可让模板读取当前管理员姓名。
const admin = computed(() => {
  const text = localStorage.getItem('admin')
  return text ? JSON.parse(text) : null
})

function goPage(path) {
  // 菜单 key 本身就是路由地址，点击后直接跳转。
  router.push(path)
}

function logout() {
  // 清除本地登录凭证后回到管理员登录页。
  localStorage.removeItem('admin')
  router.push('/admin/login')
}
</script>

<style scoped>
/* scoped 表示这些样式只作用于 App.vue 生成的后台布局，不污染其他页面。 */
.admin-layout {
  min-height: 100vh;
}

.sider {
  min-height: 100vh;
  background: #ffffff;
  border-right: 1px solid #e5e6eb;
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  padding-left: 24px;
  color: #165dff;
  font-size: 20px;
  font-weight: 700;
}

.header {
  height: 56px;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e5e6eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content {
  padding: 24px;
  background: #f2f3f5;
}
</style>
