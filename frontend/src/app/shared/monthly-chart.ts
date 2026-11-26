import { AfterViewInit, Component, ElementRef, effect, input, OnDestroy, viewChild } from '@angular/core';
import { BarController, BarElement, CategoryScale, Chart, LinearScale, Tooltip } from 'chart.js';
import { MONTHS_SHORT } from '../core/dates';
import { MonthlyPoint } from '../core/models';

Chart.register(BarController, BarElement, CategoryScale, LinearScale, Tooltip);

const money = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const compact = new Intl.NumberFormat('es-CO', { notation: 'compact', maximumFractionDigits: 1 });

/** Barras de ingresos vs. gastos por mes. El mes seleccionado se resalta. */
@Component({
  selector: 'app-monthly-chart',
  template: `<div class="chart-box"><canvas #canvas aria-label="Ingresos y gastos por mes" role="img"></canvas></div>`,
})
export class MonthlyChart implements AfterViewInit, OnDestroy {
  readonly points = input.required<MonthlyPoint[]>();
  readonly highlight = input<number>(0);

  private readonly canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private chart?: Chart<'bar'>;

  constructor() {
    effect(() => {
      const points = this.points();
      const highlight = this.highlight();
      if (this.chart) {
        this.apply(points, highlight);
      }
    });
  }

  ngAfterViewInit(): void {
    const css = getComputedStyle(document.documentElement);
    const ink = css.getPropertyValue('--ink-soft').trim() || '#56627a';
    const rule = css.getPropertyValue('--rule').trim() || '#d5ddd3';

    this.chart = new Chart(this.canvas().nativeElement, {
      type: 'bar',
      data: { labels: MONTHS_SHORT, datasets: [] },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? false : { duration: 400 },
        interaction: { mode: 'index', intersect: false },
        scales: {
          x: { grid: { display: false }, ticks: { color: ink, font: { family: 'IBM Plex Sans' } } },
          y: {
            grid: { color: rule },
            border: { display: false },
            ticks: { color: ink, font: { family: 'IBM Plex Sans' }, callback: v => compact.format(Number(v)) },
          },
        },
        plugins: {
          tooltip: {
            callbacks: { label: ctx => `${ctx.dataset.label}: ${money.format(Number(ctx.raw))}` },
          },
        },
      },
    });
    this.apply(this.points(), this.highlight());
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private apply(points: MonthlyPoint[], highlight: number): void {
    if (!this.chart) return;
    const fade = (color: string, faded: string) => points.map(p => (highlight === 0 || p.month === highlight ? color : faded));
    this.chart.data.datasets = [
      {
        label: 'Ingresos',
        data: points.map(p => p.income),
        backgroundColor: fade('#1F7A4D', '#A9CDB9'),
        borderRadius: 3,
        maxBarThickness: 18,
      },
      {
        label: 'Gastos',
        data: points.map(p => p.expense),
        backgroundColor: fade('#B4233C', '#E3AEB7'),
        borderRadius: 3,
        maxBarThickness: 18,
      },
    ];
    this.chart.update();
  }
}
