<script lang="ts">
  import { Button, ButtonSet, TextInput } from 'carbon-components-svelte';
  import { host } from '$lib/global';

  let { imageId, note = '' }: { imageId: number; note?: string } = $props();

  function updateNote(id: number, noteText: string) {
    const body = JSON.stringify({ note: noteText });
    fetch(`${host()}/app/images/${id}/note`, { method: 'PUT', body });
  }
</script>

<ButtonSet>
  <TextInput inline labelText="Note" bind:value={note} size="sm" />
  <Button on:click={() => updateNote(imageId, note)} size="small">Update Note</Button>
</ButtonSet>
