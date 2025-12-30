<script lang="ts">
  import 'carbon-components-svelte/css/g80.css';
  import '$lib/app.css';
  import MyHeader from '$lib/MyHeader.svelte';
  import { Button, Column, Content, Grid, Link, Row } from 'carbon-components-svelte';
  import { DateTime } from 'luxon';
  import { host } from '$lib/global.js';
  import { thumbnail } from '$lib/image_type.js';
  import { CaretLeft, CaretRight } from 'carbon-icons-svelte';
  import LoadImage from '$lib/LoadImage.svelte';
  import MonthsMenu from './MonthsMenu.svelte';
  import type { CalendarPageResult } from './+page';

  export let data: CalendarPageResult;
  $: agg = data.agg;
  $: month = data.month;
  $: monthDate = DateTime.fromISO(month);

  const YMUser = 'LLLL, yyyy';
  const YMMachine = 'yyyyMM';

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
  <Grid narrow>
    <Row style="align-items: center">
      <Column lg={4}>
        <Button href="/calendar/{monthDate.minus({ months: 1 }).toFormat(YMMachine)}" kind="ghost">
          <CaretLeft size={24} />{monthDate.minus({ months: 1 }).toFormat(YMUser)}
        </Button>
      </Column>
      <Column lg={8} style="text-align: center;">
        <h2>{monthDate.toFormat(YMUser)}</h2>
      </Column>
      <Column lg={1}>
        <MonthsMenu now={monthDate.toFormat(YMMachine)} style="padding: 11px 16px;" />
      </Column>
      <Column lg={3}>
        <Button href="/calendar/{monthDate.plus({ months: 1 }).toFormat(YMMachine)}" kind="ghost">
          {monthDate.plus({ months: 1 }).toFormat(YMUser)}
          <CaretRight size={24} />
        </Button>
      </Column>
    </Row>
  </Grid>
  {#if agg.length === 0}
    <h3>Not found images...</h3>
  {:else}
    <Grid>
      <Row padding>
        {#each agg as day}
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
