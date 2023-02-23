<script lang="ts">
	import { Button, Modal } from "carbon-components-svelte";
	import { host } from "$lib/global";
	import { TrashCan } from "carbon-icons-svelte";

	let imageId: number;
	let open: boolean = false;

	function remove() {
		fetch(`${host()}/app/images/${imageId}`, {method: 'DELETE'})
			.then(() => open = false)
			.then(() => history.back());
	}
</script>

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
