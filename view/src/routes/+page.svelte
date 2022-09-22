<script lang="ts">
	import "carbon-components-svelte/css/g80.css";
	import {host} from "$lib/global";
	import MyHeader from "$lib/MyHeader.svelte"
	import {
		Content,
		Form,
		FormGroup,
		Link,
		ListItem,
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
	export let query = "";
	export let images: ImageData[] = [];
	export let allCount = 0;

	onMount(() => {
		if(data.q != null) {
			query = data.q;
			search(null);
		}
	});

	function search(e) {
		if(e != null) e.preventDefault();
		const q = new URLSearchParams({q: query})
		fetch(host + "/app/images/search?" + q)
				.then(res => res.json())
				.then(res => {
					images = res.data;
					allCount = res.allCount;
					goto('/?'+ q)
				});
	}

	function isoDate(at: String) {
		return DateTime.fromISO(at).toISODate();
	}
</script>

<svelte:head>
	<title>Photographic Indexer</title>
	<meta name="description" content="Photographic Search Server" />
</svelte:head>

<MyHeader />
<Content>
	<Form on:submit={search}>
		<FormGroup legendText="Search Query">
			<Search id="query" bind:value={query} />
		</FormGroup>
	</Form>
	<p>Count: {allCount}</p>
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
						<ListItem>{image.geo.address}</ListItem>
						{#each image.files as file}
							<ListItem><Link href="{host}/image{file.path}">{file.path}</Link></ListItem>
						{/each}
					</UnorderedList>
				</StructuredListCell>
			</StructuredListRow>
			{/each}
		</StructuredListBody>
	</StructuredList>
</Content>

<style>
	.fixed {
		aspect-ratio: 3 / 2;
		object-fit: contain;
	}
</style>
