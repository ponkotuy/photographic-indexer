<script lang="ts">
  import 'carbon-components-svelte/css/g80.css';
  import { host } from '$lib/global';
  import ExifList from '$lib/ExifList.svelte';
  import { OverflowMenu, OverflowMenuItem } from 'carbon-components-svelte';

  export let data: ImageData;
  $: url = `${host()}/app/public/static/images/${data.id}`;
</script>

<svelte:head>
  <title>images id={data.id}</title>
  <meta name="description" content="Photographic Image" />
</svelte:head>

<div
  style={`background: #1b1b1b url("${url}") no-repeat fixed center; background-size: contain; min-height: 100vh`}
>
  <div style="background-color: transparent;">
    {#if data.flickr}
      <div style="display: flex; justify-content: flex-end;">
        <OverflowMenu flipped>
          <OverflowMenuItem text="Flickr" href={data.flickr.url} />
        </OverflowMenu>
      </div>
    {/if}
    <ExifList image={data} />
  </div>
</div>
