<template>
	<div class="my-appeals-container">
		<div class="page-header">
			<h2>我的申诉</h2>
		</div>

		<div class="appeals-table card" v-if="appeals.length > 0">
			<table>
				<thead>
					<tr>
						<th>课程</th>
						<th>原分数</th>
						<th>期望分数</th>
						<th>最终分数</th>
						<th>状态</th>
						<th>提交时间</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="item in appeals" :key="item.id">
						<td><strong>{{ item.courseName }}</strong></td>
						<td>{{ item.originalScore }}</td>
						<td>{{ item.expectedScore }}</td>
						<td>
							<span v-if="item.finalScore">{{ item.finalScore }}</span>
							<span v-else class="text-gray">-</span>
						</td>
						<td>
							<span class="tag" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</span>
						</td>
						<td class="time">{{ formatDate(item.createdAt) }}</td>
						<td>
							<button class="btn-link" @click="showDetail(item)">详情</button>
							<button v-if="item.status === 'REJECTED'" class="btn-link danger" @click="openArbitrateModal(item)">仲裁</button>
						</td>
					</tr>
				</tbody>
			</table>
		</div>

		<div v-else class="empty-card">
			<p class="empty-text">暂无申诉记录</p>
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
							<label>状态</label>
							<span class="tag" :class="getStatusClass(selectedAppeal.status)">{{ getStatusText(selectedAppeal.status) }}</span>
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
						<div class="detail-item" v-if="selectedAppeal.currentHandlerName">
							<label>当前处理人</label>
							<span>{{ selectedAppeal.currentHandlerName }}</span>
						</div>
						<div class="detail-item full-width">
							<label>申诉理由</label>
							<p class="reason-text">{{ selectedAppeal.reason }}</p>
						</div>
						<div class="detail-item full-width" v-if="selectedAppeal.teacherComment">
							<label>教师意见</label>
							<p class="reason-text">{{ selectedAppeal.teacherComment }}</p>
						</div>
						<div class="detail-item full-width" v-if="selectedAppeal.adminComment">
							<label>管理员意见</label>
							<p class="reason-text">{{ selectedAppeal.adminComment }}</p>
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
					<button v-if="selectedAppeal.status === 'REJECTED'" class="btn btn-primary" @click="openArbitrateModal(selectedAppeal); showDetailModal = false">
						申请仲裁
					</button>
					<button type="button" class="btn btn-default" @click="showDetailModal = false">关闭</button>
				</div>
			</div>
		</div>

		<div v-if="showArbitrateModal && selectedAppeal" class="modal-mask">
			<div class="modal-content">
				<div class="modal-header">
					<h3>申请仲裁 - {{ selectedAppeal.courseName }}</h3>
					<span class="close-btn" @click="closeArbitrateModal">×</span>
				</div>
				<form @submit.prevent="submitArbitrate">
					<div class="modal-body">
						<div class="form-group">
							<label>仲裁理由 (10-500字) <span class="required">*</span></label>
							<textarea v-model="arbitrateForm.reason" rows="4" required maxlength="500" placeholder="请详细说明申请仲裁的理由..."></textarea>
							<p class="hint">{{ arbitrateForm.reason.length }}/500</p>
						</div>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" @click="closeArbitrateModal">取消</button>
						<button type="submit" class="btn btn-primary" :disabled="submitting" :class="{ 'btn-loading': submitting }">
							提交
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
	getMyAppeals,
	getAppealDetail,
	getAppealAuditLogs,
	requestArbitration,
	type AppealDto,
	type AuditLogDto,
} from '@/api/appeal'
import Toast from '@/utils/toast'

const appeals = ref<AppealDto[]>([])
const showDetailModal = ref(false)
const selectedAppeal = ref<AppealDto | null>(null)
const auditLogs = ref<AuditLogDto[]>([])

const showArbitrateModal = ref(false)
const arbitrateForm = ref({ reason: '' })
const submitting = ref(false)

onMounted(async () => {
	await fetchAppeals()
})

const fetchAppeals = async () => {
	try {
		appeals.value = await getMyAppeals()
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
	arbitrateForm.value.reason = ''
	showArbitrateModal.value = true
}

const closeArbitrateModal = () => {
	showArbitrateModal.value = false
	if (!showDetailModal.value) {
		selectedAppeal.value = null
	}
}

const submitArbitrate = async () => {
	if (!selectedAppeal.value || arbitrateForm.value.reason.length < 10) {
		Toast.error('仲裁理由至少10个字符')
		return
	}

	submitting.value = true
	try {
		await requestArbitration(selectedAppeal.value.id, { reason: arbitrateForm.value.reason })
		Toast.success('仲裁申请已提交')
		closeArbitrateModal()
		await fetchAppeals()
	} catch (e: any) {
		console.error(e)
	} finally {
		submitting.value = false
	}
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
.my-appeals-container {
	padding: 24px;
	animation: fadeIn 0.6s ease;
	max-width: 1200px;
	margin: 0 auto;
}

.page-header h2 {
	margin: 0 0 24px 0;
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
	padding: 16px 24px;
	text-align: left;
	border-bottom: 1px solid #f0f0f0;
	font-size: 14px;
}

th {
	background: #fafafa;
	color: #595959;
	font-weight: 600;
	text-transform: uppercase;
	letter-spacing: 0.5px;
}

tbody tr:hover {
	background: #f8fbff;
}

tbody tr:last-child td {
	border-bottom: none;
}

.time {
	color: #909399;
	font-size: 13px;
}

.text-gray {
	color: #909399;
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
	width: 500px;
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

.btn {
	padding: 8px 20px;
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

.form-group label {
	display: block;
	margin-bottom: 8px;
	color: #606266;
}

.form-group textarea {
	width: 100%;
	padding: 10px 12px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	box-sizing: border-box;
	font-size: 14px;
	font-family: inherit;
	resize: vertical;
}

.form-group textarea:focus {
	border-color: #409eff;
	outline: none;
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
