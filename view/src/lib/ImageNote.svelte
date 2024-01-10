<script lang="ts">
  import { Button, ButtonSet, Form, Modal, TextInput } from "carbon-components-svelte";
  import { host } from "$lib/global";

  export let imageId: number;
  export let note: string = "";

  function updateNote(id: number, note: string) {
    const body = JSON.stringify({ note });
    fetch(`${host()}/app/images/${id}/note`, { method: "PUT", body });
  }
</script>

<ButtonSet>
  <TextInput inline labelText="Note" bind:value={note} size="sm" />
  <Button on:click={() => updateNote(imageId, note)} size="small">Update Note</Button>
</ButtonSet>
