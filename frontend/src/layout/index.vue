<template>
	<div class="app-wrapper">
		<div class="sidebar-container">
			<h3>Grade System</h3>
			<ul>
				<li v-for="route in permissionStore.routes" :key="route.path">
					<router-link v-if="!route.meta?.hidden && route.path !== '/'" :to="route.path">
						{{ route.meta?.title }}
					</router-link>
					<div v-else-if="route.children && route.path === '/'">
						<router-link :to="'/dashboard'">Dashboard</router-link>
					</div>
				</li>
			</ul>
		</div>
		<div class="main-container">
			<div class="header">
				<div class="navbar">
					<span>Welcome, {{ userStore.userInfo?.realName }}</span>
					<button @click="logout">Logout</button>
				</div>
			</div>
			<div class="app-main">
				<router-view />
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const permissionStore = usePermissionStore()
const router = useRouter()

const logout = async () => {
	await userStore.logout()
	router.push('/login')
}
</script>

<style scoped>
.app-wrapper {
	display: flex;
	width: 100vw;
	height: 100vh;
}
.sidebar-container {
	width: 200px;
	background: #2c3e50;
	color: white;
	padding: 20px;
}
.sidebar-container a {
	color: white;
	text-decoration: none;
	display: block;
	margin: 10px 0;
}
.main-container {
	flex: 1;
	display: flex;
	flex-direction: column;
}
.header {
	height: 50px;
	border-bottom: 1px solid #ddd;
	padding: 0 20px;
	display: flex;
	align-items: center;
	justify-content: flex-end;
}
.app-main {
	flex: 1;
	padding: 20px;
	overflow: auto;
}
</style>
