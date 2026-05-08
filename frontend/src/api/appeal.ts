import request from '@/utils/request'

export interface AppealDto {
	id: number
	gradeId: number
	studentId: number
	studentName: string
	courseId: number
	courseName: string
	originalScore: number
	expectedScore: number
	finalScore: number | null
	reason: string
	teacherComment: string | null
	adminComment: string | null
	status: string
	currentHandlerId: number | null
	createdAt: string
	updatedAt: string
	closedAt: string | null
}

export interface AuditLogDto {
	id: number
	appealId: number
	fromStatus: string | null
	toStatus: string
	operatorId: number | null
	operatorRole: string
	comment: string | null
	success: boolean
	errorMsg: string | null
	createdAt: string
}

export interface AppealCreateDto {
	gradeId: number
	expectedScore: number
	reason: string
}

export interface AppealDecisionDto {
	decision: string
	comment: string
	finalScore?: number | null
}

export interface ArbitrateRequestDto {
	reason: string
}

export interface ArbitrateDto {
	decision: string
	comment: string
	finalScore?: number | null
}

export function createAppeal(data: AppealCreateDto) {
	return request<AppealDto>({
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
	return request<AppealDto>({
		url: `/appeals/${id}/claim`,
		method: 'post',
	})
}

export function decideAppeal(id: number, data: AppealDecisionDto) {
	return request<AppealDto>({
		url: `/appeals/${id}/decision`,
		method: 'post',
		data,
	})
}

export function requestArbitration(id: number, data: ArbitrateRequestDto) {
	return request<AppealDto>({
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

export function arbitrateAppeal(id: number, data: ArbitrateDto) {
	return request<AppealDto>({
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
