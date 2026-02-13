import { Link } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import ProductCard from '../components/ProductCard';
import { motion } from 'framer-motion';

export default function Watchlist() {
  const { watchlist, removeFromWatchlist } = useApp();

  return (
    <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <motion.h1
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        className="font-display text-2xl uppercase tracking-tight text-white sm:text-3xl"
      >
        Watchlist
      </motion.h1>
      <p className="mt-2 text-white/70">
        Products you're tracking. Open any to see latest prices and recommendations.
      </p>

      {watchlist.length === 0 ? (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="mt-12 rounded-xl border border-white/10 bg-surface-elevated/50 p-12 text-center"
        >
          <p className="text-white/60">No products in your watchlist yet.</p>
          <Link
            to="/"
            className="mt-4 inline-block rounded-lg bg-accent px-4 py-2 font-display text-sm uppercase text-surface hover:bg-accent-muted focus:outline-none focus:ring-2 focus:ring-accent"
          >
            Compare a product
          </Link>
        </motion.div>
      ) : (
        <ul className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {watchlist.map((item, i) => (
            <li key={item.id} className="relative list-none">
              <ProductCard product={item} index={i} />
              <button
                type="button"
                onClick={() => removeFromWatchlist(item.id)}
                className="absolute right-2 top-2 rounded bg-white/10 px-2 py-1 text-xs text-white/70 hover:bg-danger/20 hover:text-danger focus:outline-none focus:ring-2 focus:ring-accent"
                aria-label={`Remove ${item.name} from watchlist`}
              >
                Remove
              </button>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
