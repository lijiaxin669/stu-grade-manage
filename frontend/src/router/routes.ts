import type { RouteRecordRaw } from 'vue-router'

// Layouts
const AdminLayout = () => import('@/layout/AdminLayout.vue')
const UserLayout = () => import('@/layout/UserLayout.vue')

// Common Pages
const Login = () => import('@/views/login/index.vue')
const Register = () => import('@/views/register/index.vue')
const Page403 = () => import('@/views/error/403.vue')
const Page404 = () => import('@/views/error/404.vue')

export const constantRoutes: RouteRecordRaw[] = [
	{
		path: '/login',
		name: 'Login',
		component: Login,
		meta: { hidden: true },
	},
	{
		path: '/register',
		name: 'Register',
		component: Register,
		meta: { hidden: true },
	},
	{
		path: '/',
		name: 'Root',
		redirect: '/login', // Default fail-safe, guarded by beforeEach
		meta: { hidden: true },
	},
	{
		path: '/403',
		name: 'Page403',
		component: Page403,
		meta: { hidden: true },
	},
	{
		path: '/404',
		name: 'Page404',
		component: Page404,
		meta: { hidden: true },
	},
]

// Admin Router Map
export const adminRoutes: RouteRecordRaw[] = [
	{
		path: '/admin',
		name: 'AdminRoot',
		component: AdminLayout,
		redirect: '/admin/overview',
		meta: { roles: ['ROLE_SUPER_ADMIN'], title: '成绩管理后台' },
			children: [
				{
					path: 'overview',
					name: 'AdminOverview',
				// Reuse Dashboard component or create new one?
				// User asked for "Admin Overview" page separately.
				// I'll point to the new component I'm about to create.
				component: () => import('@/views/admin/overview/index.vue'),
				meta: { title: '系统概览', icon: 'dashboard', permissions: ['MENU_ADMIN_OVERVIEW'] },
				},
				{
					path: 'users',
					name: 'UserManage',
					component: () => import('@/views/admin/user/index.vue'),
					meta: { title: '用户管理', permissions: ['MENU_ADMIN_USERS'] },
				},
				{
					path: 'roles',
					name: 'RoleManage',
					component: () => import('@/views/admin/role/index.vue'),
					meta: { title: '权限管理', permissions: ['MENU_ADMIN_ROLES'], hidden: true },
				},
				{
					path: 'courses',
				name: 'CourseManage',
				component: () => import('@/views/admin/course/index.vue'),
				meta: { title: '课程管理', permissions: ['MENU_ADMIN_COURSES'] },
			},
				{
					path: 'roster',
					name: 'AdminRoster',
					component: () => import('@/views/admin/roster/index.vue'),
					meta: { title: '课程名单', permissions: ['MENU_ADMIN_ROSTER'] },
				},
				{
					path: 'stats',
					name: 'AdminStats',
					component: () => import('@/views/teacher/stats/index.vue'),
					meta: { title: '成绩统计', permissions: ['MENU_ADMIN_STATS'] },
				},
				{
					path: 'export',
					name: 'AdminExport',
					component: () => import('@/views/teacher/export/index.vue'),
					meta: { title: '成绩导出', permissions: ['MENU_ADMIN_EXPORT'] },
				},
				{
					path: 'appeals',
					name: 'AdminAppeals',
					component: () => import('@/views/admin/appeal/index.vue'),
					meta: { title: '申诉仲裁', icon: 'audit', permissions: ['MENU_ADMIN_APPEAL'] },
				},
			],
		},
]

export const teacherRoutes: RouteRecordRaw[] = [
	{
		path: '/teacher',
		name: 'TeacherRoot',
		component: AdminLayout,
		redirect: '/teacher/overview',
		meta: { roles: ['ROLE_TEACHER'], title: 'Teaching Console' },
		children: [
			{
				path: 'overview',
				name: 'TeacherOverview',
				component: () => import('@/views/teacher/overview/index.vue'),
				meta: { title: '教学概览', icon: 'dashboard', permissions: ['MENU_TEACHER_OVERVIEW'] },
			},
			{
				path: 'courses',
				name: 'TeacherCourses',
				component: () => import('@/views/teacher/course/index.vue'),
				meta: { title: '我的课程', icon: 'book', permissions: ['MENU_TEACHER_COURSES'] },
			},
			{
				path: 'grades',
				name: 'TeacherGrades',
				component: () => import('@/views/teacher/grade/index.vue'),
				meta: { title: '成绩录入', icon: 'edit', permissions: ['MENU_TEACHER_GRADES'] },
			},
			{
				path: 'stats',
				name: 'TeacherStats',
				component: () => import('@/views/teacher/stats/index.vue'),
				meta: { title: '成绩统计', icon: 'chart', permissions: ['MENU_TEACHER_STATS'] },
			},
			{
				path: 'export',
				name: 'TeacherExport',
				component: () => import('@/views/teacher/export/index.vue'),
				meta: { title: '成绩导出', icon: 'export', permissions: ['MENU_TEACHER_EXPORT'] },
			},
			{
				path: 'appeals',
				name: 'TeacherAppeals',
				component: () => import('@/views/teacher/appeal/index.vue'),
				meta: { title: '申诉处理', icon: 'audit', permissions: ['MENU_TEACHER_APPEAL'] },
			},
		],
	},
]

// Student Router Map
export const studentRoutes: RouteRecordRaw[] = [
	{
		path: '/student',
		name: 'StudentRoot',
		component: UserLayout,
		redirect: '/student/overview',
		meta: { roles: ['ROLE_STUDENT'], title: 'Student Center' },
			children: [
				{
					path: 'overview',
					name: 'StudentOverview',
					component: () => import('@/views/student/overview/index.vue'),
					meta: { title: '我的首页', permissions: ['MENU_STUDENT_OVERVIEW'] },
				},
				{
					path: 'my-grades',
					name: 'StudentGrades',
					component: () => import('@/views/student/grade/index.vue'),
					meta: { title: '我的成绩', permissions: ['MENU_STUDENT_GRADES'] },
				},
				{
					path: 'courses',
					name: 'StudentCourses',
					component: () => import('@/views/student/course/index.vue'),
					meta: { title: '课程信息', permissions: ['MENU_STUDENT_COURSES'] },
				},
			{
				path: 'profile',
				name: 'StudentProfile',
				component: () => import('@/views/student/profile/index.vue'),
				meta: { title: '个人信息', permissions: ['MENU_STUDENT_PROFILE'] },
			},
			{
				path: 'appeals',
				name: 'StudentAppeals',
				component: () => import('@/views/student/appeal/index.vue'),
				meta: { title: '我的申诉', permissions: ['MENU_STUDENT_APPEAL'] },
			},
		],
	},
]

// Default redirect for root - logic in permission store will handle where to go
export const asyncRoutes: RouteRecordRaw[] = [...adminRoutes, ...teacherRoutes, ...studentRoutes]
