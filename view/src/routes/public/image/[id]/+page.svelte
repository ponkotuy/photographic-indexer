<script lang="ts">
  import { host } from "$lib/global";

  export let data;
  $: url = `${host()}/app/public/static/images/${data.id}`;

</script>

<svelte:head>
</svelte:head>

<html lang="en" style={`background: url("${url}") no-repeat center fixed; background-color: #1b1b1b; background-size: contain;`}>
<head>
  <title>images id={data.id}</title>
  <meta name="description" content="Photographic Image" />
</head>
</html>
