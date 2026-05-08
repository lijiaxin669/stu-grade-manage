<template>
	<div class="user-layout">
		<header class="top-header">
			<div class="header-container">
				<div class="logo">
					<h2>🎓 学生服务中心</h2>
				</div>
				<nav class="top-nav">
					<router-link
						v-for="item in menuItems"
						:key="item.path"
						:to="item.path"
						custom
						v-slot="{ navigate, isActive }"
					>
						<span :class="{ active: isActive }" @click="navigate">{{ item.title }}</span>
					</router-link>
				</nav>
				<div class="user-actions">
					<router-link to="/student/profile" class="username-link">
						<span>你好, {{ userStore.userInfo?.realName }}</span>
					</router-link>
					<button @click="handleLogout">退出登录</button>
				</div>
			</div>
		</header>
		<main class="main-container">
			<div class="content-wrapper">
				<router-view />
			</div>
		</main>
		<!-- 
		<footer class="footer">
			<div class="content-wrapper">
				<p>© 2026 Student Grade Management System</p>
			</div>
		</footer>
		--></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const menuItems = computed(() => {
	const root = permissionStore.addRoutes.find((r: any) => r.path === '/student')
	const children = (root?.children || []).filter((c: any) => !(c.meta && (c.meta as any).hidden))
	return children.map((c: any) => ({
		path: c.path.startsWith('/') ? c.path : `/student/${c.path}`,
		title: c.meta?.title || c.name || c.path,
	}))
})

const handleLogout = async () => {
	await userStore.logout()
	router.push('/login')
}
</script>

<style scoped>
.user-layout {
	min-height: 100vh;
	background-color: #f4f5f7;
	display: flex;
	flex-direction: column;
}

.top-header {
	background: #fff;
	height: 64px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
	position: sticky;
	top: 0;
	z-index: 100;
}

/* Header 专用容器：Flex布局 */
.header-container {
	max-width: 1200px;
	margin: 0 auto;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 20px;
	height: 100%; /* Header 高度固定，这里保留 100% 没问题，或者直接用 flex align center */
}

/* 通用内容容器：块级布局 */
.content-wrapper {
	max-width: 1200px;
	margin: 0 auto;
	padding: 0 20px;
	/* height: 100%; REMOVED */
}

.logo h2 {
	margin: 0;
	color: #1890ff;
	font-size: 20px;
}

.top-nav {
	display: flex;
	gap: 30px;
}

.top-nav span {
	cursor: pointer;
	padding: 0 5px;
	height: 64px;
	display: flex;
	align-items: center;
	border-bottom: 2px solid transparent;
	font-size: 16px;
	color: #333;
	transition: all 0.3s;
}

.top-nav span:hover {
	color: #1890ff;
}

.top-nav span.active {
	color: #1890ff;
	border-bottom-color: #1890ff;
}

.user-actions {
	display: flex;
	align-items: center;
	gap: 15px;
	font-size: 14px;
}

.user-actions button {
	border: 1px solid #d9d9d9;
	background: transparent;
	border-radius: 4px;
	padding: 4px 12px;
	cursor: pointer;
}

.user-actions button:hover {
	color: #ff4d4f;
	border-color: #ff4d4f;
}

.main-container {
	flex: 1;
	padding: 30px 0 60px 0; /* 增加底部 padding 到 60px */
}

/* Footer Removed
.footer {
	background: #fff;
	padding: 20px 0;
	text-align: center;
	color: #999;
	font-size: 13px;
	border-top: 1px solid #eee;
}
*/
</style>
