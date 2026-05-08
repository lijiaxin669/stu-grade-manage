import request from '@/utils/request'

export function register(data: any) {
	return request({
		url: '/auth/register',
		method: 'post',
		data,
	})
}

export function login(data: any) {
	return request({
		url: '/auth/login',
		method: 'post',
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		data: data, // Axios automatically serializes to form-urlencoded if we import qs, OR we can use URLSearchParams.
		// simpler:
		// data: new URLSearchParams(data)
	})
}

export function logout() {
	return request({
		url: '/auth/logout',
		method: 'post',
	})
}

export function getUserInfo() {
	return request({
		url: '/auth/me', // Or whatever endpoint we defined to get current user info.
		method: 'get',
	})
}
