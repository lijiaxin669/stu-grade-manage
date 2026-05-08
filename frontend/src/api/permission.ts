import request from '@/utils/request'

export interface PermissionDto {
	id: number
	code: string
	name: string
	type: 'MENU' | 'API'
}

export function getAllPermissions() {
	return request<PermissionDto[]>({
		url: '/permissions',
		method: 'get',
	})
}

