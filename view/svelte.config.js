import adapter from '@sveltejs/adapter-static';
import preprocess from 'svelte-preprocess';
import { optimizeImports, elements } from "carbon-preprocess-svelte";

/** @type {import('@sveltejs/kit').Config} */
const config = {
	// Consult https://github.com/sveltejs/svelte-preprocess
	// for more information about preprocessors
	preprocess: [preprocess(), optimizeImports(), elements()],

	kit: {
	  adapter: adapter()
	}
};

export default config;
