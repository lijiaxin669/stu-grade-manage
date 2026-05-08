import { createVNode, render } from 'vue'
import ConfirmModal from '@/components/ConfirmModal/index.vue'

export interface ConfirmOptions {
	title?: string
	message: string
	confirmText?: string
	cancelText?: string
	type?: 'default' | 'danger'
}

export function confirm(options: ConfirmOptions | string): Promise<boolean> {
	const opts: ConfirmOptions = typeof options === 'string' ? { message: options } : options

	const container = document.createElement('div')
	document.body.appendChild(container)

	return new Promise((resolve) => {
		const cleanup = () => {
			render(null, container)
			if (document.body.contains(container)) document.body.removeChild(container)
		}

		const vm = createVNode(ConfirmModal, {
			...opts,
			onClose: (confirmed: boolean) => {
				cleanup()
				resolve(confirmed)
			},
		})

		render(vm, container)
	})
}

