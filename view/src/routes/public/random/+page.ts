import { host } from '$lib/global';
import type { PageLoad } from './$types';

export const load = (async ({ fetch }): Promise<ImageData> => {
	return await fetch(`${host()}/app/public/images/random?exif=true`).then(res => res.json());
}) satisfies PageLoad;
