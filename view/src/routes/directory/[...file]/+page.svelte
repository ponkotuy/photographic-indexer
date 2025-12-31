<script lang="ts">
  import 'carbon-components-svelte/css/g80.css';
  import MyHeader from '$lib/MyHeader.svelte';
  import {
    Breadcrumb,
    BreadcrumbItem,
    Content,
    Link,
    ListItem,
    UnorderedList
  } from 'carbon-components-svelte';
  import { FileType } from '$lib/file_element';
  import type { DirectoryPageResult } from './+page';
  import * as pathUtil from 'path-browserify';

  let { data }: { data: DirectoryPageResult } = $props();

  let filePath = $derived(data.file);
  let fileList = $derived(data.fileList);
  let paths = $derived(('/' + data.file).split('/'));
</script>

<MyHeader />
<Content>
  <Breadcrumb noTrailingSlash>
    {#each paths as path, index}
      <BreadcrumbItem href="/directory/{paths.slice(0, index).join('/')}">
        {#if index === 0}
          Top
        {:else}
          {path}
        {/if}
      </BreadcrumbItem>
    {/each}
  </Breadcrumb>
  <UnorderedList>
    {#each fileList as file}
      <ListItem>
        {#if file.fileType === FileType.Directory}
          <Link href="/directory/{pathUtil.join(filePath, file.name)}">{file.name}/</Link>
        {:else if file.imageId}
          <Link href="/image/{file.imageId}">{file.name}</Link>
        {:else}
          {file.name}
        {/if}
      </ListItem>
    {/each}
  </UnorderedList>
</Content>
