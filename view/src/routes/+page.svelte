<script lang="ts">
	import "carbon-components-svelte/css/g80.css";
	import {host} from "$lib/global";
	import MyHeader from "$lib/MyHeader.svelte"
	import {
		Button,
		Content,
		Form,
		FormGroup, InlineNotification,
		Link,
		ListItem, Pagination,
		Search,
		StructuredList, StructuredListBody, StructuredListCell,
		StructuredListHead, StructuredListRow, UnorderedList
	} from "carbon-components-svelte";
	import {onMount} from "svelte";
	import { goto } from '$app/navigation';
	import type {ImageData} from "$lib/image_type"
	import {thumbnail} from "$lib/image_type";
	import {DateTime} from "luxon";

	export let data;
	export let address = "";
	export let path = "";
	export let images: ImageData[] = [];
	export let allCount = -1;
	let page = 1;
	let pageSize = 20;

	onMount(() => {
		let execSearch = false;
		if(data.address != null) {
			address = data.address;
			execSearch = true;
		}
		if(data.path != null) {
			path = data.path;
			execSearch = true;
		}
		if(execSearch) search(null);
	});

	function search(e) {
		if(e != null) e.preventDefault();
		const params = new URLSearchParams({address, path, page: page - 1, perPage: pageSize})
		fetch(host + "/app/images/search?" + params)
				.then(res => res.json())
				.then(res => {
					images = res.data;
					allCount = res.allCount;
					const params = new URLSearchParams({address, path})
					goto(`/?${params}`)
				});
	}

	function isoDate(at: String) {
		return DateTime.fromISO(at).toISODate();
	}

	function updatePage() {
		search(null)
	}
</script>

<svelte:head>
	<title>Photographic Indexer</title>
	<meta name="description" content="Photographic Search Server" />
</svelte:head>

<MyHeader />
<Content>
	<Form on:submit={search} style="margin-bottom: 24px;">
		<FormGroup legendText="Search Address Query">
			<Search id="address" bind:value={address} />
		</FormGroup>
		<FormGroup legendText="Search Path(File) Query">
			<Search id="path" bind:value={path} />
		</FormGroup>
		<Button type="submit" disabled="{address === '' && path === ''}">Search</Button>
	</Form>

	{#if allCount === 0}
		<InlineNotification kind="warning" title="Not found image" />
	{/if}

	{#if 0 < allCount}
		<Pagination totalItems={allCount} pageSizes={[20, 50]} bind:page={page} bind:pageSize={pageSize} on:update={updatePage} />

		<StructuredList condensed>
			<StructuredListHead>
				<StructuredListRow head>
					<StructuredListCell head>image</StructuredListCell>
					<StructuredListCell head>detail</StructuredListCell>
				</StructuredListRow>
			</StructuredListHead>
			<StructuredListBody>
				{#each images as image}
				<StructuredListRow>
					<StructuredListCell style="vertical-align: bottom">
						<Link href="/image/{image.id}">
							<img src="{host}/app/images/{image.id}/thumbnail" width="320px" alt="{thumbnail(image).path}" class="fixed">
						</Link>
					</StructuredListCell>
					<StructuredListCell>
						<UnorderedList>
							<ListItem><Link href="/image/date/{isoDate(image.shootingAt)}">{image.shootingAt}</Link></ListItem>
							{#if image.geo}
								<ListItem>{image.geo.address}</ListItem>
							{/if}
							{#each image.files as file}
								<ListItem><Link href="{host}/static{file.path}">{file.path}</Link></ListItem>
							{/each}
						</UnorderedList>
					</StructuredListCell>
				</StructuredListRow>
				{/each}
			</StructuredListBody>
		</StructuredList>

		<Pagination totalItems={allCount} pageSizeInputDisabled pageSize={pageSize} bind:page={page} on:update={updatePage} />
	{/if}
</Content>

<style>
	.fixed {
		aspect-ratio: 3 / 2;
		object-fit: contain;
	}
</style>
