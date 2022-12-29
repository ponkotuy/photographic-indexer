<script lang="ts">
  import { ImageLoader, Loading } from "carbon-components-svelte";

  export let src: string;
</script>

<ImageLoader {src} {...$$restProps}>
  <svelte:fragment slot="loading">
    <Loading withOverlay={false} />
  </svelte:fragment>
  <svelte:fragment slot="error">An error occurred.</svelte:fragment>
</ImageLoader>
