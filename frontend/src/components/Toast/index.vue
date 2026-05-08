<template>
	<transition name="toast-fade">
		<div v-if="visible" class="toast-container" :class="type" :style="{ top: top + 'px' }">
			<span class="icon">{{ iconMap[type] }}</span>
			<span class="message">{{ message }}</span>
		</div>
	</transition>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const props = defineProps({
	message: String,
	type: {
		type: String,
		default: 'info', // info, success, warning, error
	},
	duration: {
		type: Number,
		default: 3000,
	},
	top: {
		type: Number,
		default: 20,
	},
})

const visible = ref(false)

const iconMap: any = {
	info: 'ℹ️',
	success: '✅',
	warning: '⚠️',
	error: '❌',
}

onMounted(() => {
	visible.value = true
	setTimeout(() => {
		visible.value = false
		// Remove element logic handled by manager or v-if cleanup if managed by App
	}, props.duration)
})
</script>

<style scoped>
.toast-container {
	position: fixed;
	left: 50%;
	transform: translateX(-50%);
	padding: 10px 20px;
	border-radius: 4px;
	background: #fff;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
	display: flex;
	align-items: center;
	gap: 10px;
	z-index: 9999;
	min-width: 300px;
	border: 1px solid #ebeef5;
}

.success {
	border-left: 5px solid #67c23a;
}
.warning {
	border-left: 5px solid #e6a23c;
}
.error {
	border-left: 5px solid #f56c6c;
}
.info {
	border-left: 5px solid #909399;
}

.message {
	font-size: 14px;
	color: #606266;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
	transition: all 0.3s ease;
}
.toast-fade-enter-from,
.toast-fade-leave-to {
	opacity: 0;
	transform: translate(-50%, -20px);
}
</style>
