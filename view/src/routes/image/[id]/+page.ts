import {host} from "$lib/global";
import type {ImageData} from "$lib/image_type";


/** @type {import('./$types').PageLoad} */
export async function load({params}) {
  const id = params.id;
  const image: ImageData = await fetch(`${host}/app/images/${id}`).then(res => res.json());
  return image;
}
