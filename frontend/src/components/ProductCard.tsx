import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingCart, Check, AlertCircle } from 'lucide-react';
import { Product } from '../types';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

interface ProductCardProps {
  product: Product;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    try {
      setAdding(true);
      setError(null);
      await addToCart(product.id, 1);
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to add item';
      setError(msg);
      setTimeout(() => setError(null), 3000);
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className="card card-interactive" style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Link to={`/products/${product.id}`} style={{ textDecoration: 'none', color: 'inherit', flex: 1 }}>
        <div
          style={{
            position: 'relative',
            width: '100%',
            height: '210px',
            background: 'linear-gradient(135deg, #1e293b, #0f172a)',
            borderRadius: 'var(--radius-md)',
            overflow: 'hidden',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: '1rem',
          }}
        >
          {product.imageUrl ? (
            <img
              src={product.imageUrl}
              alt={product.name}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              onError={(e) => {
                // fallback to styled placeholder
                (e.target as HTMLElement).style.display = 'none';
              }}
            />
          ) : (
            <div style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '1rem' }}>
              <div style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>📦</div>
              <div style={{ fontSize: '0.8rem', fontWeight: 500 }}>{product.categoryName || product.category?.name || 'Electronics'}</div>
            </div>
          )}

          <div
            style={{
              position: 'absolute',
              top: '10px',
              left: '10px',
              background: 'rgba(15, 23, 42, 0.75)',
              backdropFilter: 'blur(6px)',
              padding: '2px 8px',
              borderRadius: 'var(--radius-full)',
              fontSize: '0.72rem',
              fontWeight: 600,
              color: '#818cf8',
              border: '1px solid rgba(129, 140, 248, 0.2)',
            }}
          >
            {product.categoryName || product.category?.name || 'Hardware'}
          </div>

          <div
            style={{
              position: 'absolute',
              bottom: '10px',
              right: '10px',
              background: 'rgba(15, 23, 42, 0.75)',
              backdropFilter: 'blur(6px)',
              padding: '2px 6px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.68rem',
              fontFamily: 'monospace',
              color: 'var(--text-dim)',
            }}
          >
            {product.sku}
          </div>
        </div>

        <h3
          style={{
            fontSize: '1.05rem',
            marginBottom: '0.4rem',
            lineHeight: 1.3,
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
          }}
        >
          {product.name}
        </h3>

        <p
          style={{
            color: 'var(--text-muted)',
            fontSize: '0.84rem',
            marginBottom: '1rem',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
          }}
        >
          {product.description}
        </p>
      </Link>

      <div style={{ marginTop: 'auto', paddingTop: '0.75rem', borderTop: '1px solid var(--border-subtle)' }}>
        <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
          <span className="price-tag">${Number(product.price).toFixed(2)}</span>
          <span style={{ fontSize: '0.75rem', color: 'var(--emerald)', fontWeight: 600 }}>In Stock</span>
        </div>

        {error && (
          <div
            style={{
              fontSize: '0.75rem',
              color: 'var(--rose)',
              display: 'flex',
              alignItems: 'center',
              gap: '0.3rem',
              marginBottom: '0.5rem',
            }}
          >
            <AlertCircle size={12} />
            <span>{error}</span>
          </div>
        )}

        <button
          onClick={handleAddToCart}
          disabled={adding}
          className={`btn ${added ? 'btn-secondary' : 'btn-primary'}`}
          style={{
            width: '100%',
            background: added ? 'var(--emerald-bg)' : undefined,
            color: added ? 'var(--emerald)' : undefined,
            border: added ? '1px solid rgba(16, 185, 129, 0.3)' : undefined,
          }}
        >
          {adding ? (
            <div className="spinner" style={{ width: 16, height: 16 }} />
          ) : added ? (
            <>
              <Check size={16} /> Added to Cart
            </>
          ) : (
            <>
              <ShoppingCart size={16} /> Add to Cart
            </>
          )}
        </button>
      </div>
    </div>
  );
};
