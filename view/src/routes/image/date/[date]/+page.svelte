<script lang="ts">
  import "carbon-components-svelte/css/g80.css";
  import MyHeader from "$lib/MyHeader.svelte";
  import {Button, Column, Content, Grid, Link, Row, Tile, TooltipIcon} from "carbon-components-svelte";
  import {CaretLeft, CaretRight} from "carbon-icons-svelte"
  import {host} from "$lib/global";
  import {thumbnail} from "$lib/image_type";
  import {DateTime} from "luxon";

  export let data;
  let datetime = DateTime.fromISO(data.date);

  function hm(date: String): String {
    return DateTime.fromISO(date).toFormat('H:mm');
  }
</script>

<MyHeader />
<Content>
  <Grid narrow>
    <Row>
      <Column lg={2}>
        <Button href="/image/date/{datetime.minus({days: 1}).toISODate()}" kind="ghost">
          <CaretLeft size={24} />Yesterday
        </Button>
      </Column>
      <Column lg={12} style="text-align: center"><h2>{data.date}({data.images.length})</h2></Column>
      <Column lg={2}>
        <Button href="/image/date/{datetime.plus({days: 1}).toISODate()}" kind="ghost">
          Tomorrow<CaretRight size={24} />
        </Button>
      </Column>
    </Row>
  </Grid>
  <Grid>
    <Row padding>
      {#each data.images as image}
        {@const path = thumbnail(image).path}
        <Column lg={4}>
          <Link href="/image/{image.id}">
            <figure>
              <img src="{host}/app/images/{image.id}/thumbnail" class="fixed" style="width: 100%;" title="{path}" alt="{path}">
              <figcaption>{hm(image.shootingAt)}</figcaption>
            </figure>
          </Link>
        </Column>
      {/each}
    </Row>
  </Grid>
</Content>

<style>
  .fixed {
      aspect-ratio: 3 / 2;
      object-fit: contain;
  }
</style>
