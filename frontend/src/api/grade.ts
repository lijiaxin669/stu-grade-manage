import request from '@/utils/request'

export interface GradeDto {
	id: number
	courseId: number
	courseName: string
	studentId: number
	studentName: string
	studentUsername: string
	score: number
	gradedAt: string
}

export interface GradeInputDto {
	courseId: number
	studentId: number
	score: number
	type?: string
}

export interface GradeUpdateDto {
	score: number
	type?: string
}

export interface CourseStatisticsDto {
	courseId: number
	courseName: string
	averageScore: number
	passRate: number
	studentCount: number
	passedCount: number
	maxScore: number
	minScore: number
}

// Queries
export function getGrades(params: any) {
	// params: courseId, studentId, page, size
	return request({
		url: '/grades',
		method: 'get',
		params,
	})
}

// Actions
export function inputGrade(data: GradeInputDto) {
	return request({
		url: '/grades',
		method: 'post',
		data,
	})
}

export function updateGrade(id: number, data: GradeUpdateDto) {
	return request({
		url: `/grades/${id}`,
		method: 'put',
		data,
	})
}

export function deleteGrade(id: number) {
	return request({
		url: `/grades/${id}`,
		method: 'delete',
	})
}

// Stats
export function getStats(params: { courseId: number; passLine?: number }) {
	return request<CourseStatisticsDto>({
		url: '/grades/stats',
		method: 'get',
		params: { courseId: params.courseId },
	})
}

export function exportGrades(courseId: number) {
	return request({
		url: '/grades/export',
		method: 'get',
		params: { courseId },
		responseType: 'blob',
	})
}
