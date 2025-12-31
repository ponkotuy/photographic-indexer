<script lang="ts">
  import { ListItem, UnorderedList } from 'carbon-components-svelte';
  import type { ImageData } from '$lib/image_type';

  let { image }: { image: ImageData } = $props();
</script>

{#if image.exif}
  <footer style="position: fixed; bottom: 0; right: 0; background-color: rgba(12, 12, 12, 0.5);">
    <UnorderedList>
      <ListItem>{image.exif.camera}/{image.exif.lens}</ListItem>
      <ListItem>
        {image.exif.focalLength}mm f/{image.exif.aperture}
        {image.exif.exposure}s ISO{image.exif.iso}
      </ListItem>
    </UnorderedList>
  </footer>
{/if}
