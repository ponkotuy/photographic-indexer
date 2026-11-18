import { defineEnvVars } from '@sveltejs/kit/env';

export const variables = defineEnvVars({
  DEV_BACKEND_ORIGIN: {
    // Set by docker-compose.dev.yml; unset everywhere else.
    schema: (value) => value,
    description: 'Backend origin reachable from the dev view container'
  }
});
