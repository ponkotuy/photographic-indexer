<script lang="ts">
	import 'carbon-components-svelte/css/g80.css';
	import '$lib/app.css'
	import MyHeader from '$lib/MyHeader.svelte';
	import { Button, Column, Content, Grid, Link, Pagination, Row } from "carbon-components-svelte";
	import { CaretLeft, CaretRight } from 'carbon-icons-svelte';
	import { host } from '$lib/global';
	import { thumbnail } from '$lib/image_type';
	import { DateTime } from 'luxon';
	import type { DatePageResult } from './+page';
	import LoadImage from "$lib/LoadImage.svelte";
	import PagingGrid from "./PagingGrid.svelte";

	export let data: DatePageResult;
	export let page = data.page;
	export let count = data.count;

	function yesterday(date: string) {
		return DateTime.fromISO(date).minus({ days: 1 }).toISODate();
	}

	function tomorrow(date: string) {
		return DateTime.fromISO(date).plus({ days: 1}).toISODate();
	}

	function hm(date: string): string {
		return DateTime.fromISO(date).toFormat('H:mm');
	}
</script>

<MyHeader />
<Content>
	<Grid narrow>
		<Row padding>
			<Column lg={2}>
				<Button href="/image/date/{yesterday(data.date)}" kind="ghost">
					<CaretLeft size={24} />Yesterday
				</Button>
			</Column>
			<Column lg={12} style="text-align: center;"><h2>{data.date}({data.images.length})</h2></Column>
			<Column lg={2}>
				<Button href="/image/date/{tomorrow(data.date)}" kind="ghost">
					Tomorrow<CaretRight size={24} />
				</Button>
			</Column>
		</Row>
	</Grid>

	{#if data.images.length > 20}
		<PagingGrid totalItems={data.images.length} bind:page bind:pageSize={count}></PagingGrid>
	{/if}

	<Grid>
		<Row padding>
			{#each data.images.slice((page - 1) * count, page * count) as image}
				{@const path = thumbnail(image).path}
				<Column lg={4}>
					<Link href="/image/{image.id}">
						<figure>
							<LoadImage
								src="{host()}/app/images/{image.id}/thumbnail"
								class="fixed"
								style="width: 100%;"
								title={path}
								alt={path}
							/>
							<figcaption>{hm(image.shootingAt)}</figcaption>
						</figure>
					</Link>
				</Column>
			{/each}
		</Row>
	</Grid>

	{#if data.images.length > 20}
		<PagingGrid totalItems={data.images.length} bind:page bind:pageSize={count}></PagingGrid>
	{/if}
</Content>
