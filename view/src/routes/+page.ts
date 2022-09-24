
/** @type {import('./$types').PageLoad} */
export function load({url}) {
  const address = url.searchParams.get('address');
  const path = url.searchParams.get('path')
  return {address, path};
}
