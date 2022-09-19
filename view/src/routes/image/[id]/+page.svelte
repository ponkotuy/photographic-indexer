<script lang="ts">
  import "carbon-components-svelte/css/g80.css";
  import MyHeader from "$lib/MyHeader.svelte";
  import {Grid} from "carbon-components-svelte";

  export let data;
</script>

<Grid>
  <MyHeader />
  <p>{data.id}</p>
</Grid>
