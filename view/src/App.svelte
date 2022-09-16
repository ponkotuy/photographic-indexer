<script lang="ts">
	import {host} from "./global";

	type Geom = {
		id: number
		address: string
		lat: number
		lon: number
	}

	type Image = {
		id: number
		cameraId: number
		shotId: number
		shootingAt: string
		geo: Geom | null
	}

	export let query: string;
	export let images: Image[];

	function search(){
		const q = new URLSearchParams({q: query})
		fetch(host + "/app/images/search?" + q)
				.then(res => res.json())
				.then(res => images = res.data);
	}
</script>

<main>
	<h1>Photographic Indexer</h1>
	<label for="query">Search Query</label>
	<input id="query" type="text" bind:value={query}>
	<button on:click={search}>Search</button>
	<ul>
		{#each images as image}
			<li>{image.id} {image.shootingAt} {image.geo.address}</li>
		{/each}
	</ul>
</main>
