<template>
	<div class="teacher-course-container">
		<div class="page-header">
			<h2>我的课程</h2>
		</div>

		<div class="course-grid" v-if="courseList.length > 0">
			<div class="course-card" v-for="course in courseList" :key="course.id">
				<div class="card-header">
					<h3>{{ course.name }}</h3>
					<span class="badge">{{ formatSemester(course.semester) }}</span>
				</div>
				<div class="card-body">
					<p class="desc">{{ course.description || '暂无描述' }}</p>
					<div class="meta">
						<span class="credit">学分: {{ course.credits }}</span>
						<!-- Can add enrollment count if available in DTO -->
					</div>
				</div>
				<div class="card-footer">
					<button class="btn btn-primary block" @click="goToGrade(course)">管理成绩</button>
				</div>
			</div>
		</div>

		<div v-else class="empty-state">
			<p>您当前学期暂无授课任务。</p>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCourses } from '@/api/course'
import type { CourseDto } from '@/api/course'
import { useUserStore } from '@/store/user'
import { formatSemester } from '@/utils/semester'

const router = useRouter()
const userStore = useUserStore()
const courseList = ref<CourseDto[]>([])

onMounted(async () => {
	try {
		// API automatically filters by Teacher Role in backend CourseService
		// But we can also pass teacherId explicitly to be sure
		const res: any = await getCourses({
			page: 1,
			size: 100,
			teacherId: userStore.userInfo.id,
		})

		if (Array.isArray(res)) {
			courseList.value = res
		} else {
			courseList.value = res.list || []
		}
	} catch (e) {
		console.error(e)
	}
})

const goToGrade = (course: CourseDto) => {
	// Navigate to Grade Management, pre-selecting this course
	// We can use query param or state
	// Assuming TeacherGrade page reads 'courseId' from query or just lists courses
	// Let's pass query
	// Note: The TeacherGrade view logic currently selects based on dropdown.
	// We should enhance it to read query param on mount.
	router.push({ path: '/teacher/grades', query: { courseId: course.id } })
}
</script>

<style scoped>
.teacher-course-container {
	padding: 20px;
	animation: fadeIn 0.4s ease;
}

.page-header h2 {
	margin-top: 0;
	margin-bottom: 20px;
	color: #303133;
}

.course-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
	gap: 20px;
}

.course-card {
	background: #fff;
	border-radius: 8px;
	box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
	transition: all 0.3s;
	display: flex;
	flex-direction: column;
	border: 1px solid #ebeef5;
}

.course-card:hover {
	transform: translateY(-5px);
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
	border-color: #c6e2ff;
}

.card-header {
	padding: 15px 20px;
	border-bottom: 1px solid #f5f7fa;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.card-header h3 {
	margin: 0;
	font-size: 18px;
	color: #303133;
}

.badge {
	background: #ecf5ff;
	color: #409eff;
	padding: 2px 8px;
	border-radius: 12px;
	font-size: 12px;
}

.card-body {
	padding: 20px;
	flex: 1;
}

.desc {
	color: #606266;
	font-size: 14px;
	line-height: 1.5;
	margin-bottom: 15px;
	height: 42px; /* 2 lines */
	overflow: hidden;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
}

.meta {
	font-size: 13px;
	color: #909399;
}

.card-footer {
	padding: 15px 20px;
	background: #fcfcfc;
	border-top: 1px solid #f5f7fa;
}

.btn {
	padding: 10px 0;
	border-radius: 4px;
	border: none;
	cursor: pointer;
	font-weight: 500;
	transition: background 0.3s;
}

.btn.block {
	width: 100%;
	display: block;
}

.btn-primary {
	background: #409eff;
	color: #fff;
}
.btn-primary:hover {
	background: #66b1ff;
}

.empty-state {
	text-align: center;
	padding: 40px;
	color: #909399;
	background: #fff;
	border-radius: 8px;
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
