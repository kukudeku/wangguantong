import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 是前端开发服务器和打包工具，此文件是它的项目配置入口。
export default defineConfig({
  // 让 Vite 能够解析和编译 .vue 单文件组件。
  plugins: [vue()],
  server: {
    // 本地执行 npm run dev 后，通过 http://localhost:5173 访问前端。
    port: 5173
  }
})
