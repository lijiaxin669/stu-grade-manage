<template>
	<div class="admin-appeal-container">
		<div class="page-header">
			<h2>申诉仲裁</h2>
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
						<th>教师意见</th>
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
						<td class="reason-cell" :title="item.reason">{{ truncate(item.reason, 25) }}</td>
						<td class="reason-cell" :title="item.teacherComment">{{ truncate(item.teacherComment || '', 25) }}</td>
						<td class="time">{{ formatDate(item.createdAt) }}</td>
						<td>
							<button class="btn btn-primary btn-sm" @click="openArbitrateModal(item)">仲裁</button>
							<button class="btn-link" @click="showDetail(item)">详情</button>
						</td>
					</tr>
				</tbody>
			</table>
		</div>

		<div v-else class="empty-card">
			<p class="empty-text">暂无待仲裁的申诉</p>
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
							<span class="tag tag-warning">仲裁中</span>
						</div>
						<div class="detail-item full-width">
							<label>申诉理由</label>
							<p class="reason-text">{{ selectedAppeal.reason }}</p>
						</div>
						<div class="detail-item full-width" v-if="selectedAppeal.teacherComment">
							<label>教师处理意见</label>
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
					<button class="btn btn-primary" @click="openArbitrateModal(selectedAppeal); showDetailModal = false">
						进行仲裁
					</button>
					<button type="button" class="btn btn-default" @click="showDetailModal = false">关闭</button>
				</div>
			</div>
		</div>

		<div v-if="showArbitrateModal && selectedAppeal" class="modal-mask">
			<div class="modal-content">
				<div class="modal-header">
					<h3>仲裁申诉 - {{ selectedAppeal.courseName }}</h3>
					<span class="close-btn" @click="closeArbitrateModal">×</span>
				</div>
				<form @submit.prevent="submitArbitrate">
					<div class="modal-body">
						<div class="case-summary">
							<div class="summary-row">
								<label>学生:</label>
								<span>{{ selectedAppeal.studentName }}</span>
							</div>
							<div class="summary-row">
								<label>原分数:</label>
								<span class="highlight">{{ selectedAppeal.originalScore }}</span>
							</div>
							<div class="summary-row">
								<label>期望分数:</label>
								<span class="highlight">{{ selectedAppeal.expectedScore }}</span>
							</div>
						</div>
						<div class="form-group">
							<label>申诉理由</label>
							<div class="reason-box">{{ selectedAppeal.reason }}</div>
						</div>
						<div class="form-group" v-if="selectedAppeal.teacherComment">
							<label>教师意见</label>
							<div class="reason-box">{{ selectedAppeal.teacherComment }}</div>
						</div>
						<div class="form-group">
							<label>仲裁结果 <span class="required">*</span></label>
							<select v-model="arbitrateForm.decision" required>
								<option value="">请选择</option>
								<option value="APPROVED">通过</option>
								<option value="REJECTED">驳回</option>
							</select>
						</div>
						<div class="form-group" v-if="arbitrateForm.decision === 'APPROVED'">
							<label>最终分数 (0-100) <span class="required">*</span></label>
							<input type="number" v-model.number="arbitrateForm.finalScore" min="0" max="100" step="0.5" required />
						</div>
						<div class="form-group">
							<label>仲裁意见 <span class="required">*</span></label>
							<textarea v-model="arbitrateForm.comment" rows="3" required maxlength="500" placeholder="请输入仲裁意见..."></textarea>
							<p class="hint">{{ arbitrateForm.comment.length }}/500</p>
						</div>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" @click="closeArbitrateModal">取消</button>
						<button type="submit" class="btn btn-primary" :disabled="submitting">
							提交仲裁
						</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
	getArbitratingAppeals,
	getAppealDetail,
	getAppealAuditLogs,
	arbitrateAppeal,
	type AppealDto,
	type AuditLogDto,
} from '@/api/appeal'
import Toast from '@/utils/toast'

const appeals = ref<AppealDto[]>([])
const showDetailModal = ref(false)
const showArbitrateModal = ref(false)
const selectedAppeal = ref<AppealDto | null>(null)
const auditLogs = ref<AuditLogDto[]>([])

const arbitrateForm = ref({
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
		appeals.value = await getArbitratingAppeals()
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

const openArbitrateModal = (item: AppealDto) => {
	selectedAppeal.value = item
	arbitrateForm.value = { decision: '', comment: '', finalScore: undefined }
	showArbitrateModal.value = true
}

const closeArbitrateModal = () => {
	showArbitrateModal.value = false
	if (!showDetailModal.value) {
		selectedAppeal.value = null
	}
}

const submitArbitrate = async () => {
	if (!selectedAppeal.value) return
	if (!arbitrateForm.value.decision) {
		Toast.error('请选择仲裁结果')
		return
	}
	if (arbitrateForm.value.decision === 'APPROVED' && (arbitrateForm.value.finalScore === undefined || arbitrateForm.value.finalScore < 0 || arbitrateForm.value.finalScore > 100)) {
		Toast.error('请输入有效的最终分数 (0-100)')
		return
	}
	if (arbitrateForm.value.comment.length < 1) {
		Toast.error('请输入仲裁意见')
		return
	}

	submitting.value = true
	try {
		await arbitrateAppeal(selectedAppeal.value.id, {
			decision: arbitrateForm.value.decision as 'APPROVED' | 'REJECTED',
			comment: arbitrateForm.value.comment,
			finalScore: arbitrateForm.value.finalScore,
		})
		Toast.success('仲裁已提交')
		closeArbitrateModal()
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
.admin-appeal-container {
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
	padding: 14px 16px;
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
	max-width: 150px;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.time {
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
.tag-warning {
	background: #fffbe6;
	color: #faad14;
	border: 1px solid #ffe58f;
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
	width: 560px;
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
	max-height: 65vh;
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

.case-summary {
	background: #f5f7fa;
	padding: 16px;
	border-radius: 6px;
	margin-bottom: 16px;
}

.summary-row {
	display: flex;
	align-items: center;
	gap: 8px;
	margin-bottom: 8px;
}

.summary-row:last-child {
	margin-bottom: 0;
}

.summary-row label {
	color: #909399;
	min-width: 70px;
}

.summary-row .highlight {
	font-weight: 600;
	color: #409eff;
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

.reason-box {
	padding: 12px;
	background: #fafafa;
	border-radius: 4px;
	font-size: 14px;
	line-height: 1.6;
	white-space: pre-wrap;
	border: 1px solid #e4e7ed;
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
