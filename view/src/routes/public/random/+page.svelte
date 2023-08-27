<script lang="ts">
  import { host } from "$lib/global";
  import { onMount } from "svelte";

  onMount(() => {
    fetch(`${host()}/app/public/images/random`)
      .then(res => res.json())
      .then(res => {
        const url = `${host()}/app/public/static/images/${res.id}`;
        const html = document.getElementsByTagName("html").item(0);
        html.style.background = `url(${url}) no-repeat center fixed`;
        html.style.backgroundSize = "contain";
        html.style.backgroundColor = "#1b1b1b";
      });
  });
</script>

<svelte:head>
</svelte:head>

<html lang="en">
<head>
  <title>Random Image</title>
  <meta name="description" content="Photographic Random Image" />
</head>
</html>
