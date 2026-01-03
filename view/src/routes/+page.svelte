<script lang="ts">
  import 'carbon-components-svelte/css/g80.css';
  import '$lib/app.css';
  import { host } from '$lib/global';
  import MyHeader from '$lib/MyHeader.svelte';
  import {
    Button,
    Content,
    Form,
    FormGroup,
    InlineNotification,
    Link,
    Pagination,
    Search,
    Tag
  } from 'carbon-components-svelte';
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import type { ImageData } from '$lib/image_type';
  import { thumbnail } from '$lib/image_type';
  import { DateTime } from 'luxon';
  import { page as pageState } from '$app/state';
  import LoadImage from '$lib/LoadImage.svelte';

  type DateCount = {
    date: string;
    count: number;
  };

  let keyword = $state('');
  let images = $state<ImageData[]>([]);
  let allCount = $state(-1);
  let dateCounts = $state<DateCount[]>([]);
  let page = $state(1);
  let pageSize = $state(20);

  onMount(() => {
    const params = pageState.url.searchParams;
    keyword = params.get('keyword') || '';
    if (keyword != '') search();
  });

  function searchSubmit(e: SubmitEvent) {
    e.preventDefault();
    search();
  }

  function search() {
    const allParams = new URLSearchParams({
      keyword,
      page: (page - 1).toString(),
      perPage: pageSize.toString()
    });
    const coreParams = new URLSearchParams({ keyword });
    if (coreParams.get('keyword') == '') coreParams.delete('keyword');
    fetch(host() + '/app/images/search?' + allParams)
      .then((res) => res.json())
      .then((res) => {
        images = res.data;
        allCount = res.allCount;
        goto(`/?${coreParams}`);
      });
    fetch(host() + '/app/images/search_date_count?' + coreParams)
      .then((res) => res.json())
      .then((res) => (dateCounts = res));
  }

  function searchClip() {
    const params = new URLSearchParams({
      keyword,
      page: (page - 1).toString(),
      perPage: pageSize.toString()
    });
    fetch(host() + '/app/images/search_clip?' + params)
      .then((res) => res.json())
      .then((res) => {
        images = res.data;
        allCount = res.allCount;
        dateCounts = res.dateCounts;
      });
  }
  function disableSubmit(kw: string): boolean {
    return kw == '';
  }
</script>

<svelte:head>
  <title>Photographic Indexer</title>
  <meta name="description" content="Photographic Search Server" />
</svelte:head>

<MyHeader />
<Content>
  <Form on:submit={searchSubmit} style="margin-bottom: 24px;">
    <FormGroup legendText="Search Keyword(Tab/Address/Note/Path)">
      <Search id="keyword" bind:value={keyword} />
    </FormGroup>
    <Button type="submit" disabled={disableSubmit(keyword)}>Search</Button>
    <Button type="button" kind="tertiary" disabled={disableSubmit(keyword)} on:click={searchClip}
      >SearchCLIP</Button
    >
  </Form>

  {#if allCount === 0}
    <InlineNotification kind="warning" title="Not found image" />
  {/if}

  {#if 0 < allCount}
    <div class="date-tags">
      {#each dateCounts as dc}
        <Tag type="outline">
          <Link href="/image/date/{dc.date}">{dc.date}({dc.count})</Link>
        </Tag>
      {/each}
    </div>

    <Pagination
      totalItems={allCount}
      pageSizes={[20, 50]}
      bind:page
      bind:pageSize
      on:change={search}
    />

    <div class="image-grid">
      {#each images as image}
        <a href="/image/{image.id}" class="image-card">
          <LoadImage
            src="{host()}/app/images/{image.id}/thumbnail"
            alt={thumbnail(image).path}
            class="grid-image"
          />
          <div class="image-overlay">
            {#if image.geo}
              <div class="overlay-address">{image.geo.address}</div>
            {/if}
            {#if image.note}
              <div class="overlay-note">{image.note}</div>
            {/if}
            {#if image.tags.length > 0}
              <div class="overlay-tags">
                {#each image.tags as tag}
                  <span class="overlay-tag">{tag.name}</span>
                {/each}
              </div>
            {/if}
          </div>
        </a>
      {/each}
    </div>
  {/if}
</Content>

<style>
  .date-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    margin-bottom: 16px;
  }

  .image-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 8px;
    margin: 16px 0;
  }

  .image-card {
    position: relative;
    display: block;
    aspect-ratio: 3 / 2;
    overflow: hidden;
    background: #262626;
  }

  .image-card :global(.grid-image) {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }

  .image-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.8);
    color: #fff;
    padding: 8px;
    font-size: 12px;
    opacity: 0;
    transition: opacity 0.2s;
    max-height: 100%;
    overflow-y: auto;
  }

  .image-card:hover .image-overlay {
    opacity: 1;
  }

  .overlay-address {
    margin-bottom: 4px;
    font-weight: 500;
  }

  .overlay-note {
    margin-bottom: 4px;
    color: #a8a8a8;
  }

  .overlay-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }

  .overlay-tag {
    background: #393939;
    padding: 2px 6px;
    border-radius: 2px;
  }
</style>
