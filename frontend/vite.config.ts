import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
	plugins: [vue()],
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url)),
		},
	},
	server: {
		port: 5173,
		proxy: {
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				// rewrite: (path) => path.replace(/^\/api/, '') // Context path is /api in backend if configured?
				// Backend application.yml has `server.servlet.context-path: /api`?
				// Let's check backend config. If context-path is /, then we rewrite.
				// If context-path is /api, we keep it.
				// Checking BACKEND_SKELETON.md: `server.servlet.context-path: /api`
				// So NO rewrite needed.
			},
		},
	},
})
