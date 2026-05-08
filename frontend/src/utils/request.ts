import axios, { type AxiosRequestConfig } from 'axios'
import { useUserStore } from '@/store/user'
import router from '@/router'
import Toast from '@/utils/toast'

// Create Axios Instance
const service = axios.create({
	baseURL: '/api', // Proxy will handle this
	timeout: 10000,
	withCredentials: true, // Crucial for JSESSIONID cookie
})

// Request Interceptor
service.interceptors.request.use(
	(config) => {
		// If we were using Token header, we would add it here.
		// const userStore = useUserStore();
		// if (userStore.token) { config.headers['Authorization'] = userStore.token; }
		return config
	},
	(error) => {
		return Promise.reject(error)
	},
)

// Response Interceptor
service.interceptors.response.use(
	(response) => {
		// File downloads / binary responses (not wrapped by standard Result)
		const responseType = response.config?.responseType
		if (responseType === 'blob' || responseType === 'arraybuffer') {
			return response.data
		}

		const res = response.data
		// Backend returns standard Result: { code: 0, message: "...", data: ... }
		// code: 0 = success, other codes = error
		if (res.code !== 0) {
			if (res.code === 1002) {
				// Not Authenticated (ErrorCode.UNAUTHORIZED)
				const userStore = useUserStore()
				userStore.logout()
				router.push(`/login?redirect=${encodeURIComponent(window.location.pathname)}`)
				return Promise.reject(new Error(res.message || 'Unauthorized'))
			}
			if (res.code === 1003) {
				// Forbidden (ErrorCode.FORBIDDEN)
				Toast.error(res.message || 'Forbidden')
				return Promise.reject(new Error(res.message || 'Forbidden'))
			}
			console.error('API Error:', res.message)
			Toast.error(res.message || 'Error')
			return Promise.reject(new Error(res.message || 'Error'))
		}
		return res.data
	},
	(error) => {
		// HTTP 层面的错误（网络问题、服务器宕机等）
		// 业务错误不会走这里，因为后端始终返回 HTTP 200
		console.error('Network Error:', error.message)
		return Promise.reject(error)
	},
)

const request = <T = any>(config: AxiosRequestConfig): Promise<T> => {
	return service(config) as unknown as Promise<T>
}

export default request
