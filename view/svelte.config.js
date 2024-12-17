import adapter from '@sveltejs/adapter-node';
import sveltePreprocess from 'svelte-preprocess';
import {optimizeImports} from 'carbon-preprocess-svelte';

/** @type {import('@sveltejs/kit').Config} */
const config = {
  preprocess: [sveltePreprocess(), optimizeImports()],

  kit: {
    adapter: adapter({out: 'build'})
  }
};

export default config;
