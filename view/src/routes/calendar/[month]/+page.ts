export type CalendarPageResult = {
	month: string;
};

/** @type {import('./$types').PageLoad} */
export async function load({ params }): Promise<CalendarPageResult> {
	const month = params.month;
	return { month };
}
