<template>
  <div class="user-login-page">
    <a-card class="user-login-card" :title="isRegister ? '注册用户账号' : '网管通用户登录'">
      <div class="mode-switch">
        <button :class="{ active: !isRegister }" @click="isRegister = false">登录</button>
        <button :class="{ active: isRegister }" @click="isRegister = true">注册</button>
      </div>

      <a-form v-if="!isRegister" :model="form" layout="vertical">
        <a-form-item label="身份证号">
          <a-input v-model="form.username" placeholder="请输入身份证号" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model="form.password" placeholder="请输入密码" />
        </a-form-item>
        <a-button type="primary" long @click="handleLogin">登录用户系统</a-button>
        <a-button class="admin-entry" long @click="goAdmin">进入管理员后台</a-button>
      </a-form>

      <a-form v-else :model="registerForm" layout="vertical">
        <a-form-item label="姓名">
          <a-input v-model="registerForm.name" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="身份证号">
          <a-input v-model="registerForm.idCard" placeholder="请输入身份证号" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model="registerForm.password" placeholder="请设置登录密码" />
        </a-form-item>
        <a-button type="primary" long @click="handleRegister">注册并登录</a-button>
      </a-form>

      <div class="login-tip">{{ isRegister ? '注册后默认为普通会员，可联系前台充值。' : '测试身份证号 110101199001011234，默认密码 123456。' }}</div>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
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
  password: ''
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
.user-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef3ff;
}

.user-login-card {
  width: 390px;
}

.admin-entry {
  margin-top: 12px;
}

.mode-switch {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  padding: 4px;
  margin-bottom: 18px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #f3f6fa;
}

.mode-switch button {
  height: 34px;
  border: 0;
  border-radius: 6px;
  color: #4b5563;
  background: transparent;
  cursor: pointer;
  font-weight: 700;
}

.mode-switch button.active {
  color: #ffffff;
  background: #1d4ed8;
}

.login-tip {
  margin-top: 14px;
  color: #86909c;
  font-size: 13px;
  text-align: center;
}
</style>
