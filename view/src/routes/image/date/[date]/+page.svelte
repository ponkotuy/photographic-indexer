<script lang="ts">
  import "carbon-components-svelte/css/g80.css";
  import MyHeader from "$lib/MyHeader.svelte";
  import {Column, Content, Grid, Row} from "carbon-components-svelte";
  import {host} from "$lib/global";
  import {thumbnail} from "$lib/image_type";
  import {DateTime} from "luxon"

  /** @type {import('./$types').PageLoad} */
  export let data;

  function hm(date: String): String {
    return DateTime.fromISO(date).toFormat('H:mm');
  }
</script>

<MyHeader />
<Content>
  <h1>{data.date}</h1>
  <Grid>
    <Row padding>
      {#each data.images as image}
        {@const path = thumbnail(image).path}
        <Column lg={4}>
          <figure>
            <img src="{host}/image{path}" style="aspect-ratio: 3 / 2; object-fit: contain; width: 100%;" title="{path}" alt="{path}">
            <figcaption>{hm(image.shootingAt)}</figcaption>
          </figure>
        </Column>
      {/each}
    </Row>
  </Grid>
</Content>
