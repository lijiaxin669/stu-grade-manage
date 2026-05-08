import { defineStore } from 'pinia'
import { asyncRoutes, constantRoutes } from '@/router/routes'
import type { RouteRecordRaw } from 'vue-router'

function hasPermission(roles: string[], permissions: string[], route: RouteRecordRaw) {
	if (route.meta && (route.meta as any).permissions) {
		const required = (route.meta as any).permissions as string[]
		return required.some((p) => permissions.includes(p))
	}
	if (route.meta && route.meta.roles) {
		return roles.some((role) => (route.meta!.roles as string[]).includes(role))
	}
	return true
}

export function filterAsyncRoutes(routes: RouteRecordRaw[], roles: string[], permissions: string[]) {
	const res: RouteRecordRaw[] = []
	routes.forEach((route) => {
		const tmp = { ...route }
		if (hasPermission(roles, permissions, tmp)) {
			if (tmp.children) {
				tmp.children = filterAsyncRoutes(tmp.children, roles, permissions)
			}
			res.push(tmp)
		}
	})
	return res
}

export const usePermissionStore = defineStore('permission', {
	state: () => ({
		routes: [] as RouteRecordRaw[],
		addRoutes: [] as RouteRecordRaw[],
	}),
	actions: {
		generateRoutes(roles: string[], permissions: string[]) {
			const accessedRoutes = filterAsyncRoutes(asyncRoutes, roles, permissions)
			this.addRoutes = accessedRoutes
			this.routes = constantRoutes.concat(accessedRoutes)
			return accessedRoutes
		},
	},
})
