<template>
	<div class="my-grades-container">
		<div class="page-header">
			<h2>我的成绩</h2>
		</div>

		<div class="filter-bar">
			<div class="filter-item">
				<label>学期:</label>
				<select v-model="filters.semester">
					<option value="">全部</option>
					<option value="2026-Spring">2026-春季</option>
					<option value="2025-Fall">2025-秋季</option>
				</select>
			</div>
			<div class="filter-item">
				<label>课程名称:</label>
				<input v-model="filters.keyword" placeholder="搜索课程..." />
			</div>

			<div class="summary" v-if="filteredGrades.length > 0">
				<span
					>已修课程: <strong>{{ filteredGrades.length }}</strong></span
				>
				<span
					>平均分: <strong>{{ avgScore }}</strong></span
				>
			</div>
		</div>

		<div class="grades-table card">
			<table>
				<thead>
					<tr>
						<th>课程名称</th>
						<th>学期</th>
						<th>学分</th>
						<th>分数</th>
						<th>绩点</th>
						<th>状态</th>
						<th>录入时间</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="g in filteredGrades" :key="g.id">
						<td>
							<strong>{{ g.courseName }}</strong>
						</td>
						<td>{{ formatSemester(g.semester) }}</td>
						<td>3.0</td>
						<td>
							<span class="score" :class="getScoreClass(g.score)">{{ g.score }}</span>
						</td>
						<td>{{ calculateGPA(g.score) }}</td>
						<td>
							<span class="tag" :class="g.score >= 60 ? 'tag-success' : 'tag-danger'">
								{{ g.score >= 60 ? '及格' : '不及格' }}
							</span>
						</td>
						<td class="time">{{ formatDate(g.gradedAt) }}</td>
						<td>
							<button class="btn btn-sm btn-primary" @click="openAppealModal(g)">申诉</button>
						</td>
					</tr>
					<tr v-if="filteredGrades.length === 0">
						<td colspan="8" class="empty-text">暂无成绩记录</td>
					</tr>
				</tbody>
			</table>
		</div>

		<div v-if="showAppealModal && selectedGrade" class="modal-mask">
			<div class="modal-content">
				<div class="modal-header">
					<h3>成绩申诉 - {{ selectedGrade.courseName }}</h3>
					<span class="close-btn" @click="closeAppealModal">×</span>
				</div>
				<form @submit.prevent="submitAppeal">
					<div class="modal-body">
						<div class="form-group">
							<label>当前分数</label>
							<p class="form-hint"><strong>{{ selectedGrade.score }}</strong></p>
						</div>
						<div class="form-group">
							<label>期望分数 (0-100，与原分数差异≥2分) <span class="required">*</span></label>
							<input type="number" v-model.number="appealForm.expectedScore" min="0" max="100" step="0.5" required />
							<p v-if="appealForm.expectedScore !== undefined && Math.abs(appealForm.expectedScore - selectedGrade.score) < 2" class="error-hint">
								期望分数与原分数差异必须至少2分
							</p>
						</div>
						<div class="form-group">
							<label>申诉理由 (10-500字) <span class="required">*</span></label>
							<textarea v-model="appealForm.reason" rows="4" required maxlength="500" placeholder="请详细说明申诉理由..."></textarea>
							<p class="hint">{{ appealForm.reason.length }}/500</p>
						</div>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" @click="closeAppealModal">取消</button>
						<button type="submit" class="btn btn-primary" :disabled="submitting">
							提交申诉
						</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getGrades } from '@/api/grade'
import { createAppeal } from '@/api/appeal'
import { formatSemester } from '@/utils/semester'
import Toast from '@/utils/toast'

const rawGrades = ref<any[]>([])

const filters = reactive({
	semester: '',
	keyword: '',
})

const showAppealModal = ref(false)
const selectedGrade = ref<any | null>(null)
const appealForm = ref({
	expectedScore: undefined as number | undefined,
	reason: '',
})
const submitting = ref(false)

onMounted(async () => {
	try {
		const res: any = await getGrades({})
		const grades = Array.isArray(res) ? res : res.list || []
		rawGrades.value = grades
			.slice()
			.sort((a: any, b: any) => new Date(b.gradedAt).getTime() - new Date(a.gradedAt).getTime())
	} catch (e) {
		console.error(e)
	}
})

const openAppealModal = (grade: any) => {
	selectedGrade.value = grade
	appealForm.value = { expectedScore: undefined, reason: '' }
	showAppealModal.value = true
}

const closeAppealModal = () => {
	showAppealModal.value = false
	selectedGrade.value = null
}

const submitAppeal = async () => {
	if (!selectedGrade.value) return
	if (appealForm.value.expectedScore === undefined || appealForm.value.expectedScore < 0 || appealForm.value.expectedScore > 100) {
		Toast.error('请输入有效的期望分数 (0-100)')
		return
	}
	if (Math.abs(appealForm.value.expectedScore - selectedGrade.value.score) < 2) {
		Toast.error('期望分数与原分数差异必须至少2分')
		return
	}
	if (appealForm.value.reason.length < 10 || appealForm.value.reason.length > 500) {
		Toast.error('申诉理由长度应在 10-500 字之间')
		return
	}

	submitting.value = true
	try {
		await createAppeal({
			gradeId: selectedGrade.value.id,
			expectedScore: appealForm.value.expectedScore,
			reason: appealForm.value.reason,
		})
		Toast.success('申诉已提交')
		closeAppealModal()
	} catch (e: any) {
		console.error(e)
	} finally {
		submitting.value = false
	}
}

const filteredGrades = computed(() => {
	return rawGrades.value.filter((g) => {
		let match = true
		if (filters.semester && g.semester !== filters.semester) match = false
		if (filters.keyword && !g.courseName.toLowerCase().includes(filters.keyword.toLowerCase())) match = false
		return match
	})
})

const avgScore = computed(() => {
	if (filteredGrades.value.length === 0) return '-'
	const total = filteredGrades.value.reduce((acc, curr) => acc + Number(curr.score || 0), 0)
	return (total / filteredGrades.value.length).toFixed(1)
})

const getScoreClass = (score: number) => {
	if (score >= 90) return 'text-success'
	if (score < 60) return 'text-danger'
	return ''
}

const calculateGPA = (rawScore: any) => {
	const score = Number(rawScore)
	if (score >= 90) return '4.0'
	if (score >= 85) return '3.7'
	if (score >= 82) return '3.3'
	if (score >= 78) return '3.0'
	if (score >= 75) return '2.7'
	if (score >= 72) return '2.3'
	if (score >= 68) return '2.0'
	if (score >= 64) return '1.5'
	if (score >= 60) return '1.0'
	return '0.0'
}

const formatDate = (str: string) => {
	if (!str) return '-'
	return new Date(str).toLocaleDateString()
}
</script>

<style scoped>
.my-grades-container {
	padding: 24px;
	animation: fadeIn 0.6s ease;
	max-width: 1200px;
	margin: 0 auto;
}

.page-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24px;
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
	background: linear-gradient(180deg, #1890ff, #69c0ff);
	border-radius: 3px;
}

.filter-bar {
	background: #fff;
	padding: 20px 24px;
	border-radius: 12px;
	margin-bottom: 24px;
	display: flex;
	align-items: center;
	gap: 24px;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
	border: 1px solid #f0f0f0;
	flex-wrap: wrap;
}

.filter-item {
	display: flex;
	align-items: center;
	gap: 10px;
	font-size: 14px;
	color: #606266;
}

.filter-item label {
	font-weight: 500;
}

.filter-item select,
.filter-item input {
	padding: 8px 12px;
	border: 1px solid #dcdfe6;
	border-radius: 6px;
	color: #606266;
	transition: all 0.3s;
	min-width: 160px;
}

.filter-item select:focus,
.filter-item input:focus {
	border-color: #409eff;
	outline: none;
	box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.summary {
	margin-left: auto;
	display: flex;
	gap: 24px;
	font-size: 14px;
	color: #606266;
	background: #f8f9fa;
	padding: 8px 16px;
	border-radius: 6px;
}

.summary strong {
	color: #1890ff;
	font-size: 18px;
	font-family: 'DIN Alternate', sans-serif;
	margin-left: 4px;
}

.card {
	background: #fff;
	border-radius: 12px;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
	overflow: hidden;
	border: 1px solid #f0f0f0;
}

table {
	width: 100%;
	border-collapse: separate;
	border-spacing: 0;
}

th,
td {
	padding: 16px 24px;
	text-align: left;
	border-bottom: 1px solid #f0f0f0;
	font-size: 14px;
}

th {
	background: #fafafa;
	color: #595959;
	font-weight: 600;
	font-size: 14px;
	text-transform: uppercase;
	letter-spacing: 0.5px;
}

tbody tr {
	transition: background 0.2s;
}

tbody tr:hover {
	background: #f8fbff;
}

tbody tr:last-child td {
	border-bottom: none;
}

.score {
	font-weight: 700;
	font-size: 16px;
	font-family: 'DIN Alternate', sans-serif;
}
.text-success {
	color: #52c41a;
}
.text-danger {
	color: #ff4d4f;
}

.tag {
	padding: 4px 10px;
	border-radius: 4px;
	font-size: 12px;
	display: inline-block;
	font-weight: 500;
}
.tag-success {
	background: #f6ffed;
	color: #52c41a;
	border: 1px solid #b7eb8f;
}
.tag-danger {
	background: #fff2f0;
	color: #ff4d4f;
	border: 1px solid #ffccc7;
}

.time {
	color: #909399;
	font-size: 13px;
	font-family: monospace;
}

.empty-text {
	text-align: center;
	color: #909399;
	padding: 60px;
	font-size: 14px;
	background: #fff;
}

.btn {
	padding: 6px 14px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 14px;
}
.btn-primary {
	background: #409eff;
	color: white;
}
.btn-primary:disabled {
	opacity: 0.6;
	cursor: not-allowed;
}
.btn-sm {
	padding: 5px 12px;
	font-size: 13px;
}
.btn-default {
	background: #fff;
	border: 1px solid #dcdfe6;
	color: #606266;
}

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
	width: 500px;
	border-radius: 8px;
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
	font-size: 24px;
	color: #909399;
}

.close-btn:hover {
	color: #606266;
}

.modal-body {
	padding: 20px;
	max-height: 60vh;
	overflow-y: auto;
}

.modal-footer {
	padding: 15px 20px;
	border-top: 1px solid #ebeef5;
	display: flex;
	justify-content: flex-end;
	gap: 10px;
}

.form-group {
	margin-bottom: 16px;
}

.form-group label {
	display: block;
	margin-bottom: 8px;
	color: #606266;
	font-weight: 500;
}

.form-hint {
	margin: 4px 0;
	font-size: 13px;
	color: #909399;
}

.error-hint {
	margin: 4px 0;
	font-size: 12px;
	color: #f56c6c;
}

.form-group input,
.form-group textarea {
	width: 100%;
	padding: 10px 12px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	box-sizing: border-box;
	font-size: 14px;
}

.form-group input:focus,
.form-group textarea:focus {
	border-color: #409eff;
	outline: none;
}

.form-group textarea {
	font-family: inherit;
	resize: vertical;
}

.hint {
	margin-top: 5px;
	font-size: 12px;
	color: #909399;
}

.required {
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
		transform: translateY(10px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}
</style>
