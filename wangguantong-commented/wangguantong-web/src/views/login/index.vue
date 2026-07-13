<template>
  <!-- 管理员登录页：表单数据通过 v-model 与 script 中的 form 双向绑定。 -->
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
        <a-button class="user-entry" long @click="goUserPage">进入用户系统</a-button>
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
// reactive 让对象字段变化后自动更新输入框；这里预填课堂演示账号。
const form = reactive({
  username: 'admin',
  password: '123456'
})

async function handleLogin() {
  // 前端先做必填校验，减少一次无意义的网络请求。
  if (!form.username || !form.password) {
    Message.error('请输入用户名和密码')
    return
  }
  // await 会等待后端验证完成；请求失败由公共 request 拦截器显示消息。
  const admin = await login(form)
  // localStorage 在刷新页面后仍保留，路由守卫会用它判断管理员是否已登录。
  localStorage.setItem('admin', JSON.stringify(admin))
  Message.success('登录成功')
  router.push('/admin/dashboard')
}

function goUserPage() {
  // 管理员和普通用户使用不同登录入口。
  router.push('/user/login')
}
</script>

<style scoped>
/* 使用 Flex 布局让登录卡片在窗口中水平、垂直居中。 */
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

.user-entry {
  margin-top: 12px;
}
</style>
