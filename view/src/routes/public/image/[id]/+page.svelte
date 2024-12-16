<script lang="ts">
  import { host } from "$lib/global";
  import { onMount } from "svelte";

  export let data;

  onMount(() => {
    const url = `${host()}/app/public/static/images/${data.id}`;
    const html = document.getElementsByTagName("html").item(0);
    html.style.background = `url(${url}) no-repeat center fixed`;
    html.style.backgroundSize = "contain";
    html.style.backgroundColor = "#1b1b1b";
  });
</script>

<svelte:head>
</svelte:head>

<html lang="en">
<head>
  <title>images id={data.id}</title>
  <meta name="description" content="Photographic Image" />
</head>
</html>
