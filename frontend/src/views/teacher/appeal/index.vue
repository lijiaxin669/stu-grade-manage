<template>
	<div class="teacher-appeal-container">
		<div class="page-header">
			<h2>申诉处理</h2>
		</div>

		<div class="appeals-table card">
			<table>
				<thead>
					<tr>
						<th>课程</th>
						<th>学生</th>
						<th>原分数</th>
						<th>期望分数</th>
						<th>申诉原因</th>
						<th>状态</th>
						<th>提交时间</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="a in appeals" :key="a.id">
						<td>{{ a.courseName }}</td>
						<td>{{ a.studentName }}</td>
						<td>{{ a.originalScore }}</td>
						<td>{{ a.expectedScore }}</td>
						<td class="reason-cell" :title="a.reason">{{ a.reason }}</td>
						<td>
							<span class="tag" :class="statusClass(a.status)">{{ statusLabel(a.status) }}</span>
						</td>
						<td class="time">{{ formatDate(a.createdAt) }}</td>
						<td>
							<button v-if="a.status === 'PENDING'" class="btn btn-primary btn-sm" @click="handleClaim(a)">认领</button>
							<button v-if="a.status === 'UNDER_REVIEW'" class="btn btn-warning btn-sm" @click="openDecisionModal(a)">裁定</button>
							<button class="btn-link" @click="viewDetail(a)">详情</button>
						</td>
					</tr>
					<tr v-if="appeals.length === 0">
						<td colspan="8" class="empty-text">暂无待处理申诉</td>
					</tr>
				</tbody>
			</table>
		</div>

		<div v-if="showDecisionModal" class="modal-mask">
			<div class="modal-content">
				<div class="modal-header">
					<h3>裁定申诉 - {{ currentAppeal?.studentName }}</h3>
					<span class="close-btn" @click="showDecisionModal = false">&times;</span>
				</div>
				<form @submit.prevent="submitDecision">
					<div class="modal-body">
						<div class="info-row">
							<span>课程: {{ currentAppeal?.courseName }}</span>
							<span>原分数: {{ currentAppeal?.originalScore }}</span>
							<span>期望: {{ currentAppeal?.expectedScore }}</span>
						</div>
						<div class="form-group">
							<label>裁定结果 <span class="required">*</span></label>
							<select v-model="decisionForm.decision" required>
								<option value="">请选择</option>
								<option value="APPROVED">通过</option>
								<option value="REJECTED">驳回</option>
							</select>
						</div>
						<div v-if="decisionForm.decision === 'APPROVED'" class="form-group">
							<label>最终分数 (0-100) <span class="required">*</span></label>
							<input type="number" v-model.number="decisionForm.finalScore" min="0" max="100" step="0.5" required />
						</div>
						<div class="form-group">
							<label>评语 <span class="required">*</span></label>
							<textarea v-model="decisionForm.comment" rows="3" required></textarea>
						</div>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" @click="showDecisionModal = false">取消</button>
						<button type="submit" class="btn btn-primary" :disabled="submitting">提交</button>
					</div>
				</form>
			</div>
		</div>

		<div v-if="showDetailModal" class="modal-mask">
			<div class="modal-content modal-lg">
				<div class="modal-header">
					<h3>申诉详情</h3>
					<span class="close-btn" @click="showDetailModal = false">&times;</span>
				</div>
				<div class="modal-body">
					<div class="detail-grid">
						<div class="detail-item"><span class="label">课程:</span> {{ detailData?.courseName }}</div>
						<div class="detail-item"><span class="label">学生:</span> {{ detailData?.studentName }}</div>
						<div class="detail-item"><span class="label">原分数:</span> {{ detailData?.originalScore }}</div>
						<div class="detail-item"><span class="label">期望分数:</span> {{ detailData?.expectedScore }}</div>
						<div class="detail-item"><span class="label">最终分数:</span> {{ detailData?.finalScore ?? '-' }}</div>
						<div class="detail-item"><span class="label">状态:</span> <span class="tag" :class="statusClass(detailData?.status || '')">{{ statusLabel(detailData?.status || '') }}</span></div>
						<div class="detail-item full"><span class="label">申诉原因:</span> {{ detailData?.reason }}</div>
						<div class="detail-item full"><span class="label">教师评语:</span> {{ detailData?.teacherComment ?? '-' }}</div>
					</div>
					<div v-if="auditLogs.length > 0" class="audit-section">
						<h4>审计日志</h4>
						<table>
							<thead>
								<tr>
									<th>操作</th>
									<th>操作人角色</th>
									<th>备注</th>
									<th>时间</th>
								</tr>
							</thead>
							<tbody>
								<tr v-for="log in auditLogs" :key="log.id">
									<td>{{ log.fromStatus || '-' }} → {{ log.toStatus }}</td>
									<td>{{ log.operatorRole }}</td>
									<td>{{ log.comment ?? '-' }}</td>
									<td class="time">{{ formatDate(log.createdAt) }}</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-default" @click="showDetailModal = false">关闭</button>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPendingAppeals, claimAppeal, decideAppeal, getAppealDetail, getAppealAuditLogs, type AppealDto, type AuditLogDto } from '@/api/appeal'
import Toast from '@/utils/toast'

const appeals = ref<AppealDto[]>([])
const showDecisionModal = ref(false)
const showDetailModal = ref(false)
const submitting = ref(false)
const currentAppeal = ref<AppealDto | null>(null)
const decisionForm = ref({ decision: '', comment: '', finalScore: null as number | null })
const detailData = ref<AppealDto | null>(null)
const auditLogs = ref<AuditLogDto[]>([])

onMounted(async () => {
	await fetchAppeals()
})

const fetchAppeals = async () => {
	try {
		const res = await getPendingAppeals()
		appeals.value = Array.isArray(res) ? res : []
	} catch (e) {
		console.error(e)
	}
}

const statusLabel = (status: string) => {
	const map: Record<string, string> = {
		PENDING: '待处理',
		UNDER_REVIEW: '审核中',
		APPROVED: '已通过',
		REJECTED: '已驳回',
		ARBITRATING: '仲裁中',
		CLOSED: '已关闭',
	}
	return map[status] || status
}

const statusClass = (status: string) => {
	const map: Record<string, string> = {
		PENDING: 'tag-warning',
		UNDER_REVIEW: 'tag-info',
		APPROVED: 'tag-success',
		REJECTED: 'tag-danger',
		ARBITRATING: 'tag-purple',
		CLOSED: 'tag-default',
	}
	return map[status] || ''
}

const formatDate = (str: string) => {
	if (!str) return '-'
	return new Date(str).toLocaleString()
}

const handleClaim = async (a: AppealDto) => {
	try {
		await claimAppeal(a.id)
		Toast.success('认领成功')
		await fetchAppeals()
	} catch (e) {
		console.error(e)
	}
}

const openDecisionModal = (a: AppealDto) => {
	currentAppeal.value = a
	decisionForm.value = { decision: '', comment: '', finalScore: null }
	showDecisionModal.value = true
}

const submitDecision = async () => {
	if (!currentAppeal.value) return
	submitting.value = true
	try {
		await decideAppeal(currentAppeal.value.id, {
			decision: decisionForm.value.decision,
			comment: decisionForm.value.comment,
			finalScore: decisionForm.value.decision === 'APPROVED' ? decisionForm.value.finalScore : undefined,
		})
		showDecisionModal.value = false
		Toast.success('裁定完成')
		await fetchAppeals()
	} catch (e) {
		console.error(e)
	} finally {
		submitting.value = false
	}
}

const viewDetail = async (a: AppealDto) => {
	try {
		const detail = await getAppealDetail(a.id)
		detailData.value = detail as any
		const logs = await getAppealAuditLogs(a.id)
		auditLogs.value = Array.isArray(logs) ? logs : []
		showDetailModal.value = true
	} catch (e) {
		console.error(e)
	}
}
</script>

<style scoped>
.teacher-appeal-container {
	padding: 20px;
	animation: fadeIn 0.3s;
}

.page-header h2 {
	margin-top: 0;
	margin-bottom: 20px;
	color: #303133;
}

.card {
	background: #fff;
	border-radius: 4px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
	overflow: hidden;
}

table {
	width: 100%;
	border-collapse: collapse;
}

th, td {
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

tbody tr:hover {
	background: #f8fbff;
}

.reason-cell {
	max-width: 200px;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.tag {
	padding: 4px 10px;
	border-radius: 4px;
	font-size: 12px;
	display: inline-block;
	font-weight: 500;
}

.tag-warning { background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591; }
.tag-info { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.tag-success { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.tag-danger { background: #fff2f0; color: #ff4d4f; border: 1px solid #ffccc7; }
.tag-purple { background: #f9f0ff; color: #722ed1; border: 1px solid #d3adf7; }
.tag-default { background: #f5f5f5; color: #8c8c8c; border: 1px solid #d9d9d9; }

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
}

.btn {
	padding: 6px 16px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 14px;
}

.btn-sm {
	padding: 4px 12px;
	font-size: 13px;
}

.btn-primary { background: #1890ff; color: white; }
.btn-primary:hover { background: #40a9ff; }
.btn-warning { background: #fa8c16; color: white; }
.btn-warning:hover { background: #ffa940; }
.btn-default { background: #fff; border: 1px solid #dcdfe6; color: #606266; }

.btn-link {
	background: none;
	border: none;
	color: #1890ff;
	cursor: pointer;
	padding: 0 5px;
	font-size: 14px;
}

.btn-link:hover {
	text-decoration: underline;
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
	width: 480px;
	border-radius: 8px;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
	animation: slideDown 0.3s ease;
}

.modal-lg {
	width: 700px;
	max-height: 80vh;
	overflow-y: auto;
}

.modal-header {
	padding: 16px 20px;
	border-bottom: 1px solid #f0f0f0;
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
	color: #909399;
}

.modal-body {
	padding: 20px;
}

.modal-footer {
	padding: 16px 20px;
	border-top: 1px solid #f0f0f0;
	display: flex;
	justify-content: flex-end;
	gap: 10px;
}

.info-row {
	display: flex;
	gap: 20px;
	margin-bottom: 16px;
	font-size: 14px;
	color: #606266;
	background: #f8f9fa;
	padding: 10px 16px;
	border-radius: 4px;
}

.form-group {
	margin-bottom: 16px;
}

.form-group label {
	display: block;
	margin-bottom: 8px;
	color: #606266;
	font-size: 14px;
}

.form-group input,
.form-group select,
.form-group textarea {
	width: 100%;
	padding: 8px 12px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	box-sizing: border-box;
	font-size: 14px;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
	border-color: #409eff;
	outline: none;
	box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.form-group textarea {
	resize: vertical;
}

.required {
	color: #ff4d4f;
}

.detail-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 12px;
}

.detail-item {
	font-size: 14px;
	color: #333;
}

.detail-item.full {
	grid-column: 1 / -1;
}

.detail-item .label {
	color: #909399;
	margin-right: 8px;
}

.audit-section {
	margin-top: 24px;
	border-top: 1px solid #f0f0f0;
	padding-top: 16px;
}

.audit-section h4 {
	margin: 0 0 12px;
	color: #303133;
	font-size: 16px;
}

.audit-section table {
	margin-top: 8px;
}

@keyframes fadeIn {
	from { opacity: 0; }
	to { opacity: 1; }
}

@keyframes slideDown {
	from { opacity: 0; transform: translateY(-20px); }
	to { opacity: 1; transform: translateY(0); }
}
</style>
