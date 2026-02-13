import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { searchProductByUrl } from '../services/api';
import { useApp } from '../context/AppContext';
import SearchBar from '../components/SearchBar';
import ProductDetails from '../components/ProductDetails';
import { motion, AnimatePresence } from 'framer-motion';

export default function Home() {
  const navigate = useNavigate();
  const { showToast, addToWatchlist, isInWatchlist } = useApp();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [product, setProduct] = useState(null);

  const handleSearch = useCallback(async (url) => {
    setError(null);
    setProduct(null);
    setLoading(true);
    try {
      const data = await searchProductByUrl(url);
      setProduct(data);
      showToast('Product loaded. View comparison below.', 'success');
      // Optional: scroll to result
      setTimeout(() => {
        document.getElementById('result')?.scrollIntoView({ behavior: 'smooth' });
      }, 100);
    } catch (err) {
      const message = err.response?.data?.error || err.message || 'Search failed';
      setError(message);
      showToast(message, 'error');
    } finally {
      setLoading(false);
    }
  }, [showToast]);

  const handleAddToWatchlist = useCallback(() => {
    if (!product) return;
    addToWatchlist({ id: product.id, name: product.name, imageUrl: product.imageUrl, bestPrice: product.bestPrice });
    showToast('Added to watchlist');
  }, [product, addToWatchlist, showToast]);

  return (
    <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <motion.section
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="flex flex-col items-center gap-6 text-center"
      >
        <h1 className="font-display text-3xl uppercase tracking-tight text-white sm:text-4xl md:text-5xl">
          Compare prices
        </h1>
        <p className="max-w-xl text-white/70">
          Paste a product link from Amazon, Flipkart, or Myntra to see the best deal and price history.
        </p>
        <SearchBar onSearch={handleSearch} loading={loading} error={error} />
      </motion.section>

      <AnimatePresence>
        {product && (
          <motion.section
            id="result"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0 }}
            className="mt-12"
          >
            <div className="mb-4 flex items-center justify-between">
              <h2 className="font-display text-xl uppercase tracking-wide text-white">Result</h2>
              <button
                type="button"
                onClick={() => navigate(`/product/${product.id}`)}
                className="text-sm font-medium text-accent hover:underline focus:outline-none focus:ring-2 focus:ring-accent"
              >
                View full details →
              </button>
            </div>
            <ProductDetails
              product={product}
              history={null}
              recommendation={null}
              onAddToWatchlist={handleAddToWatchlist}
              isInWatchlist={isInWatchlist(product?.id)}
            />
          </motion.section>
        )}
      </AnimatePresence>
    </main>
  );
}
