<template>
	<div class="teacher-stats-container">
		<div class="page-header">
			<h2>成绩统计</h2>
		</div>

		<div class="filter-bar">
			<label>学期:</label>
			<select v-model="semester" @change="fetchData">
				<option value="">全部</option>
				<option value="2026-Spring">2026-春季</option>
				<option value="2025-Fall">2025-秋季</option>
			</select>
			<button class="btn btn-primary" @click="fetchData">刷新</button>
		</div>

		<div class="stats-table card" v-if="!loading">
			<table>
				<thead>
					<tr>
						<th>课程名称</th>
						<th>学期</th>
						<th>总人数</th>
						<th>及格人数</th>
						<th>及格率</th>
						<th>平均分</th>
						<th>最高分</th>
						<th>最低分</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="item in statsList" :key="item.courseId">
						<td>
							<strong>{{ item.courseName }}</strong>
						</td>
						<td>{{ formatSemester(item.semester) }}</td>
						<td>{{ item.totalCount }}</td>
						<td>{{ item.passedCount }}</td>
						<td>
							<div class="progress-bar">
								<div
									class="progress"
									:style="{ width: item.passRate * 100 + '%' }"
									:class="getRateColor(item.passRate)"
								></div>
								<span class="progress-text">{{ (item.passRate * 100).toFixed(1) }}%</span>
							</div>
						</td>
						<td>
							<span :class="getScoreColor(item.avgScore)">{{ formatScore(item.avgScore, 1) }}</span>
						</td>
						<td>{{ formatScore(item.maxScore, 1) }}</td>
						<td>{{ formatScore(item.minScore, 1) }}</td>
					</tr>
					<tr v-if="statsList.length === 0">
						<td colspan="8" class="empty-text">当前学期暂无课程数据</td>
					</tr>
				</tbody>
			</table>
		</div>
		<div v-else class="loading">加载中...</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCourses } from '@/api/course'
import { getStats, type CourseStatisticsDto } from '@/api/grade'
import { useUserStore } from '@/store/user'
import { formatSemester } from '@/utils/semester'

const userStore = useUserStore()
const semester = ref('2026-Spring')
const loading = ref(false)
const statsList = ref<any[]>([])

onMounted(() => {
	fetchData()
})

const fetchData = async () => {
	loading.value = true
	statsList.value = []
	try {
		// 1. Get Courses
		const courseRes: any = await getCourses({
			page: 1,
			size: 100,
			teacherId: userStore.userInfo.id,
		})
		const courses = Array.isArray(courseRes) ? courseRes : courseRes.list || []

		// Filter by semester if selected
		const targetCourses = semester.value ? courses.filter((c: any) => c.semester === semester.value) : courses

		// 2. Fetch Stats for each course (Parallel)
		const promises = targetCourses.map(async (c: any) => {
			try {
				const s: CourseStatisticsDto = await getStats({ courseId: c.id })
				return {
					courseId: c.id,
					courseName: c.name,
					semester: c.semester,
					totalCount: s.studentCount,
					passedCount: s.passedCount,
					passRate: s.passRate,
					avgScore: s.averageScore,
					maxScore: s.maxScore,
					minScore: s.minScore,
				}
			} catch (e) {
				// Return basic info if no stats (e.g. empty)
				return {
					courseId: c.id,
					courseName: c.name,
					semester: c.semester,
					totalCount: 0,
					passedCount: 0,
					passRate: 0,
					avgScore: null,
					maxScore: null,
					minScore: null,
				}
			}
		})

		statsList.value = await Promise.all(promises)
	} catch (e) {
		console.error(e)
	} finally {
		loading.value = false
	}
}

const getRateColor = (rate: number) => {
	if (rate >= 0.9) return 'bg-success'
	if (rate >= 0.6) return 'bg-warning'
	return 'bg-danger'
}

const getScoreColor = (score: number) => {
	if (score == null) return ''
	if (score >= 80) return 'text-success'
	if (score < 60) return 'text-danger'
	return ''
}

const formatScore = (val: any, digits = 1) => {
	if (val == null) return '-'
	const n = typeof val === 'number' ? val : Number(val)
	if (Number.isNaN(n)) return '-'
	return n.toFixed(digits)
}
</script>

<style scoped>
.teacher-stats-container {
	padding: 20px;
	animation: fadeIn 0.3s;
}

.page-header h2 {
	margin-top: 0;
	color: #303133;
}

.filter-bar {
	background: #fff;
	padding: 15px 20px;
	border-radius: 4px;
	margin-bottom: 20px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
	display: flex;
	align-items: center;
	gap: 15px;
}
.filter-bar select {
	padding: 6px 10px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	min-width: 150px;
}

.card {
	background: #fff;
	padding: 20px;
	border-radius: 4px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

table {
	width: 100%;
	border-collapse: collapse;
}
th,
td {
	padding: 12px 15px;
	text-align: left;
	border-bottom: 1px solid #ebeef5;
	font-size: 14px;
}
th {
	background: #f5f7fa;
	color: #909399;
}
.empty-text {
	text-align: center;
	color: #909399;
	padding: 20px;
}

/* Progress Bar */
.progress-bar {
	background: #ebeef5;
	height: 16px;
	border-radius: 8px;
	position: relative;
	overflow: hidden;
	width: 100px;
	display: inline-block;
	vertical-align: middle;
}
.progress {
	height: 100%;
	transition: width 0.5s;
}
.progress-text {
	font-size: 12px;
	margin-left: 8px;
	vertical-align: middle;
}
.bg-success {
	background: #67c23a;
}
.bg-warning {
	background: #e6a23c;
}
.bg-danger {
	background: #f56c6c;
}

.text-success {
	color: #67c23a;
	font-weight: bold;
}
.text-danger {
	color: #f56c6c;
	font-weight: bold;
}

.btn {
	padding: 6px 16px;
	background: #409eff;
	color: #fff;
	border: none;
	border-radius: 4px;
	cursor: pointer;
}
.btn:hover {
	background: #66b1ff;
}

@keyframes fadeIn {
	from {
		opacity: 0;
	}
	to {
		opacity: 1;
	}
}
</style>
