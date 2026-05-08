export function formatSemester(semester?: string) {
	if (!semester) return ''
	return semester
		.replace(/-Spring\b/g, '-春季')
		.replace(/-Summer\b/g, '-夏季')
		.replace(/-Fall\b/g, '-秋季')
		.replace(/-Winter\b/g, '-冬季')
}

