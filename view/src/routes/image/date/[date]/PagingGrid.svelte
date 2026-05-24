<script lang="ts">
  import { Grid, Pagination, Row } from 'carbon-components-svelte';
  import { goto } from '$app/navigation';
  import { page as pageState } from '$app/state';
  import type { ResolvedPathname } from '$app/types';

  type Props = {
    page: number;
    pageSize: number;
    totalItems: number;
  };
  const { page: initialPage, pageSize: initialPageSize, totalItems }: Props = $props();

  let page = $state(initialPage);
  let pageSize = $state(initialPageSize);

  function updatePage(e: CustomEvent<{ page?: number; pageSize?: number }>) {
    const query = new URLSearchParams();
    if (e.detail.page) query.set('page', e.detail.page.toString());
    if (e.detail.pageSize) query.set('count', e.detail.pageSize.toString());
    goto(`${pageState.url.pathname}?${query.toString()}` as ResolvedPathname);
  }
</script>

<Grid narrow>
  <Row padding>
    <Pagination {totalItems} pageSizes={[20, 50]} {page} {pageSize} on:update={updatePage} />
  </Row>
</Grid>
