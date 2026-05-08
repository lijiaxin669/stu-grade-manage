<template>
	<div class="admin-overview">
		<h2>管理员概览</h2>

		<div class="stats-container" v-if="!loading">
			<div class="card">
				<h3>用户统计</h3>
				<div class="stat-item">
					<span class="label">总用户数:</span>
					<span class="value">{{ stats.userTotal }}</span>
				</div>
				<ul class="detail-list">
					<li>管理员: {{ stats.adminCount }}</li>
					<li>教师: {{ stats.teacherCount }}</li>
					<li>学生: {{ stats.studentCount }}</li>
				</ul>
			</div>

				<div class="card">
					<h3>课程统计</h3>
					<div class="stat-item">
						<span class="label">总课程数:</span>
						<span class="value">{{ stats.courseTotal }}</span>
					</div>
					<ul class="detail-list">
						<li>(支持按学期统计，共包含所有学期数据)</li>
					</ul>
				</div>
			</div>

		<div v-else class="loading">正在加载系统数据...</div>
	</div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { getUsers } from '@/api/user'
import { getCourses } from '@/api/course'

const loading = ref(true)
	const stats = reactive({
		userTotal: 0,
		adminCount: 0,
		teacherCount: 0,
		studentCount: 0,
		courseTotal: 0,
	})

	onMounted(async () => {
		try {
			const [usersRes, coursesRes] = await Promise.all([
				getUsers({ pageNum: 1, pageSize: 1000 }),
				getCourses({ page: 1, size: 1000 }),
			])

		const users = (usersRes as any).list || []
		stats.userTotal = (usersRes as any).total || users.length

		// ADMIN is merged into SUPER_ADMIN; count both for backward-compat.
		stats.adminCount = users.filter((u: any) => u.roleCode === 'ROLE_SUPER_ADMIN' || u.roleCode === 'ROLE_ADMIN').length
		stats.teacherCount = users.filter((u: any) => u.roleCode === 'ROLE_TEACHER').length
		stats.studentCount = users.filter((u: any) => u.roleCode === 'ROLE_STUDENT').length

				const courses = (coursesRes as any).list || (Array.isArray(coursesRes) ? coursesRes : [])
				stats.courseTotal = courses.length
		} catch (e) {
			console.error('Failed to load overview data', e)
		} finally {
			loading.value = false
		}
})
</script>

<style scoped>
.admin-overview {
	padding: 20px;
}
.stats-container {
	display: flex;
	gap: 20px;
	flex-wrap: wrap;
}
.card {
	background: #fff;
	padding: 20px;
	border-radius: 8px;
	box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
	flex: 1;
	min-width: 250px;
}
.card h3 {
	margin-top: 0;
	color: #333;
	border-bottom: 2px solid #f0f0f0;
	padding-bottom: 10px;
}
.stat-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin: 15px 0;
	font-size: 18px;
}
.value {
	font-weight: bold;
	color: #409eff;
	font-size: 24px;
}
.detail-list {
	list-style: none;
	padding: 0;
	margin: 0;
	color: #666;
	font-size: 14px;
}
.detail-list li {
	margin-bottom: 5px;
	display: flex;
	justify-content: space-between;
}
</style>
