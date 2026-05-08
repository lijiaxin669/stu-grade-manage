import request from '@/utils/request'

export interface RoleDto {
	id: number
	code: string
	name: string
	permissions?: any[]
}

export function getAllRoles() {
	return request<RoleDto[]>({
		url: '/roles',
		method: 'get',
	})
}

export function getRolePermissions(roleId: number) {
	return request<string[]>({
		url: `/roles/${roleId}/permissions`,
		method: 'get',
	})
}

export function setRolePermissions(roleId: number, permissionCodes: string[]) {
	return request<void>({
		url: `/roles/${roleId}/permissions`,
		method: 'put',
		data: { permissionCodes },
	})
}

