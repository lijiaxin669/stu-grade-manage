<template>
	<div class="error-page">
		<h1>404 Not Found</h1>
		<p>The page you are looking for does not exist.</p>
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
	color: #faad14;
}
</style>
