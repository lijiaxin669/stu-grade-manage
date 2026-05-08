<template>
	<div class="user-profile-container">
		<!-- 顶部 Header Banner -->
		<div class="profile-header">
			<div class="banner"></div>

			<div class="profile-content">
				<!-- 头像和基础信息悬浮区 -->
				<div class="header-info">
					<div class="avatar-wrapper">
						<div class="avatar">{{ userStore.userInfo?.realName?.charAt(0) }}</div>
						<div class="avatar-ring"></div>
					</div>
					<div class="user-names">
						<h2>{{ userStore.userInfo?.realName }}</h2>
						<div class="badges">
							<span class="role-badge">{{ formatRole(userStore.userInfo?.roleCode) }}</span>
							<span class="status-badge">已激活</span>
						</div>
					</div>
				</div>

				<!-- 主要内容网格 -->
				<div class="main-grid">
					<!-- 左侧：详细信息 -->
					<div class="info-card">
						<h3 class="card-title">基本资料</h3>
						<div class="detail-list">
							<div class="detail-item">
								<label>用户名</label>
								<span class="val">{{ userStore.userInfo?.username }}</span>
							</div>
							<div class="detail-item">
								<label>学号</label>
								<span class="val">{{ userStore.userInfo?.username }}</span>
							</div>
							<div class="detail-item">
								<label>注册时间</label>
								<span class="val">
									{{
										userStore.userInfo?.createdAt ? new Date(userStore.userInfo.createdAt).toLocaleDateString() : '未知'
									}}
								</span>
							</div>
							<div class="detail-item">
								<label>所属学院</label>
								<span class="val">计算机科学学院</span>
							</div>
						</div>
					</div>

					<!-- 右侧：安全设置 -->
					<div class="info-card">
						<h3 class="card-title">安全设置</h3>
						<form @submit.prevent="handleUpdatePassword" class="security-form">
							<div class="form-group">
								<label>当前密码</label>
								<input
									type="password"
									v-model="form.oldPassword"
									required
									class="form-input"
									placeholder="请输入当前密码"
								/>
							</div>
							<div class="form-group">
								<label>新密码</label>
								<input
									type="password"
									v-model="form.newPassword"
									required
									minlength="6"
									class="form-input"
									placeholder="请输入新密码（至少6位）"
								/>
							</div>
							<div class="form-group">
								<label>确认密码</label>
								<input
									type="password"
									v-model="form.confirmPassword"
									required
									minlength="6"
									class="form-input"
									placeholder="请再次输入新密码"
								/>
							</div>
							<div class="form-actions">
								<button type="submit" class="btn-submit" :disabled="loading">
									{{ loading ? '提交中...' : '保存更改' }}
								</button>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useUserStore } from '@/store/user'
import { updateMyPassword } from '@/api/user'
import Toast from '@/utils/toast'

const userStore = useUserStore()
const loading = ref(false)
const form = reactive({
	oldPassword: '',
	newPassword: '',
	confirmPassword: '',
})

const formatRole = (code?: string) => {
	if (!code) return ''
	const map: Record<string, string> = {
		ROLE_SUPER_ADMIN: '超级管理员',
		ROLE_ADMIN: '超级管理员', // backward-compat
		ROLE_TEACHER: '教师',
		ROLE_STUDENT: '学生',
	}
	return map[code] || code
}

const handleUpdatePassword = async () => {
	if (form.newPassword !== form.confirmPassword) {
		Toast.warning('两次新密码输入不一致')
		return
	}

	loading.value = true
	try {
		// Backend expects { oldPassword, newPassword }
		await updateMyPassword({
			oldPassword: form.oldPassword,
			newPassword: form.newPassword,
		})
		Toast.success('修改成功，请重新登录')
		// 延迟刷新，让用户看到成功提示
		setTimeout(async () => {
			await userStore.logout()
			window.location.href = '/login'
		}, 1500)
	} catch (e: any) {
		console.error(e)
		loading.value = false
	}
}
</script>

<style scoped>
.user-profile-container {
	padding: 0;
	animation: fadeIn 0.6s ease;
	background: #f4f5f7;
	min-height: calc(100vh - 64px); /* 减去顶部导航高度 */
	overflow-x: clip; /* 避免卡片阴影导致横向溢出/滚动条 */
}

/* Header Banner 区域 */
.profile-header {
	position: relative;
	background: #fff;
	/* margin-bottom 移到 user-names 下面撑开，或者让 header-info 撑开 header */
	padding-bottom: 20px;
	box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
	margin-bottom: 24px;
}

.banner {
	height: 160px; /* 稍微调小一点 */
	background: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);
	position: relative;
	overflow: hidden;
}

/* 装饰性背景图案 */
.banner::after {
	content: '';
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-image: radial-gradient(#fff 10%, transparent 10%);
	background-size: 30px 30px;
	opacity: 0.1;
}

.profile-content {
	max-width: 1000px;
	margin: 0 auto;
	padding: 0 24px;
	position: relative;
	overflow: visible; /* 确保阴影不被截断 */
}

/* 主内容区域额外底部间距 */
.main-content {
	padding-bottom: 40px; /* 增加底部 padding */
}

/* .header-wrapper removed */

/* 头像 & 基础信息 */
.header-info {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-top: -50px; /* 负边距实现叠加效果 */
	position: relative;
	z-index: 10;
	margin-bottom: 10px;
}

.avatar-wrapper {
	position: relative;
	margin-bottom: 16px;
}

.avatar {
	width: 120px;
	height: 120px;
	background: #fff;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 48px;
	color: #1890ff;
	border: 4px solid #fff;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
	position: relative;
	z-index: 2;
}

/* 旋转光环 */
.avatar-ring {
	position: absolute;
	top: -5px;
	left: -5px;
	right: -5px;
	bottom: -5px;
	border: 2px dashed rgba(255, 255, 255, 0.8);
	border-radius: 50%;
	animation: spin 20s linear infinite;
	z-index: 1;
}

.user-names {
	text-align: center;
}

.user-names h2 {
	margin: 0 0 8px 0;
	font-size: 24px;
	color: #2c3e50;
	font-weight: 700;
}

.badges {
	display: flex;
	gap: 10px;
	justify-content: center;
}

.role-badge {
	background: #e6f7ff;
	color: #1890ff;
	padding: 4px 12px;
	border-radius: 20px;
	font-size: 12px;
	font-weight: 600;
	border: 1px solid #91d5ff;
}

.status-badge {
	background: #f6ffed;
	color: #52c41a;
	padding: 4px 12px;
	border-radius: 20px;
	font-size: 12px;
	font-weight: 600;
	border: 1px solid #b7eb8f;
}

/* 主要内容网格布局 */
.main-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 24px;
	margin-bottom: 20px; /* 为底部阴影留出空间 */
	min-width: 0;
}
@media (max-width: 768px) {
	.main-grid {
		grid-template-columns: 1fr;
	}
}

.info-card {
	background: #fff;
	border-radius: 12px;
	padding: 30px;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
	/* height: 100%; */
	min-width: 0;
}
@media (max-width: 768px) {
	.info-card {
		padding: 18px;
	}
}

.card-title {
	margin: 0 0 24px 0;
	font-size: 18px;
	color: #2c3e50;
	font-weight: 600;
	padding-bottom: 12px;
	border-bottom: 1px solid #f0f0f0;
	display: flex;
	align-items: center;
	gap: 8px;
}

.card-title::before {
	content: '';
	width: 4px;
	height: 18px;
	background: #1890ff;
	border-radius: 2px;
}

/* 详情列表 */
.detail-list {
	display: flex;
	flex-direction: column;
	gap: 20px;
}

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-bottom: 16px;
	border-bottom: 1px dashed #f0f0f0;
}
.detail-item:last-child {
	border-bottom: none;
}

.detail-item label {
	color: #8c8c8c;
	font-size: 14px;
}

.detail-item .val {
	color: #2c3e50;
	font-weight: 600;
	font-size: 15px;
	min-width: 0;
	max-width: 65%;
	text-align: right;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

/* 表单样式 */
.security-form {
	max-width: 100%;
}

.form-group {
	margin-bottom: 20px;
	position: relative;
}

.form-group label {
	display: block;
	margin-bottom: 8px;
	color: #595959;
	font-weight: 500;
	font-size: 13px;
}

.form-input {
	width: 100%;
	padding: 12px 14px;
	border: 1px solid #d9d9d9;
	border-radius: 8px;
	font-size: 14px;
	transition: all 0.3s;
	background: #fafafa;
}

.form-input:focus {
	background: #fff;
	border-color: #1890ff;
	box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
	outline: none;
}

.form-actions {
	margin-top: 30px;
	display: flex;
	justify-content: flex-end;
}

.btn-submit {
	background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
	color: #fff;
	border: none;
	width: 100%; /* 全宽 */
	padding: 12px 32px;
	border-radius: 8px;
	font-weight: 600;
	cursor: pointer;
	box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
	transition: all 0.3s;
	margin-top: 10px;
}

.btn-submit:hover {
	transform: translateY(-2px);
	box-shadow: 0 8px 16px rgba(24, 144, 255, 0.4);
}

.btn-submit:active {
	transform: translateY(0);
}

.btn-submit:disabled {
	background: #d9d9d9;
	cursor: not-allowed;
	box-shadow: none;
	transform: none;
}

@keyframes fadeIn {
	from {
		opacity: 0;
		transform: translateY(20px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

@keyframes spin {
	from {
		transform: rotate(0deg);
	}
	to {
		transform: rotate(360deg);
	}
}
</style>
