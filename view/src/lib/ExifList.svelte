<script lang="ts">
  import { ListItem, UnorderedList } from 'carbon-components-svelte'

  export let image: ImageData
</script>

{#if image.exif}
  <footer style="position: fixed; bottom: 0; right: 0; background-color: rgba(12, 12, 12, 0.5);">
    <UnorderedList>
      <ListItem>{image.exif.camera}/{image.exif.lens}</ListItem>
      <ListItem>{image.exif.focal}mm f/{image.exif.aperture} {image.exif.exposureTime}s ISO{image.exif.iso}</ListItem>
    </UnorderedList>
  </footer>
{/if}
