<script lang="ts">
	import 'carbon-components-svelte/css/g80.css';
	import MyHeader from '$lib/MyHeader.svelte';
	import {
		Button,
		Content, ImageLoader,
		Link,
		ListItem, Modal, StructuredList,
		StructuredListBody,
		StructuredListCell,
		StructuredListHead,
		StructuredListRow, Tag,
		Tile,
		UnorderedList
	} from "carbon-components-svelte";
	import { host } from '$lib/global';
	import type { ImageData } from '$lib/image_type';
	import { TrashCan } from "carbon-icons-svelte";
	import ImageTag from "$lib/ImageTag.svelte";

	export let data: ImageData;
	export let open: boolean = false;
	export let tags: Tag[] = [];

	const extensions = ['jpg', 'jpeg', 'png', 'webp'];
	function isValidImage(path: String): Boolean {
		const ext: string | undefined = path.split('.').pop()?.toLowerCase();
		if (!ext) return false;
		return extensions.includes(ext);
	}

	function remove() {
		fetch(`${host()}/app/images/${data.id}`, {method: 'DELETE'})
				.then(() => history.back());
	}
</script>

<MyHeader />
<Content>
	<StructuredList condensed>
		<StructuredListHead>
			<StructuredListRow>
				<StructuredListCell />
				<StructuredListCell />
			</StructuredListRow>
		</StructuredListHead>
		<StructuredListBody>
			<StructuredListRow>
				<StructuredListCell head>Shooting At</StructuredListCell>
				<StructuredListCell>{data.shootingAt}</StructuredListCell>
			</StructuredListRow>
			{#if data.geo}
				<StructuredListRow>
					<StructuredListCell head>Address</StructuredListCell>
					<StructuredListCell>{data.geo.address}</StructuredListCell>
				</StructuredListRow>
			{/if}
			<StructuredListRow>
				<StructuredListCell head>Files</StructuredListCell>
				<StructuredListCell>
					<UnorderedList>
						{#each data.files as file}
							<ListItem>
								<Link href="{host()}/static{file.path}">{file.path}</Link>
							</ListItem>
						{/each}
					</UnorderedList>
				</StructuredListCell>
			</StructuredListRow>
			<StructuredListRow>
				<StructuredListCell head>Tags</StructuredListCell>
				<StructuredListCell>
					<ImageTag image={data} />
				</StructuredListCell>
			</StructuredListRow>
			{#if data.exif}
				<StructuredListRow>
					<StructuredListCell head>
						<div>Camera</div>
						{#if data.exif.lens}<div>Lens</div>{/if}
						{#if data.exif.focal}<div>Focal length</div>{/if}
						<div>Exposure</div>
					</StructuredListCell>
					<StructuredListCell>
						<div>{data.exif.camera}</div>
						{#if data.exif.lens}<div>{data.exif.lens}</div>{/if}
						{#if data.exif.focal}<div>{data.exif.focal} mm (35mm equivalent)</div>{/if}
						<div>
							{#if data.exif.aperture}<span class="exposure">f/{data.exif.aperture}</span>{/if}
							<span class="exposure">{data.exif.exposureTime} sec</span>
							<span class="exposure">ISO {data.exif.iso}</span>
						</div>
					</StructuredListCell>
				</StructuredListRow>
			{/if}
			<StructuredListRow>
				<StructuredListCell head>Operation</StructuredListCell>
				<StructuredListCell>
					<Button
							kind="danger"
							size="small"
							icon={TrashCan}
							iconDescription="Delete"
							on:click={() => open = true}>
					</Button>
					<Modal
							danger
							bind:open
							modalHeading="Delete a image"
							primaryButtonText="Delete"
							secondaryButtonText="cancel"
							on:click:button--primary={remove}
							on:click:button--secondary={() => { open = false }}
							on:open
							on:close
							on:submit>
						<p>Delete all bound record and files.</p>
					</Modal>
				</StructuredListCell>
			</StructuredListRow>
		</StructuredListBody>
	</StructuredList>
	{#each data.files.filter((file) => isValidImage(file.path)) as file}
		<Tile style="margin: 16px 0;">
			<figure style="text-align: center;">
				<ImageLoader
					src="{host()}/static{file.path}"
					style="max-width: 100%"
					title={file.path}
					alt={file.path}
				/>
				<figcaption>{file.path}</figcaption>
			</figure>
		</Tile>
	{/each}
</Content>

<style>
	span.exposure {
		margin-right: 10px;
	}
</style>
