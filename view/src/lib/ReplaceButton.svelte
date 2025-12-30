<script lang="ts">
  import {
    Button,
    FileUploaderDropContainer,
    FileUploaderItem,
    Modal
  } from 'carbon-components-svelte';
  import { Upload } from 'carbon-icons-svelte';
  import { host } from '$lib/global';

  export let path: string;

  let open: boolean = false;
  let isReady: boolean = false;
  let files: File[];

  function replace() {
    const formData = new FormData();
    formData.append('file', files[0]);
    fetch(`${host()}/app/static${path}`, { method: 'PUT', body: formData }).then(reset);
  }

  function checkFile(files: File[]) {
    return files.filter((file) => path.includes(file.name));
  }

  function reset() {
    open = false;
    isReady = false;
    files = [];
  }
</script>

<Button
  kind="ghost"
  size="small"
  icon={Upload}
  iconDescription="Replace File"
  on:click={() => (open = true)}
/>
<Modal
  bind:open
  modalHeading="Replace file"
  primaryButtonText="Replace"
  secondaryButtonText="Cancel"
  on:click:button--primary={replace}
  on:click:button--secondary={reset}
  primaryButtonDisabled={!isReady}
>
  {#if isReady}
    <p>Replace {path} with the following file</p>
    {#each files as file}
      <FileUploaderItem name={file.name} status="complete" />
    {/each}
  {:else}
    <p>Upload file to replace {path}</p>
    <div style="text-align: center; margin-top: 8px;">
      <FileUploaderDropContainer
        bind:files
        labelText="Drag and drop files here or click to upload"
        validateFiles={checkFile}
        on:change={(e) => {
          console.log(e);
          console.log(files);
          isReady = true;
        }}
      />
    </div>
  {/if}
</Modal>
