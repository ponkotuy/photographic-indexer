import {host} from '../../../lib/global';
import type { ImageData } from '../../../lib/image_type';

export type CalendarPageResult = {
  agg: AggregateDate[];
}

export type AggregateDate = {
  date: string;
  imageCount: number;
  favoriteImage: ImageData;
}

/** @type {import('./$types').PageLoad} */
export async function load({params}): Promise<CalendarPageResult> {
  const month = params.month;
  const agg = await fetch(`${host()}/app/images/calendar/${month}`)
    .then((res) => res.json())
    .catch(response => console.error(response));
  return {agg}
}
