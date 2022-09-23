<script lang="ts">
  import "carbon-components-svelte/css/g80.css";
  import MyHeader from "$lib/MyHeader.svelte";
  import {
    Button,
    Content, Link, ListItem,
    StructuredList,
    StructuredListBody,
    StructuredListCell, StructuredListHead,
    StructuredListRow,
    Tile, UnorderedList
  } from "carbon-components-svelte";
  import {host} from "$lib/global.js";

  export let data;

  const exts = ["jpg", "jpeg", "png", "webp"];
  function isValidImage(path: String): Boolean {
    const ext = path.split('.').pop().toLowerCase();
    return exts.includes(ext);
  }
</script>

<MyHeader />
<Content>
  <StructuredList condensed>
    <StructuredListHead>
      <StructuredListRow>
        <StructuredListCell></StructuredListCell>
        <StructuredListCell></StructuredListCell>
      </StructuredListRow>
    </StructuredListHead>
    <StructuredListBody>
      <StructuredListRow>
        <StructuredListCell head>Shooting At</StructuredListCell>
        <StructuredListCell>{data.shootingAt}</StructuredListCell>
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
                <Link href="{host}/static{file.path}">{file.path}</Link>
              </ListItem>
            {/each}
          </UnorderedList>
        </StructuredListCell>
      </StructuredListRow>
    </StructuredListBody>
  </StructuredList>
  {#each data.files.filter(file => isValidImage(file.path)) as file}
    <Tile style="margin: 16px 0;">
      <figure style="text-align: center;">
        <img src="{host}/static{file.path}" style="max-width: 100%" title="{file.path}" alt="{file.path}">
        <figcaption>{file.path}</figcaption>
      </figure>
    </Tile>
  {/each}
</Content>
