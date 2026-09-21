import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Trash2, ArrowRight, ShoppingBag, AlertCircle, ArrowLeft } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

export const CartPage: React.FC = () => {
  const { cart, updateQuantity, removeItem, clearCart, loading } = useCart();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  if (!isAuthenticated) {
    return (
      <div className="card flex-center" style={{ minHeight: '50vh', flexDirection: 'column', gap: '1rem', padding: '3rem' }}>
        <ShoppingBag size={48} color="var(--primary)" />
        <h2>Sign In to View Your Cart</h2>
        <p className="text-muted">Items added to your cart are securely stored in your personal account.</p>
        <Link to="/login" className="btn btn-primary">
          Sign In Now
        </Link>
      </div>
    );
  }

  if (loading && !cart) {
    return (
      <div className="flex-center" style={{ minHeight: '50vh' }}>
        <div className="spinner" style={{ width: 36, height: 36 }} />
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="card flex-center" style={{ minHeight: '50vh', flexDirection: 'column', gap: '1rem', padding: '3rem' }}>
        <div
          style={{
            width: 72,
            height: 72,
            borderRadius: '50%',
            background: 'var(--bg-surface)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--text-dim)',
          }}
        >
          <ShoppingBag size={36} />
        </div>
        <h2>Your Cart is Empty</h2>
        <p className="text-muted">Explore our collection of high-performance tech hardware.</p>
        <Link to="/products" className="btn btn-primary">
          Browse Catalog
        </Link>
      </div>
    );
  }

  const handleUpdateQty = async (itemId: number, newQty: number) => {
    if (newQty <= 0) {
      await handleRemoveItem(itemId);
      return;
    }
    try {
      setUpdatingId(itemId);
      setError(null);
      await updateQuantity(itemId, newQty);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to update item quantity';
      setError(msg);
    } finally {
      setUpdatingId(null);
    }
  };

  const handleRemoveItem = async (itemId: number) => {
    try {
      setUpdatingId(itemId);
      setError(null);
      await removeItem(itemId);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to remove item';
      setError(msg);
    } finally {
      setUpdatingId(null);
    }
  };

  const handleClearCart = async () => {
    if (window.confirm('Are you sure you want to remove all items from your cart?')) {
      try {
        setError(null);
        await clearCart();
      } catch (err: unknown) {
        const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to clear cart';
        setError(msg);
      }
    }
  };

  return (
    <div>
      <div className="flex-between" style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.75rem' }}>Shopping Cart ({cart.totalItems} items)</h1>
        <button onClick={handleClearCart} className="btn btn-outline btn-sm" style={{ color: 'var(--rose)' }}>
          <Trash2 size={14} /> Clear Cart
        </button>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
          gap: '2rem',
          alignItems: 'start',
        }}
      >
        {/* Cart Item List */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {cart.items.map((item) => (
            <div
              key={item.id}
              className="card"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '1.25rem',
                gap: '1rem',
                flexWrap: 'wrap',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flex: '1 1 200px' }}>
                <div
                  style={{
                    width: 52,
                    height: 52,
                    borderRadius: 'var(--radius-md)',
                    background: 'var(--bg-surface)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '1.5rem',
                  }}
                >
                  📦
                </div>
                <div>
                  <h4 style={{ fontSize: '1rem', marginBottom: '0.2rem' }}>{item.productName}</h4>
                  <div style={{ display: 'flex', gap: '0.75rem', fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                    <span className="font-mono">{item.sku}</span>
                    <span>${Number(item.unitPrice).toFixed(2)} each</span>
                  </div>
                </div>
              </div>

              {/* Quantity Controls */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-sm)',
                    background: 'var(--bg-input)',
                  }}
                >
                  <button
                    disabled={updatingId === item.id}
                    onClick={() => handleUpdateQty(item.id, item.quantity - 1)}
                    className="btn btn-outline btn-sm"
                    style={{ border: 'none', padding: '0.35rem 0.65rem' }}
                  >
                    -
                  </button>
                  <span style={{ padding: '0 0.65rem', fontWeight: 600, fontSize: '0.88rem' }}>
                    {updatingId === item.id ? '...' : item.quantity}
                  </span>
                  <button
                    disabled={updatingId === item.id}
                    onClick={() => handleUpdateQty(item.id, item.quantity + 1)}
                    className="btn btn-outline btn-sm"
                    style={{ border: 'none', padding: '0.35rem 0.65rem' }}
                  >
                    +
                  </button>
                </div>

                <div style={{ minWidth: '90px', textAlign: 'right' }}>
                  <div style={{ fontWeight: 700, fontSize: '1.05rem' }}>
                    ${Number(item.subtotal).toFixed(2)}
                  </div>
                </div>

                <button
                  onClick={() => handleRemoveItem(item.id)}
                  className="btn btn-outline btn-sm"
                  style={{ color: 'var(--rose)', padding: '0.45rem' }}
                  title="Remove item"
                >
                  <Trash2 size={15} />
                </button>
              </div>
            </div>
          ))}

          <Link
            to="/products"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.4rem',
              color: 'var(--text-muted)',
              fontSize: '0.88rem',
              marginTop: '0.5rem',
            }}
          >
            <ArrowLeft size={15} /> Continue Shopping
          </Link>
        </div>

        {/* Order Summary Card */}
        <div className="card" style={{ padding: '1.75rem', position: 'sticky', top: '90px' }}>
          <h3 style={{ fontSize: '1.25rem', marginBottom: '1.25rem' }}>Order Summary</h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem', marginBottom: '1.5rem' }}>
            <div className="flex-between text-muted text-sm">
              <span>Items Subtotal ({cart.totalItems})</span>
              <span style={{ color: '#fff', fontWeight: 600 }}>${Number(cart.totalAmount).toFixed(2)}</span>
            </div>
            <div className="flex-between text-muted text-sm">
              <span>Estimated Shipping</span>
              <span style={{ color: 'var(--emerald)', fontWeight: 600 }}>FREE</span>
            </div>
            <div className="flex-between text-muted text-sm">
              <span>Estimated Sales Tax</span>
              <span style={{ color: '#fff', fontWeight: 600 }}>$0.00</span>
            </div>
            <div
              className="flex-between"
              style={{
                paddingTop: '1rem',
                marginTop: '0.5rem',
                borderTop: '1px solid var(--border-subtle)',
              }}
            >
              <span style={{ fontSize: '1.1rem', fontWeight: 700 }}>Total Due</span>
              <span className="price-tag" style={{ fontSize: '1.4rem' }}>
                ${Number(cart.totalAmount).toFixed(2)}
              </span>
            </div>
          </div>

          <button
            onClick={() => navigate('/checkout')}
            className="btn btn-primary btn-lg"
            style={{ width: '100%' }}
          >
            Proceed to Checkout <ArrowRight size={18} />
          </button>

          <div style={{ textAlign: 'center', marginTop: '1rem', fontSize: '0.78rem', color: 'var(--text-dim)' }}>
            Instant inventory reservation upon order placement
          </div>
        </div>
      </div>
    </div>
  );
};
