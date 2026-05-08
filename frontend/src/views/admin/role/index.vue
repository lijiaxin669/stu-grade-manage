<template>
	<div class="role-perm-page">
		<div class="page-header">
			<h2>权限管理</h2>
		</div>

		<!-- Add/Delete/Disable teacher/student accounts -->
		<div class="card">
			<div class="row">
				<label>账号类型</label>
				<select v-model="userQuery.roleCode" @change="handleUserSearch">
					<option value="ROLE_TEACHER">教师</option>
					<option value="ROLE_STUDENT">学生</option>
				</select>
				<input v-model="userQuery.keyword" placeholder="搜索用户名或姓名" @keyup.enter="handleUserSearch" />
				<button class="btn btn-default" @click="handleUserSearch">查询</button>
				<button class="btn btn-primary" @click="openCreateModal">新增账号</button>
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
						<tr v-for="u in userList" :key="u.id">
							<td>{{ u.id }}</td>
							<td>{{ u.username }}</td>
							<td>{{ u.realName }}</td>
							<td>
								<span class="role-badge" :class="getRoleClass(u.roleCode)">{{ formatRole(u.roleCode) }}</span>
							</td>
							<td>
								<span class="status-dot" :class="u.status === 1 ? 'active' : 'inactive'"></span>
								{{ u.status === 1 ? '正常' : '禁用' }}
							</td>
							<td>{{ formatDate(u.createdAt) }}</td>
							<td class="actions">
								<button class="btn-link warning" :disabled="isSelf(u)" @click="!isSelf(u) && handleResetPassword(u)">
									重置密码
								</button>
								<button class="btn-link danger" :disabled="isSelf(u)" @click="!isSelf(u) && handleToggleStatus(u)">
									{{ u.status === 1 ? '禁用' : '启用' }}
								</button>
								<button class="btn-link danger" :disabled="isSelf(u)" @click="!isSelf(u) && handleDeleteUser(u)">
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
					<button :disabled="userQuery.pageNum <= 1" @click="changeUserPage(userQuery.pageNum - 1)">上一页</button>
					<span>第 {{ userQuery.pageNum }} 页 / 共 {{ userTotalPages }} 页</span>
					<button :disabled="userQuery.pageNum >= userTotalPages" @click="changeUserPage(userQuery.pageNum + 1)">
						下一页
					</button>
				</div>
			</div>
		</div>

		<!-- Create Modal -->
		<div v-if="showModal" class="modal-mask">
			<div class="modal-content">
				<div class="modal-header">
					<h3>新增账号</h3>
					<span class="close-btn" @click="showModal = false">×</span>
				</div>
				<div class="modal-body">
					<form @submit.prevent="handleCreate">
						<div class="form-group">
							<label>用户名 <span class="required">*</span></label>
							<input v-model="form.username" required minlength="4" placeholder="登录账号" />
						</div>
						<div class="form-group">
							<label>真实姓名 <span class="required">*</span></label>
							<input v-model="form.realName" required placeholder="用户姓名" />
						</div>
						<div class="form-group">
							<label>角色 <span class="required">*</span></label>
							<select v-model="form.roleCode" required>
								<option value="ROLE_TEACHER">教师</option>
								<option value="ROLE_STUDENT">学生</option>
							</select>
						</div>
						<div class="form-help">默认密码: 123456</div>

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
import { computed, onMounted, reactive, ref } from 'vue'
import Toast from '@/utils/toast'
import {
	getUsers,
	resetPassword,
	updateUserStatus,
	deleteUser,
	createUser,
	type UserDto,
	type UserCreateDto,
} from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isSelf = (u: UserDto) => u.id === userStore.userInfo?.id || u.username === userStore.userInfo?.username

/* ---------------- Admin: Account enable/disable ---------------- */
const userList = ref<UserDto[]>([])
const userTotal = ref(0)
const userQuery = reactive({
	pageNum: 1,
	pageSize: 10,
	keyword: '',
	roleCode: 'ROLE_TEACHER',
})
const userTotalPages = computed(() => Math.max(1, Math.ceil(userTotal.value / userQuery.pageSize)))

const showModal = ref(false)
const submitting = ref(false)
const form = reactive<UserCreateDto>({
	username: '',
	realName: '',
	roleCode: 'ROLE_TEACHER',
})

const fetchUsers = async () => {
	try {
		const res: any = await getUsers(userQuery as any)
		userList.value = res.list || []
		userTotal.value = res.total || 0
	} catch (e: any) {
		console.error(e)
	}
}

const handleUserSearch = () => {
	userQuery.pageNum = 1
	fetchUsers()
}

const changeUserPage = (p: number) => {
	userQuery.pageNum = p
	fetchUsers()
}

const openCreateModal = () => {
	form.username = ''
	form.realName = ''
	form.roleCode = userQuery.roleCode as any
	showModal.value = true
}

const handleCreate = async () => {
	submitting.value = true
	try {
		await createUser(form)
		Toast.success('创建成功')
		showModal.value = false
		handleUserSearch()
	} catch (e: any) {
		console.error(e)
	} finally {
		submitting.value = false
	}
}

import { confirm } from '@/utils/confirm'

// ... existing imports

const handleToggleStatus = async (user: UserDto) => {
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

const handleDeleteUser = async (user: UserDto) => {
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
		handleUserSearch()
	} catch (e: any) {
		console.error(e)
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
	if (code === 'ROLE_TEACHER') return 'teacher'
	if (code === 'ROLE_STUDENT') return 'student'
	if (code === 'ROLE_SUPER_ADMIN' || code === 'ROLE_ADMIN') return 'admin'
	return 'superadmin'
}

const formatDate = (str: string) => {
	if (!str) return '-'
	return new Date(str).toLocaleString()
}

onMounted(async () => {
	fetchUsers()
})
</script>

<style scoped>
.role-perm-page {
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
.row {
	display: flex;
	align-items: center;
	gap: 12px;
	flex-wrap: wrap;
}
select {
	height: 36px;
	padding: 0 10px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
}
input {
	height: 36px;
	padding: 0 10px;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	min-width: 220px;
}
.btn {
	height: 36px;
	padding: 0 14px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
}
.btn-default {
	background: #f4f4f5;
	color: #606266;
}
.btn-primary {
	background: #409eff;
	color: #fff;
}
.btn-primary:disabled {
	background: #a0cfff;
	cursor: not-allowed;
}
.table-card {
	background: #fff;
	padding: 0;
}
table {
	width: 100%;
	border-collapse: collapse;
}
th,
td {
	padding: 12px 15px;
	text-align: left;
	border-bottom: 1px solid #ebeef5;
	font-size: 14px;
}
th {
	background: #f5f7fa;
	color: #909399;
}
.empty-text {
	text-align: center;
	color: #909399;
	padding: 20px;
}
.pagination {
	display: flex;
	justify-content: center;
	align-items: center;
	gap: 16px;
	padding: 14px 0;
}
.pagination button {
	padding: 6px 14px;
	border: 1px solid #dcdfe6;
	background: white;
	cursor: pointer;
}
.pagination button:disabled {
	background: #f5f7fa;
	cursor: not-allowed;
}
.status-dot {
	display: inline-block;
	width: 8px;
	height: 8px;
	border-radius: 50%;
	margin-right: 6px;
}
.status-dot.active {
	background: #67c23a;
}
.status-dot.inactive {
	background: #f56c6c;
}
.actions {
	display: flex;
	gap: 10px;
}
.btn-link {
	background: transparent;
	border: none;
	color: #409eff;
	cursor: pointer;
	padding: 0;
}
.btn-link.warning {
	color: #e6a23c;
}
.btn-link.danger {
	color: #f56c6c;
}
.btn-link:disabled {
	color: #c0c4cc;
	cursor: not-allowed;
}
.role-badge {
	padding: 2px 8px;
	border-radius: 12px;
	font-size: 12px;
	color: #fff;
}
.role-badge.teacher {
	background: #409eff;
}
.role-badge.student {
	background: #67c23a;
}
.role-badge.admin {
	background: #e6a23c;
}
.role-badge.superadmin {
	background: #f56c6c;
}

.modal-mask {
	position: fixed;
	z-index: 9999;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: rgba(0, 0, 0, 0.4);
	display: flex;
	align-items: center;
	justify-content: center;
}
.modal-content {
	width: 520px;
	max-width: 92vw;
	background: white;
	border-radius: 8px;
	overflow: hidden;
	box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}
.modal-header {
	padding: 14px 16px;
	border-bottom: 1px solid #ebeef5;
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.close-btn {
	cursor: pointer;
	font-size: 20px;
	line-height: 20px;
	color: #909399;
}
.modal-body {
	padding: 16px;
}
.form-group {
	margin-bottom: 12px;
}
.form-group label {
	display: block;
	margin-bottom: 6px;
	color: #303133;
	font-size: 13px;
}
.required {
	color: #f56c6c;
}
.form-help {
	color: #909399;
	font-size: 12px;
	margin-top: 6px;
}
.modal-footer {
	display: flex;
	justify-content: flex-end;
	gap: 10px;
	padding-top: 10px;
}
</style>
