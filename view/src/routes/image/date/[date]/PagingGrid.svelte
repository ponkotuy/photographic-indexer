<script lang="ts">
  import { Grid, Pagination, Row } from 'carbon-components-svelte';
  import { goto } from '$app/navigation';

  export let totalItems: number;
  export let page = 1;
  export let pageSize = 20;

  function updatePage() {
    const query = new URLSearchParams();
    query.set('page', page.toString());
    query.set('count', pageSize.toString());
    goto(`?${query.toString()}`);
  }
</script>

<Grid narrow>
  <Row padding>
    <Pagination {totalItems} pageSizes={[20, 50]} bind:page bind:pageSize on:update={updatePage} />
  </Row>
</Grid>
