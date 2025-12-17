<script>
  import { Header, HeaderNav, HeaderNavItem } from "carbon-components-svelte";
  import { DateTime } from "luxon";
</script>

<Header platformName="Photographic Indexer" href="/">
  <HeaderNav>
    <HeaderNavItem text="Search" href="/" />
    <HeaderNavItem text="Calendar" href="/calendar/{DateTime.now().toFormat('yyyyMM')}" />
    <HeaderNavItem text="Stats" href="/stats" />
  </HeaderNav>
</Header>
