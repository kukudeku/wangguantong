import axios from 'axios'
import { Message } from '@arco-design/web-vue'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code === 200) {
      return result.data
    }
    Message.error(result.message || '请求失败')
    return Promise.reject(new Error(result.message || '请求失败'))
  },
  (error) => {
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
