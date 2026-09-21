import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, ShoppingCart, Check, ShieldCheck, Truck, RefreshCw, AlertCircle } from 'lucide-react';
import { productService } from '../services/productService';
import { inventoryService } from '../services/inventoryService';
import { Product, InventoryItem } from '../types';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

export const ProductDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();

  const [product, setProduct] = useState<Product | null>(null);
  const [inventory, setInventory] = useState<InventoryItem | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    const fetchDetails = async () => {
      try {
        setLoading(true);
        setError(null);
        const prod = await productService.getProductById(Number(id));
        setProduct(prod);

        // Fetch real-time stock
        try {
          const inv = await inventoryService.getStockBySku(prod.sku);
          setInventory(inv);
        } catch {
          // Fallback if inventory service is warming up
        }
      } catch (err: unknown) {
        const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to load product details';
        setError(msg);
      } finally {
        setLoading(false);
      }
    };

    fetchDetails();
  }, [id]);

  const handleAddToCart = async () => {
    if (!product) return;
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    try {
      setAdding(true);
      setError(null);
      await addToCart(product.id, quantity);
      setAdded(true);
      setTimeout(() => setAdded(false), 2500);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to add item to cart';
      setError(msg);
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return (
      <div className="flex-center" style={{ minHeight: '60vh', flexDirection: 'column', gap: '1rem' }}>
        <div className="spinner" style={{ width: 36, height: 36 }} />
        <p className="text-muted text-sm">Fetching product specifications...</p>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="card flex-center" style={{ minHeight: '40vh', flexDirection: 'column', gap: '1rem', padding: '3rem' }}>
        <h2>Product Not Found</h2>
        <p className="text-muted">{error || 'The requested product could not be located.'}</p>
        <Link to="/products" className="btn btn-primary">
          <ArrowLeft size={16} /> Return to Catalog
        </Link>
      </div>
    );
  }

  const availableStock = inventory ? inventory.availableQuantity : 10;
  const isOutOfStock = availableStock <= 0;

  return (
    <div>
      {/* Breadcrumb Navigation */}
      <div style={{ marginBottom: '1.5rem' }}>
        <Link
          to="/products"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.4rem',
            color: 'var(--text-muted)',
            fontSize: '0.9rem',
            fontWeight: 500,
          }}
        >
          <ArrowLeft size={16} /> Back to Catalog
        </Link>
      </div>

      <div
        className="card"
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
          gap: '2.5rem',
          padding: '2.5rem',
        }}
      >
        {/* Left Column: Image / Showcase */}
        <div
          style={{
            background: 'linear-gradient(135deg, #1e293b, #0f172a)',
            borderRadius: 'var(--radius-lg)',
            overflow: 'hidden',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '380px',
            position: 'relative',
            border: '1px solid var(--border-subtle)',
          }}
        >
          {product.imageUrl ? (
            <img
              src={product.imageUrl}
              alt={product.name}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              onError={(e) => {
                (e.target as HTMLElement).style.display = 'none';
              }}
            />
          ) : (
            <div style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '2rem' }}>
              <div style={{ fontSize: '4.5rem', marginBottom: '0.5rem' }}>📦</div>
              <div style={{ fontSize: '1.1rem', fontWeight: 600 }}>{product.categoryName || product.category?.name || 'Hardware'}</div>
            </div>
          )}
        </div>

        {/* Right Column: Specs & Purchasing */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
              <span
                style={{
                  background: 'var(--primary-light)',
                  color: '#818cf8',
                  padding: '3px 10px',
                  borderRadius: 'var(--radius-full)',
                  fontSize: '0.78rem',
                  fontWeight: 700,
                }}
              >
                {product.categoryName || product.category?.name || 'Hardware'}
              </span>
              <span className="font-mono text-sm" style={{ color: 'var(--text-dim)' }}>
                SKU: {product.sku}
              </span>
            </div>

            <h1 style={{ fontSize: '1.85rem', marginBottom: '0.75rem', lineHeight: 1.25 }}>
              {product.name}
            </h1>

            <div className="price-tag" style={{ fontSize: '2rem', color: '#fff' }}>
              ${Number(product.price).toFixed(2)}
            </div>
          </div>

          <p style={{ color: 'var(--text-muted)', lineHeight: 1.6, fontSize: '0.96rem' }}>
            {product.description}
          </p>

          {/* Real-time Inventory Status */}
          <div
            style={{
              padding: '1rem',
              background: 'var(--bg-surface)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-subtle)',
            }}
          >
            <div className="flex-between" style={{ marginBottom: '0.4rem' }}>
              <span style={{ fontSize: '0.88rem', fontWeight: 600 }}>Availability:</span>
              <span
                style={{
                  fontWeight: 700,
                  fontSize: '0.88rem',
                  color: isOutOfStock ? 'var(--rose)' : 'var(--emerald)',
                }}
              >
                {isOutOfStock ? 'Out of Stock' : `${availableStock} units available`}
              </span>
            </div>
            {inventory && (
              <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                Total Stock: {inventory.quantity} | Reserved: {inventory.reservedQuantity}
              </div>
            )}
          </div>

          {/* Quantity and Add to Cart */}
          <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', marginTop: '0.5rem' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                border: '1px solid var(--border-subtle)',
                borderRadius: 'var(--radius-md)',
                background: 'var(--bg-input)',
              }}
            >
              <button
                type="button"
                disabled={quantity <= 1 || isOutOfStock}
                onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                className="btn btn-outline btn-sm"
                style={{ border: 'none', padding: '0.7rem 1rem' }}
              >
                -
              </button>
              <span style={{ padding: '0 1rem', fontWeight: 700, minWidth: '40px', textAlign: 'center' }}>
                {quantity}
              </span>
              <button
                type="button"
                disabled={quantity >= availableStock || isOutOfStock}
                onClick={() => setQuantity((q) => q + 1)}
                className="btn btn-outline btn-sm"
                style={{ border: 'none', padding: '0.7rem 1rem' }}
              >
                +
              </button>
            </div>

            <button
              onClick={handleAddToCart}
              disabled={adding || isOutOfStock}
              className={`btn ${added ? 'btn-secondary' : 'btn-primary'} btn-lg`}
              style={{
                flex: 1,
                background: added ? 'var(--emerald-bg)' : undefined,
                color: added ? 'var(--emerald)' : undefined,
                border: added ? '1px solid rgba(16, 185, 129, 0.4)' : undefined,
              }}
            >
              {adding ? (
                <div className="spinner" style={{ width: 20, height: 20 }} />
              ) : added ? (
                <>
                  <Check size={18} /> Added ({quantity}) to Cart
                </>
              ) : isOutOfStock ? (
                'Out of Stock'
              ) : (
                <>
                  <ShoppingCart size={18} /> Add to Cart
                </>
              )}
            </button>
          </div>

          {error && (
            <div className="alert alert-error" style={{ marginTop: '0.5rem' }}>
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          {/* Trust badges */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(3, 1fr)',
              gap: '0.75rem',
              marginTop: '1rem',
              paddingTop: '1.25rem',
              borderTop: '1px solid var(--border-subtle)',
            }}
          >
            <div style={{ textAlign: 'center', color: 'var(--text-dim)', fontSize: '0.78rem' }}>
              <Truck size={18} color="var(--primary)" style={{ margin: '0 auto 0.35rem' }} />
              <div>Free Delivery</div>
            </div>
            <div style={{ textAlign: 'center', color: 'var(--text-dim)', fontSize: '0.78rem' }}>
              <ShieldCheck size={18} color="var(--emerald)" style={{ margin: '0 auto 0.35rem' }} />
              <div>2-Year Warranty</div>
            </div>
            <div style={{ textAlign: 'center', color: 'var(--text-dim)', fontSize: '0.78rem' }}>
              <RefreshCw size={18} color="var(--amber)" style={{ margin: '0 auto 0.35rem' }} />
              <div>30-Day Return</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
