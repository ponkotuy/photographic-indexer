<script lang="ts">
  import { StructuredListCell, StructuredListRow } from "carbon-components-svelte";

  export let exif;
</script>


{#if exif}
  <StructuredListRow>
    <StructuredListCell head>
      <div>Camera</div>
      {#if exif.lens}<div>Lens</div>{/if}
      {#if exif.focal}<div>Focal length</div>{/if}
      <div>Exposure</div>
    </StructuredListCell>
    <StructuredListCell>
      <div>{exif.camera}</div>
      {#if exif.lens}<div>{exif.lens}</div>{/if}
      {#if exif.focal}<div>{exif.focal} mm (35mm equivalent)</div>{/if}
      <div>
        {#if exif.aperture}<span class="exposure">f/{exif.aperture}</span>{/if}
        <span class="exposure">{exif.exposureTime} sec</span>
        <span class="exposure">ISO {exif.iso}</span>
      </div>
    </StructuredListCell>
  </StructuredListRow>
{/if}

<style>
    span.exposure {
        margin-right: 10px;
    }
</style>
