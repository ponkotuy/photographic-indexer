<script lang="ts">
  import 'carbon-components-svelte/css/g80.css';
  import { OverflowMenu, OverflowMenuItem } from 'carbon-components-svelte';
  import ExifList from '$lib/ExifList.svelte';
  import { host } from '$lib/global';
  import { page } from '$app/state';

  export let data: ImageData;
  $: url = `${host()}/app/public/static/images/${data.id}`;
  $: link = `${page.url.origin}/public/image/${data.id}`;
</script>

<svelte:head>
  <title>Random Image</title>
  <meta name="description" content="Photographic Random Image" />
</svelte:head>

<div
  style={`background: #1b1b1b url("${url}") no-repeat fixed center; background-size: contain; min-height: 100vh`}
>
  <div style="background-color: transparent;">
    <div style="display: flex; justify-content: flex-end;">
      <OverflowMenu flipped>
        <OverflowMenuItem text="This image" href={link} />
        {#if data.flickr}
          <OverflowMenuItem text="Flickr" href={data.flickr.url} />
        {/if}
      </OverflowMenu>
    </div>
    <ExifList image={data} />
  </div>
</div>
