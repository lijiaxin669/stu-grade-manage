<template>
	<div class="login-container">
		<div class="login-content">
			<div class="login-header">
				<div class="logo-icon">🎓</div>
				<h2>学生成绩管理系统</h2>
				<p class="subtitle">Grade Management System</p>
			</div>
			<form @submit.prevent="handleLogin" class="login-form">
				<div class="form-item">
					<!-- <label>用户名</label> -->
					<div class="input-wrapper">
						<span class="icon">👤</span>
						<input v-model="form.username" type="text" placeholder="请输入用户名 / 学号 / 工号" required />
					</div>
				</div>
				<div class="form-item">
					<!-- <label>密码</label> -->
					<div class="input-wrapper">
						<span class="icon">🔒</span>
						<input v-model="form.password" type="password" placeholder="请输入密码" required />
					</div>
				</div>

				<div class="form-actions">
					<button type="submit" :disabled="loading" class="login-btn">
						{{ loading ? '登录中...' : '登 录' }}
					</button>
				</div>

				<div class="form-footer">
					<a href="#" @click.prevent="router.push('/register')">注册账号</a>
				</div>

				<div v-if="error" class="error-message">
					{{ error }}
				</div>
			</form>
		</div>
	</div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter, useRoute } from 'vue-router'

const store = useUserStore()
const router = useRouter()
const route = useRoute()

const getHomePath = (roles: string[]) => {
	if (roles.includes('ROLE_STUDENT')) return '/student'
	if (roles.includes('ROLE_TEACHER')) return '/teacher'
	if (roles.includes('ROLE_SUPER_ADMIN')) return '/admin'
	return '/login'
}

const form = reactive({
	username: '',
	password: '',
})
const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
	loading.value = true
	error.value = ''
	try {
		await store.login(form)
		const redirect = (route.query.redirect as string) || '/'
		const target = redirect === '/' || redirect === '/login' ? getHomePath(store.roles) : redirect
		router.push(target)
	} catch (err: any) {
		error.value = '登录失败: ' + (err.message || '用户名或密码错误')
	} finally {
		loading.value = false
	}
}
</script>

<style scoped>
.login-container {
	display: flex;
	justify-content: center;
	align-items: center;
	height: 100vh;
	width: 100%;
	background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
	background-image: linear-gradient(to top, #dfe9f3 0%, white 100%);
	font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.login-content {
	width: 400px;
	background: white;
	padding: 40px;
	border-radius: 12px;
	box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08); /* Soft shadow */
	display: flex;
	flex-direction: column;
	align-items: center;
	box-sizing: border-box;
}

.login-header {
	text-align: center;
	margin-bottom: 30px;
}

.logo-icon {
	font-size: 48px;
	margin-bottom: 10px;
	display: inline-block;
}

.login-header h2 {
	margin: 0;
	font-size: 24px;
	color: #303133;
	font-weight: 600;
}

.subtitle {
	margin: 5px 0 0;
	font-size: 14px;
	color: #909399;
	letter-spacing: 0.5px;
}

.login-form {
	width: 100%;
}

.form-item {
	margin-bottom: 20px;
}

.input-wrapper {
	position: relative;
	display: flex;
	align-items: center;
}

.input-wrapper .icon {
	position: absolute;
	left: 12px;
	font-size: 16px;
	color: #c0c4cc;
	z-index: 1;
}

.input-wrapper input {
	width: 100%;
	padding: 12px 12px 12px 40px; /* Leave space for icon */
	border: 1px solid #dcdfe6;
	border-radius: 6px;
	font-size: 14px;
	color: #606266;
	outline: none;
	transition: all 0.3s;
	box-sizing: border-box;
	background: #fff;
}

.input-wrapper input:focus {
	border-color: #409eff;
	box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.input-wrapper input::placeholder {
	color: #c0c4cc;
}

.login-btn {
	width: 100%;
	padding: 12px;
	background: #409eff; /* Primary Blue */
	color: white;
	border: none;
	border-radius: 6px;
	font-size: 16px;
	font-weight: 500;
	cursor: pointer;
	transition: background 0.3s;
}

.login-btn:hover {
	background: #66b1ff;
}

.login-btn:active {
	background: #3a8ee6;
}

.login-btn:disabled {
	background: #a0cfff;
	cursor: not-allowed;
}

.form-footer {
	margin-top: 20px;
	display: flex;
	justify-content: center;
	align-items: center;
	font-size: 13px;
	color: #909399;
	gap: 10px;
}

.form-footer a {
	color: #409eff;
	text-decoration: none;
	transition: color 0.2s;
}

.form-footer a:hover {
	color: #66b1ff;
}

.error-message {
	margin-top: 15px;
	padding: 10px;
	background: #fef0f0;
	color: #f56c6c;
	border-radius: 4px;
	font-size: 13px;
	text-align: center;
	border: 1px solid #fde2e2;
	animation: shake 0.5s;
}

@keyframes shake {
	0%,
	100% {
		transform: translateX(0);
	}
	10%,
	30%,
	50%,
	70%,
	90% {
		transform: translateX(-4px);
	}
	20%,
	40%,
	60%,
	80% {
		transform: translateX(4px);
	}
}
</style>
