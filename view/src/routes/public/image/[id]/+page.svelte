<script lang="ts">
  import 'carbon-components-svelte/css/g80.css'
  import { host } from '$lib/global'
  import { CopyButton } from 'carbon-components-svelte'
  import { page } from '$app/stores'
  import ExifList from '$lib/ExifList.svelte'

  export let data: ImageData
  $: url = `${host()}/app/public/static/images/${data.id}`
</script>

<svelte:head>
</svelte:head>

<html lang="en"
      style={`background: url("${url}") no-repeat center fixed; background-color: #1b1b1b; background-size: contain;`}>
<head>
  <title>images id={data.id}</title>
  <meta name="description" content="Photographic Image"/>
</head>

<body style="background-color: transparent;">

<div style="display: flex; justify-content: flex-end;">
  <CopyButton text={$page.url.href}/>
</div>
<ExifList image={data}/>

</body>

</html>
