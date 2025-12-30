import { dev } from '$app/environment';

export function host(): string {
  if (dev) return 'http://localhost:8080';
  return '';
}
