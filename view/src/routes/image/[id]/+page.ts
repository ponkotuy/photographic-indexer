import { host } from '$lib/global';
import type { ImageData } from '$lib/image_type';

/** @type {import("./$types").PageLoad} */
export async function load({ params, fetch }): Promise<ImageData> {
	const id = params.id;
	return await fetch(`${host()}/app/images/${id}?exif=true`).then((res) => res.json());
}
