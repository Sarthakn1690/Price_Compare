import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';

export default function Navbar() {
  return (
    <motion.header
      initial={{ y: -20, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.4 }}
      className="sticky top-0 z-50 border-b border-white/10 bg-surface/95 backdrop-blur-card"
    >
      <nav className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8" aria-label="Main navigation">
        <Link to="/" className="font-display text-xl tracking-tight text-accent sm:text-2xl">
          PRICE COMP
        </Link>
        <div className="flex items-center gap-6">
          <Link
            to="/"
            className="text-sm font-medium text-white/80 transition hover:text-accent focus:text-accent"
          >
            Compare
          </Link>
          <Link
            to="/watchlist"
            className="text-sm font-medium text-white/80 transition hover:text-accent focus:text-accent"
          >
            Watchlist
          </Link>
        </div>
      </nav>
    </motion.header>
  );
}
