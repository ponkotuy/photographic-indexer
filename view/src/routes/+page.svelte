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

	function search(e){
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
				<StructuredListCell head>id</StructuredListCell>
				<StructuredListCell head>image</StructuredListCell>
				<StructuredListCell head>detail</StructuredListCell>
			</StructuredListRow>
		</StructuredListHead>
		<StructuredListBody>
			{#each images as image}
			<StructuredListRow>
				<StructuredListCell>
					<Link href="/image/{image.id}">{image.id}</Link>
				</StructuredListCell>
				<StructuredListCell style="vertical-align: bottom">
					<img src="{host}/image{thumbnail(image).path}" width="240px" alt="{thumbnail(image).path}">
				</StructuredListCell>
				<StructuredListCell>
					<UnorderedList>
						<ListItem>{image.shootingAt}</ListItem>
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
