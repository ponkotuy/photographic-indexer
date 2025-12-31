import { host } from '$lib/global';
import type { PageLoad } from './$types';

export const load = (async ({ params, fetch }) => {
  const id = params.id;
  return await fetch(`${host()}/app/images/${id}?exif=true`).then((res) => res.json());
}) satisfies PageLoad;
