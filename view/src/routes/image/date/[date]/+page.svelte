<script lang="ts">
	import 'carbon-components-svelte/css/g80.css';
	import '$lib/app.css'
	import MyHeader from '$lib/MyHeader.svelte';
	import { Button, Column, Content, Grid, ImageLoader, Link, Pagination, Row } from "carbon-components-svelte";
	import { CaretLeft, CaretRight } from 'carbon-icons-svelte';
	import { host } from '$lib/global';
	import { thumbnail } from '$lib/image_type';
	import { DateTime } from 'luxon';
	import type { DatePageResult } from './+page';

	export let data: DatePageResult;
	export let page = 1;
	export let pageSize = 20;

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
		<Grid narrow>
			<Row padding>
				<Pagination
					totalItems={data.images.length}
					pageSizes={[20, 50]}
					bind:page
					bind:pageSize
				/>
			</Row>
		</Grid>
	{/if}

	<Grid>
		<Row padding>
			{#each data.images.slice((page - 1) * pageSize, page * pageSize) as image}
				{@const path = thumbnail(image).path}
				<Column lg={4}>
					<Link href="/image/{image.id}">
						<figure>
							<ImageLoader
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
		<Grid narrow>
			<Row padding>
				<Pagination
					totalItems={data.images.length}
					pageSizes={[20, 50]}
					bind:page
					bind:pageSize
				/>
			</Row>
		</Grid>
	{/if}
</Content>
