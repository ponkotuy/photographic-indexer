import { host } from '$lib/global';
import type { PageLoad } from './$types';
import type { FileElement } from '$lib/file_element';

export type DirectoryPageResult = {
  file: string;
  fileList: FileElement[];
};

export const load = (async ({ params, fetch }) => {
  const file = params.file;
  const res = await fetch(`${host()}/app/directory/${file}`);
  const json = await res.json();
  return { file, fileList: json };
}) satisfies PageLoad;

export const prerender = false;
