import { host } from '$lib/global';

export type AggregateDate = {
	date: string;
	imageCount: number;
	favoriteImage: ImageData;
};

export type CalendarPageResult = {
	month: string;
	agg: AggregateDate;
};

export async function load({ params, fetch }): Promise<CalendarPageResult> {
	const month = params.month;
	const res = await fetch(`${host()}/app/images/calendar/${month}`);
	const json = await res.json();
	return { month, agg: json };
}

export const prerender = false;
