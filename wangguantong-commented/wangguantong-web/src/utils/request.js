import axios from 'axios'
import { Message } from '@arco-design/web-vue'

// 创建全项目共用的 Axios 请求对象。
const request = axios.create({
  // 本地和宝塔都请求同源 /api，再分别由 Vite 或 Nginx 转发到 8087 后端。
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  // 10 秒仍未返回就中止请求，避免页面无限等待。
  timeout: 10000
})

// 响应拦截器统一拆解后端 Result：成功时页面直接拿 data，失败时统一弹出错误消息。
request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code === 200) {
      // 例如后端返回 { code: 200, data: [...] }，调用页面最终只会收到数组部分。
      return result.data
    }
    Message.error(result.message || '请求失败')
    return Promise.reject(new Error(result.message || '请求失败'))
  },
  (error) => {
    // 宝塔常见的 404 是 /api 代理缺失，502/503 则表示 8087 后端没有正常运行。
    const status = error.response?.status
    const message = status === 404
      ? '接口地址不存在，请检查宝塔 /api 反向代理'
      : status === 502 || status === 503
        ? '后端服务未启动，请检查 8087 端口'
        : '服务器连接失败'
    Message.error(message)
    return Promise.reject(new Error(message))
  }
)

export default request
