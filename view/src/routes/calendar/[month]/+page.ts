import { host } from '$lib/global';
import type { PageLoad } from './$types';

export type AggregateDate = {
	date: string;
	imageCount: number;
	favoriteImage: ImageData;
};

export type CalendarPageResult = {
	month: string;
	agg: AggregateDate[];
};

export const load = (async ({ params, fetch }) => {
	const month = params.month;
	const res = await fetch(`${host()}/app/images/calendar/${month}`);
	const json = await res.json();
	return { month, agg: json };
}) satisfies PageLoad;

export const prerender = false;
