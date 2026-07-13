<template>
  <div class="ul-shell">
    <header class="ul-header">
      <div class="ul-brand">
        <div class="ul-brand-mark">W</div>
        <div>
          <strong>网管通</strong>
          <span>电竞网吧自助终端</span>
        </div>
      </div>
      <button class="ul-admin-link" @click="goAdmin">管理员入口 <IconArrowRight /></button>
    </header>

    <main class="ul-main">
      <section class="ul-intro">
        <span class="ul-system-name">网管通用户系统</span>
        <h1><span>上机、点餐、查记录</span><span>一个账号就够了</span></h1>
        <p>使用身份证号登录，查看账户余额并使用网吧自助服务。</p>

        <div class="ul-feature-list">
          <div>
            <IconDesktop />
            <section><strong>座位上机</strong><span>查看机位状态，自助上机或预约</span></section>
          </div>
          <div>
            <IconApps />
            <section><strong>自助点餐</strong><span>选择商品和支付方式后下单</span></section>
          </div>
          <div>
            <IconList />
            <section><strong>消费记录</strong><span>余额、上机和订单明细集中查看</span></section>
          </div>
        </div>
      </section>

      <section class="ul-form-panel">
        <div class="ul-mode-switch">
          <button :class="{ active: !isRegister }" @click="isRegister = false">登录</button>
          <button :class="{ active: isRegister }" @click="isRegister = true">注册</button>
        </div>

        <div class="ul-form-title">
          <h2>{{ isRegister ? '注册用户账号' : '登录用户系统' }}</h2>
          <p>{{ isRegister ? '注册后默认为普通会员' : '请输入身份证号和登录密码' }}</p>
        </div>

        <a-form v-if="!isRegister" :model="form" layout="vertical" class="ul-form">
          <a-form-item label="身份证号">
            <a-input v-model="form.username" placeholder="请输入身份证号" size="large" />
          </a-form-item>
          <a-form-item label="密码">
            <a-input-password v-model="form.password" placeholder="请输入密码" size="large" />
          </a-form-item>
          <a-button type="primary" long size="large" @click="handleLogin">登录</a-button>
        </a-form>

        <a-form v-else :model="registerForm" layout="vertical" class="ul-form">
          <div class="ul-form-grid">
            <a-form-item label="姓名">
              <a-input v-model="registerForm.name" placeholder="请输入姓名" size="large" />
            </a-form-item>
            <a-form-item label="手机号">
              <a-input v-model="registerForm.phone" placeholder="请输入手机号" size="large" />
            </a-form-item>
          </div>
          <a-form-item label="身份证号">
            <a-input v-model="registerForm.idCard" placeholder="请输入身份证号" size="large" />
          </a-form-item>
          <a-form-item label="密码">
            <a-input-password v-model="registerForm.password" placeholder="请设置登录密码" size="large" />
          </a-form-item>
          <a-form-item label="邀请码（选填）">
            <a-input v-model="registerForm.invitationCode" allow-clear placeholder="填写好友邀请码，注册后双方得奖励" size="large" />
          </a-form-item>
          <a-button type="primary" long size="large" @click="handleRegister">注册并登录</a-button>
        </a-form>

        <div class="ul-tip">
          <IconInfoCircle />
          <span>{{ isRegister ? '完成注册后可联系前台充值。' : '测试账号：110101199001011234 / 123456' }}</span>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { IconApps, IconArrowRight, IconDesktop, IconInfoCircle, IconList } from '@arco-design/web-vue/es/icon'
import { userLogin, userRegister } from '../../api/user'

const router = useRouter()
const isRegister = ref(false)
const form = reactive({
  username: '110101199001011234',
  password: '123456'
})
const registerForm = reactive({
  name: '',
  idCard: '',
  phone: '',
  password: '',
  invitationCode: ''
})

async function handleLogin() {
  if (!form.username || !form.password) {
    Message.error('请输入身份证号和密码')
    return
  }
  const user = await userLogin(form)
  localStorage.setItem('user', JSON.stringify(user))
  Message.success('登录成功')
  router.push('/user')
}

async function handleRegister() {
  if (!registerForm.name || !registerForm.idCard || !registerForm.phone || !registerForm.password) {
    Message.error('请完整填写注册信息')
    return
  }
  const user = await userRegister(registerForm)
  localStorage.setItem('user', JSON.stringify(user))
  Message.success('注册成功')
  router.push('/user')
}

function goAdmin() {
  router.push('/admin/login')
}
</script>

<style scoped>
.ul-shell {
  --ul-accent: #2457d6;
  --ul-text: #172033;
  --ul-muted: #6b768b;
  --ul-border: #dbe2ec;
  min-height: 100vh;
  padding: 0 42px 42px;
  color: var(--ul-text);
  background: #f3f5f8;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.ul-shell *,
.ul-shell *::before,
.ul-shell *::after {
  box-sizing: border-box;
}

.ul-header {
  width: min(1260px, 100%);
  height: 88px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ul-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ul-brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  color: #ffffff;
  background: var(--ul-accent);
  display: grid;
  place-items: center;
  font-size: 20px;
  font-weight: 800;
}

.ul-brand strong,
.ul-brand span {
  display: block;
}

.ul-brand strong {
  font-size: 18px;
}

.ul-brand span {
  margin-top: 2px;
  color: var(--ul-muted);
  font-size: 11px;
}

.ul-admin-link {
  height: 38px;
  padding: 0 4px;
  border: 0;
  color: #526078;
  background: transparent;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  white-space: nowrap;
}

.ul-admin-link:hover {
  color: var(--ul-accent);
}

.ul-main {
  width: min(1260px, 100%);
  min-height: calc(100vh - 130px);
  margin: 0 auto;
  border: 1px solid var(--ul-border);
  border-radius: 8px;
  background: #ffffff;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(420px, 0.85fr);
  overflow: hidden;
}

.ul-intro {
  padding: clamp(50px, 7vw, 94px);
  border-right: 1px solid var(--ul-border);
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.ul-system-name {
  color: var(--ul-accent);
  font-size: 13px;
  font-weight: 700;
}

.ul-intro h1 {
  margin: 20px 0 16px;
  font-size: clamp(34px, 3.4vw, 42px);
  line-height: 1.22;
  font-weight: 780;
  letter-spacing: 0;
}

.ul-intro h1 span {
  display: block;
  white-space: nowrap;
}

.ul-intro > p {
  max-width: 500px;
  margin: 0;
  color: var(--ul-muted);
  font-size: 15px;
  line-height: 1.7;
}

.ul-feature-list {
  max-width: 520px;
  margin-top: 54px;
  border-top: 1px solid var(--ul-border);
}

.ul-feature-list > div {
  min-height: 74px;
  border-bottom: 1px solid var(--ul-border);
  display: grid;
  grid-template-columns: 24px 1fr;
  align-items: center;
  gap: 14px;
}

.ul-feature-list svg {
  color: var(--ul-accent);
  font-size: 20px;
}

.ul-feature-list strong,
.ul-feature-list span {
  display: block;
}

.ul-feature-list strong {
  font-size: 14px;
}

.ul-feature-list span {
  margin-top: 3px;
  color: var(--ul-muted);
  font-size: 12px;
}

.ul-form-panel {
  padding: clamp(44px, 6vw, 78px);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.ul-mode-switch {
  width: 100%;
  padding: 3px;
  border: 1px solid var(--ul-border);
  border-radius: 6px;
  background: #f5f7fa;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3px;
}

.ul-mode-switch button {
  height: 36px;
  border: 0;
  border-radius: 4px;
  color: var(--ul-muted);
  background: transparent;
  cursor: pointer;
  font: inherit;
  font-weight: 650;
}

.ul-mode-switch button.active {
  color: var(--ul-accent);
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(24, 36, 56, 0.1);
}

.ul-mode-switch button:focus-visible,
.ul-admin-link:focus-visible {
  outline: 2px solid #9bb4ea;
  outline-offset: 1px;
}

.ul-form-title {
  margin: 30px 0 24px;
}

.ul-form-title h2,
.ul-form-title p {
  margin: 0;
}

.ul-form-title h2 {
  font-size: 25px;
}

.ul-form-title p {
  margin-top: 6px;
  color: var(--ul-muted);
  font-size: 13px;
}

.ul-form :deep(.arco-form-item-label-col) {
  color: #46536a;
  font-weight: 650;
}

.ul-form :deep(.arco-input-wrapper) {
  min-height: 46px;
  padding: 0 14px;
  border: 1px solid #b8c4d6;
  border-radius: 5px;
  background: #f8fafc;
  box-shadow: inset 0 1px 2px rgba(23, 32, 51, 0.03);
  transition: border-color 160ms ease, background 160ms ease, box-shadow 160ms ease;
}

.ul-form :deep(.arco-input-wrapper:hover) {
  border-color: #8798b3;
  background: #ffffff;
}

.ul-form :deep(.arco-input-wrapper:focus-within),
.ul-form :deep(.arco-input-wrapper.arco-input-focus) {
  border-color: var(--ul-accent);
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(36, 87, 214, 0.14);
}

.ul-form :deep(.arco-input) {
  color: var(--ul-text);
  font-size: 15px;
}

.ul-form :deep(.arco-input::placeholder) {
  color: #98a3b5;
}

.ul-form :deep(.arco-btn-primary) {
  border-color: var(--ul-accent);
  border-radius: 5px;
  background: var(--ul-accent);
  font-weight: 650;
}

.ul-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.ul-tip {
  margin-top: 18px;
  padding: 11px 12px;
  border: 1px solid #cbd9f6;
  border-radius: 5px;
  color: #526a9c;
  background: #f3f7ff;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
}

@media (max-width: 900px) {
  .ul-shell {
    padding: 0 20px 20px;
  }

  .ul-main {
    grid-template-columns: 1fr;
  }

  .ul-intro {
    padding: 42px;
    border-right: 0;
    border-bottom: 1px solid var(--ul-border);
  }

  .ul-intro h1 br {
    display: none;
  }

  .ul-feature-list {
    margin-top: 30px;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    border-bottom: 1px solid var(--ul-border);
  }

  .ul-feature-list > div {
    min-height: 104px;
    padding: 14px;
    border-right: 1px solid var(--ul-border);
    border-bottom: 0;
    grid-template-columns: 1fr;
  }

  .ul-feature-list > div:last-child {
    border-right: 0;
  }

  .ul-form-panel {
    padding: 42px;
  }
}

@media (max-width: 600px) {
  .ul-shell {
    padding: 0 12px 12px;
  }

  .ul-header {
    height: 72px;
  }

  .ul-brand span {
    display: none;
  }

  .ul-main {
    min-height: calc(100vh - 84px);
  }

  .ul-intro {
    padding: 30px 24px;
  }

  .ul-intro h1 {
    margin-top: 14px;
    font-size: 30px;
  }

  .ul-intro > p,
  .ul-feature-list {
    display: none;
  }

  .ul-form-panel {
    padding: 30px 24px;
    justify-content: flex-start;
  }

  .ul-form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>
