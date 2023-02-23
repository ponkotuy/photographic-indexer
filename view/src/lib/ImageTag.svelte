<script lang="ts" context="module">
  import type { Tag as TagType } from "$lib/image_type";
  import { host } from "$lib/global";

  export let loading: boolean = false;
  export let tags: TagType[] = [];
</script>
<script lang="ts">
  import { Button, Modal, OverflowMenu, OverflowMenuItem, Tag, TextInput } from "carbon-components-svelte";
  import { Add } from "carbon-icons-svelte";
  import type { ImageData } from "$lib/image_type";
  import { onMount } from "svelte";

  export let image: ImageData;
  export let refresh;
  export let open = false;
  export let tagName = '';

  onMount(() => {
    if (loading === false) {
      loading = true;
      refreshTags().then(xs => tags = xs);
    }
  })

  async function addTag(name: string) {
    const body = JSON.stringify({name});
    open = false;
    await fetch(host() + '/app/images/tags', {method: 'PUT', body})
    tags = await refreshTags();
  }

  async function setTag(image: ImageData, tag: TagType, finalize: () => void = function () {}) {
    const result = await fetch(host() + `/app/images/${image.id}/tag/${tag.id}`, { method: 'PUT' });
    if (result.ok) {
      image.tags.push(tag);
      finalize();
    }
  }

  async function removeTag(image: ImageData, tag: TagType, finalize: () => void = function () {}) {
    const result = await fetch(host() + `/app/images/${image.id}/tag/${tag.id}`, {
      method: 'DELETE'
    });
    if (result.ok) {
      image.tags = image.tags.filter((t) => t.id !== tag.id);
      finalize();
    }
  }

  async function refreshTags(): Promise<TagType[]> {
    return fetch(host() + '/app/images/tags').then((res) => res.json());
  }
</script>

{#each image.tags as tag}
  <Tag filter on:close={() => removeTag(image, tag, refresh)} style="vertical-align: bottom;">{tag.name}</Tag>
{/each}
<OverflowMenu style="width: auto; height: auto; display: inline;">
  <Button slot="menu" icon={Add} size="small">Tag</Button>
  {#each tags as tag}
    {#if !image.tags.map(t => t.id).includes(tag.id)}
      <OverflowMenuItem text={tag.name} on:click={() => setTag(image, tag, refresh)} />
    {/if}
  {/each}
  <OverflowMenuItem hasDivider text="+ New tag" on:click={() => open = true} />
</OverflowMenu>
<Modal
  bind:open
  modalHeading="Create new tag"
  primaryButtonText="Add tag"
  secondaryButtonText="Cancel"
  on:click:button--secondary={() => open = false}
  on:submit={() => addTag(tagName)}
>
  <p>Add a new tag in Photographic Indexer.</p>
  <TextInput id="tagName" labelText="Tag name" bind:value={tagName} />
</Modal>
