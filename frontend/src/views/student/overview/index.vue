<template>
	<div class="student-overview">
		<div class="welcome-banner">
			<div class="text">
				<h2>👋 你好, {{ userStore.userInfo?.realName }}同学</h2>
				<p>学号: {{ userStore.userInfo?.username }} | 当前学期: 2026-春季</p>
			</div>
			<div class="illustration">🎓</div>
		</div>

		<div class="stats-row">
			<div class="stat-card blue">
				<div class="count">{{ stats.courseCount }}</div>
				<div class="label">已选课程</div>
			</div>
			<div class="stat-card green">
				<div class="count">{{ stats.gradedCount }}</div>
				<div class="label">已出成绩</div>
			</div>
			<div class="stat-card purple">
				<div class="count">{{ stats.avgScore || '-' }}</div>
				<div class="label">平均分</div>
			</div>
		</div>

		<div class="recent-grades section">
			<h3>最新成绩</h3>
			<div class="grade-list" v-if="recentGrades.length > 0">
				<div class="grade-item" v-for="g in recentGrades" :key="g.id">
					<div class="left">
						<div class="course-name">{{ g.courseName }}</div>
						<div class="time">{{ formatDate(g.gradedAt) }}</div>
					</div>
					<div class="score" :class="getScoreClass(g.score)">{{ g.score }}</div>
				</div>
			</div>
			<div v-else class="empty-text">暂无最新成绩</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { reactive, onMounted, ref } from 'vue'
import { useUserStore } from '@/store/user'
import { getGrades } from '@/api/grade'
import { getCourses } from '@/api/course'

const userStore = useUserStore()
const stats = reactive({
	courseCount: 0,
	gradedCount: 0,
	avgScore: '',
})
const recentGrades = ref<any[]>([])

onMounted(async () => {
	try {
		const studentId = userStore.userInfo?.id
		const courseRes: any = await getCourses({ page: 1, size: 1000, studentId })
		const courses = Array.isArray(courseRes) ? courseRes : courseRes?.list || []
		stats.courseCount = courses.length

		// 2. Get Grades
		const gradeRes: any = await getGrades({})
		const allGrades = Array.isArray(gradeRes) ? gradeRes : gradeRes.list || []
		const grades = allGrades
			.slice()
			.sort((a: any, b: any) => new Date(b.gradedAt).getTime() - new Date(a.gradedAt).getTime())
			.slice(0, 5)

		if (grades.length > 0) {
			recentGrades.value = grades
			stats.gradedCount = allGrades.length

			// Calc Average (Simple Frontend approximation from visible or need backend agg)
			// Backend Agg is better. For now simple average of recent ones or '-'
			// Let's leave Average as '-' if backend doesn't provide it in "My Stats"
			// Or sum visible grades
			const totalScore = grades.reduce((acc: number, curr: any) => acc + curr.score, 0)
			stats.avgScore = (totalScore / grades.length).toFixed(1)
		}
	} catch (e) {
		console.error(e)
	}
})

const formatDate = (str: string) => {
	if (!str) return ''
	return new Date(str).toLocaleDateString()
}

const getScoreClass = (score: number) => {
	if (score >= 90) return 'text-success'
	if (score < 60) return 'text-danger'
	return 'text-primary'
}
</script>

<style scoped>
.student-overview {
	padding: 20px;
	animation: fadeIn 0.6s ease;
	max-width: 1100px;
	margin: 0 auto;
	overflow-x: clip; /* 避免卡片阴影/动画导致横向溢出 */
}

.welcome-banner {
	background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
	color: #fff;
	padding: 40px;
	border-radius: 16px;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 10px 30px rgba(24, 144, 255, 0.25);
	margin-bottom: 40px;
	position: relative;
	overflow: hidden;
}

.welcome-banner::after {
	content: '';
	position: absolute;
	top: 0;
	right: 0;
	bottom: 0;
	left: 0;
	background: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0IiBoZWlnaHQ9IjQiPgo8cmVjdCB3aWR0aD0iNCIgaGVpZ2h0PSI0IiBmaWxsPSIjZmZmIiBmaWxsLW9wYWNpdHk9IjAuMSIvPgo8L3N2Zz4=');
	opacity: 0.3;
}

.welcome-banner .text {
	position: relative;
	z-index: 1;
}

.welcome-banner h2 {
	margin: 0 0 12px 0;
	font-size: 28px;
	font-weight: 700;
	letter-spacing: 0.5px;
}

.welcome-banner p {
	margin: 0;
	opacity: 0.9;
	font-size: 15px;
}

.illustration {
	font-size: 80px;
	opacity: 0.9;
	transform: rotate(10deg);
	filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.stats-row {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 24px;
	margin-bottom: 40px;
	min-width: 0;
}

.stat-card {
	background: #fff;
	padding: 24px;
	border-radius: 12px;
	text-align: center;
	box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
	transition: all 0.3s ease;
	border: 1px solid #f0f0f0;
	position: relative;
	overflow: hidden;
	min-width: 0;
}

.stat-card:hover {
	transform: translateY(-5px);
	box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.stat-card::before {
	content: '';
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	height: 4px;
	background: #e6e6e6;
}

.stat-card .count {
	font-size: 42px;
	font-weight: 800;
	margin-bottom: 8px;
	color: #2c3e50;
	line-height: 1;
}

.stat-card .label {
	color: #8c8c8c;
	font-size: 14px;
	font-weight: 500;
}

.stat-card.blue::before {
	background: linear-gradient(90deg, #1890ff, #69c0ff);
}
.stat-card.blue .count {
	color: #1890ff;
}

.stat-card.green::before {
	background: linear-gradient(90deg, #52c41a, #95de64);
}
.stat-card.green .count {
	color: #52c41a;
}

.stat-card.purple::before {
	background: linear-gradient(90deg, #722ed1, #b37feb);
}
.stat-card.purple .count {
	color: #722ed1;
}

.section {
	background: #fff;
	padding: 30px;
	border-radius: 16px;
	box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
	border: 1px solid #f0f0f0;
}

.section h3 {
	margin-top: 0;
	margin-bottom: 24px;
	color: #2c3e50;
	font-size: 20px;
	font-weight: 600;
	display: flex;
	align-items: center;
	gap: 10px;
}

.section h3::before {
	content: '';
	width: 4px;
	height: 20px;
	background: #1890ff;
	border-radius: 2px;
	display: block;
}

.grade-list {
	display: flex;
	flex-direction: column;
	gap: 16px;
}

.grade-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 18px 24px;
	background: #f8f9fa;
	border-radius: 10px;
	transition: all 0.3s;
	border: 1px solid transparent;
}

.grade-item:hover {
	background: #fff;
	border-color: #e6f7ff;
	box-shadow: 0 4px 15px rgba(24, 144, 255, 0.1);
	transform: translateX(5px);
}

.course-name {
	font-weight: 600;
	font-size: 16px;
	color: #2c3e50;
	margin-bottom: 6px;
}

.time {
	font-size: 13px;
	color: #8c8c8c;
}

.score {
	font-size: 32px;
	font-weight: 700;
	font-family: 'DIN Alternate', 'Arial', sans-serif;
	min-width: 60px;
	text-align: right;
}

.text-success {
	color: #52c41a;
}
.text-danger {
	color: #ff4d4f;
}
.text-primary {
	color: #1890ff;
}

.empty-text {
	color: #8c8c8c;
	text-align: center;
	padding: 40px;
	background: #f9f9f9;
	border-radius: 8px;
	border: 1px dashed #d9d9d9;
}

@media (max-width: 768px) {
	.student-overview {
		padding: 16px;
	}
	.welcome-banner {
		padding: 20px;
		flex-direction: column;
		align-items: flex-start;
		gap: 12px;
	}
	.illustration {
		align-self: flex-end;
		font-size: 56px;
	}
	.stats-row {
		grid-template-columns: 1fr;
		gap: 16px;
	}
	.section {
		padding: 18px;
	}
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
</style>
