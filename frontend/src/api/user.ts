import request from '@/utils/request'

export interface UserDto {
	id: number
	username: string
	realName: string
	roleCode: string // ROLE_SUPER_ADMIN, ROLE_TEACHER, ROLE_STUDENT (ROLE_ADMIN legacy)
	status: number // 1=active, 0=disabled
	createdAt: string
}

export interface UserQuery extends Pagination {
	keyword?: string
	roleCode?: string
}

export interface Pagination {
	pageNum: number
	pageSize: number
}

export interface UserCreateDto {
	username: string
	realName: string
	roleCode: string
}

export interface UserUpdateDto {
	realName: string
	roleCode?: string
}

export function getUsers(params: UserQuery) {
	return request({
		url: '/users',
		method: 'get',
		params,
	})
}

export function createUser(data: UserCreateDto) {
	return request({
		url: '/users',
		method: 'post',
		data, // JSON body
	})
}

export function updateUser(userId: number, data: UserUpdateDto) {
	return request({
		url: `/users/${userId}`,
		method: 'put',
		data,
	})
}

export function updateUserStatus(userId: number, enabled: boolean) {
	return request({
		url: `/users/${userId}/status`,
		method: 'put',
		params: { status: enabled ? 1 : 0 },
	})
}

export function deleteUser(userId: number) {
	return request({
		url: `/users/${userId}`,
		method: 'delete',
	})
}

export function resetPassword(userId: number) {
	return request({
		url: `/users/${userId}/password/reset`,
		method: 'put',
	})
}

export function updateMyPassword(data: any) {
	return request({
		url: '/auth/password',
		method: 'put',
		data,
	})
}
