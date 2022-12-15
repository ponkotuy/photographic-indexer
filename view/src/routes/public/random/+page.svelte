<script lang="ts">
  import type { ImageData } from '$lib/image_type';
  import { host } from '$lib/global';

  export let data: ImageData;

  function url() {
    return `${host()}/public/static/images/${data.id}`;
  }
</script>

<svelte:head>
</svelte:head>

<html style="background: url({url()}) no-repeat center fixed; background-size: contain; background-color: #1b1b1b;" >
<head>
  <title>Random Image</title>
  <meta name="description" content="Photographic Random Image" />
</head>
</html>
