import { host } from '$lib/global';
import _ from 'lodash';
import type { ImageData } from '$lib/image_type';
import type { PageLoad } from './$types';

export type DatePageResult = {
	date: string;
	images: ImageData[];
	page: number;
	count: number;
};

export const load = (async ({ params, url }) => {
	const date = params.date;
	const page = url.searchParams.get('page') || 1;
	const count = url.searchParams.get('count') || 20;
	let images: ImageData[] = await fetch(`${host()}/app/images/date/${date}`).then((res) =>
		res.json()
	);
	images = _.sortBy(images, (image) => image.shootingAt);
	return { date, images, page, count };
}) satisfies PageLoad;
