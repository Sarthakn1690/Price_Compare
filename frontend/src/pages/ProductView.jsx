import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getProduct, getHistory, getRecommendation, trackProduct } from '../services/api';
import { useApp } from '../context/AppContext';
import ProductDetails from '../components/ProductDetails';
import { motion } from 'framer-motion';

export default function ProductView() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast, addToWatchlist, isInWatchlist } = useApp();
  const [product, setProduct] = useState(null);
  const [history, setHistory] = useState(null);
  const [recommendation, setRecommendation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [historyDays, setHistoryDays] = useState(14);

  const load = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const [productRes, historyRes, recRes] = await Promise.all([
        getProduct(id),
        getHistory(id, historyDays),
        getRecommendation(id),
      ]);
      setProduct(productRes);
      setHistory(historyRes);
      setRecommendation(recRes);
    } catch (err) {
      const message = err.response?.data?.error || err.message || 'Failed to load product';
      setError(message);
      showToast(message, 'error');
    } finally {
      setLoading(false);
    }
  }, [id, historyDays, showToast]);

  useEffect(() => {
    load();
  }, [load]);

  const handleAddToWatchlist = useCallback(async () => {
    if (!product) return;
    try {
      await trackProduct(product.id);
      addToWatchlist({ id: product.id, name: product.name, imageUrl: product.imageUrl, bestPrice: product.bestPrice });
      showToast('Added to watchlist');
    } catch (err) {
      showToast(err.response?.data?.error || 'Could not add to watchlist', 'error');
    }
  }, [product, addToWatchlist, showToast]);

  if (loading && !product) {
    return (
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="flex min-h-[40vh] items-center justify-center">
          <div className="h-10 w-10 animate-spin rounded-full border-2 border-accent border-t-transparent" aria-hidden />
        </div>
      </main>
    );
  }

  if (error && !product) {
    return (
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="rounded-xl border border-danger/30 bg-danger/10 p-6 text-center"
        >
          <p className="text-danger">{error}</p>
          <button
            type="button"
            onClick={() => navigate('/')}
            className="mt-4 rounded-lg bg-white/10 px-4 py-2 text-sm hover:bg-white/20 focus:outline-none focus:ring-2 focus:ring-accent"
          >
            Back to search
          </button>
        </motion.div>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="mb-6 text-sm text-white/70 hover:text-accent focus:outline-none focus:ring-2 focus:ring-accent"
        >
          ← Back
        </button>
        <div className="mb-6 flex flex-wrap items-center gap-2">
          <span className="font-mono text-sm text-white/50">History:</span>
          {[7, 14].map((d) => (
            <button
              key={d}
              type="button"
              onClick={() => setHistoryDays(d)}
              className={`rounded px-3 py-1 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-accent ${
                historyDays === d ? 'bg-accent text-surface' : 'bg-white/10 text-white/80 hover:bg-white/20'
              }`}
            >
              {d} days
            </button>
          ))}
        </div>
        {product && (
          <>
            {historyDays !== 14 && (
              <div className="mb-4">
                <button
                  type="button"
                  onClick={() => load()}
                  className="text-sm text-accent hover:underline"
                >
                  Refresh history
                </button>
              </div>
            )}
            <ProductDetails
              product={product}
              history={history}
              recommendation={recommendation}
              historyDays={historyDays}
              onAddToWatchlist={handleAddToWatchlist}
              isInWatchlist={isInWatchlist(product.id)}
            />
          </>
        )}
      </motion.div>
    </main>
  );
}
