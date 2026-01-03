<script lang="ts">
  import 'carbon-components-svelte/css/g80.css';
  import MyHeader from '$lib/MyHeader.svelte';
  import {
    Content,
    Link,
    ListItem,
    StructuredList,
    StructuredListBody,
    StructuredListCell,
    StructuredListHead,
    StructuredListRow,
    Tile,
    UnorderedList
  } from 'carbon-components-svelte';
  import { host } from '$lib/global';
  import type { ImageData } from '$lib/image_type';
  import ImageTag from '$lib/ImageTag.svelte';
  import LoadImage from '$lib/LoadImage.svelte';
  import { DateTime } from 'luxon';
  import Exif from './Exif.svelte';
  import DeleteImage from './DeleteImage.svelte';
  import TogglePublic from '$lib/TogglePublic.svelte';
  import ReplaceButton from '$lib/ReplaceButton.svelte';
  import ImageNote from "$lib/ImageNote.svelte";

  let { data }: { data: ImageData } = $props();

  const extensions = ['jpg', 'jpeg', 'png', 'webp'];

  function isValidImage(path: string): boolean {
    const ext: string | undefined = path.split('.').pop()?.toLowerCase();
    if (!ext) return false;
    return extensions.includes(ext);
  }

  function isoDate(at: string): string {
    return DateTime.fromISO(at).toISODate()!;
  }

  function refreshImage() {
    data = data;
  }
</script>

<MyHeader />
<Content>
  <StructuredList condensed>
    <StructuredListHead>
      <StructuredListRow>
        <StructuredListCell />
        <StructuredListCell />
      </StructuredListRow>
    </StructuredListHead>
    <StructuredListBody>
      <StructuredListRow>
        <StructuredListCell head>Shooting At</StructuredListCell>
        <StructuredListCell>
          <Link href="/image/date/{isoDate(data.shootingAt)}">{data.shootingAt}</Link>
        </StructuredListCell>
      </StructuredListRow>
      {#if data.geo}
        <StructuredListRow>
          <StructuredListCell head>Address</StructuredListCell>
          <StructuredListCell>{data.geo.address}</StructuredListCell>
        </StructuredListRow>
      {/if}
      <StructuredListRow>
        <StructuredListCell head>Files</StructuredListCell>
        <StructuredListCell>
          <UnorderedList>
            {#each data.files as file}
              <ListItem>
                <Link href="{host()}/app/static{file.path}">{file.path}</Link>
                <ReplaceButton path={file.path} />
              </ListItem>
            {/each}
          </UnorderedList>
        </StructuredListCell>
      </StructuredListRow>
      <StructuredListRow>
        <StructuredListCell head>Tags</StructuredListCell>
        <StructuredListCell>
          <ImageTag image={data} refresh={refreshImage} />
        </StructuredListCell>
      </StructuredListRow>
      <Exif exif={data.exif} />
      <StructuredListRow>
        <StructuredListCell head>Operation</StructuredListCell>
        <StructuredListCell style="display: flex; flex-direction: column; gap: 6px;">
          <div>
            <TogglePublic imageId={data.id} state={data.isPublic} />
          </div>
          <ImageNote imageId={data.id} note={data.note} />
          <DeleteImage imageId={data.id} withText={true} />
        </StructuredListCell>
      </StructuredListRow>
    </StructuredListBody>
  </StructuredList>
  {#each data.files.filter((file) => isValidImage(file.path)) as file}
    <Tile style="margin: 16px 0;">
      <figure style="text-align: center;">
        <LoadImage
          src="{host()}/app/static{file.path}"
          style="max-width: 100%"
          title={file.path}
          alt={file.path}
        />
        <figcaption>{file.path}</figcaption>
      </figure>
    </Tile>
  {/each}
</Content>
