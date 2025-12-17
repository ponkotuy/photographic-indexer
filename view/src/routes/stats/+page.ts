import { host } from '$lib/global';
import type { StatsAggregate } from '$lib/image_type';
import type { PageLoad } from './$types';

export type StatsPageResult = {
	data: StatsAggregate[];
	metric: string;
	granularity: string;
	year: number | null;
	month: number | null;
};

export const load = (async ({ url, fetch }) => {
	const metric = url.searchParams.get('metric') || 'focal_length';
	const granularity = url.searchParams.get('granularity') || 'yearly';
	const year = url.searchParams.get('year');
	const month = url.searchParams.get('month');

	const params = new URLSearchParams({ metric, granularity });
	if (year) params.set('year', year);
	if (month) params.set('month', month);

	const res = await fetch(`${host()}/app/stats/?${params.toString()}`);
	const json = await res.json();

	return {
		data: json as StatsAggregate[],
		metric,
		granularity,
		year: year ? parseInt(year) : null,
		month: month ? parseInt(month) : null
	};
}) satisfies PageLoad;

export const prerender = false;
