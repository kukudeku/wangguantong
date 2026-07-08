<template>
  <div class="login-page">
    <a-card class="login-card" title="网管通管理员登录">
      <a-form :model="form" layout="vertical">
        <a-form-item label="用户名">
          <a-input v-model="form.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model="form.password" placeholder="请输入密码" />
        </a-form-item>
        <a-button type="primary" long @click="handleLogin">登录</a-button>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
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
  router.push('/dashboard')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f3ff 0%, #f2f3f5 100%);
}

.login-card {
  width: 380px;
}
</style>
