<template>
  <router-view v-if="publicPage" />

  <div v-else class="admin-shell" :class="{ collapsed }">
    <header class="admin-navbar">
      <div class="admin-brand">
        <div class="admin-brand-mark"><IconComputer /></div>
        <div v-show="!collapsed" class="admin-brand-copy">
          <strong>网管通</strong>
          <span>运营管理系统</span>
        </div>
      </div>

      <div class="admin-navbar-main">
        <button class="admin-icon-button" :title="collapsed ? '展开菜单' : '收起菜单'" @click="toggleCollapsed">
          <IconMenuUnfold v-if="collapsed" />
          <IconMenuFold v-else />
        </button>

        <div class="admin-navbar-title">
          <strong>{{ currentMenuTitle }}</strong>
          <span>网管通 · 电竞网吧运营管理系统</span>
        </div>

        <div class="admin-navbar-actions">
          <a-tooltip content="进入用户系统">
            <button class="admin-icon-button" title="进入用户系统" @click="openUserSystem">
              <IconLaunch />
            </button>
          </a-tooltip>

          <a-dropdown trigger="click" @select="handleAccountAction">
            <button class="admin-profile">
              <a-avatar :size="32" class="admin-avatar">{{ adminInitial }}</a-avatar>
              <span class="admin-profile-copy">
                <strong>{{ admin?.realName || '管理员' }}</strong>
                <small>系统管理员</small>
              </span>
              <IconDown class="admin-profile-arrow" />
            </button>
            <template #content>
              <a-doption value="user">
                <template #icon><IconLaunch /></template>
                进入用户系统
              </a-doption>
              <a-doption value="logout">
                <template #icon><IconPoweroff /></template>
                退出登录
              </a-doption>
            </template>
          </a-dropdown>
        </div>
      </div>
    </header>

    <aside class="admin-sider">
      <div v-show="!collapsed" class="admin-menu-label">功能导航</div>
      <a-menu
        v-model:open-keys="openKeys"
        class="admin-menu"
        :collapsed="collapsed"
        :selected-keys="[currentMenuKey]"
        :accordion="true"
        :auto-open-selected="true"
        :level-indent="20"
        :tooltip-props="{ position: 'right' }"
        @menu-item-click="goPage"
      >
        <a-sub-menu key="module-dashboard">
          <template #icon><IconDashboard /></template>
          <template #title>数据概览</template>
          <a-menu-item key="/admin/dashboard">首页看板</a-menu-item>
        </a-sub-menu>

        <a-sub-menu key="module-member">
          <template #icon><IconUserGroup /></template>
          <template #title>会员服务</template>
          <a-menu-item key="/admin/member">会员列表</a-menu-item>
          <a-menu-item key="/admin/recharge">余额充值</a-menu-item>
        </a-sub-menu>

        <a-sub-menu key="module-computer">
          <template #icon><IconDesktop /></template>
          <template #title>机位管理</template>
          <a-menu-item key="/admin/computer?tab=computer">电脑列表</a-menu-item>
          <a-menu-item key="/admin/computer?tab=repair">报修管理</a-menu-item>
        </a-sub-menu>

        <a-sub-menu key="module-operation">
          <template #icon><IconCalendarClock /></template>
          <template #title>上机运营</template>
          <a-menu-item key="/admin/reservation">预约管理</a-menu-item>
          <a-menu-item key="/admin/online">上机管理</a-menu-item>
        </a-sub-menu>

        <a-sub-menu key="module-food">
          <template #icon><IconApps /></template>
          <template #title>点餐管理</template>
          <a-menu-item key="/admin/food?tab=order">点餐订单</a-menu-item>
          <a-menu-item key="/admin/food?tab=item">商品维护</a-menu-item>
        </a-sub-menu>

        <a-sub-menu key="module-marketing">
          <template #icon><IconGift /></template>
          <template #title>营销活动</template>
          <a-menu-item key="/admin/voucher">团购券管理</a-menu-item>
          <a-menu-item key="/admin/coupon?tab=template">优惠券模板</a-menu-item>
          <a-menu-item key="/admin/coupon?tab=rule">签到奖励规则</a-menu-item>
          <a-menu-item key="/admin/promotion">推广计划</a-menu-item>
        </a-sub-menu>
      </a-menu>

      <div class="admin-sider-footer">
        <span class="admin-system-dot"></span>
        <span v-show="!collapsed">系统运行正常</span>
      </div>
    </aside>

    <main class="admin-main">
      <div class="admin-breadcrumb">
        <IconHome />
        <span>{{ currentModuleTitle }}</span>
        <IconRight />
        <strong>{{ currentMenuTitle }}</strong>
      </div>
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  IconApps,
  IconCalendarClock,
  IconComputer,
  IconDashboard,
  IconDesktop,
  IconDown,
  IconGift,
  IconHome,
  IconLaunch,
  IconMenuFold,
  IconMenuUnfold,
  IconPoweroff,
  IconRight,
  IconUserGroup
} from '@arco-design/web-vue/es/icon'

const router = useRouter()
const route = useRoute()
const collapsed = ref(localStorage.getItem('admin-menu-collapsed') === 'true')

const menuMeta = {
  '/admin/dashboard': { module: '数据概览', group: 'module-dashboard', title: '首页看板' },
  '/admin/member': { module: '会员服务', group: 'module-member', title: '会员列表' },
  '/admin/recharge': { module: '会员服务', group: 'module-member', title: '余额充值' },
  '/admin/computer?tab=computer': { module: '机位管理', group: 'module-computer', title: '电脑列表' },
  '/admin/computer?tab=repair': { module: '机位管理', group: 'module-computer', title: '报修管理' },
  '/admin/reservation': { module: '上机运营', group: 'module-operation', title: '预约管理' },
  '/admin/online': { module: '上机运营', group: 'module-operation', title: '上机管理' },
  '/admin/food?tab=order': { module: '点餐管理', group: 'module-food', title: '点餐订单' },
  '/admin/food?tab=item': { module: '点餐管理', group: 'module-food', title: '商品维护' },
  '/admin/voucher': { module: '营销活动', group: 'module-marketing', title: '团购券管理' },
  '/admin/coupon?tab=template': { module: '营销活动', group: 'module-marketing', title: '优惠券模板' },
  '/admin/coupon?tab=rule': { module: '营销活动', group: 'module-marketing', title: '签到奖励规则' },
  '/admin/promotion': { module: '营销活动', group: 'module-marketing', title: '推广计划' }
}

const publicPage = computed(() => !route.path.startsWith('/admin') || route.path === '/admin/login')
const currentMenuKey = computed(() => resolveMenuKey(route.path, route.query.tab))
const currentMenuInfo = computed(() => menuMeta[currentMenuKey.value])
const currentMenuTitle = computed(() => currentMenuInfo.value?.title || '运营管理')
const currentModuleTitle = computed(() => currentMenuInfo.value?.module || '运营管理')
const currentMenuGroup = computed(() => currentMenuInfo.value?.group || '')
const openKeys = ref([])

const admin = computed(() => {
  const text = localStorage.getItem('admin')
  return text ? JSON.parse(text) : null
})

const adminInitial = computed(() => String(admin.value?.realName || '管').slice(0, 1))

function goPage(path) {
  router.push(path)
}

function resolveMenuKey(path, tab) {
  const tabRoutes = {
    '/admin/computer': ['computer', 'repair'],
    '/admin/food': ['order', 'item'],
    '/admin/coupon': ['template', 'rule']
  }
  const allowedTabs = tabRoutes[path]
  if (!allowedTabs) return path
  const selectedTab = allowedTabs.includes(String(tab)) ? String(tab) : allowedTabs[0]
  return `${path}?tab=${selectedTab}`
}

function toggleCollapsed() {
  collapsed.value = !collapsed.value
  localStorage.setItem('admin-menu-collapsed', String(collapsed.value))
}

function openUserSystem() {
  router.push('/user')
}

function handleAccountAction(action) {
  if (action === 'user') openUserSystem()
  if (action === 'logout') logout()
}

function logout() {
  localStorage.removeItem('admin')
  router.push('/admin/login')
}

function syncResponsiveLayout() {
  if (window.innerWidth <= 1024) collapsed.value = true
}

onMounted(() => {
  syncResponsiveLayout()
  window.addEventListener('resize', syncResponsiveLayout)
})

onBeforeUnmount(() => window.removeEventListener('resize', syncResponsiveLayout))

watch(currentMenuGroup, (group) => {
  if (group && !openKeys.value.includes(group)) openKeys.value = [group]
}, { immediate: true })
</script>

<style scoped>
.admin-shell {
  --admin-nav-height: 60px;
  --admin-sider-width: 220px;
  min-height: 100vh;
  color: #1d2129;
  background: #f2f3f5;
}

.admin-shell.collapsed {
  --admin-sider-width: 48px;
}

.admin-navbar {
  position: fixed;
  inset: 0 0 auto 0;
  z-index: 100;
  height: var(--admin-nav-height);
  border-bottom: 1px solid #e5e6eb;
  background: #ffffff;
  display: flex;
}

.admin-brand {
  width: var(--admin-sider-width);
  padding: 0 18px;
  border-right: 1px solid #e5e6eb;
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
  transition: width 0.2s cubic-bezier(0.34, 0.69, 0.1, 1), padding 0.2s ease;
}

.collapsed .admin-brand {
  padding: 0 8px;
}

.admin-brand-mark {
  flex: 0 0 auto;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  color: #ffffff;
  background: #165dff;
  display: grid;
  place-items: center;
  font-size: 18px;
}

.admin-brand-copy {
  min-width: 124px;
}

.admin-brand-copy strong,
.admin-brand-copy span {
  display: block;
  white-space: nowrap;
}

.admin-brand-copy strong {
  font-size: 17px;
  line-height: 1.2;
}

.admin-brand-copy span {
  margin-top: 3px;
  color: #86909c;
  font-size: 11px;
}

.admin-navbar-main {
  flex: 1;
  min-width: 0;
  padding: 0 20px 0 12px;
  display: flex;
  align-items: center;
}

.admin-icon-button {
  flex: 0 0 auto;
  width: 36px;
  height: 36px;
  padding: 0;
  border: 1px solid transparent;
  border-radius: 50%;
  color: #4e5969;
  background: transparent;
  display: inline-grid;
  place-items: center;
  cursor: pointer;
  font-size: 18px;
}

.admin-icon-button:hover {
  color: #165dff;
  background: #f2f3f5;
}

.admin-icon-button:focus-visible,
.admin-profile:focus-visible {
  outline: 2px solid #94bfff;
  outline-offset: 1px;
}

.admin-navbar-title {
  min-width: 0;
  margin-left: 12px;
}

.admin-navbar-title strong,
.admin-navbar-title span {
  display: block;
  white-space: nowrap;
}

.admin-navbar-title strong {
  font-size: 15px;
  font-weight: 600;
}

.admin-navbar-title span {
  margin-top: 2px;
  color: #86909c;
  font-size: 11px;
}

.admin-navbar-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-profile {
  height: 42px;
  padding: 0 8px;
  border: 0;
  border-radius: 4px;
  background: transparent;
  display: flex;
  align-items: center;
  gap: 9px;
  cursor: pointer;
  font: inherit;
  text-align: left;
}

.admin-profile:hover {
  background: #f7f8fa;
}

.admin-avatar {
  color: #165dff;
  background: #e8f3ff;
  font-weight: 600;
}

.admin-profile-copy strong,
.admin-profile-copy small {
  display: block;
  white-space: nowrap;
}

.admin-profile-copy strong {
  color: #1d2129;
  font-size: 13px;
}

.admin-profile-copy small {
  margin-top: 2px;
  color: #86909c;
  font-size: 10px;
}

.admin-profile-arrow {
  color: #86909c;
  font-size: 12px;
}

.admin-sider {
  position: fixed;
  inset: var(--admin-nav-height) auto 0 0;
  z-index: 99;
  width: var(--admin-sider-width);
  border-right: 1px solid #e5e6eb;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
}

.admin-menu-label {
  flex: 0 0 auto;
  padding: 18px 20px 8px;
  color: #c9cdd4;
  font-size: 11px;
  font-weight: 600;
}

.admin-menu {
  flex: 1;
  width: 100%;
  overflow-y: auto;
}

.admin-menu :deep(.arco-menu-inner) {
  padding: 4px 8px;
}

.admin-menu :deep(.arco-menu-item) {
  height: 36px;
  margin-bottom: 2px;
  border-radius: 4px;
  color: #4e5969;
}

.admin-menu :deep(.arco-menu-inline-header) {
  height: 42px;
  margin-bottom: 2px;
  border-radius: 4px;
  color: #4e5969;
}

.admin-menu :deep(.arco-menu-inline-header:hover) {
  color: #1d2129;
  background: #f7f8fa;
}

.admin-menu :deep(.arco-menu-inline-header .arco-icon) {
  font-size: 18px;
}

.admin-menu :deep(.arco-menu-item.arco-menu-selected) {
  color: #165dff;
  background: #e8f3ff;
  font-weight: 600;
}

.admin-menu :deep(.arco-menu-inline-header.arco-menu-selected) {
  color: #165dff;
  background: #f7f8fa;
  font-weight: 600;
}

.collapsed .admin-menu :deep(.arco-menu-inner) {
  padding-right: 4px;
  padding-left: 4px;
}

.admin-sider-footer {
  flex: 0 0 auto;
  height: 48px;
  padding: 0 18px;
  border-top: 1px solid #f2f3f5;
  color: #86909c;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  white-space: nowrap;
}

.collapsed .admin-sider-footer {
  padding: 0;
  justify-content: center;
}

.admin-system-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #00b42a;
  box-shadow: 0 0 0 3px #e8ffea;
}

.admin-main {
  min-width: 0;
  min-height: 100vh;
  margin-left: var(--admin-sider-width);
  padding-top: var(--admin-nav-height);
  transition: margin 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
}

.admin-breadcrumb {
  height: 44px;
  padding: 0 20px;
  color: #86909c;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.admin-breadcrumb strong {
  color: #4e5969;
  font-weight: 500;
}

.admin-breadcrumb svg {
  flex: 0 0 auto;
}

.admin-content {
  padding: 0 20px 24px;
}

@media (max-width: 760px) {
  .admin-navbar-main {
    padding-right: 8px;
  }

  .admin-navbar-title span,
  .admin-profile-copy,
  .admin-profile-arrow {
    display: none;
  }

  .admin-navbar-actions {
    gap: 2px;
  }

  .admin-breadcrumb {
    padding: 0 12px;
  }

  .admin-content {
    padding: 0 12px 18px;
  }
}
</style>
