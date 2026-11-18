import { dev } from '$app/env';

export function host(): string {
  if (dev) return 'http://localhost:8080';
  return '';
}
