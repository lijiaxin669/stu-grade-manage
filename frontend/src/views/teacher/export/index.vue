<template>
	<div class="teacher-export-container">
		<div class="page-header">
			<h2>成绩导出</h2>
		</div>

		<div class="main-card card">
			<div class="step-guide">
				<div class="step active">1. 选择课程</div>
				<div class="line"></div>
				<div class="step" :class="{ active: selectedCourseId }">2. 导出文件</div>
			</div>

			<div class="form-section">
				<div class="form-item">
					<label>选择课程:</label>
					<select v-model="selectedCourseId" class="course-select">
						<option value="" disabled>-- 请选择 --</option>
						<option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name }}（{{ formatSemester(c.semester) }}）</option>
					</select>
				</div>

				<div class="action-area" v-if="selectedCourseId">
					<div class="info-preview">
						<p>
							即将导出 <strong>{{ getSelectedCourseName() }}</strong> 的成绩单。
						</p>
						<p class="sub-text">包含所有已录入的学生成绩。</p>
					</div>

					<button class="btn btn-primary export-btn" @click="handleExport" :disabled="loading">
						<span v-if="!loading">📥 立即导出 Excel</span>
						<span v-else>⏳ 正在生成文件... ({{ progress }}%)</span>
					</button>

					<div class="status-msg" v-if="statusMsg" :class="statusType">
						{{ statusMsg }}
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCourses } from '@/api/course'
import { exportGrades } from '@/api/grade'
import { useUserStore } from '@/store/user'
import { formatSemester } from '@/utils/semester'

const userStore = useUserStore()
const courses = ref<any[]>([])
const selectedCourseId = ref<number | string>('')
const loading = ref(false)
const progress = ref(0)
const statusMsg = ref('')
const statusType = ref('')

onMounted(async () => {
	try {
		const res: any = await getCourses({
			page: 1,
			size: 100,
			teacherId: userStore.userInfo.id,
		})
		courses.value = Array.isArray(res) ? res : res.list || []
	} catch (e) {
		console.error(e)
	}
})

const getSelectedCourseName = () => {
	const c = courses.value.find((item) => item.id === Number(selectedCourseId.value))
	return c ? c.name : ''
}

const handleExport = async () => {
	if (!selectedCourseId.value) return

	loading.value = true
	progress.value = 0
	statusMsg.value = ''

	// Simulate Progress
	const timer = setInterval(() => {
		if (progress.value < 90) {
			progress.value += 10
		}
	}, 200)

	try {
		const id = Number(selectedCourseId.value)
		const blob: Blob = await exportGrades(id)

		// Complete progress
		clearInterval(timer)
		progress.value = 100

		// Download
		const url = window.URL.createObjectURL(blob)
		const link = document.createElement('a')
		link.href = url
		const courseName = String(getSelectedCourseName() || id).replace(/[\\/:*?"<>|]/g, '_')
		const dateStr = new Date().toISOString().slice(0, 10)
		link.setAttribute('download', `成绩单_${courseName}_${dateStr}.xlsx`)
		document.body.appendChild(link)
		link.click()
		document.body.removeChild(link)
		window.URL.revokeObjectURL(url)

		statusMsg.value = '导出成功！文件已开始下载。'
		statusType.value = 'success'
	} catch (e: any) {
		clearInterval(timer)
		statusMsg.value = '导出失败: ' + (e.message || '后端服务异常')
		statusType.value = 'error'
	} finally {
		setTimeout(() => {
			loading.value = false
			progress.value = 0
		}, 1000)
	}
}
</script>

<style scoped>
.teacher-export-container {
	padding: 20px;
	animation: fadeIn 0.3s;
}

.page-header h2 {
	margin-top: 0;
	color: #303133;
}

.card {
	background: #fff;
	padding: 40px;
	border-radius: 4px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
	max-width: 600px;
	margin: 0 auto;
}

/* Steps */
.step-guide {
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 40px;
}
.step {
	font-weight: bold;
	color: #c0c4cc;
	transition: color 0.3s;
}
.step.active {
	color: #409eff;
}
.line {
	width: 60px;
	height: 2px;
	background: #ebeef5;
	margin: 0 15px;
}

.form-section {
	text-align: center;
}
.course-select {
	padding: 10px;
	font-size: 16px;
	width: 300px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	margin-left: 10px;
}

.action-area {
	margin-top: 40px;
	padding-top: 20px;
	border-top: 1px solid #f5f7fa;
}

.info-preview {
	margin-bottom: 20px;
	color: #606266;
}
.sub-text {
	font-size: 12px;
	color: #909399;
	margin-top: 5px;
}

.export-btn {
	padding: 12px 30px;
	font-size: 16px;
	border-radius: 4px;
	border: none;
	cursor: pointer;
	background: #409eff;
	color: #fff;
	transition: all 0.3s;
}
.export-btn:hover {
	background: #66b1ff;
}
.export-btn:disabled {
	background: #a0cfff;
	cursor: not-allowed;
}

.status-msg {
	margin-top: 20px;
	padding: 10px;
	border-radius: 4px;
	font-size: 14px;
}
.success {
	background: #f0f9eb;
	color: #67c23a;
}
.error {
	background: #fef0f0;
	color: #f56c6c;
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
