<template>
  <router-view v-if="$route.path === '/login'" />
  <a-layout v-else class="admin-layout">
    <a-layout-sider class="sider">
      <div class="logo">网管通</div>
      <a-menu :selected-keys="[$route.path]" @menu-item-click="goPage">
        <a-menu-item key="/dashboard">首页</a-menu-item>
        <a-menu-item key="/member">会员管理</a-menu-item>
        <a-menu-item key="/computer">电脑管理</a-menu-item>
        <a-menu-item key="/reservation">预约管理</a-menu-item>
        <a-menu-item key="/recharge">充值管理</a-menu-item>
        <a-menu-item key="/online">上机管理</a-menu-item>
        <a-menu-item key="/food">网吧点餐</a-menu-item>
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
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const admin = computed(() => {
  const text = localStorage.getItem('admin')
  return text ? JSON.parse(text) : null
})

function goPage(path) {
  router.push(path)
}

function logout() {
  localStorage.removeItem('admin')
  router.push('/login')
}
</script>

<style scoped>
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
