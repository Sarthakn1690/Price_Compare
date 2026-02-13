import { useMemo } from 'react';
import { formatPrice, platformDisplayName } from '../utils/helpers';
import { motion } from 'framer-motion';

const PLATFORM_COLORS = {
  amazon: 'from-amber-500/20 to-orange-600/20 border-amber-500/30',
  flipkart: 'from-blue-500/20 to-indigo-600/20 border-blue-500/30',
  myntra: 'from-pink-500/20 to-rose-600/20 border-pink-500/30',
};

export default function PriceComparison({ prices, bestPrice }) {
  const sorted = useMemo(
    () => (prices || []).slice().sort((a, b) => (a.price || 0) - (b.price || 0)),
    [prices]
  );
  const lowest = sorted[0]?.price;

  if (!sorted.length) return null;

  return (
    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {sorted.map((p, i) => {
        const isBest = p.id === bestPrice?.id || (lowest != null && p.price === lowest);
        const platformClass = PLATFORM_COLORS[p.platform?.toLowerCase()] || 'from-white/10 to-white/5 border-white/20';
        return (
          <motion.a
            key={p.id}
            href={p.productUrl}
            target="_blank"
            rel="noopener noreferrer"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.08 }}
            className={`group relative overflow-hidden rounded-xl border bg-gradient-to-br ${platformClass} p-4 backdrop-blur-sm transition hover:scale-[1.02] focus:outline-none focus:ring-2 focus:ring-accent`}
          >
            {isBest && (
              <span className="absolute right-2 top-2 rounded bg-accent px-2 py-0.5 font-mono text-xs font-medium text-surface">
                Best deal
              </span>
            )}
            <p className="font-display text-sm uppercase tracking-wide text-white/70">
              {platformDisplayName(p.platform)}
            </p>
            <p className="mt-1 font-mono text-2xl font-medium text-white">
              {formatPrice(p.price)}
            </p>
            {p.percentSavings != null && p.percentSavings > 0 && (
              <p className="mt-1 text-sm text-success">
                Save {p.percentSavings}% vs highest
              </p>
            )}
            {p.availability === false && (
              <p className="mt-1 text-xs text-danger">Out of stock</p>
            )}
          </motion.a>
        );
      })}
    </div>
  );
}
