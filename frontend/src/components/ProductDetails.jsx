import { formatPrice, platformDisplayName } from '../utils/helpers';
import { motion } from 'framer-motion';
import PriceComparison from './PriceComparison';
import PriceHistoryChart from './PriceHistoryChart';
import RecommendationCard from './RecommendationCard';

export default function ProductDetails({
  product,
  history,
  recommendation,
  historyDays,
  onAddToWatchlist,
  isInWatchlist,
}) {
  const imageUrl = product?.imageUrl || '';

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="space-y-8"
    >
      <section className="grid gap-6 lg:grid-cols-[1fr,1fr]">
        <div className="overflow-hidden rounded-xl border border-white/10 bg-surface-elevated">
          {imageUrl ? (
            <img
              src={imageUrl}
              alt=""
              className="h-auto w-full max-h-[400px] object-contain bg-black/30"
            />
          ) : (
            <div className="flex aspect-square items-center justify-center text-white/30">No image</div>
          )}
        </div>
        <div className="flex flex-col justify-between gap-4">
          <div>
            <h1 className="font-display text-2xl uppercase tracking-tight text-white sm:text-3xl">
              {product?.name}
            </h1>
            {product?.brand && (
              <p className="mt-1 text-white/70">{product.brand}</p>
            )}
            {product?.bestPrice && (
              <p className="mt-3 font-mono text-3xl text-accent">
                Best: {formatPrice(product.bestPrice.price)}
              </p>
            )}
          </div>
          {onAddToWatchlist && (
            <button
              type="button"
              onClick={() => onAddToWatchlist(product)}
              disabled={isInWatchlist}
              className="w-fit rounded-lg border border-accent/50 bg-accent-dim px-4 py-2 text-sm font-medium text-accent transition hover:bg-accent/20 disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-accent"
            >
              {isInWatchlist ? 'In watchlist' : 'Add to watchlist'}
            </button>
          )}
        </div>
      </section>

      <section>
        <h2 className="font-display text-lg uppercase tracking-wide text-white/90">Price comparison</h2>
        <div className="mt-3">
          <PriceComparison
            prices={product?.prices}
            bestPrice={product?.bestPrice}
          />
        </div>
      </section>

      {recommendation && (
        <section>
          <h2 className="font-display text-lg uppercase tracking-wide text-white/90">AI recommendation</h2>
          <div className="mt-3">
            <RecommendationCard recommendation={recommendation} />
          </div>
        </section>
      )}

      {history && (
        <section>
          <h2 className="font-display text-lg uppercase tracking-wide text-white/90">Price history</h2>
          <div className="mt-3 rounded-xl border border-white/10 bg-surface-elevated/50 p-4">
            <PriceHistoryChart historyByPlatform={history.historyByPlatform} days={historyDays} />
          </div>
        </section>
      )}
    </motion.div>
  );
}
