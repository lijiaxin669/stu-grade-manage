import request from '@/utils/request'

export interface EnrollmentDto {
	id: number
	studentId: number
	studentName: string
	studentUsername: string
	courseId: number
}

export function getRoster(courseId: number) {
	return request<EnrollmentDto[]>({
		url: '/enrollments',
		method: 'get',
		params: { courseId },
	})
}

export function addStudentToCourse(courseId: number, studentId: number) {
	return request<void>({
		url: `/enrollments/courses/${courseId}/students/${studentId}`,
		method: 'post',
	})
}

export function removeStudentFromCourse(courseId: number, studentId: number) {
	return request<void>({
		url: `/enrollments/courses/${courseId}/students/${studentId}`,
		method: 'delete',
	})
}

