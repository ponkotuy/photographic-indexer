<script lang="ts">
  import "carbon-components-svelte/css/g80.css";
  import "$lib/app.css";
  import { host } from "$lib/global";
  import MyHeader from "$lib/MyHeader.svelte";
  import {
    Button,
    Column,
    Content,
    Form,
    FormGroup,
    Grid, InlineNotification,
    Link,
    ListItem, Pagination,
    Row,
    Search,
    StructuredList,
    StructuredListBody,
    StructuredListCell,
    StructuredListHead,
    StructuredListRow,
    Tag, UnorderedList
  } from "carbon-components-svelte";
  import { onMount } from "svelte";
  import { goto } from "$app/navigation";
  import type { ImageData } from "$lib/image_type";
  import { thumbnail } from "$lib/image_type";
  import { DateTime } from "luxon";
  import { page as pp } from "$app/stores";
  import ImageTag from "$lib/ImageTag.svelte";
  import ImageNote from "$lib/ImageNote.svelte";
  import LoadImage from "$lib/LoadImage.svelte";
  import TogglePublic from "$lib/TogglePublic.svelte";

  type DateCount = {
    date: string;
    count: number;
  };

  export let keyword = "";
  export let images: ImageData[] = [];
  export let allCount = -1;
  export let dateCounts: DateCount[] = [];
  let page = 1;
  let pageSize = 20;

  onMount(() => {
    const params = $pp.url.searchParams;
    keyword = params.get("keyword") || "";
    if (keyword != "") search();
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
    if (coreParams.get("keyword") == "") coreParams.delete("keyword");
    fetch(host() + "/app/images/search?" + allParams)
      .then(res => res.json())
      .then(res => {
        images = res.data;
        allCount = res.allCount;
        goto(`/?${coreParams}`);
      });
    fetch(host() + "/app/images/search_date_count?" + coreParams)
      .then((res) => res.json())
      .then((res) => (dateCounts = res));
  }

  function searchClip() {
    const params = new URLSearchParams({
      keyword,
      page: (page - 1).toString(),
      perPage: pageSize.toString()
    });
    fetch(host() + "/app/images/search_clip?" + params)
      .then(res => res.json())
      .then(res => {
        images = res.data;
        allCount = res.allCount;
        dateCounts = res.dateCounts;
      });
  }

  function isoDate(at: string): string {
    return DateTime.fromISO(at).toISODate();
  }

  function disableSubmit(keyword: string): boolean {
    return keyword == "";
  }

  function updateImage() {
    images = images;
  }
</script>

<svelte:head>
  <title>Photographic Indexer</title>
  <meta name="description" content="Photographic Search Server" />
</svelte:head>

<MyHeader />
<Content>
  <Form
    on:submit={searchSubmit}
    disabled={disableSubmit(keyword)}
    style="margin-bottom: 24px;"
  >
    <FormGroup legendText="Search Keyword(Tab/Address/Note/Path)">
      <Search id="keyword" bind:value={keyword} />
    </FormGroup>
    <Button type="submit" disabled={disableSubmit(keyword)}>Search</Button>
    <Button type="button" kind="tertiary" disabled={disableSubmit(keyword)} on:click={searchClip}>SearchCLIP</Button>
  </Form>

  {#if allCount === 0}
    <InlineNotification kind="warning" title="Not found image" />
  {/if}

  {#if 0 < allCount}
    <h3>Date Result</h3>
    <Grid style="margin-bottom: 24px;">
      <Row>
        {#each dateCounts as dc}
          <Column>
            <Tag type="outline">
              <Link href="/image/date/{dc.date}">{dc.date}({dc.count})</Link>
            </Tag>
          </Column>
        {/each}
      </Row>
    </Grid>

    <h3>Image Result</h3>

    <Pagination
      totalItems={allCount}
      pageSizes={[20, 50]}
      bind:page
      bind:pageSize
      on:update={search}
    />

    <StructuredList condensed>
      <StructuredListHead>
        <StructuredListRow head>
          <StructuredListCell head>image</StructuredListCell>
          <StructuredListCell head>detail/files/tags</StructuredListCell>
        </StructuredListRow>
      </StructuredListHead>
      <StructuredListBody>
        {#each images as image}
          <StructuredListRow>
            <StructuredListCell style="vertical-align: bottom">
              <Link href="/image/{image.id}">
                <LoadImage
                  src="{host()}/app/images/{image.id}/thumbnail"
                  style="width: 320px"
                  alt={thumbnail(image).path}
                  class="fixed"
                />
              </Link>
            </StructuredListCell>
            <StructuredListCell>
              <UnorderedList>
                <ListItem>
                  <Link href="/image/date/{isoDate(image.shootingAt)}">{image.shootingAt}</Link>
                </ListItem>
                {#if image.geo}
                  <ListItem>{image.geo.address}</ListItem>
                {/if}
                <UnorderedList nested>
                  {#each image.files as file}
                    <ListItem>
                      <Link href="{host()}/app/static{file.path}">{file.path}</Link>
                    </ListItem>
                  {/each}
                </UnorderedList>
              </UnorderedList>
              <div class="space-form">
                <TogglePublic imageId={image.id} state={image.isPublic} />
              </div>
              <div class="space-form">
                <ImageTag image={image} refresh={updateImage} />
              </div>
              <div class="space-form">
                <ImageNote imageId={image.id} note={image.note || ''} />
              </div>
            </StructuredListCell>
          </StructuredListRow>
        {/each}
      </StructuredListBody>
    </StructuredList>

    <Pagination
      totalItems={allCount}
      pageSizeInputDisabled
      {pageSize}
      bind:page
      on:update={search}
    />
  {/if}
</Content>

<style>
    .space-form {
        margin-top: 4px;
    }
</style>
