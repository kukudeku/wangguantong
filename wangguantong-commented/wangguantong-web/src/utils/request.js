import axios from 'axios'
import { Message } from '@arco-design/web-vue'

// 创建全项目共用的 Axios 请求对象。
const request = axios.create({
  // 宝塔部署时可通过 VITE_API_BASE_URL 指向线上后端；本地默认访问 8080 端口。
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
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
  () => {
    // 此分支处理断网、后端未启动、请求超时等没有正常业务响应的情况。
    Message.error('服务器连接失败')
    return Promise.reject(new Error('服务器连接失败'))
  }
)

export default request
