import { dev } from '$app/env';
import { DEV_BACKEND_ORIGIN } from '$app/env/private';
import { host } from '#lib/global.ts';
import type { HandleFetch } from '@sveltejs/kit/hooks';

export const handleFetch = (({ request, fetch }) => {
  if (!dev) {
    const url = new URL(request.url);
    url.protocol = 'http';
    url.port = '8080';
    url.hostname = 'web';
    request = new Request(url.href, request);
  } else if (DEV_BACKEND_ORIGIN && request.url.startsWith(host())) {
    // Set by docker-compose.dev.yml: inside the view container, localhost:8080 is not the backend.
    request = new Request(DEV_BACKEND_ORIGIN + request.url.slice(host().length), request);
  }
  return fetch(request);
}) satisfies HandleFetch;
