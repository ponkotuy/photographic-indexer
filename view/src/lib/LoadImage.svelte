<script lang="ts">
  import { ImageLoader, Loading } from 'carbon-components-svelte';

  let { src, ...rest }: { src: string; [key: string]: unknown } = $props();
</script>

<ImageLoader {src} {...rest}>
  <svelte:fragment slot="loading">
    <Loading withOverlay={false} />
  </svelte:fragment>
  <svelte:fragment slot="error">An error occurred.</svelte:fragment>
</ImageLoader>
