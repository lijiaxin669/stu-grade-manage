import { createVNode, render } from 'vue'
import ToastComponent from '@/components/Toast/index.vue'

const container = document.createElement('div')
container.id = 'toast-container'
document.body.appendChild(container)

let seed = 1

interface ToastOptions {
	message: string
	type?: 'success' | 'warning' | 'error' | 'info'
	duration?: number
}

const Toast = (options: ToastOptions | string) => {
	const opts = typeof options === 'string' ? { message: options } : options

	// Create wrapper
	const wrapper = document.createElement('div')
	container.appendChild(wrapper)

	// Calculate top offset if we want stacking (simplified to overlapping or fixed top for now)
	// For simplicity, let's just stack them slightly or keep simplified.
	// Enhanced: Stack them?
	// Let's keep it simple: Fixed top center. New one covers old one or we implement simple queue?
	// Let's implement simple removal.

	const vm = createVNode(ToastComponent, {
		...opts,
		top: 20 + (seed % 5) * 60, // Simple staggering
		onDistroy: () => {
			render(null, wrapper)
			container.removeChild(wrapper)
		},
	})

	render(vm, wrapper)

	// Cleanup is handled by component timer setting visible=false, but we need to destroy DOM.
	setTimeout(
		() => {
			render(null, wrapper)
			if (container.contains(wrapper)) {
				container.removeChild(wrapper)
			}
		},
		(opts.duration || 3000) + 500,
	)

	seed++
}

Toast.success = (msg: string) => Toast({ message: msg, type: 'success' })
Toast.warning = (msg: string) => Toast({ message: msg, type: 'warning' })
Toast.error = (msg: string) => Toast({ message: msg, type: 'error' })
Toast.info = (msg: string) => Toast({ message: msg, type: 'info' })

export default Toast
