import { host } from '$lib/global';
import type { ImageData, Tag } from '$lib/image_type';
import type { PageLoad } from './$types';
import _ from 'lodash';

export type DatePageResult = {
	date: string;
	images: ImageData[];
	tags: Tag[];
	page: number;
	count: number;
};

export const load = (async ({ params, url, fetch }) => {
	const date = params.date;
	const page = url.searchParams.get('page') || 1;
	const count = url.searchParams.get('count') || 20;
	let images: ImageData[] = await fetch(`${host()}/app/images/date/${date}`).then((res) =>
		res.json()
	);
	let tags: Tag[] = _.uniqBy(
		images.flatMap((img) => img.tags),
		(tag) => tag.id
	);
	return { date, images, tags, page, count };
}) satisfies PageLoad;
