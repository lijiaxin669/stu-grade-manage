<template>
	<div class="teacher-appeal-container">
		<div class="page-header">
			<h2>申诉处理</h2>
		</div>

		<div class="appeals-table card" v-if="appeals.length > 0">
			<table>
				<thead>
					<tr>
						<th>课程</th>
						<th>学生</th>
						<th>原分数</th>
						<th>期望分数</th>
						<th>申诉理由</th>
						<th>状态</th>
						<th>提交时间</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="item in appeals" :key="item.id">
						<td><strong>{{ item.courseName }}</strong></td>
						<td>{{ item.studentName }} ({{ item.studentUsername }})</td>
						<td>{{ item.originalScore }}</td>
						<td>{{ item.expectedScore }}</td>
						<td class="reason-cell" :title="item.reason">{{ truncate(item.reason, 30) }}</td>
						<td>
							<span class="tag" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</span>
						</td>
						<td class="time">{{ formatDate(item.createdAt) }}</td>
						<td>
							<template v-if="item.status === 'PENDING'">
								<button class="btn btn-primary btn-sm" @click="handleClaim(item)">认领</button>
							</template>
							<template v-else-if="item.status === 'UNDER_REVIEW'">
								<template v-if="!item.currentHandlerId || item.currentHandlerId === myUserId">
									<button class="btn btn-success btn-sm" @click="openDecisionModal(item)">裁定</button>
									<button class="btn-link" @click="showDetail(item)">详情</button>
								</template>
								<template v-else>
									<span class="text-muted">{{ item.currentHandlerName }} 处理中</span>
								</template>
							</template>
							<template v-else>
								<button class="btn-link" @click="showDetail(item)">详情</button>
							</template>
						</td>
					</tr>
				</tbody>
			</table>
		</div>

		<div v-else class="empty-card">
			<p class="empty-text">暂无待处理的申诉</p>
		</div>

		<div v-if="showDetailModal && selectedAppeal" class="modal-mask">
			<div class="modal-content modal-wide">
				<div class="modal-header">
					<h3>申诉详情</h3>
					<span class="close-btn" @click="showDetailModal = false">×</span>
				</div>
				<div class="modal-body">
					<div class="detail-grid">
						<div class="detail-item">
							<label>课程</label>
							<span>{{ selectedAppeal.courseName }}</span>
						</div>
						<div class="detail-item">
							<label>学生</label>
							<span>{{ selectedAppeal.studentName }} ({{ selectedAppeal.studentUsername }})</span>
						</div>
						<div class="detail-item">
							<label>原分数</label>
							<span>{{ selectedAppeal.originalScore }}</span>
						</div>
						<div class="detail-item">
							<label>期望分数</label>
							<span>{{ selectedAppeal.expectedScore }}</span>
						</div>
						<div class="detail-item" v-if="selectedAppeal.finalScore">
							<label>最终分数</label>
							<span>{{ selectedAppeal.finalScore }}</span>
						</div>
						<div class="detail-item">
							<label>状态</label>
							<span class="tag" :class="getStatusClass(selectedAppeal.status)">{{ getStatusText(selectedAppeal.status) }}</span>
						</div>
						<div class="detail-item full-width">
							<label>申诉理由</label>
							<p class="reason-text">{{ selectedAppeal.reason }}</p>
						</div>
						<div class="detail-item full-width" v-if="selectedAppeal.teacherComment">
							<label>处理意见</label>
							<p class="reason-text">{{ selectedAppeal.teacherComment }}</p>
						</div>
					</div>

					<div class="audit-section" v-if="auditLogs.length > 0">
						<h4>处理日志</h4>
						<div class="audit-timeline">
							<div v-for="log in auditLogs" :key="log.id" class="audit-item">
								<div class="audit-time">{{ formatDateTime(log.createdAt) }}</div>
								<div class="audit-content">
									<span class="operator">{{ log.operatorName || '系统' }}</span>
									<span class="role">({{ getRoleText(log.operatorRole) }})</span>
									<span v-if="log.success" class="text-success">: {{ log.comment }}</span>
									<span v-else class="text-danger">: {{ log.comment }} ({{ log.errorMsg }})</span>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<button v-if="selectedAppeal.status === 'UNDER_REVIEW' && (!selectedAppeal.currentHandlerId || selectedAppeal.currentHandlerId === myUserId)" 
					        class="btn btn-primary" @click="openDecisionModal(selectedAppeal); showDetailModal = false">
						裁定
					</button>
					<button type="button" class="btn btn-default" @click="showDetailModal = false">关闭</button>
				</div>
			</div>
		</div>

		<div v-if="showDecisionModal && selectedAppeal" class="modal-mask">
			<div class="modal-content">
				<div class="modal-header">
					<h3>裁定申诉 - {{ selectedAppeal.courseName }}</h3>
					<span class="close-btn" @click="closeDecisionModal">×</span>
				</div>
				<form @submit.prevent="submitDecision">
					<div class="modal-body">
						<div class="form-group">
							<label>学生: {{ selectedAppeal.studentName }}</label>
							<p class="form-hint">原分数: <strong>{{ selectedAppeal.originalScore }}</strong> | 期望分数: <strong>{{ selectedAppeal.expectedScore }}</strong></p>
							<p class="form-hint">申诉理由: {{ selectedAppeal.reason }}</p>
						</div>
						<div class="form-group">
							<label>裁定结果 <span class="required">*</span></label>
							<select v-model="decisionForm.decision" required>
								<option value="">请选择</option>
								<option value="APPROVED">通过</option>
								<option value="REJECTED">驳回</option>
							</select>
						</div>
						<div class="form-group" v-if="decisionForm.decision === 'APPROVED'">
							<label>最终分数 (0-100) <span class="required">*</span></label>
							<input type="number" v-model.number="decisionForm.finalScore" min="0" max="100" step="0.5" required />
						</div>
						<div class="form-group">
							<label>处理意见 <span class="required">*</span></label>
							<textarea v-model="decisionForm.comment" rows="3" required maxlength="500" placeholder="请输入处理意见..."></textarea>
							<p class="hint">{{ decisionForm.comment.length }}/500</p>
						</div>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" @click="closeDecisionModal">取消</button>
						<button type="submit" class="btn btn-primary" :disabled="submitting">
							提交
						</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
	getPendingAppeals,
	getAppealDetail,
	getAppealAuditLogs,
	claimAppeal,
	decideAppeal,
	type AppealDto,
	type AuditLogDto,
} from '@/api/appeal'
import { useUserStore } from '@/store/user'
import Toast from '@/utils/toast'

const userStore = useUserStore()
const myUserId = computed(() => userStore.userInfo?.id || 0)

const appeals = ref<AppealDto[]>([])
const showDetailModal = ref(false)
const showDecisionModal = ref(false)
const selectedAppeal = ref<AppealDto | null>(null)
const auditLogs = ref<AuditLogDto[]>([])

const decisionForm = ref({
	decision: '',
	comment: '',
	finalScore: undefined as number | undefined,
})
const submitting = ref(false)

onMounted(async () => {
	await fetchAppeals()
})

const fetchAppeals = async () => {
	try {
		appeals.value = await getPendingAppeals()
	} catch (e: any) {
		console.error(e)
	}
}

const showDetail = async (item: AppealDto) => {
	try {
		selectedAppeal.value = await getAppealDetail(item.id)
		auditLogs.value = await getAppealAuditLogs(item.id)
		showDetailModal.value = true
	} catch (e: any) {
		console.error(e)
	}
}

const handleClaim = async (item: AppealDto) => {
	try {
		await claimAppeal(item.id)
		Toast.success('认领成功')
		await fetchAppeals()
	} catch (e: any) {
		console.error(e)
	}
}

const openDecisionModal = (item: AppealDto) => {
	selectedAppeal.value = item
	decisionForm.value = { decision: '', comment: '', finalScore: undefined }
	showDecisionModal.value = true
}

const closeDecisionModal = () => {
	showDecisionModal.value = false
	if (!showDetailModal.value) {
		selectedAppeal.value = null
	}
}

const submitDecision = async () => {
	if (!selectedAppeal.value) return
	if (!decisionForm.value.decision) {
		Toast.error('请选择裁定结果')
		return
	}
	if (decisionForm.value.decision === 'APPROVED' && (decisionForm.value.finalScore === undefined || decisionForm.value.finalScore < 0 || decisionForm.value.finalScore > 100)) {
		Toast.error('请输入有效的最终分数 (0-100)')
		return
	}
	if (decisionForm.value.comment.length < 1) {
		Toast.error('请输入处理意见')
		return
	}

	submitting.value = true
	try {
		await decideAppeal(selectedAppeal.value.id, {
			decision: decisionForm.value.decision as 'APPROVED' | 'REJECTED',
			comment: decisionForm.value.comment,
			finalScore: decisionForm.value.finalScore,
		})
		Toast.success('裁定已提交')
		closeDecisionModal()
		await fetchAppeals()
	} catch (e: any) {
		console.error(e)
	} finally {
		submitting.value = false
	}
}

const truncate = (str: string, maxLen: number) => {
	if (!str) return ''
	if (str.length <= maxLen) return str
	return str.slice(0, maxLen) + '...'
}

const getStatusText = (status: string) => {
	const map: Record<string, string> = {
		PENDING: '待认领',
		UNDER_REVIEW: '审核中',
		APPROVED: '已通过',
		REJECTED: '已驳回',
		ARBITRATING: '仲裁中',
		CLOSED: '已关闭',
	}
	return map[status] || status
}

const getStatusClass = (status: string) => {
	const map: Record<string, string> = {
		PENDING: 'tag-warning',
		UNDER_REVIEW: 'tag-info',
		APPROVED: 'tag-success',
		REJECTED: 'tag-danger',
		ARBITRATING: 'tag-warning',
		CLOSED: 'tag-default',
	}
	return map[status] || ''
}

const getRoleText = (role: string) => {
	const map: Record<string, string> = {
		ROLE_STUDENT: '学生',
		ROLE_TEACHER: '教师',
		ROLE_SUPER_ADMIN: '管理员',
		ROLE_ADMIN: '管理员',
		SYSTEM: '系统',
	}
	return map[role] || role
}

const formatDate = (str: string) => {
	if (!str) return '-'
	return new Date(str).toLocaleDateString()
}

const formatDateTime = (str: string) => {
	if (!str) return '-'
	return new Date(str).toLocaleString()
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
	border-radius: 12px;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
	overflow: hidden;
	border: 1px solid #f0f0f0;
}

table {
	width: 100%;
	border-collapse: collapse;
}

th,
td {
	padding: 14px 18px;
	text-align: left;
	border-bottom: 1px solid #f0f0f0;
	font-size: 14px;
}

th {
	background: #fafafa;
	color: #909399;
	font-weight: 600;
}

tbody tr:hover {
	background: #f8fbff;
}

tbody tr:last-child td {
	border-bottom: none;
}

.reason-cell {
	max-width: 180px;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.time {
	color: #909399;
	font-size: 13px;
}

.text-muted {
	color: #909399;
	font-size: 13px;
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
.tag-warning {
	background: #fffbe6;
	color: #faad14;
	border: 1px solid #ffe58f;
}
.tag-info {
	background: #e6f7ff;
	color: #1890ff;
	border: 1px solid #91d5ff;
}
.tag-default {
	background: #f5f5f5;
	color: #666;
	border: 1px solid #d9d9d9;
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
.btn-success {
	background: #67c23a;
	color: white;
}
.btn-sm {
	padding: 5px 12px;
	font-size: 13px;
}
.btn-link {
	background: none;
	border: none;
	color: #409eff;
	cursor: pointer;
	padding: 0 5px;
	margin-left: 8px;
}
.btn-link:hover {
	text-decoration: underline;
}

.empty-card {
	background: #fff;
	border-radius: 12px;
	padding: 60px;
	text-align: center;
}

.empty-text {
	color: #909399;
	font-size: 14px;
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
	width: 520px;
	border-radius: 8px;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
	animation: slideDown 0.3s ease;
}

.modal-content.modal-wide {
	width: 700px;
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

.btn-default {
	background: #fff;
	border: 1px solid #dcdfe6;
	color: #606266;
}

.detail-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 16px;
}

.detail-item {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.detail-item.full-width {
	grid-column: span 2;
}

.detail-item label {
	font-size: 12px;
	color: #909399;
}

.detail-item span {
	font-size: 14px;
	color: #303133;
}

.reason-text {
	margin: 0;
	padding: 12px;
	background: #f5f7fa;
	border-radius: 4px;
	font-size: 14px;
	line-height: 1.6;
	white-space: pre-wrap;
}

.audit-section {
	margin-top: 24px;
	padding-top: 20px;
	border-top: 1px solid #ebeef5;
}

.audit-section h4 {
	margin: 0 0 16px 0;
	font-size: 15px;
	color: #303133;
}

.audit-timeline {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.audit-item {
	display: flex;
	flex-direction: column;
	gap: 4px;
	padding-left: 16px;
	border-left: 2px solid #e4e7ed;
	position: relative;
}

.audit-item::before {
	content: '';
	position: absolute;
	left: -6px;
	top: 4px;
	width: 10px;
	height: 10px;
	border-radius: 50%;
	background: #409eff;
}

.audit-time {
	font-size: 12px;
	color: #909399;
}

.audit-content {
	font-size: 14px;
}

.operator {
	color: #303133;
	font-weight: 500;
}

.role {
	color: #909399;
	font-size: 12px;
}

.text-success {
	color: #67c23a;
}

.text-danger {
	color: #f56c6c;
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

.form-group select,
.form-group input,
.form-group textarea {
	width: 100%;
	padding: 10px 12px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	box-sizing: border-box;
	font-size: 14px;
}

.form-group select:focus,
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
	}
	to {
		opacity: 1;
	}
}
</style>
