<script lang="ts">
  import 'carbon-components-svelte/css/g80.css';
  import '$lib/app.css'
  import MyHeader from "../../../lib/MyHeader.svelte";
  import {Column, Content, Grid, Link, Row} from "carbon-components-svelte";
  import type {CalendarPageResult} from "./+page";
  import {DateTime} from "luxon";
  import {host} from "$lib/global.js";
  import {thumbnail} from "$lib/image_type.js";

  export let data: CalendarPageResult;

  function parse(date: string): DateTime {
    return DateTime.fromISO(date);
  }

  function getYM(date: string): string {
    return parse(date).toFormat("LLLL, yyyy")
  }

  function getDay(date: string): number {
    return parse(date).day;
  }
</script>

<svelte:head>
  <title>Photographic Indexer -Calendar-</title>
</svelte:head>

<MyHeader />
<Content>
  {#if data.agg.length === 0}
    <h3>Not found images...</h3>
  {:else}
    <h2>{getYM(data.agg[0].date)}</h2>
    <Grid>
      <Row padding>
        {#each data.agg as day}
          {@const path = thumbnail(day.favoriteImage).path}
          <Column lg={4}>
            <Link href="/image/date/{day.date}">
              <h4>{getDay(day.date)} ({day.imageCount})</h4>
            </Link>
            <figure>
              <img
                src="{host()}/app/images/{day.favoriteImage.id}/thumbnail"
                class="fixed"
                style="width: 100%;"
                title={path}
                alt={path}
              />
            </figure>
          </Column>
        {/each}
      </Row>
    </Grid>
  {/if}
</Content>
