<template>
	<div class="my-courses-container">
		<div class="page-header">
			<h2>我的课程</h2>
		</div>

		<div class="courses-grid" v-if="courseList.length > 0">
			<div class="course-card" v-for="c in courseList" :key="c.id">
				<div class="cover">
					<div class="semester-tag">{{ formatSemester(c.semester) }}</div>
					<div class="icon">📖</div>
				</div>
				<div class="card-body">
					<h3>{{ c.name }}</h3>
					<div class="info-row">
						<span class="label">学分:</span>
						<span class="value">{{ c.credits }}</span>
					</div>
					<div class="info-row">
						<span class="label">教师:</span>
						<span class="value">{{ c.teacherName || '未知' }}</span>
					</div>
					<div class="desc">{{ c.description || '暂无描述' }}</div>
				</div>
			</div>
		</div>

		<div v-else class="empty-state">
			<div class="empty-icon">📭</div>
			<p>暂无已选课程，请联系教务处选课。</p>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCourses } from '@/api/course'
import { useUserStore } from '@/store/user'
import { formatSemester } from '@/utils/semester'

const userStore = useUserStore()
const courseList = ref<any[]>([])

onMounted(async () => {
	try {
		// 传入当前学生 ID，只获取已选课程
		const studentId = userStore.userInfo?.id
		const res: any = await getCourses({ page: 1, size: 1000, studentId })
		courseList.value = Array.isArray(res) ? res : res?.list || []
	} catch (e) {
		console.error(e)
	}
})
</script>

<style scoped>
.my-courses-container {
	padding: 24px;
	animation: fadeIn 0.6s ease;
	max-width: 1200px;
	margin: 0 auto;
}

.page-header {
	margin-bottom: 30px;
}

.page-header h2 {
	margin: 0;
	color: #2c3e50;
	font-size: 24px;
	font-weight: 600;
	position: relative;
	padding-left: 16px;
}

.page-header h2::before {
	content: '';
	position: absolute;
	left: 0;
	top: 50%;
	transform: translateY(-50%);
	width: 6px;
	height: 24px;
	background: linear-gradient(180deg, #52c41a, #95de64);
	border-radius: 3px;
}

/* 横向列表布局改造 */
.courses-grid {
	display: flex;
	flex-direction: column;
	gap: 20px;
	max-width: 1000px; /* 限制最大宽度，防止横向过长 */
	margin: 0 auto; /* 居中 */
}

.course-card {
	background: #fff;
	border-radius: 12px;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
	overflow: hidden;
	transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
	border: 1px solid #f0f0f0;
	display: flex;
	flex-direction: row; /* 横向排列 */
	height: 160px; /* 固定高度 */
}

/* 移动端/小屏幕适配 */
@media (max-width: 768px) {
	.course-card {
		flex-direction: column;
		height: auto;
	}
	.cover {
		width: 100% !important;
		height: 120px !important;
		clip-path: none !important;
	}
	.cover {
		clip-path: polygon(0 0, 100% 0, 100% 85%, 0 100%);
	}
}

.course-card:hover {
	transform: translateY(-4px) translateX(4px);
	box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
	border-color: #e6f7ff;
}

.cover {
	width: 240px; /* 左侧固定宽度 */
	height: 100%;
	background: linear-gradient(135deg, #69c0ff 0%, #1890ff 100%);
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	clip-path: polygon(0 0, 100% 0, 85% 100%, 0 100%); /* 斜切效果 */
}

.course-card:nth-child(3n + 1) .cover {
	background: linear-gradient(135deg, #ff9c6e 0%, #ff4d4f 100%);
}
.course-card:nth-child(3n + 2) .cover {
	background: linear-gradient(135deg, #95de64 0%, #52c41a 100%);
}
.course-card:nth-child(3n + 3) .cover {
	background: linear-gradient(135deg, #b37feb 0%, #722ed1 100%);
}

.icon {
	font-size: 48px;
	filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.2));
	transform: scale(1);
	transition: transform 0.4s;
}

.course-card:hover .icon {
	transform: scale(1.1) rotate(5deg);
}

.semester-tag {
	position: absolute;
	top: 12px;
	left: 12px; /* 改为左上角 */
	right: auto;
	background: rgba(255, 255, 255, 0.25);
	backdrop-filter: blur(4px);
	color: #fff;
	padding: 4px 10px;
	border-radius: 20px;
	font-size: 12px;
	font-weight: 600;
	letter-spacing: 0.5px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-body {
	padding: 24px 30px;
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.card-body h3 {
	margin: 0 0 12px 0;
	font-size: 20px;
	font-weight: 700;
	color: #2c3e50;
	display: flex;
	align-items: center;
	gap: 10px;
}

.info-row {
	display: inline-flex;
	margin-right: 20px;
	margin-bottom: 8px;
	font-size: 14px;
	background: #f5f7fa;
	padding: 4px 10px;
	border-radius: 4px;
	color: #606266;
}

.label {
	color: #909399;
	margin-right: 6px;
}

.value {
	color: #2c3e50;
	font-weight: 600;
}

.desc {
	margin-top: 12px;
	font-size: 14px;
	color: #606266;
	line-height: 1.6;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
	background: transparent;
	padding: 0;
}

.empty-state {
	text-align: center;
	padding: 80px;
	background: #fff;
	border-radius: 12px;
	color: #8c8c8c;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
	border: 1px dashed #d9d9d9;
}

.empty-icon {
	font-size: 64px;
	margin-bottom: 16px;
	opacity: 0.5;
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
