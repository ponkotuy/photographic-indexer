import {host} from "$lib/global";

/** @type {import('./$types').PageLoad} */
export async function load({params}) {
  const id = params.id
  const image: Image = await fetch(`${host}/app/images/${id}`).then(res => res.json());
  return image;
}
