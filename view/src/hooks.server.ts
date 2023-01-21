import { dev } from '$app/environment';
import type { HandleFetch } from '@sveltejs/kit';

export const handleFetch = (({ request, fetch }) => {
	if (!dev) {
		const url = new URL(request.url);
		url.protocol = 'http';
		url.port = '8080';
		url.hostname = 'web';
		request = new Request(url.href, request);
	}
	return fetch(request);
}) satisfies HandleFetch;
