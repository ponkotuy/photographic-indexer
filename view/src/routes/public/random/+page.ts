import { host } from '$lib/global';
import type { ImageData } from '$lib/image_type';

/** @type {import('./$types').PageLoad} */
export async function load(): Promise<ImageData> {
	return await fetch(`${host()}/public/images/random`).then((res) => res.json());
}
