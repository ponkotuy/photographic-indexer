<script lang="ts">
	import "carbon-components-svelte/css/g80.css";
	import {host} from "./global";
	import {
		Form,
		FormGroup,
		Grid,
		ListItem,
		Search,
		StructuredList, StructuredListBody, StructuredListCell,
		StructuredListHead, StructuredListRow, UnorderedList
	} from "carbon-components-svelte";
	import _ from "lodash";

	type Geom = {
		id: number
		address: string
		lat: number
		lon: number
	}

	type ImageFile = {
		id: number
		path: string
		filesize: number
	}

	type Image = {
		id: number
		cameraId: number
		shotId: number
		shootingAt: string
		geo: Geom | null
		files: ImageFile[]
	}

	export let query: string;
	export let images: Image[];
	export let allCount: number;

	function search(e){
		e.preventDefault()
		const q = new URLSearchParams({q: query})
		fetch(host + "/app/images/search?" + q)
				.then(res => res.json())
				.then(res => {
					images = res.data;
					allCount = res.allCount;
				});
	}

	function thumbnail(image: Image): ImageFile {
		return _.minBy(image.files, f => f.filesize)
	}
</script>

<main>
	<Grid>
		<h1>Photographic Indexer</h1>
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
					<StructuredListCell>{image.id}</StructuredListCell>
					<StructuredListCell style="vertical-align: bottom"><img src="{host}/image{thumbnail(image).path}" width="240px"></StructuredListCell>
					<StructuredListCell>
						<UnorderedList>
							<ListItem>{image.shootingAt}</ListItem>
							<ListItem>{image.geo.address}</ListItem>
							{#each image.files as file}
								<ListItem>{file.path}</ListItem>
							{/each}
						</UnorderedList>
					</StructuredListCell>
				</StructuredListRow>
				{/each}
			</StructuredListBody>
		</StructuredList>
	</Grid>
</main>
