import { dev } from '$app/environment';
import { env } from '$env/dynamic/private';
import { host } from '$lib/global';
import type { HandleFetch } from '@sveltejs/kit';

export const handleFetch = (({ request, fetch }) => {
  if (!dev) {
    const url = new URL(request.url);
    url.protocol = 'http';
    url.port = '8080';
    url.hostname = 'web';
    request = new Request(url.href, request);
  } else if (env.DEV_BACKEND_ORIGIN && request.url.startsWith(host())) {
    // Set by docker-compose.dev.yml: inside the view container, localhost:8080 is not the backend.
    request = new Request(env.DEV_BACKEND_ORIGIN + request.url.slice(host().length), request);
  }
  return fetch(request);
}) satisfies HandleFetch;
