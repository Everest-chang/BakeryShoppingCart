import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
   server: {
    // 將 /api 請求代理到後端，避免 CORS 問題
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }

})
