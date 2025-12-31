<script lang="ts">
  import 'carbon-components-svelte/css/g80.css';
  import '$lib/app.css';
  import MyHeader from '$lib/MyHeader.svelte';
  import { Button, Column, Content, Dropdown, Grid, Row } from 'carbon-components-svelte';
  import { untrack } from 'svelte';
  import type { StatsPageResult } from './+page';
  import type { StatsAggregate } from '$lib/image_type';
  import _ from 'lodash';
  import { Chart } from 'chart.js/auto';
  import { browser } from '$app/environment';
  import { host } from '$lib/global';
  import { DateTime } from 'luxon';

  let { data }: { data: StatsPageResult } = $props();

  const now = DateTime.now();
  let metric = $derived(data.metric);
  let granularity = $derived(data.granularity);
  let year = $derived(data.year?.toString() ?? now.year.toString());
  let month = $derived(data.month?.toString() ?? now.month.toString());
  let camera = $derived(data.camera ?? '');
  let lens = $derived(data.lens ?? '');
  let tagId = $derived(data.tagId?.toString() ?? '');

  let canvas: HTMLCanvasElement | undefined = $state();
  let pieCanvas: HTMLCanvasElement | undefined = $state();
  let chart: Chart | null = $state(null);
  let pieChart: Chart | null = $state(null);
  let availableMonths: string[] = $state([]);

  const metricItems = [
    { id: 'focal_length', text: 'Focal Length' },
    { id: 'camera', text: 'Camera' },
    { id: 'lens', text: 'Lens' },
    { id: 'iso', text: 'ISO' }
  ];

  const granularityItems = [
    { id: 'yearly', text: 'Yearly' },
    { id: 'monthly', text: 'Monthly' },
    { id: 'daily', text: 'Daily' }
  ];

  const cameraItems = $derived([
    { id: '', text: 'All Cameras' },
    ...data.cameras.map((c) => ({ id: c, text: c }))
  ]);

  const lensItems = $derived([
    { id: '', text: 'All Lenses' },
    ...data.lenses.map((l) => ({ id: l, text: l }))
  ]);

  const tagItems = $derived([
    { id: '', text: 'All Tags' },
    ...data.tags.map((t) => ({ id: t.id.toString(), text: t.name }))
  ]);

  const updateUrl = $derived(buildUrl());

  // Extract unique years from availableMonths (format: YYYYMM)
  const yearItems = $derived(
    _.uniq(availableMonths.map((m) => m.substring(0, 4)))
      .sort()
      .reverse()
      .map((y) => ({ id: y, text: y }))
  );

  // Extract months for selected year
  const monthItems = $derived(
    availableMonths
      .filter((m) => m.startsWith(year))
      .map((m) => {
        const mo = m.substring(4, 6);
        return { id: parseInt(mo, 10).toString(), text: mo };
      })
      .sort((a, b) => parseInt(a.id) - parseInt(b.id))
  );

  // Fetch available months on mount
  $effect(() => {
    fetch(`${host()}/app/images/calendar/months`)
      .then((res) => res.json())
      .then((json) => {
        availableMonths = json;
      });
  });

  function buildUrl(): string {
    const params = new URLSearchParams({ metric, granularity });
    if (granularity === 'monthly' || granularity === 'daily') {
      if (year) params.set('year', year);
    }
    if (granularity === 'daily') {
      if (month) params.set('month', month);
    }
    if (camera) params.set('camera', camera);
    if (lens) params.set('lens', lens);
    if (tagId) params.set('tagId', tagId);
    return `/stats?${params.toString()}`;
  }

  const colors = [
    'rgba(255, 99, 132, 0.8)',
    'rgba(54, 162, 235, 0.8)',
    'rgba(255, 206, 86, 0.8)',
    'rgba(75, 192, 192, 0.8)',
    'rgba(153, 102, 255, 0.8)',
    'rgba(255, 159, 64, 0.8)',
    'rgba(199, 199, 199, 0.8)',
    'rgba(83, 102, 255, 0.8)',
    'rgba(255, 99, 255, 0.8)',
    'rgba(99, 255, 132, 0.8)',
    'rgba(132, 99, 255, 0.8)',
    'rgba(255, 132, 99, 0.8)',
    'rgba(99, 255, 255, 0.8)',
    'rgba(255, 255, 99, 0.8)',
    'rgba(192, 75, 192, 0.8)',
    'rgba(192, 192, 75, 0.8)',
    'rgba(75, 75, 192, 0.8)',
    'rgba(192, 75, 75, 0.8)',
    'rgba(75, 192, 75, 0.8)',
    'rgba(128, 128, 255, 0.8)',
    'rgba(255, 128, 128, 0.8)',
    'rgba(128, 255, 128, 0.8)'
  ];

  function getSortedCategories(stats: StatsAggregate[]): string[] {
    // Build category info with min value for sorting
    const categoryMinMap = new Map<string, number | null>();
    for (const stat of stats) {
      if (!categoryMinMap.has(stat.category)) {
        categoryMinMap.set(stat.category, stat.min);
      }
    }

    // Sort categories: null first, then by min value ascending
    return Array.from(categoryMinMap.entries())
      .sort((a, b) => {
        if (a[1] === null && b[1] === null) return 0;
        if (a[1] === null) return -1;
        if (b[1] === null) return 1;
        return a[1] - b[1];
      })
      .map(([cat]) => cat);
  }

  function transformBarData(stats: StatsAggregate[]) {
    const periods = _.uniq(stats.map((s) => s.period)).sort();
    const categories = getSortedCategories(stats);

    const dataByCategory = new Map<string, Map<string, number>>();
    for (const cat of categories) {
      dataByCategory.set(cat, new Map());
    }

    for (const stat of stats) {
      dataByCategory.get(stat.category)?.set(stat.period, stat.count);
    }

    const datasets = categories.map((cat, idx) => ({
      label: cat,
      data: periods.map((p) => dataByCategory.get(cat)?.get(p) ?? 0),
      backgroundColor: colors[idx % colors.length]
    }));

    return {
      labels: periods,
      datasets
    };
  }

  function transformPieData(stats: StatsAggregate[]) {
    const categories = getSortedCategories(stats);

    // Sum counts by category
    const categoryTotals = new Map<string, number>();
    for (const stat of stats) {
      const current = categoryTotals.get(stat.category) ?? 0;
      categoryTotals.set(stat.category, current + stat.count);
    }

    return {
      labels: categories,
      datasets: [
        {
          data: categories.map((cat) => categoryTotals.get(cat) ?? 0),
          backgroundColor: categories.map((_, idx) => colors[idx % colors.length])
        }
      ]
    };
  }

  function createBarChart() {
    if (!browser) return;
    if (!canvas) return;

    if (chart) {
      chart.destroy();
    }

    const chartData = transformBarData(data.data);
    chart = new Chart(canvas, {
      type: 'bar',
      data: chartData,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: {
              color: '#f4f4f4'
            }
          },
          title: {
            display: false
          }
        },
        scales: {
          x: {
            stacked: true,
            ticks: {
              color: '#c6c6c6'
            },
            grid: {
              color: '#525252'
            }
          },
          y: {
            stacked: true,
            ticks: {
              color: '#c6c6c6'
            },
            grid: {
              color: '#525252'
            }
          }
        }
      }
    });
  }

  function createPieChart() {
    if (!browser) return;
    if (!pieCanvas) return;

    if (pieChart) {
      pieChart.destroy();
    }

    const chartData = transformPieData(data.data);
    pieChart = new Chart(pieCanvas, {
      type: 'pie',
      data: chartData,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: {
              color: '#f4f4f4'
            }
          }
        },
        elements: {
          arc: {
            borderWidth: 0
          }
        }
      }
    });
  }

  function createCharts() {
    createBarChart();
    createPieChart();
  }

  $effect(() => {
    if (canvas && pieCanvas && data) {
      untrack(() => createCharts());
    }
  });
</script>

<svelte:head>
  <title>Photographic Indexer -Stats-</title>
</svelte:head>

<MyHeader />
<Content>
  <Grid narrow>
    <Row style="margin-bottom: 1rem;">
      <Column lg={3}>
        <Dropdown {...{ titleText: 'Metric' }} bind:selectedId={metric} items={metricItems} />
      </Column>
      <Column lg={3}>
        <Dropdown
          {...{ titleText: 'Granularity' }}
          bind:selectedId={granularity}
          items={granularityItems}
        />
      </Column>
      {#if granularity === 'monthly' || granularity === 'daily'}
        <Column lg={2}>
          <Dropdown {...{ titleText: 'Year' }} bind:selectedId={year} items={yearItems} />
        </Column>
      {/if}
      {#if granularity === 'daily'}
        <Column lg={2}>
          <Dropdown {...{ titleText: 'Month' }} bind:selectedId={month} items={monthItems} />
        </Column>
      {/if}
      <Column lg={2} style="display: flex; align-items: flex-end;">
        <Button href={updateUrl}>Update</Button>
      </Column>
    </Row>
    <Row style="margin-bottom: 1rem;">
      <Column lg={4}>
        <Dropdown {...{ titleText: 'Camera' }} bind:selectedId={camera} items={cameraItems} />
      </Column>
      <Column lg={4}>
        <Dropdown {...{ titleText: 'Lens' }} bind:selectedId={lens} items={lensItems} />
      </Column>
      <Column lg={4}>
        <Dropdown {...{ titleText: 'Tag' }} bind:selectedId={tagId} items={tagItems} />
      </Column>
    </Row>
    <Row>
      <Column>
        <div class="chart-container">
          <canvas bind:this={canvas} id="statsChart"></canvas>
        </div>
      </Column>
    </Row>
    <Row style="margin-top: 2rem;">
      <Column>
        <div class="pie-chart-container">
          <canvas bind:this={pieCanvas} id="pieChart"></canvas>
        </div>
      </Column>
    </Row>
  </Grid>
</Content>

<style>
  .chart-container {
    height: 600px;
    width: 100%;
  }

  .pie-chart-container {
    height: 400px;
    width: 100%;
  }
</style>
