<template>
	<div class="admin-layout">
		<div class="sidebar">
			<div class="logo">
				<h3>{{ consoleTitle }}</h3>
			</div>
			<ul class="menu">
				<router-link
					v-for="item in menuItems"
					:key="item.path"
					:to="item.path"
					custom
					v-slot="{ navigate, isActive }"
				>
					<li :class="{ active: isActive }" @click="navigate">
						<span class="icon">{{ item.icon }}</span> {{ item.title }}
					</li>
				</router-link>
			</ul>
		</div>
		<div class="main-content">
			<div class="navbar">
				<div class="breadcrumb">
					{{ currentRouteName }}
				</div>
				<div class="user-info">
					<span>{{ userStore.userInfo?.realName || userStore.userInfo?.username }} ({{ roleLabel }})</span>
					<button @click="handleLogout" class="logout-btn">退出</button>
				</div>
			</div>
			<div class="content-wrapper">
				<router-view />
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const currentRouteName = computed(() => route.meta.title || 'Dashboard')

const basePath = computed(() => {
	if (userStore.roles.includes('ROLE_TEACHER')) return '/teacher'
	return '/admin'
})

const consoleTitle = computed(() => (basePath.value === '/teacher' ? '教学后台' : '成绩管理后台'))

const roleLabel = computed(() => {
	if (userStore.roles.includes('ROLE_SUPER_ADMIN')) return '超级管理员'
	if (userStore.roles.includes('ROLE_TEACHER')) return '教师'
	if (userStore.roles.includes('ROLE_STUDENT')) return '学生'
	return '用户'
})

const menuItems = computed(() => {
	const root = permissionStore.addRoutes.find((r: any) => r.path === basePath.value)
	const children = (root?.children || []).filter((c: any) => !(c.meta && (c.meta as any).hidden))

	return children.map((c: any) => ({
		path: c.path.startsWith('/') ? c.path : `${basePath.value}/${c.path}`,
		title: c.meta?.title || c.name || c.path,
		icon: c.meta?.icon ? '•' : '•',
	}))
})

const handleLogout = async () => {
	await userStore.logout()
	router.push('/login')
}
</script>

<style scoped>
.admin-layout {
	display: flex;
	height: 100vh;
	width: 100vw;
	background-color: #f5f7fa;
}

.sidebar {
	width: 240px;
	background-color: #2c3e50;
	color: #fff;
	display: flex;
	flex-direction: column;
	border-right: 1px solid #e6e6e6;
}

.logo {
	height: 60px;
	display: flex;
	align-items: center;
	justify-content: center;
	border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.menu {
	list-style: none;
	padding: 0;
	margin: 0;
	flex: 1;
}

.menu li {
	padding: 15px 20px;
	cursor: pointer;
	display: flex;
	align-items: center;
	transition: background 0.3s;
	color: #aeb9c2;
}

.menu li:hover {
	background-color: #34495e;
	color: #fff;
}

.menu li.active {
	background-color: #1890ff;
	color: #fff;
}

.icon {
	margin-right: 10px;
	font-size: 1.2em;
}

.main-content {
	flex: 1;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.navbar {
	height: 60px;
	background: #fff;
	box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 20px;
	z-index: 10;
}

.breadcrumb {
	font-weight: 500;
	font-size: 16px;
	color: #333;
}

.user-info {
	display: flex;
	align-items: center;
	gap: 15px;
	font-size: 14px;
}

.logout-btn {
	padding: 5px 12px;
	border: 1px solid #dcdfe6;
	background: #fff;
	color: #606266;
	border-radius: 4px;
	cursor: pointer;
}

.logout-btn:hover {
	color: #1890ff;
	border-color: #c6e2ff;
	background-color: #ecf5ff;
}

.content-wrapper {
	flex: 1;
	padding: 20px;
	overflow-y: auto;
}
</style>
