import {host} from "$lib/global";
import _ from "lodash";

/** @type {import('./$types').PageLoad} */
export async function load({params}) {
  const date = params.date
  let images: Image[] = await fetch(`${host}/app/images/date/${date}`)
    .then(res => res.json());
  images = _.sortBy(images, (image) => image.shootingAt);
  return {date, images};
}
