import {host} from "$lib/global";
import _ from "lodash";
import type {ImageData} from "$lib/image_type";

export type DatePageResult = {
  date: string,
  images: ImageData[]
}

/** @type {import('./$types').PageLoad} */
export async function load({params}): Promise<DatePageResult> {
  const date = params.date
  let images: ImageData[] = await fetch(`${host}/app/images/date/${date}`)
    .then(res => res.json());
  images = _.sortBy(images, (image) => image.shootingAt);
  return {date, images};
}
