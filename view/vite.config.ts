import adapter from '@sveltejs/adapter-node';
import { sveltekit } from '@sveltejs/kit/vite';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';
import { optimizeImports } from 'carbon-preprocess-svelte';
import type { UserConfig } from 'vite';

const config: UserConfig = {
  plugins: [
    sveltekit({
      preprocess: [vitePreprocess(), optimizeImports()],
      adapter: adapter({ out: 'build' })
    })
  ]
};

export default config;
