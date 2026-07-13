<template>
  <div class="admin-login-page">
    <div class="admin-login-brand">
      <div class="admin-login-brand-mark"><IconComputer /></div>
      <div>
        <strong>网管通</strong>
        <span>电竞网吧运营管理系统</span>
      </div>
    </div>

    <section class="admin-login-banner">
      <div class="admin-login-banner-content">
        <div class="admin-login-banner-icon"><IconDashboard /></div>
        <p>WANG GUAN TONG</p>
        <h1>电竞网吧运营管理系统</h1>
        <span>统一管理会员、机位、预约、上机计费与点餐订单。</span>
      </div>
      <div class="admin-login-banner-status">
        <i></i>
        运营后台
      </div>
    </section>

    <main class="admin-login-main">
      <div class="admin-login-form-wrap">
        <div class="admin-login-heading">
          <span>ADMIN CONSOLE</span>
          <h2>登录网管通</h2>
          <p>使用管理员账号进入运营后台</p>
        </div>

        <a-form :model="form" layout="vertical" @submit-success="handleLogin">
          <a-form-item label="用户名">
            <a-input v-model="form.username" placeholder="请输入用户名" size="large" allow-clear>
              <template #prefix><IconUser /></template>
            </a-input>
          </a-form-item>
          <a-form-item label="密码">
            <a-input-password v-model="form.password" placeholder="请输入密码" size="large">
              <template #prefix><IconLock /></template>
            </a-input-password>
          </a-form-item>
          <a-button class="admin-login-submit" type="primary" size="large" long html-type="submit">
            登录后台
            <template #icon><IconArrowRight /></template>
          </a-button>
          <button type="button" class="admin-user-entry" @click="goUserPage">
            进入用户系统
            <IconLaunch />
          </button>
        </a-form>
      </div>

      <footer>网管通 · 电竞网吧运营管理系统</footer>
    </main>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconArrowRight,
  IconComputer,
  IconDashboard,
  IconLaunch,
  IconLock,
  IconUser
} from '@arco-design/web-vue/es/icon'
import { login } from '../../api/admin'

const router = useRouter()
const form = reactive({
  username: 'admin',
  password: '123456'
})

async function handleLogin() {
  if (!form.username || !form.password) {
    Message.error('请输入用户名和密码')
    return
  }
  const admin = await login(form)
  localStorage.setItem('admin', JSON.stringify(admin))
  Message.success('登录成功')
  router.push('/admin/dashboard')
}

function goUserPage() {
  router.push('/user/login')
}
</script>

<style scoped>
.admin-login-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  background: #ffffff;
  display: grid;
  grid-template-columns: minmax(380px, 42%) minmax(460px, 1fr);
}

.admin-login-brand {
  position: fixed;
  top: 24px;
  left: 28px;
  z-index: 2;
  color: #ffffff;
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-login-brand-mark {
  width: 36px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 4px;
  display: grid;
  place-items: center;
  font-size: 20px;
}

.admin-login-brand strong,
.admin-login-brand span {
  display: block;
  white-space: nowrap;
}

.admin-login-brand strong {
  font-size: 18px;
}

.admin-login-brand span {
  margin-top: 2px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 11px;
}

.admin-login-banner {
  position: relative;
  min-height: 100vh;
  padding: 110px 58px 52px;
  color: #ffffff;
  background: #165dff;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.admin-login-banner::before,
.admin-login-banner::after {
  position: absolute;
  content: "";
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.admin-login-banner::before {
  width: 320px;
  height: 320px;
  right: -120px;
  top: 18%;
  transform: rotate(20deg);
}

.admin-login-banner::after {
  width: 190px;
  height: 190px;
  left: -70px;
  bottom: 8%;
  transform: rotate(45deg);
}

.admin-login-banner-content {
  position: relative;
  z-index: 1;
  max-width: 430px;
}

.admin-login-banner-icon {
  width: 58px;
  height: 58px;
  margin-bottom: 32px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 6px;
  display: grid;
  place-items: center;
  font-size: 29px;
}

.admin-login-banner-content p {
  margin: 0 0 13px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 11px;
  font-weight: 600;
}

.admin-login-banner-content h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.35;
  letter-spacing: 0;
}

.admin-login-banner-content > span {
  display: block;
  margin-top: 18px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 14px;
  line-height: 1.8;
}

.admin-login-banner-status {
  position: absolute;
  left: 58px;
  bottom: 38px;
  color: rgba(255, 255, 255, 0.72);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
}

.admin-login-banner-status i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #7be188;
}

.admin-login-main {
  position: relative;
  min-height: 100vh;
  padding: 72px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.admin-login-form-wrap {
  width: min(400px, 100%);
}

.admin-login-heading {
  margin-bottom: 30px;
}

.admin-login-heading > span {
  color: #165dff;
  font-size: 11px;
  font-weight: 700;
}

.admin-login-heading h2 {
  margin: 9px 0 0;
  color: #1d2129;
  font-size: 28px;
  line-height: 1.3;
}

.admin-login-heading p {
  margin: 8px 0 0;
  color: #86909c;
  font-size: 13px;
}

.admin-login-form-wrap :deep(.arco-form-item) {
  margin-bottom: 21px;
}

.admin-login-form-wrap :deep(.arco-form-item-label-col) {
  padding-bottom: 8px;
  color: #4e5969;
  font-size: 13px;
}

.admin-login-form-wrap :deep(.arco-input-wrapper) {
  height: 44px;
  border-color: #c9cdd4;
  border-radius: 3px;
  background: #ffffff;
}

.admin-login-form-wrap :deep(.arco-input-wrapper:hover) {
  border-color: #94bfff;
}

.admin-login-form-wrap :deep(.arco-input-focus) {
  box-shadow: 0 0 0 3px #e8f3ff;
}

.admin-login-submit {
  height: 44px;
  margin-top: 4px;
  border-radius: 3px;
  font-weight: 600;
}

.admin-user-entry {
  width: 100%;
  height: 38px;
  margin-top: 12px;
  padding: 0;
  border: 0;
  color: #4e5969;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  cursor: pointer;
  font: inherit;
  white-space: nowrap;
}

.admin-user-entry:hover {
  color: #165dff;
}

.admin-login-main footer {
  position: absolute;
  right: 0;
  bottom: 22px;
  left: 0;
  color: #c9cdd4;
  font-size: 11px;
  text-align: center;
}

@media (max-width: 900px) {
  .admin-login-page {
    grid-template-columns: 1fr;
  }

  .admin-login-banner {
    display: none;
  }

  .admin-login-brand {
    color: #1d2129;
  }

  .admin-login-brand-mark {
    color: #ffffff;
    border-color: #165dff;
    background: #165dff;
  }

  .admin-login-brand span {
    color: #86909c;
  }
}

@media (max-width: 520px) {
  .admin-login-brand {
    top: 18px;
    left: 18px;
  }

  .admin-login-main {
    padding: 88px 24px 60px;
    align-items: flex-start;
  }

  .admin-login-form-wrap {
    margin-top: 12vh;
  }
}
</style>
