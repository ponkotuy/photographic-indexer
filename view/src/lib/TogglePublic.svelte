<script lang="ts">
  import { Toggle } from "carbon-components-svelte";
  import { host } from "$lib/global";

  export let imageId: number;
  export let state: Boolean;

  function togglePublic() {
    const method = state ? "PUT" : "DELETE";
    fetch(host() + `/app/images/${imageId}/public`, { method });
  }
</script>

<Toggle
  size="sm"
  labelText="Public"
  labelA="Private"
  labelB="Public"
  hideLabel
  bind:toggled={state}
  on:toggle={togglePublic}
/>
