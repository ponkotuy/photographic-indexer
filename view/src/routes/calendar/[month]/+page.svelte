<script lang="ts">
  import 'carbon-components-svelte/css/g80.css';
  import '$lib/app.css'
  import MyHeader from "../../../lib/MyHeader.svelte";
  import {Button, Column, Content, Grid, Link, Row} from "carbon-components-svelte";
  import type {CalendarPageResult} from "./+page";
  import {DateTime} from "luxon";
  import {host} from "$lib/global.js";
  import {thumbnail} from "$lib/image_type.js";
  import {CaretLeft, CaretRight} from "carbon-icons-svelte";
  import LoadImage from "$lib/LoadImage.svelte";

  export let data: CalendarPageResult;

  const YMUser = "LLLL, yyyy";
  const YMMachine = "yyyyMM";

  function parse(date: string): DateTime {
    return DateTime.fromISO(date);
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
    {@const datetime = parse(data.agg[0].date)}
    <Grid narrow>
      <Row>
        <Column lg={3}>
          <Button href="/calendar/{datetime.minus({ months: 1}).toFormat(YMMachine)}" kind="ghost">
            <CaretLeft size={24} />{datetime.minus({ months: 1}).toFormat(YMUser)}
          </Button>
        </Column>
        <Column lg={10} style="text-align: center;">
          <h2>{datetime.toFormat(YMUser)}</h2>
        </Column>
        <Column lg={3}>
          <Button href="/image/calendar/{datetime.plus({ months: 1}).toFormat(YMMachine)}" kind="ghost">
            {datetime.plus({ months: 1}).toFormat(YMUser)}<CaretRight size={24} />
          </Button>
        </Column>
      </Row>
    </Grid>
    <Grid>
      <Row padding>
        {#each data.agg as day}
          {@const path = thumbnail(day.favoriteImage).path}
          <Column lg={4}>
            <Link href="/image/date/{day.date}">
              <h4>{getDay(day.date)} ({day.imageCount})</h4>
            </Link>
            <figure>
              <LoadImage
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
