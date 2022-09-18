import App from './App.svelte';

const app = new App({
	target: document.body,
	props: {
		query: "",
		images: [],
		allCount: 0
	}
});

export default app;
