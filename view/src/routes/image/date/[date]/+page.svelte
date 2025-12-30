<script lang="ts">
  import 'carbon-components-svelte/css/g80.css';
  import '$lib/app.css';
  import MyHeader from '$lib/MyHeader.svelte';
  import { Button, Column, Content, Dropdown, Grid, Link, Row } from 'carbon-components-svelte';
  import { CaretLeft, CaretRight } from 'carbon-icons-svelte';
  import { host } from '$lib/global';
  import { thumbnail, type ImageData } from '$lib/image_type';
  import { DateTime } from 'luxon';
  import type { DatePageResult } from './+page';
  import LoadImage from '$lib/LoadImage.svelte';
  import PagingGrid from './PagingGrid.svelte';

  export let data: DatePageResult;
  export let page = data.page;
  export let count = data.count;
  let tags = [
    { id: '-1', text: 'All' },
    ...data.tags.map((tag) => {
      return { id: tag.id.toString(), text: tag.name };
    })
  ];
  let isPublic = '0';
  let selectTag = '-1';

  $: images = data.images.filter(imageFilter).slice((page - 1) * count, page * count);

  $: imageValidCount = data.images.filter(imageFilter).length;

  $: imageFilter = (img: ImageData) => {
    return (
      (isPublic == '0' || img.isPublic) &&
      (selectTag == '-1' || img.tags.map((t) => t.id.toString()).includes(selectTag))
    );
  };

  function yesterday(date: string) {
    return DateTime.fromISO(date).minus({ days: 1 }).toISODate()!;
  }

  function tomorrow(date: string) {
    return DateTime.fromISO(date).plus({ days: 1 }).toISODate()!;
  }

  function hm(date: string): string {
    return DateTime.fromISO(date).toFormat('H:mm');
  }
</script>

<MyHeader />
<Content>
  <Grid narrow>
    <Row padding>
      <Column lg={2}>
        <Button href="/image/date/{yesterday(data.date)}" kind="ghost">
          <CaretLeft size={24} />
          Yesterday
        </Button>
      </Column>
      <Column lg={12} style="text-align: center;"><h2>{data.date}({data.images.length})</h2></Column
      >
      <Column lg={2}>
        <Button href="/image/date/{tomorrow(data.date)}" kind="ghost">
          Tomorrow
          <CaretRight size={24} />
        </Button>
      </Column>
    </Row>
  </Grid>

  <Grid>
    <Dropdown
      type="inline"
      {...{ titleText: 'public' }}
      bind:selectedId={isPublic}
      items={[
        { id: '0', text: 'All' },
        { id: '1', text: 'Public Only' }
      ]}
    />
    <Dropdown type="inline" {...{ titleText: 'tag' }} bind:selectedId={selectTag} items={tags} />
  </Grid>

  {#if imageValidCount > 20}
    <PagingGrid totalItems={imageValidCount} bind:page bind:pageSize={count}></PagingGrid>
  {/if}

  <Grid>
    <Row padding>
      {#each images as image}
        <Column lg={4}>
          <Link href="/image/{image.id}">
            <figure>
              <LoadImage
                src="{host()}/app/images/{image.id}/thumbnail"
                class="fixed"
                style="width: 100%;"
                title={thumbnail(image).path}
                alt={thumbnail(image).path}
              />
              <figcaption>{hm(image.shootingAt)}</figcaption>
            </figure>
          </Link>
        </Column>
      {/each}
    </Row>
  </Grid>

  {#if imageValidCount > 20}
    <PagingGrid totalItems={imageValidCount} bind:page bind:pageSize={count}></PagingGrid>
  {/if}
</Content>
