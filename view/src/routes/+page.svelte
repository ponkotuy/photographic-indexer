<script lang="ts">
	import 'carbon-components-svelte/css/g80.css';
	import '$lib/app.css';
	import { host } from '$lib/global';
	import MyHeader from '$lib/MyHeader.svelte';
	import {
		Button,
		Column,
		Content,
		Form,
		FormGroup,
		Grid,
		InlineNotification,
		Link,
		ListItem, Pagination,
		Row,
		Search,
		StructuredList,
		StructuredListBody,
		StructuredListCell,
		StructuredListHead,
		StructuredListRow,
		Tag, Toggle,
		UnorderedList
	} from 'carbon-components-svelte';
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import type { ImageData } from '$lib/image_type';
	import { thumbnail } from '$lib/image_type';
	import { DateTime } from 'luxon';
	import { page as pp } from '$app/stores';
	import ImageTag from "$lib/ImageTag.svelte";
	import ImageNote from "$lib/ImageNote.svelte";

	type DateCount = {
		date: string;
		count: number;
	};

	export let address = '';
	export let path = '';
	export let images: ImageData[] = [];
	export let allCount = -1;
	export let dateCounts: DateCount[] = [];
	let page = 1;
	let pageSize = 20;

	onMount(() => {
		const params = $pp.url.searchParams;
		address = params.get('address') || '';
		path = params.get('path') || '';
		if (address != '' || path != '') search();
	});

	function searchSubmit(e) {
		e.preventDefault();
		search();
	}

	function search() {
		const allParams = new URLSearchParams({
			address,
			path,
			page: (page - 1).toString(),
			perPage: pageSize.toString()
		});
		const coreParams = new URLSearchParams({ address, path });
		if (coreParams.get('address') == '') coreParams.delete('address');
		if (coreParams.get('path') == '') coreParams.delete('path');
		fetch(host() + '/app/images/search?' + allParams)
			.then(res => res.json())
			.then(res => {
				images = res.data;
				allCount = res.allCount;
				goto(`/?${coreParams}`);
			});
		fetch(host() + '/app/images/search_date_count?' + coreParams)
			.then((res) => res.json())
			.then((res) => (dateCounts = res));
	}

	function isoDate(at: string): string {
		return DateTime.fromISO(at).toISODate();
	}

	function disableSubmit(address: string, path: string): boolean {
		return address == '' && path == '';
	}

	async function togglePublic(image: ImageData) {
		const method = image.isPublic ? 'DELETE' : 'PUT';
		await fetch(host() + `/app/images/${image.id}/public`, {method});
		image.isPublic = !image.isPublic;
	}

	function updateImage() {
		images = images;
	}
</script>

<svelte:head>
	<title>Photographic Indexer</title>
	<meta name="description" content="Photographic Search Server" />
</svelte:head>

<MyHeader />
<Content>
	<Form
		on:submit={searchSubmit}
		disabled={disableSubmit(address, path)}
		style="margin-bottom: 24px;"
	>
		<FormGroup legendText="Search Address Query">
			<Search id="address" bind:value={address} />
		</FormGroup>
		<FormGroup legendText="Search Path(File) Query">
			<Search id="path" bind:value={path} />
		</FormGroup>
		<Button type="submit" disabled={disableSubmit(address, path)}>Search</Button>
	</Form>

	{#if allCount === 0}
		<InlineNotification kind="warning" title="Not found image" />
	{/if}

	{#if 0 < allCount}
		<h3>Date Result</h3>
		<Grid style="margin-bottom: 24px;">
			<Row>
				{#each dateCounts as dc}
					<Column>
						<Tag type="outline">
							<Link href="/image/date/{dc.date}">{dc.date}({dc.count})</Link>
						</Tag>
					</Column>
				{/each}
			</Row>
		</Grid>

		<h3>Image Result</h3>

		<Pagination
			totalItems={allCount}
			pageSizes={[20, 50]}
			bind:page
			bind:pageSize
			on:update={search}
		/>

		<StructuredList condensed>
			<StructuredListHead>
				<StructuredListRow head>
					<StructuredListCell head>image</StructuredListCell>
					<StructuredListCell head>detail/files/tags</StructuredListCell>
				</StructuredListRow>
			</StructuredListHead>
			<StructuredListBody>
				{#each images as image}
					<StructuredListRow>
						<StructuredListCell style="vertical-align: bottom">
							<Link href="/image/{image.id}">
								<img
									src="{host()}/app/images/{image.id}/thumbnail"
									width="320px"
									alt={thumbnail(image).path}
									class="fixed"
								/>
							</Link>
						</StructuredListCell>
						<StructuredListCell>
							<UnorderedList>
								<ListItem>
									<Link href="/image/date/{isoDate(image.shootingAt)}">{image.shootingAt}</Link>
								</ListItem>
								{#if image.geo}
									<ListItem>{image.geo.address}</ListItem>
								{/if}
								<UnorderedList nested>
									{#each image.files as file}
										<ListItem><Link href="{host()}/static{file.path}">{file.path}</Link></ListItem>
									{/each}
								</UnorderedList>
							</UnorderedList>
							<div class="space-form">
								<Toggle
									size="sm"
									style="margin-top: 5px"
									labelText="Public"
									labelA="Private"
									labelB="Public"
									hideLabel
									toggled={image.isPublic}
									on:toggle={() => togglePublic(image)}
								/>
							</div>
							<div class="space-form"><ImageTag image={image} refresh={updateImage} /></div>
							<div class="space-form"><ImageNote imageId={image.id} note={image.note} /></div>
						</StructuredListCell>
					</StructuredListRow>
				{/each}
			</StructuredListBody>
		</StructuredList>

		<Pagination
			totalItems={allCount}
			pageSizeInputDisabled
			{pageSize}
			bind:page
			on:update={search}
		/>
	{/if}
</Content>

<style>
	.space-form {
		margin-top: 4px;
	}
</style>
