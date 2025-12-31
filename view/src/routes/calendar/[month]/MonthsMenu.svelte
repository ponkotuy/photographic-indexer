<script lang="ts">
  import { Button, OverflowMenu, OverflowMenuItem } from 'carbon-components-svelte';
  import { onMount } from 'svelte';
  import { host } from '$lib/global';
  import { Calendar } from 'carbon-icons-svelte';

  let { now }: { now: string } = $props();

  let data = $state<string[]>([]);

  onMount(() => {
    fetch(`${host()}/app/images/calendar/months`)
      .then((res) => res.json())
      .then((res) => (data = res));
  });
</script>

<OverflowMenu size="sm">
  <Button icon={Calendar} iconDescription="Change month" kind="ghost" slot="menu" />
  {#each data as month}
    <OverflowMenuItem text={month} href="/calendar/{month}" primaryFocus={now === month} />
  {/each}
</OverflowMenu>
