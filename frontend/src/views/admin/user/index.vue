<template>
	<div class="user-manage-container">
		<div class="page-header">
			<h2>用户管理</h2>
			<button class="btn btn-primary" @click="openCreateModal">新增用户</button>
		</div>

		<div class="search-bar">
			<div class="form-item">
				<input v-model="query.keyword" placeholder="搜索用户名或姓名" @keyup.enter="handleSearch" />
			</div>
			<div class="form-item">
				<select v-model="query.roleCode" @change="handleSearch">
					<option value="">所有角色</option>
					<option value="ROLE_TEACHER">教师</option>
					<option value="ROLE_STUDENT">学生</option>
				</select>
			</div>
			<button class="btn btn-default" @click="handleSearch">查询</button>
		</div>

		<div class="table-card">
			<table>
				<thead>
					<tr>
						<th>ID</th>
						<th>用户名</th>
						<th>真实姓名</th>
						<th>角色</th>
						<th>状态</th>
						<th>创建时间</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="user in userList" :key="user.id">
						<td>{{ user.id }}</td>
						<td>{{ user.username }}</td>
						<td>{{ user.realName }}</td>
						<td>
							<span class="role-badge" :class="getRoleClass(user.roleCode)">
								{{ formatRole(user.roleCode) }}
							</span>
						</td>
						<td>
							<span class="status-dot" :class="user.status === 1 ? 'active' : 'inactive'"></span>
							{{ user.status === 1 ? '正常' : '禁用' }}
						</td>
						<td>{{ formatDate(user.createdAt) }}</td>
						<td class="actions">
							<button class="btn-link" @click="handleEdit(user)">编辑</button>
							<button
								class="btn-link warning"
								:disabled="isSelf(user)"
								@click="!isSelf(user) && handleResetPassword(user)"
								:title="isSelf(user) ? '不可重置该账号' : ''"
							>
								重置密码
							</button>
							<button
								class="btn-link danger"
								:disabled="isSelf(user)"
								@click="!isSelf(user) && handleStatus(user)"
								:title="isSelf(user) ? '不可禁用该账号' : ''"
							>
								{{ user.status === 1 ? '禁用' : '启用' }}
							</button>
							<button
								class="btn-link danger"
								:disabled="isSelf(user)"
								@click="!isSelf(user) && handleDelete(user)"
								:title="isSelf(user) ? '不可删除该账号' : ''"
							>
								删除
							</button>
						</td>
					</tr>
					<tr v-if="userList.length === 0">
						<td colspan="7" class="empty-text">暂无数据</td>
					</tr>
				</tbody>
			</table>

			<div class="pagination">
				<button :disabled="query.pageNum <= 1" @click="changePage(query.pageNum - 1)">上一页</button>
				<span>第 {{ query.pageNum }} 页 / 共 {{ totalPages }} 页</span>
				<button :disabled="query.pageNum >= totalPages" @click="changePage(query.pageNum + 1)">下一页</button>
			</div>
		</div>

		<!-- Create/Edit Modal -->
		<div v-if="showModal" class="modal-mask">
			<div class="modal-content">
				<div class="modal-header">
					<h3>{{ isEdit ? '编辑用户' : '新增用户' }}</h3>
					<span class="close-btn" @click="showModal = false">×</span>
				</div>
				<div class="modal-body">
					<form @submit.prevent="handleSubmit">
						<div class="form-group">
							<label>用户名 <span class="required">*</span></label>
							<input
								v-model="form.username"
								:disabled="isEdit"
								:required="!isEdit"
								minlength="4"
								placeholder="登录账号"
							/>
						</div>
						<div class="form-group">
							<label>真实姓名 <span class="required">*</span></label>
							<input v-model="form.realName" required placeholder="用户姓名" />
						</div>
						<div class="form-group">
							<label>角色 <span class="required">*</span></label>
							<select v-model="form.roleCode" required>
								<option value="ROLE_STUDENT">学生</option>
								<option value="ROLE_TEACHER">教师</option>
							</select>
						</div>
						<div class="form-help" v-if="!isEdit">默认密码: 123456</div>

						<div class="modal-footer">
							<button type="button" class="btn btn-default" @click="showModal = false">取消</button>
							<button type="submit" class="btn btn-primary" :disabled="submitting">
								{{ submitting ? '提交中...' : '确定' }}
							</button>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getUsers, createUser, updateUser, updateUserStatus, resetPassword, deleteUser } from '@/api/user'
import type { UserDto, UserCreateDto } from '@/api/user'
import { useUserStore } from '@/store/user'
import Toast from '@/utils/toast'
import { confirm } from '@/utils/confirm'

const userStore = useUserStore()
const isSelf = (u: UserDto) => u.id === userStore.userInfo?.id || u.username === userStore.userInfo?.username

/* ---------------- API Integration ---------------- */

// Data
const userList = ref<UserDto[]>([])
const total = ref(0)
const query = reactive({
	pageNum: 1,
	pageSize: 10,
	keyword: '',
	roleCode: '',
})

// Modal State
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editingUserId = ref<number | null>(null)
const form = reactive<UserCreateDto>({
	username: '',
	realName: '',
	roleCode: 'ROLE_STUDENT',
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / query.pageSize)))

onMounted(() => {
	fetchList()
})

const fetchList = async () => {
	try {
		const res: any = await getUsers(query)
		userList.value = res.list
		total.value = res.total
	} catch (e) {
		console.error(e)
	}
}

const handleSearch = () => {
	query.pageNum = 1
	fetchList()
}

const changePage = (p: number) => {
	query.pageNum = p
	fetchList()
}

/* ---------------- Actions ---------------- */

const openCreateModal = () => {
	isEdit.value = false
	editingUserId.value = null
	form.username = ''
	form.realName = ''
	form.roleCode = 'ROLE_STUDENT'
	showModal.value = true
}

const handleEdit = (user: UserDto) => {
	if (user.roleCode === 'ROLE_SUPER_ADMIN' || user.roleCode === 'ROLE_ADMIN') {
		Toast.error('禁止编辑超级管理员账号')
		return
	}
	isEdit.value = true
	editingUserId.value = user.id
	form.username = user.username
	form.realName = user.realName
	form.roleCode = user.roleCode
	showModal.value = true
}

const handleSubmit = async () => {
	submitting.value = true
	try {
		if (isEdit.value) {
			if (!editingUserId.value) throw new Error('缺少用户ID')
			await updateUser(editingUserId.value, { realName: form.realName, roleCode: form.roleCode })
			Toast.success('更新成功')
		} else {
			await createUser(form)
			Toast.success('创建成功')
		}
		showModal.value = false
		fetchList()
	} catch (e: any) {
		console.error(e)
	} finally {
		submitting.value = false
	}
}

const handleResetPassword = async (user: UserDto) => {
	const ok = await confirm({
		type: 'danger',
		title: '重置密码',
		message: `确定要重置用户 [${user.realName}] 的密码为 123456 吗？`,
		confirmText: '确认重置',
	})
	if (!ok) return
	try {
		await resetPassword(user.id)
		Toast.success('密码重置成功')
	} catch (e: any) {
		console.error(e)
	}
}

const handleStatus = async (user: UserDto) => {
	const action = user.status === 1 ? '禁用' : '启用'
	const ok = await confirm({
		type: user.status === 1 ? 'danger' : 'default',
		title: `${action}账号`,
		message: `确定要${action}用户 [${user.username}] 吗？`,
		confirmText: `确认${action}`,
	})
	if (!ok) return
	try {
		const nextEnabled = user.status !== 1
		await updateUserStatus(user.id, nextEnabled)
		user.status = nextEnabled ? 1 : 0
		Toast.success(`用户${action}成功`)
	} catch (e: any) {
		console.error(e)
	}
}

const handleDelete = async (user: UserDto) => {
	const ok = await confirm({
		type: 'danger',
		title: '删除账号',
		message: `确定要删除用户 [${user.username}] 吗？删除后无法恢复。`,
		confirmText: '确认删除',
	})
	if (!ok) return
	try {
		await deleteUser(user.id)
		Toast.success('删除成功')
		fetchList()
	} catch (e: any) {
		console.error(e)
	}
}

/* ---------------- Helpers ---------------- */

const formatRole = (code: string) => {
	const map: Record<string, string> = {
		ROLE_SUPER_ADMIN: '超级管理员',
		ROLE_ADMIN: '超级管理员', // backward-compat
		ROLE_TEACHER: '教师',
		ROLE_STUDENT: '学生',
	}
	return map[code] || code
}

const getRoleClass = (code: string) => {
	if (code === 'ROLE_SUPER_ADMIN' || code === 'ROLE_ADMIN') return 'badge-admin'
	if (code === 'ROLE_TEACHER') return 'badge-teacher'
	return 'badge-student'
}

const formatDate = (str: string) => {
	if (!str) return '-'
	return new Date(str).toLocaleString()
}
</script>

<style scoped>
/* Layout / Containers */
.user-manage-container {
	animation: fadeIn 0.3s ease;
}

.page-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;
}

.page-header h2 {
	margin: 0;
	color: #303133;
	font-size: 20px;
}

.search-bar {
	background: #fff;
	padding: 15px 20px;
	border-radius: 4px;
	margin-bottom: 20px;
	display: flex;
	gap: 15px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.form-item input,
.form-item select {
	height: 36px;
	padding: 0 10px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	min-width: 200px;
}

/* Table Style */
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

.role-badge {
	padding: 2px 8px;
	border-radius: 10px;
	font-size: 12px;
}

.badge-admin {
	background: #f0f9eb;
	color: #67c23a;
}
.badge-teacher {
	background: #ecf5ff;
	color: #409eff;
}
.badge-student {
	background: #f4f4f5;
	color: #909399;
}

.status-dot {
	display: inline-block;
	width: 8px;
	height: 8px;
	border-radius: 50%;
	margin-right: 5px;
}
.active {
	background: #67c23a;
}
.inactive {
	background: #f56c6c;
}

/* Buttons */
.btn {
	padding: 0 16px;
	height: 36px;
	border-radius: 4px;
	border: none;
	cursor: pointer;
	font-size: 14px;
	transition: all 0.2s;
}

.btn-primary {
	background: #409eff;
	color: #fff;
}
.btn-primary:hover {
	background: #66b1ff;
}
.btn-primary:active {
	background: #3a8ee6;
}

.btn-default {
	background: #fff;
	border: 1px solid #dcdfe6;
	color: #606266;
}
.btn-default:hover {
	color: #409eff;
	border-color: #c6e2ff;
	background-color: #ecf5ff;
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
.btn-link.warning {
	color: #e6a23c;
}

/* Pagination */
.pagination {
	margin-top: 20px;
	display: flex;
	justify-content: flex-end;
	align-items: center;
	gap: 10px;
}

.pagination button {
	padding: 5px 12px;
	border: 1px solid #dcdfe6;
	background: #fff;
	cursor: pointer;
	border-radius: 4px;
}
.pagination button:disabled {
	color: #c0c4cc;
	background-color: #fff;
	cursor: not-allowed;
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
	width: 480px;
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
	color: #909399;
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
.form-group label {
	display: block;
	margin-bottom: 8px;
	color: #606266;
}
.required {
	color: #f56c6c;
}
.form-group input,
.form-group select {
	width: 100%;
	height: 36px;
	padding: 0 10px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	box-sizing: border-box;
}
.form-help {
	font-size: 12px;
	color: #909399;
	margin-bottom: 15px;
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
