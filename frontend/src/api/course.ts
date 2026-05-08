import request from '@/utils/request'

export interface CourseDto {
	id: number
	name: string
	description: string
	credits: number
	semester: string
	teacherId: number
	teacherName: string
	enrolled?: boolean // For students to know if they joined
}

export interface CourseQuery {
	page: number
	size: number
	keyword?: string
	semester?: string
	teacherId?: number
	studentId?: number // To filter "My Courses"
}

export interface CourseCreateDto {
	name: string
	description: string
	credits: number
	semester: string
	teacherId?: number // Admin assigns this
}

export interface CourseUpdateDto {
	id: number
	name: string
	description: string
	credits: number
	semester: string
	teacherId?: number
}

// Courses
export function getCourses(params: CourseQuery) {
	return request({
		url: '/courses',
		method: 'get',
		params,
	})
}

export function createCourse(data: CourseCreateDto) {
	return request({
		url: '/courses',
		method: 'post',
		data,
	})
}

export function updateCourse(data: CourseUpdateDto) {
	return request({
		url: `/courses/${data.id}`,
		method: 'put',
		data,
	})
}

export function deleteCourse(id: number) {
	return request({
		url: `/courses/${id}`,
		method: 'delete',
	})
}
