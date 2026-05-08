<template>
	<div class="grade-teacher">
		<div class="page-header">
			<h2>成绩管理</h2>
		</div>

		<div class="toolbar">
			<div class="selector">
				<label>选择课程:</label>
				<select v-model="selectedCourseId" @change="handleCourseChange">
					<option value="" disabled>-- 请选择 --</option>
					<option v-for="c in courses" :key="c.id" :value="c.id">
						{{ c.name }}（{{ formatSemester(c.semester) }}）
					</option>
				</select>
			</div>

			<div class="actions" v-if="selectedCourseId">
				<button class="btn btn-warning" @click="handleExport">导出 Excel</button>
				<button class="btn btn-info" @click="handleStats">统计分析</button>
			</div>
		</div>

		<div v-if="selectedCourseId" class="content-area">
			<div class="table-card">
				<table>
					<thead>
						<tr>
							<th>学号</th>
							<th>姓名</th>
							<th>用户名</th>
							<th>成绩</th>
							<th>创建时间</th>
							<th>操作</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="row in studentList" :key="row.studentId">
							<td>{{ row.studentId }}</td>
							<td>{{ row.studentName }}</td>
							<td>{{ row.studentUsername }}</td>
							<td>
								<span v-if="row.grade" :class="getScoreClass(row.grade.score)">{{ row.grade.score }}</span>
								<span v-else class="text-gray">未录入</span>
							</td>
							<td>{{ row.grade ? formatDate(row.grade.gradedAt) : '-' }}</td>
							<td>
								<button class="btn-link" @click="openGradeModal(row)">
									{{ row.grade ? '修改' : '录入' }}
								</button>
								<button v-if="row.grade" class="btn-link danger" @click="handleDelete(row.grade.id)">删除</button>
							</td>
						</tr>
						<tr v-if="studentList.length === 0">
							<td colspan="6" class="empty-text">该课程暂无学生。</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<div v-else class="placeholder-card">
			<div class="placeholder-content">
				<span class="icon">👈</span>
				<p>请选择左上角的课程以开始管理成绩。</p>
			</div>
		</div>

		<!-- Input Modal -->
		<div v-if="showModal" class="modal-mask">
			<div class="modal-content">
				<div class="modal-header">
					<h3>{{ currentStudent?.grade ? '修改成绩' : '录入成绩' }} - {{ currentStudent?.studentName }}</h3>
					<span class="close-btn" @click="showModal = false">×</span>
				</div>
				<form @submit.prevent="submitGrade">
					<div class="modal-body">
						<div class="form-group">
							<label>分数 (0-100) <span class="required">*</span></label>
							<input type="number" v-model.number="form.score" min="0" max="100" step="0.5" required ref="scoreInput" />
							<p class="hint">按回车提交</p>
						</div>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" @click="showModal = false">取消</button>
						<button type="submit" class="btn btn-primary" :disabled="submitting">保存</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCourses } from '@/api/course'
import {
	getGrades,
	inputGrade,
	updateGrade,
	deleteGrade,
	exportGrades,
	getStats,
	type CourseStatisticsDto,
} from '@/api/grade'
import { formatSemester } from '@/utils/semester'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import Toast from '@/utils/toast'

/* API Helper to get Enrollments */
const getEnrollmentsByCourse = (courseId: number) => {
	return request({ url: '/enrollments', method: 'get', params: { courseId } })
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// Data
const courses = ref<any[]>([])
const selectedCourseId = ref<number | string>('')
const studentList = ref<any[]>([])
const showModal = ref(false)
const submitting = ref(false)
const currentStudent = ref<any>(null)
const form = reactive({ score: 0 })
const scoreInput = ref<HTMLInputElement | null>(null)

onMounted(async () => {
	await fetchCourses()
	// Check query param
	if (route.query.courseId) {
		selectedCourseId.value = Number(route.query.courseId)
		handleCourseChange()
	}
})

const fetchCourses = async () => {
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
}

const handleCourseChange = async () => {
	if (!selectedCourseId.value) return
	const cid = Number(selectedCourseId.value)

	// Update URL query without reload
	router.replace({ query: { courseId: cid } })

	try {
		// 1. Get Enrollments (Roaster)
		const enrollRes: any = await getEnrollmentsByCourse(cid)
		const enrollments = Array.isArray(enrollRes) ? enrollRes : enrollRes?.list || []

		// 2. Get Existing Grades
		const gradeRes: any = await getGrades({ courseId: cid, page: 1, size: 1000 })
		const grades = Array.isArray(gradeRes) ? gradeRes : gradeRes.list || []

		// 3. Merge
		const gradeMap = new Map()
		grades.forEach((g: any) => gradeMap.set(g.studentId, g))

		studentList.value = enrollments.map((e: any) => ({
			studentId: e.studentId,
			studentName: e.studentName,
			studentUsername: e.studentUsername,
			grade: gradeMap.get(e.studentId) || null,
		}))
	} catch (e: any) {
		console.error(e)
	}
}

const getScoreClass = (score: number) => {
	if (score < 60) return 'text-danger'
	if (score >= 90) return 'text-success'
	return ''
}

const formatDate = (str: string) => {
	if (!str) return '-'
	return new Date(str).toLocaleString()
}

// CRUD
const openGradeModal = (row: any) => {
	currentStudent.value = row
	form.score = row.grade ? row.grade.score : 0
	showModal.value = true
	// Focus input
	nextTick(() => {
		scoreInput.value?.focus()
	})
}

const submitGrade = async () => {
	if (!currentStudent.value) return
	submitting.value = true
	try {
		if (currentStudent.value.grade) {
			await updateGrade(currentStudent.value.grade.id, { score: Number(form.score) })
		} else {
			await inputGrade({
				courseId: Number(selectedCourseId.value),
				studentId: currentStudent.value.studentId,
				score: Number(form.score),
			})
		}
		showModal.value = false
		// Update local list slightly optimized or reload
		handleCourseChange()
		Toast.success('保存成功')
	} catch (e: any) {
		console.error(e)
	} finally {
		submitting.value = false
	}
}

import { confirm } from '@/utils/confirm'

// ... existing imports

const handleDelete = async (id: number) => {
	const ok = await confirm({
		type: 'danger',
		title: '删除成绩',
		message: '确定要删除该成绩吗？',
		confirmText: '确认删除',
	})
	if (!ok) return
	try {
		await deleteGrade(id)
		handleCourseChange()
		Toast.success('删除成功')
	} catch (e: any) {
		console.error(e)
	}
}

const handleStats = async () => {
	if (!selectedCourseId.value) return
	try {
		const res: CourseStatisticsDto = await getStats({ courseId: Number(selectedCourseId.value) })
		Toast.info(`平均分: ${res.averageScore} | 及格率: ${Number(res.passRate * 100).toFixed(1)}%`)
	} catch (e: any) {
		console.error(e)
	}
}

const handleExport = async () => {
	if (!selectedCourseId.value) return
	try {
		const cid = Number(selectedCourseId.value)
		const blob: Blob = await exportGrades(cid)
		const rawCourseName = courses.value.find((c: any) => c.id === cid)?.name || String(cid)
		const courseName = String(rawCourseName).replace(/[\\/:*?"<>|]/g, '_')
		const dateStr = new Date().toISOString().slice(0, 10)
		const url = window.URL.createObjectURL(blob)
		const link = document.createElement('a')
		link.href = url
		link.setAttribute('download', `成绩单_${courseName}_${dateStr}.xlsx`)
		document.body.appendChild(link)
		link.click()
		document.body.removeChild(link)
		window.URL.revokeObjectURL(url)
		Toast.success('导出成功')
	} catch (e: any) {
		console.error(e)
	}
}
</script>

<style scoped>
.grade-teacher {
	padding: 20px;
	animation: fadeIn 0.3s;
}

.page-header h2 {
	margin-top: 0;
	margin-bottom: 20px;
	color: #303133;
}

.toolbar {
	background: #fff;
	padding: 15px 20px;
	border-radius: 4px;
	margin-bottom: 20px;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.selector {
	display: flex;
	align-items: center;
	gap: 10px;
}
.selector select {
	padding: 8px;
	font-size: 14px;
	min-width: 250px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
}
.actions {
	display: flex;
	gap: 10px;
}

.table-card {
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
	border-bottom: 1px solid #ebeef5;
	text-align: left;
	font-size: 14px;
}
th {
	background: #f5f7fa;
	color: #909399;
	font-weight: 600;
}
.text-danger {
	color: #f56c6c;
	font-weight: bold;
}
.text-success {
	color: #67c23a;
	font-weight: bold;
}
.text-gray {
	color: #909399;
	font-style: italic;
}

.btn {
	padding: 6px 16px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 14px;
}
.btn-primary {
	background: #409eff;
	color: white;
}
.btn-warning {
	background: #e6a23c;
	color: white;
}
.btn-info {
	background: #909399;
	color: white;
}
.btn-default {
	background: #fff;
	border: 1px solid #dcdfe6;
	color: #606266;
}

.btn-link {
	background: none;
	border: none;
	color: #409eff;
	cursor: pointer;
	padding: 0 5px;
}
.btn-link:hover {
	text-decoration: underline;
}
.btn-link.danger {
	color: #f56c6c;
}

.placeholder-card {
	background: #fff;
	padding: 60px;
	text-align: center;
	border-radius: 4px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
	color: #909399;
}
.placeholder-content .icon {
	font-size: 40px;
	display: block;
	margin-bottom: 10px;
}

/* Modal */
.modal-mask {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: rgba(0, 0, 0, 0.5);
	z-index: 1000;
	display: flex;
	justify-content: center;
	align-items: center;
}
.modal-content {
	background: white;
	width: 400px;
	border-radius: 4px;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
	animation: slideDown 0.3s ease;
}
.modal-header {
	padding: 15px 20px;
	border-bottom: 1px solid #ebeef5;
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.modal-header h3 {
	margin: 0;
	font-size: 18px;
	color: #303133;
}
.close-btn {
	cursor: pointer;
	font-size: 20px;
}
.modal-body {
	padding: 20px;
}
.modal-footer {
	padding: 15px 20px;
	border-top: 1px solid #ebeef5;
	display: flex;
	justify-content: flex-end;
	gap: 10px;
}
.form-group label {
	display: block;
	margin-bottom: 8px;
	color: #606266;
}
.form-group input {
	width: 100%;
	padding: 8px 10px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	box-sizing: border-box;
	font-size: 16px;
}
.hint {
	font-size: 12px;
	color: #909399;
	margin-top: 5px;
}

@keyframes slideDown {
	from {
		opacity: 0;
		transform: translateY(-20px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
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
