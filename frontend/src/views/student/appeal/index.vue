<template>
	<div class="student-appeal-container">
		<div class="page-header">
			<h2>我的申诉</h2>
		</div>

		<div class="appeals-table card">
			<table>
				<thead>
					<tr>
						<th>课程</th>
						<th>原分数</th>
						<th>期望分数</th>
						<th>最终分数</th>
						<th>申诉原因</th>
						<th>教师评语</th>
						<th>状态</th>
						<th>更新时间</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="a in appeals" :key="a.id">
						<td>{{ a.courseName }}</td>
						<td>{{ a.originalScore }}</td>
						<td>{{ a.expectedScore }}</td>
						<td>{{ a.finalScore ?? '-' }}</td>
						<td class="reason-cell" :title="a.reason">{{ a.reason }}</td>
						<td>{{ a.teacherComment ?? '-' }}</td>
						<td>
							<span class="tag" :class="statusClass(a.status)">{{ statusLabel(a.status) }}</span>
						</td>
						<td class="time">{{ formatDate(a.updatedAt) }}</td>
						<td>
							<button v-if="a.status === 'REJECTED'" class="btn-link" @click="openArbitrateModal(a)">发起仲裁</button>
							<button class="btn-link" @click="viewDetail(a)">详情</button>
						</td>
					</tr>
					<tr v-if="appeals.length === 0">
						<td colspan="9" class="empty-text">暂无申诉记录</td>
					</tr>
				</tbody>
			</table>
		</div>

		<div v-if="showArbitrateModal" class="modal-mask">
			<div class="modal-content">
				<div class="modal-header">
					<h3>发起仲裁</h3>
					<span class="close-btn" @click="showArbitrateModal = false">&times;</span>
				</div>
				<form @submit.prevent="submitArbitrate">
					<div class="modal-body">
						<div class="form-group">
							<label>仲裁理由 <span class="required">*</span></label>
							<textarea v-model="arbitrateForm.reason" rows="4" required minlength="1" maxlength="500"></textarea>
						</div>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" @click="showArbitrateModal = false">取消</button>
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
import { getMyAppeals, requestArbitration, getAppealDetail, getAppealAuditLogs, type AppealDto, type AuditLogDto } from '@/api/appeal'
import Toast from '@/utils/toast'

const appeals = ref<AppealDto[]>([])
const showArbitrateModal = ref(false)
const showDetailModal = ref(false)
const submitting = ref(false)
const currentAppeal = ref<AppealDto | null>(null)
const arbitrateForm = ref({ reason: '' })
const detailData = ref<AppealDto | null>(null)
const auditLogs = ref<AuditLogDto[]>([])

onMounted(async () => {
	await fetchAppeals()
})

const fetchAppeals = async () => {
	try {
		const res = await getMyAppeals()
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

const openArbitrateModal = (a: AppealDto) => {
	currentAppeal.value = a
	arbitrateForm.value.reason = ''
	showArbitrateModal.value = true
}

const submitArbitrate = async () => {
	if (!currentAppeal.value) return
	submitting.value = true
	try {
		await requestArbitration(currentAppeal.value.id, { reason: arbitrateForm.value.reason })
		showArbitrateModal.value = false
		Toast.success('仲裁申请已提交')
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
.student-appeal-container {
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

th, td {
	padding: 14px 18px;
	text-align: left;
	border-bottom: 1px solid #f0f0f0;
	font-size: 14px;
}

th {
	background: #fafafa;
	color: #595959;
	font-weight: 600;
	font-size: 13px;
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

.tag-warning {
	background: #fff7e6;
	color: #fa8c16;
	border: 1px solid #ffd591;
}

.tag-info {
	background: #e6f7ff;
	color: #1890ff;
	border: 1px solid #91d5ff;
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

.tag-purple {
	background: #f9f0ff;
	color: #722ed1;
	border: 1px solid #d3adf7;
}

.tag-default {
	background: #f5f5f5;
	color: #8c8c8c;
	border: 1px solid #d9d9d9;
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
}

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
	width: 450px;
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

.form-group {
	margin-bottom: 16px;
}

.form-group label {
	display: block;
	margin-bottom: 8px;
	color: #606266;
	font-size: 14px;
}

.form-group textarea {
	width: 100%;
	padding: 8px 12px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	box-sizing: border-box;
	font-size: 14px;
	resize: vertical;
}

.form-group textarea:focus {
	border-color: #409eff;
	outline: none;
	box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.required {
	color: #ff4d4f;
}

.btn {
	padding: 8px 20px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 14px;
}

.btn-primary {
	background: #1890ff;
	color: white;
}

.btn-primary:hover {
	background: #40a9ff;
}

.btn-default {
	background: #fff;
	border: 1px solid #dcdfe6;
	color: #606266;
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
	from { opacity: 0; transform: translateY(10px); }
	to { opacity: 1; transform: translateY(0); }
}

@keyframes slideDown {
	from { opacity: 0; transform: translateY(-20px); }
	to { opacity: 1; transform: translateY(0); }
}
</style>
