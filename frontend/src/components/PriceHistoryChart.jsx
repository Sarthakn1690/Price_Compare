import { useMemo } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { formatPrice, formatDate } from '../utils/helpers';

const PLATFORM_COLORS = {
  amazon: '#f59e0b',
  flipkart: '#3b82f6',
  myntra: '#ec4899',
};

function CustomTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded-lg border border-white/20 bg-surface-elevated p-3 shadow-xl">
      <p className="mb-2 font-mono text-xs text-white/70">{formatDate(label)}</p>
      {payload.map((entry) => (
        <p key={entry.dataKey} className="font-mono text-sm" style={{ color: entry.color }}>
          {entry.name}: {formatPrice(entry.value)}
        </p>
      ))}
    </div>
  );
}

export default function PriceHistoryChart({ historyByPlatform, days = 14 }) {
  const { data, platforms } = useMemo(() => {
    if (!historyByPlatform || typeof historyByPlatform !== 'object') return { data: [], platforms: [] };
    const platforms = Object.keys(historyByPlatform);
    const byDate = {};
    platforms.forEach((platform) => {
      const points = historyByPlatform[platform] || [];
      points.forEach((point) => {
        const key = typeof point.date === 'string' ? point.date : point.date?.toString?.() ?? '';
        if (!byDate[key]) byDate[key] = { date: key };
        byDate[key][platform] = point.price;
      });
    });
    const data = Object.values(byDate).sort(
      (a, b) => new Date(a.date) - new Date(b.date)
    );
    return { data, platforms };
  }, [historyByPlatform]);

  if (data.length === 0) {
    return (
      <div className="flex min-h-[280px] items-center justify-center rounded-xl border border-white/10 bg-surface-elevated/50">
        <p className="text-sm text-white/50">No history data for the selected period.</p>
      </div>
    );
  }

  return (
    <div className="h-[280px] w-full">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 8, right: 8, left: 8, bottom: 8 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.08)" />
          <XAxis
            dataKey="date"
            tickFormatter={(v) => formatDate(v).split(' ').slice(0, 2).join(' ')}
            stroke="rgba(255,255,255,0.4)"
            tick={{ fontSize: 11 }}
          />
          <YAxis
            tickFormatter={(v) => `₹${v}`}
            stroke="rgba(255,255,255,0.4)"
            tick={{ fontSize: 11 }}
          />
          <Tooltip content={<CustomTooltip />} />
          <Legend
            formatter={(value) => value.charAt(0).toUpperCase() + value.slice(1)}
            wrapperStyle={{ fontSize: 12 }}
          />
          {platforms.map((platform) => (
            <Line
              key={platform}
              type="monotone"
              dataKey={platform}
              name={platform}
              stroke={PLATFORM_COLORS[platform] || '#b8ff3c'}
              strokeWidth={2}
              dot={false}
              connectNulls
            />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
