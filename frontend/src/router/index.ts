import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes } from './routes'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'

function getHomePath(roles: string[]) {
	if (roles.includes('ROLE_STUDENT')) return '/student'
	if (roles.includes('ROLE_TEACHER')) return '/teacher'
	if (roles.includes('ROLE_SUPER_ADMIN')) return '/admin'
	return '/login'
}

const router = createRouter({
	history: createWebHistory(),
	routes: constantRoutes,
})

// Permission Guard
let currentAuthKey: string | null = null

const DYNAMIC_ROUTE_NAMES = ['AdminRoot', 'TeacherRoot', 'StudentRoot', 'CatchAll'] as const

function buildAuthKey(roles: string[], permissions: string[]) {
	const r = [...roles].sort().join(',')
	const p = [...permissions].sort().join(',')
	return `${r}|${p}`
}

function resetDynamicRoutes() {
	DYNAMIC_ROUTE_NAMES.forEach((name) => {
		if (router.hasRoute(name)) router.removeRoute(name)
	})
	currentAuthKey = null
}

router.beforeEach(async (to, _from, next) => {
	const userStore = useUserStore()
	const permissionStore = usePermissionStore()

	// Try load from storage if empty
	if (!userStore.userInfo) {
		userStore.loadUserFromStorage()
	}

	const hasToken = !!userStore.userInfo

	// If logged out, remove previously generated dynamic routes.
	// Otherwise switching accounts (admin -> teacher/student) will fall through to /404.
	if (!hasToken && currentAuthKey) {
		resetDynamicRoutes()
	}

	// If we have cached user but missing roles/permissions (e.g. old localStorage after backend upgrade),
	// refresh from server; otherwise dynamic routing will filter everything and end up at /404.
	if (hasToken && (userStore.roles.length === 0 || userStore.permissions.length === 0)) {
		try {
			const resp = await fetch('/api/auth/me', { credentials: 'include' })
			const json: any = await resp.json()
			if (json && json.code === 0 && json.data) {
				userStore.setUserInfo(json.data)
			} else {
				userStore.logout()
			}
		} catch {
			userStore.logout()
		}
	}

	// If the authenticated identity changed (role switch / permission changes), rebuild dynamic routes.
	if (hasToken) {
		const nextKey = buildAuthKey(userStore.roles, userStore.permissions)
		if (currentAuthKey && currentAuthKey !== nextKey) {
			resetDynamicRoutes()
		}
	}

	if (hasToken) {
		if (to.path === '/login') {
			// Root route redirects to /login by default; send authenticated users directly to their home
			const homePath = getHomePath(userStore.roles)
			if (homePath === '/login') {
				userStore.logout()
				next('/login')
				} else {
					next({ path: homePath })
				}
			} else {
				if (currentAuthKey) {
					if (to.path === '/') {
						// Redirect based on role
						const roles = userStore.roles
						console.log('Router Check: / matched. Roles:', roles)
					const homePath = getHomePath(roles)
					if (homePath === '/login') {
						console.warn('Router: No valid role found. Logout.')
						// No valid role found or empty roles -> Logout
						userStore.logout()
						next('/login')
					} else {
						next({ path: homePath })
					}
					} else {
						next()
					}
				} else {
					try {
						const roles = userStore.roles
						const accessRoutes = permissionStore.generateRoutes(roles, userStore.permissions)
						accessRoutes.forEach((route) => router.addRoute(route))

						// Add Catch-All (404) route LAST
						router.addRoute({
							path: '/:pathMatch(.*)*',
							name: 'CatchAll',
							redirect: '/404',
							meta: { hidden: true },
						})

						currentAuthKey = buildAuthKey(userStore.roles, userStore.permissions)
						next({ ...to, replace: true })
					} catch (error) {
						userStore.logout()
						next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
					}
			}
		}
	} else {
		// No token
		if (
			constantRoutes.some((r) => r.path === to.path) ||
			to.path === '/login' ||
			to.path === '/404' ||
			to.path === '/403'
		) {
			next()
		} else {
			next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
		}
	}
})

export default router
