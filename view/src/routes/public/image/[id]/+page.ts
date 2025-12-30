import { host } from '$lib/global';
import type { PageLoad } from './$types';

export const load = (async ({ params, fetch }): Promise<ImageData> => {
  const id = params.id;
  return await fetch(`${host()}/app/public/images/${id}?exif=true`).then((res) => res.json());
}) satisfies PageLoad;
