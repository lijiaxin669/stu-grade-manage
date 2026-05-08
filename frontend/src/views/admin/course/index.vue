<template>
	<div class="course-manage-container">
		<div class="page-header">
			<h2>课程管理</h2>
			<button class="btn btn-primary" @click="openCreateModal">新增课程</button>
		</div>

		<div class="table-card">
			<table>
				<thead>
					<tr>
						<th>ID</th>
						<th>课程名称</th>
						<th>学分</th>
						<th>学期</th>
						<th>授课教师</th>
						<th>描述</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="course in courseList" :key="course.id">
						<td>{{ course.id }}</td>
						<td>
							<strong>{{ course.name }}</strong>
						</td>
						<td>{{ course.credits }}</td>
						<td>{{ formatSemester(course.semester) }}</td>
						<td>
							<span class="teacher-tag">{{ course.teacherName || '未分配' }}</span>
						</td>
						<td class="desc-cell" :title="course.description">{{ truncate(course.description) }}</td>
						<td class="actions">
							<button class="btn-link" @click="handleEdit(course)">编辑</button>
							<button class="btn-link danger" @click="handleDelete(course)">删除</button>
						</td>
					</tr>
					<tr v-if="courseList.length === 0">
						<td colspan="7" class="empty-text">暂无课程数据</td>
					</tr>
				</tbody>
			</table>
		</div>

		<!-- Modal -->
		<div v-if="showModal" class="modal-mask">
			<div class="modal-content">
				<div class="modal-header">
					<h3>{{ isEdit ? '编辑课程' : '新增课程' }}</h3>
					<span class="close-btn" @click="showModal = false">×</span>
				</div>
				<div class="modal-body">
					<form @submit.prevent="handleSubmit">
						<div class="form-group">
							<label>课程名称 <span class="required">*</span></label>
							<input v-model="form.name" required placeholder="例如：高等数学" />
						</div>
						<div class="form-row">
							<div class="form-group half">
								<label>学分 <span class="required">*</span></label>
								<input v-model.number="form.credits" type="number" required min="1" max="10" />
							</div>
							<div class="form-group half">
								<label>学期 <span class="required">*</span></label>
								<select v-model="form.semester" required>
									<option value="2025-Fall">2025-秋季</option>
									<option value="2026-Spring">2026-春季</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label>授课教师</label>
							<select v-model="form.teacherId">
								<option :value="undefined">-- 请选择 --</option>
								<option v-for="t in teacherList" :key="t.id" :value="t.id">{{ t.realName }} ({{ t.username }})</option>
							</select>
						</div>
						<div class="form-group">
							<label>课程描述</label>
							<textarea v-model="form.description" rows="3"></textarea>
						</div>

						<div class="modal-footer">
							<button type="button" class="btn btn-default" @click="showModal = false">取消</button>
							<button type="submit" class="btn btn-primary" :disabled="submitting">
								{{ submitting ? '保存中...' : '保存' }}
							</button>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCourses, createCourse, updateCourse, deleteCourse } from '@/api/course'
import type { CourseDto } from '@/api/course'
import { getUsers } from '@/api/user' // To fetch teachers
import type { UserDto } from '@/api/user'
import Toast from '@/utils/toast'
import { formatSemester } from '@/utils/semester'

/* ... */

// Data
const courseList = ref<CourseDto[]>([])
const teacherList = ref<UserDto[]>([])

// Modal
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const form = reactive<any>({
	id: 0,
	name: '',
	description: '',
	credits: 3,
	semester: '2026-Spring',
	teacherId: undefined,
})

onMounted(() => {
	fetchCourses()
	fetchTeachers()
})

const fetchCourses = async () => {
	try {
		const res: any = await getCourses({ page: 1, size: 100 })
		if (Array.isArray(res)) {
			courseList.value = res
		} else {
			// Fallback if I changed it to Page
			courseList.value = res.list || []
		}
	} catch (e) {
		console.error(e)
	}
}

const fetchTeachers = async () => {
	try {
		// getUsers returns PageResult. We want all teachers.
		// Sending size=100 for now.
		const res: any = await getUsers({ pageNum: 1, pageSize: 100, roleCode: 'ROLE_TEACHER', keyword: '' })
		teacherList.value = res.list
	} catch (e) {
		console.error(e)
	}
}

const openCreateModal = () => {
	isEdit.value = false
	form.id = 0
	form.name = ''
	form.description = ''
	form.credits = 3
	form.semester = '2026-Spring'
	form.teacherId = undefined
	showModal.value = true
}

const handleEdit = (course: CourseDto) => {
	isEdit.value = true
	form.id = course.id
	form.name = course.name
	form.description = course.description
	form.credits = course.credits
	form.semester = course.semester
	form.teacherId = course.teacherId
	showModal.value = true
}

import { confirm } from '@/utils/confirm'

// ... existing imports

const handleDelete = async (course: CourseDto) => {
	const ok = await confirm({
		type: 'danger',
		title: '删除课程',
		message: `确定要删除课程 [${course.name}] 吗？`,
		confirmText: '确认删除',
	})
	if (!ok) return
	try {
		await deleteCourse(course.id)
		Toast.success('删除成功')
		fetchCourses()
	} catch (e: any) {
		console.error(e)
	}
}

const handleSubmit = async () => {
	submitting.value = true
	try {
		if (isEdit.value) {
			await updateCourse(form)
		} else {
			await createCourse(form)
		}
		showModal.value = false
		Toast.success('保存成功')
		fetchCourses()
	} catch (e: any) {
		console.error(e)
	} finally {
		submitting.value = false
	}
}

const truncate = (str: string) => {
	if (!str) return ''
	return str.length > 20 ? str.substring(0, 20) + '...' : str
}
</script>

<style scoped>
.course-manage-container {
	animation: fadeIn 0.3s;
}

.page-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;
}

.table-card {
	background: #fff;
	border-radius: 4px;
	padding: 20px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

table {
	width: 100%;
	border-collapse: collapse;
}

th {
	background-color: #f5f7fa;
	color: #909399;
	font-weight: 600;
	text-align: left;
	padding: 12px 15px;
	font-size: 14px;
}

td {
	padding: 12px 15px;
	border-bottom: 1px solid #ebeef5;
	font-size: 14px;
	color: #606266;
}

.desc-cell {
	color: #909399;
	max-width: 200px;
}

.teacher-tag {
	background: #ecf5ff;
	color: #409eff;
	padding: 2px 6px;
	border-radius: 4px;
	font-size: 12px;
}

/* Modal */
.modal-mask {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: rgba(0, 0, 0, 0.5);
	z-index: 999;
	display: flex;
	justify-content: center;
	align-items: center;
}
.modal-content {
	background: #fff;
	width: 500px;
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
.close-btn {
	cursor: pointer;
	font-size: 20px;
}
.modal-body {
	padding: 20px;
}
.modal-footer {
	text-align: right;
	margin-top: 20px;
}

.form-group {
	margin-bottom: 15px;
}
.form-row {
	display: flex;
	gap: 15px;
}
.half {
	flex: 1;
}
.form-group label {
	display: block;
	margin-bottom: 8px;
	color: #606266;
}
.form-group input,
.form-group select,
.form-group textarea {
	width: 100%;
	height: 36px;
	padding: 0 10px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	box-sizing: border-box;
}
.form-group textarea {
	height: auto;
	padding: 8px;
}

/* Buttons */
.btn {
	padding: 8px 16px;
	border-radius: 4px;
	border: none;
	cursor: pointer;
}
.btn-primary {
	background: #409eff;
	color: #fff;
}
.btn-default {
	background: #fff;
	border: 1px solid #dcdfe6;
}
.btn-link {
	background: none;
	border: none;
	color: #409eff;
	cursor: pointer;
	margin-right: 5px;
}
.btn-link.danger {
	color: #f56c6c;
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
