import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 是前端开发服务器和打包工具，此文件是它的项目配置入口。
export default defineConfig({
  // 让 Vite 能够解析和编译 .vue 单文件组件。
  plugins: [vue()],
  server: {
    // 本地执行 npm run dev 后，通过 http://localhost:5173 访问前端。
    port: 5173,
    // 浏览器只请求同源的 /api，由 Vite 转发到 8087 后端，避免跨域问题。
    proxy: {
      '/api': {
        target: 'http://localhost:8087',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
