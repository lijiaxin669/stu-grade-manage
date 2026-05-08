<template>
	<div class="register-container">
		<div class="register-content">
			<div class="register-header">
				<div class="logo-icon">🌟</div>
				<h2>注册新账号</h2>
				<p class="subtitle">Create Your Account</p>
			</div>
			<form @submit.prevent="handleRegister" class="register-form">
				<div class="form-item">
					<div class="input-wrapper">
						<span class="icon">👤</span>
						<input v-model="form.username" type="text" placeholder="设置用户名 (至少4位)" minlength="4" required />
					</div>
				</div>
				<div class="form-item">
					<div class="input-wrapper">
						<span class="icon">📛</span>
						<input v-model="form.realName" type="text" placeholder="输入真实姓名" required />
					</div>
				</div>
				<div class="form-item">
					<div class="input-wrapper select-wrapper">
						<span class="icon">🎓</span>
						<select v-model="form.roleCode" required>
							<option value="ROLE_STUDENT">我是学生</option>
							<option value="ROLE_TEACHER">我是教师</option>
						</select>
					</div>
				</div>
				<div class="form-item">
					<div class="input-wrapper">
						<span class="icon">🔒</span>
						<input
							v-model="form.password"
							type="password"
							placeholder="设置登录密码 (至少6位)"
							minlength="6"
							required
						/>
					</div>
				</div>

				<div class="form-actions">
					<button type="submit" :disabled="loading" class="register-btn">
						{{ loading ? '注册中...' : '立即注册' }}
					</button>
				</div>

				<div class="form-footer">
					<span>已有账号？</span>
					<a href="#" @click.prevent="router.push('/login')">直接登录</a>
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
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import Toast from '@/utils/toast'

const router = useRouter()

const form = reactive({
	username: '',
	realName: '',
	password: '',
	roleCode: 'ROLE_STUDENT',
})
const loading = ref(false)
const error = ref('')

const handleRegister = async () => {
	loading.value = true
	error.value = ''
	try {
		await register(form)
		Toast.success('注册成功，请登录')
		router.push('/login')
	} catch (err: any) {
		error.value = '注册失败: ' + (err.message || '请检查输入信息')
	} finally {
		loading.value = false
	}
}
</script>

<style scoped>
.register-container {
	display: flex;
	justify-content: center;
	align-items: center;
	height: 100vh;
	width: 100%;
	background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
	background-image: linear-gradient(to top, #dfe9f3 0%, white 100%);
	font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.register-content {
	width: 400px;
	background: white;
	padding: 40px;
	border-radius: 12px;
	box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
	display: flex;
	flex-direction: column;
	align-items: center;
	box-sizing: border-box;
}

.register-header {
	text-align: center;
	margin-bottom: 30px;
}

.logo-icon {
	font-size: 40px;
	margin-bottom: 10px;
	display: inline-block;
}

.register-header h2 {
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

.register-form {
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

.input-wrapper input,
.input-wrapper select {
	width: 100%;
	padding: 12px 12px 12px 40px;
	border: 1px solid #dcdfe6;
	border-radius: 6px;
	font-size: 14px;
	color: #606266;
	outline: none;
	transition: all 0.3s;
	box-sizing: border-box;
	background: #fff;
	height: 44px; /* Align height */
}

.input-wrapper input:focus,
.input-wrapper select:focus {
	border-color: #409eff;
	box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.input-wrapper input::placeholder {
	color: #c0c4cc;
}

/* Custom styles for select to look consistent */
.select-wrapper::after {
	content: '▼';
	position: absolute;
	right: 12px;
	font-size: 10px;
	color: #c0c4cc;
	pointer-events: none;
}
.input-wrapper select {
	appearance: none;
	-webkit-appearance: none;
	cursor: pointer;
}

.register-btn {
	width: 100%;
	padding: 12px;
	background: #409eff;
	color: white;
	border: none;
	border-radius: 6px;
	font-size: 16px;
	font-weight: 500;
	cursor: pointer;
	transition: background 0.3s;
}

.register-btn:hover {
	background: #66b1ff;
}

.register-btn:active {
	background: #3a8ee6;
}

.register-btn:disabled {
	background: #a0cfff;
	cursor: not-allowed;
}

.form-footer {
	margin-top: 20px;
	display: flex;
	justify-content: center;
	align-items: center;
	font-size: 14px;
	color: #606266;
	gap: 5px;
}

.form-footer a {
	color: #409eff;
	text-decoration: none;
	transition: color 0.2s;
	font-weight: 500;
}

.form-footer a:hover {
	color: #66b1ff;
	text-decoration: underline;
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
