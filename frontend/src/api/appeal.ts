import request from '@/utils/request'

export type AppealStatus = 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'ARBITRATING' | 'CLOSED'

export interface AppealDto {
	id: number
	gradeId: number
	studentId: number
	studentName: string
	studentUsername: string
	courseId: number
	courseName: string
	originalScore: number
	expectedScore: number
	finalScore?: number
	reason: string
	teacherComment?: string
	adminComment?: string
	status: AppealStatus
	currentHandlerId?: number
	currentHandlerName?: string
	createdAt: string
	updatedAt: string
	closedAt?: string
}

export interface AuditLogDto {
	id: number
	appealId: number
	fromStatus?: AppealStatus
	toStatus?: AppealStatus
	operatorId: number
	operatorName?: string
	operatorRole: string
	comment?: string
	success: boolean
	errorMsg?: string
	createdAt: string
}

export interface AppealCreateRequest {
	gradeId: number
	expectedScore: number
	reason: string
}

export interface TeacherDecisionRequest {
	decision: 'APPROVED' | 'REJECTED'
	comment: string
	finalScore?: number
}

export interface ArbitrateRequestRequest {
	reason: string
}

export interface AdminArbitrateRequest {
	decision: 'APPROVED' | 'REJECTED'
	comment: string
	finalScore?: number
}

export function createAppeal(data: AppealCreateRequest) {
	return request<number>({
		url: '/appeals',
		method: 'post',
		data,
	})
}

export function getMyAppeals() {
	return request<AppealDto[]>({
		url: '/appeals/mine',
		method: 'get',
	})
}

export function getPendingAppeals() {
	return request<AppealDto[]>({
		url: '/appeals/pending',
		method: 'get',
	})
}

export function claimAppeal(id: number) {
	return request<void>({
		url: `/appeals/${id}/claim`,
		method: 'post',
	})
}

export function decideAppeal(id: number, data: TeacherDecisionRequest) {
	return request<void>({
		url: `/appeals/${id}/decision`,
		method: 'post',
		data,
	})
}

export function requestArbitration(id: number, data: ArbitrateRequestRequest) {
	return request<void>({
		url: `/appeals/${id}/arbitrate-request`,
		method: 'post',
		data,
	})
}

export function getArbitratingAppeals() {
	return request<AppealDto[]>({
		url: '/appeals/arbitrating',
		method: 'get',
	})
}

export function arbitrateAppeal(id: number, data: AdminArbitrateRequest) {
	return request<void>({
		url: `/appeals/${id}/arbitrate`,
		method: 'post',
		data,
	})
}

export function getAppealDetail(id: number) {
	return request<AppealDto>({
		url: `/appeals/${id}`,
		method: 'get',
	})
}

export function getAppealAuditLogs(id: number) {
	return request<AuditLogDto[]>({
		url: `/appeals/${id}/audit-logs`,
		method: 'get',
	})
}
