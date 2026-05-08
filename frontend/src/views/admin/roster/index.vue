<template>
	<div class="roster-page">
		<div class="page-header">
			<h2>课程名单维护</h2>
		</div>

		<div class="toolbar card">
			<div class="field">
				<label>课程</label>
				<select v-model.number="selectedCourseId" @change="loadRoster">
					<option :value="0" disabled>-- 请选择 --</option>
					<option v-for="c in courses" :key="c.id" :value="c.id">
						{{ c.name }}（{{ formatSemester(c.semester) }}）
					</option>
				</select>
			</div>
			<div class="field" v-if="selectedCourseId">
				<label>添加学生</label>
				<select v-model.number="selectedStudentId">
					<option :value="0" disabled>-- 选择学生 --</option>
					<option v-for="s in students" :key="s.id" :value="s.id">{{ s.realName }} ({{ s.username }})</option>
				</select>
				<button class="btn btn-primary" :disabled="!selectedStudentId || adding" @click="addStudent">
					{{ adding ? '添加中...' : '添加' }}
				</button>
			</div>
		</div>

		<div v-if="selectedCourseId" class="card">
			<table>
				<thead>
					<tr>
						<th style="width: 80px">ID</th>
						<th style="width: 120px">学号</th>
						<th>姓名</th>
						<th>用户名</th>
						<th style="width: 120px">操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="e in roster" :key="e.id">
						<td>{{ e.id }}</td>
						<td>{{ e.studentId }}</td>
						<td>{{ e.studentName }}</td>
						<td>{{ e.studentUsername }}</td>
						<td>
							<button
								class="btn-link danger"
								@click="removeStudent(e.studentId)"
								:disabled="removingId === e.studentId"
							>
								{{ removingId === e.studentId ? '移除中...' : '移除' }}
							</button>
						</td>
					</tr>
					<tr v-if="roster.length === 0">
						<td colspan="5" class="empty">暂无名单</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Toast from '@/utils/toast'
import { getCourses } from '@/api/course'
import { getUsers } from '@/api/user'
import { addStudentToCourse, getRoster, removeStudentFromCourse, type EnrollmentDto } from '@/api/roster'
import { formatSemester } from '@/utils/semester'

const courses = ref<any[]>([])
const students = ref<any[]>([])
const selectedCourseId = ref<number>(0)
const selectedStudentId = ref<number>(0)
const roster = ref<EnrollmentDto[]>([])
const adding = ref(false)
const removingId = ref<number | null>(null)

onMounted(async () => {
	try {
		const [courseRes, userRes] = await Promise.all([
			getCourses({ page: 1, size: 1000 }),
			getUsers({ pageNum: 1, pageSize: 1000, roleCode: 'ROLE_STUDENT' }),
		])

		courses.value = Array.isArray(courseRes) ? courseRes : courseRes?.list || []
		const list = (userRes as any).list || []
		students.value = list
	} catch (e: any) {
		console.error(e)
	}
})

const loadRoster = async () => {
	if (!selectedCourseId.value) return
	selectedStudentId.value = 0
	try {
		const res = await getRoster(selectedCourseId.value)
		roster.value = Array.isArray(res) ? res : []
	} catch (e: any) {
		console.error(e)
	}
}

const addStudent = async () => {
	if (!selectedCourseId.value || !selectedStudentId.value) return
	adding.value = true
	try {
		await addStudentToCourse(selectedCourseId.value, selectedStudentId.value)
		Toast.success('添加成功')
		await loadRoster()
	} catch (e: any) {
		console.error(e)
	} finally {
		adding.value = false
	}
}

import { confirm } from '@/utils/confirm'

// ... existing imports

const removeStudent = async (studentId: number) => {
	if (!selectedCourseId.value) return
	const ok = await confirm({
		type: 'danger',
		title: '移除学生',
		message: '确定要移出该学生吗？',
		confirmText: '确认移出',
	})
	if (!ok) return
	removingId.value = studentId
	try {
		await removeStudentFromCourse(selectedCourseId.value, studentId)
		Toast.success('移除成功')
		await loadRoster()
	} catch (e: any) {
		console.error(e)
	} finally {
		removingId.value = null
	}
}
</script>

<style scoped>
.roster-page {
	padding: 20px;
}
.page-header h2 {
	margin: 0 0 16px 0;
}
.card {
	background: #fff;
	border-radius: 8px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
	padding: 16px;
	margin-bottom: 16px;
}
.toolbar {
	display: flex;
	gap: 16px;
	align-items: flex-end;
	flex-wrap: wrap;
}
.field label {
	display: block;
	font-size: 12px;
	color: #606266;
	margin-bottom: 6px;
}
select {
	height: 36px;
	padding: 0 10px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	min-width: 240px;
}
.btn {
	height: 36px;
	padding: 0 12px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
}
.btn-primary {
	background: #409eff;
	color: #fff;
}
.btn-primary:disabled {
	background: #a0cfff;
	cursor: not-allowed;
}
table {
	width: 100%;
	border-collapse: collapse;
}
th,
td {
	padding: 12px 10px;
	border-bottom: 1px solid #ebeef5;
	text-align: left;
	font-size: 13px;
}
th {
	background: #f5f7fa;
	color: #606266;
}
.btn-link {
	background: none;
	border: none;
	color: #409eff;
	cursor: pointer;
	padding: 0;
}
.btn-link.danger {
	color: #f56c6c;
}
.empty {
	text-align: center;
	color: #909399;
	padding: 24px 0;
}
</style>
