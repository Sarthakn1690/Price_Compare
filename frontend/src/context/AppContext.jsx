import { createContext, useContext, useState, useCallback } from 'react';

const AppContext = createContext(null);

export function AppProvider({ children }) {
  const [toast, setToast] = useState(null);
  const [watchlist, setWatchlist] = useState(() => {
    try {
      const stored = localStorage.getItem('price-comp-watchlist');
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  });

  const showToast = useCallback((message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  }, []);

  const addToWatchlist = useCallback((product) => {
    setWatchlist((prev) => {
      const next = prev.some((p) => p.id === product.id) ? prev : [...prev, product];
      localStorage.setItem('price-comp-watchlist', JSON.stringify(next));
      return next;
    });
  }, []);

  const removeFromWatchlist = useCallback((productId) => {
    setWatchlist((prev) => {
      const next = prev.filter((p) => p.id !== productId);
      localStorage.setItem('price-comp-watchlist', JSON.stringify(next));
      return next;
    });
  }, []);

  const isInWatchlist = useCallback(
    (productId) => watchlist.some((p) => p.id === productId),
    [watchlist]
  );

  const value = {
    toast,
    showToast,
    watchlist,
    addToWatchlist,
    removeFromWatchlist,
    isInWatchlist,
  };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useApp must be used within AppProvider');
  return ctx;
}
