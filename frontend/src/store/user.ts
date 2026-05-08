import { defineStore } from 'pinia'
import { login, logout } from '@/api/auth'

interface UserState {
	userInfo: any
	roles: string[]
	permissions: string[]
}

export const useUserStore = defineStore('user', {
	state: (): UserState => ({
		userInfo: null,
		roles: [],
		permissions: [],
	}),
	actions: {
		async login(loginForm: any) {
			const res = await login(loginForm)
			this.setUserInfo(res)
		},
		async checkAuth() {
			// Placeholder for auth check if needed
		},
		setUserInfo(user: any) {
			// Normalize backend principal into a stable shape used by UI
			const raw = user ? { ...user } : null

			if (raw && raw.authorities) {
				let authorities: string[] = []
				if (Array.isArray(user.authorities) && user.authorities.length > 0 && typeof user.authorities[0] === 'string') {
					authorities = user.authorities
				} else if (Array.isArray(user.authorities)) {
					authorities = user.authorities.map((a: any) => a.authority)
				}

				this.roles = authorities.filter((a) => a.startsWith('ROLE_'))
				this.permissions = authorities.filter((a) => !a.startsWith('ROLE_'))
			} else {
				if (user && user.roles && Array.isArray(user.roles)) {
					this.roles = user.roles
				} else {
					this.roles = []
				}
				this.permissions = []
			}

			if (raw) {
				// Spring Security principal uses userId; keep a compatible id field for UI
				if (raw.userId != null && raw.id == null) raw.id = raw.userId
				if (!raw.roleCode && this.roles.length > 0) raw.roleCode = this.roles[0]
			}

			this.userInfo = raw
			localStorage.setItem('grade_user', JSON.stringify(raw))
		},
		loadUserFromStorage() {
			const stored = localStorage.getItem('grade_user')
			if (stored) {
				try {
					const user = JSON.parse(stored)
					this.setUserInfo(user)
				} catch (e) {
					localStorage.removeItem('grade_user')
				}
			}
		},
		async logout() {
			// Clear local state first to avoid router guard loops when session has expired.
			this.userInfo = null
			this.roles = []
			this.permissions = []
			localStorage.removeItem('grade_user')

			try {
				await logout()
			} catch {
				// Ignore network/auth errors during logout.
			}
		},
	},
})
