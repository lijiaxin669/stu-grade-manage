<template>
	<div class="error-page">
		<h1>403 Forbidden</h1>
		<p>You do not have permission to access this page.</p>
		<router-link to="/">Go Home</router-link>
	</div>
</template>

<style scoped>
.error-page {
	text-align: center;
	margin-top: 100px;
}
h1 {
	font-size: 48px;
	color: #f5222d;
}
</style>
