import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AppProvider, useApp } from './context/AppContext';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Home from './pages/Home';
import ProductView from './pages/ProductView';
import Watchlist from './pages/Watchlist';
import { motion, AnimatePresence } from 'framer-motion';

function Toast() {
  const { toast } = useApp();
  if (!toast) return null;
  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -10 }}
        className="fixed bottom-6 left-1/2 z-[100] -translate-x-1/2 rounded-lg border border-white/20 bg-surface-elevated px-4 py-3 shadow-xl"
        role="status"
      >
        <p className="text-sm text-white">{toast.message}</p>
      </motion.div>
    </AnimatePresence>
  );
}

function Layout({ children }) {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <div className="flex-1">{children}</div>
      <Footer />
      <Toast />
    </div>
  );
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/product/:id" element={<ProductView />} />
      <Route path="/watchlist" element={<Watchlist />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppProvider>
        <Layout>
          <AppRoutes />
        </Layout>
      </AppProvider>
    </BrowserRouter>
  );
}
