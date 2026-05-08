<template>
	<div class="confirm-mask" v-if="visible" @click.self="handleCancel">
		<div class="confirm-modal" role="dialog" aria-modal="true">
			<div class="confirm-header">
				<div class="confirm-title">{{ title }}</div>
				<button class="confirm-close" type="button" @click="handleCancel">×</button>
			</div>
			<div class="confirm-body">
				<div class="confirm-message">{{ message }}</div>
			</div>
			<div class="confirm-footer">
				<button type="button" class="btn btn-default" @click="handleCancel">{{ cancelText }}</button>
				<button type="button" class="btn" :class="confirmButtonClass" @click="handleConfirm">{{ confirmText }}</button>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const props = withDefaults(
	defineProps<{
		title?: string
		message: string
		confirmText?: string
		cancelText?: string
		type?: 'default' | 'danger'
		onClose?: (confirmed: boolean) => void
	}>(),
	{
		title: '确认操作',
		confirmText: '确定',
		cancelText: '取消',
		type: 'default',
	},
)

const visible = ref(true)

const confirmButtonClass = computed(() => {
	return props.type === 'danger' ? 'btn-danger' : 'btn-primary'
})

const close = (confirmed: boolean) => {
	visible.value = false
	props.onClose?.(confirmed)
}

const handleCancel = () => close(false)
const handleConfirm = () => close(true)
</script>

<style scoped>
.confirm-mask {
	position: fixed;
	inset: 0;
	background: rgba(0, 0, 0, 0.4);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 9999;
}

.confirm-modal {
	width: 460px;
	max-width: 92vw;
	background: #fff;
	border-radius: 8px;
	box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
	overflow: hidden;
}

.confirm-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 12px 14px;
	border-bottom: 1px solid #ebeef5;
}

.confirm-title {
	font-weight: 600;
	color: #303133;
}

.confirm-close {
	border: none;
	background: transparent;
	cursor: pointer;
	font-size: 20px;
	line-height: 20px;
	color: #909399;
}

.confirm-body {
	padding: 16px 14px;
}

.confirm-message {
	color: #606266;
	line-height: 1.6;
	white-space: pre-wrap;
}

.confirm-footer {
	display: flex;
	justify-content: flex-end;
	gap: 10px;
	padding: 12px 14px;
	border-top: 1px solid #ebeef5;
}

.btn {
	height: 34px;
	padding: 0 14px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
}

.btn-default {
	background: #f4f4f5;
	color: #606266;
}

.btn-primary {
	background: #409eff;
	color: #fff;
}

.btn-danger {
	background: #f56c6c;
	color: #fff;
}
</style>

