import { host } from '$lib/global';
import type { StatsAggregate, Tag } from '$lib/image_type';
import type { PageLoad } from './$types';

export type StatsPageResult = {
  data: StatsAggregate[];
  metric: string;
  granularity: string;
  year: number | null;
  month: number | null;
  camera: string | null;
  lens: string | null;
  tagId: number | null;
  cameras: string[];
  lenses: string[];
  tags: Tag[];
};

export const load = (async ({ url, fetch }) => {
  const metric = url.searchParams.get('metric') || 'focal_length';
  const granularity = url.searchParams.get('granularity') || 'yearly';
  const year = url.searchParams.get('year');
  const month = url.searchParams.get('month');
  const camera = url.searchParams.get('camera');
  const lens = url.searchParams.get('lens');
  const tagId = url.searchParams.get('tagId');

  const params = new URLSearchParams({ metric, granularity });
  if (year) params.set('year', year);
  if (month) params.set('month', month);
  if (camera) params.set('camera', camera);
  if (lens) params.set('lens', lens);
  if (tagId) params.set('tagId', tagId);

  const [statsRes, camerasRes, lensesRes, tagsRes] = await Promise.all([
    fetch(`${host()}/app/stats/?${params.toString()}`),
    fetch(`${host()}/app/stats/cameras`),
    fetch(`${host()}/app/stats/lenses`),
    fetch(`${host()}/app/stats/tags`)
  ]);

  const [statsData, cameras, lenses, tags] = await Promise.all([
    statsRes.json(),
    camerasRes.json(),
    lensesRes.json(),
    tagsRes.json()
  ]);

  return {
    data: statsData as StatsAggregate[],
    metric,
    granularity,
    year: year ? parseInt(year) : null,
    month: month ? parseInt(month) : null,
    camera,
    lens,
    tagId: tagId ? parseInt(tagId) : null,
    cameras: cameras as string[] || [],
    lenses: lenses as string[] || [],
    tags: tags as Tag[] || []
  };
}) satisfies PageLoad;

export const prerender = false;
