import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldCheck, CreditCard, AlertCircle, ArrowLeft, CheckCircle2 } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { orderService } from '../services/orderService';

export const CheckoutPage: React.FC = () => {
  const { cart, refreshCart } = useCart();
  const navigate = useNavigate();

  const [shipping, setShipping] = useState({
    street: '100 Silicon Way',
    city: 'San Francisco',
    state: 'CA',
    postalCode: '94105',
    country: 'USA',
  });
  const [notes, setNotes] = useState('Deliver during standard business hours');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!cart || cart.items.length === 0) {
    return (
      <div className="card flex-center" style={{ minHeight: '50vh', flexDirection: 'column', gap: '1rem', padding: '3rem' }}>
        <h2>No Items to Checkout</h2>
        <p className="text-muted">Your shopping cart is currently empty.</p>
        <Link to="/products" className="btn btn-primary">
          Back to Catalog
        </Link>
      </div>
    );
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setShipping({ ...shipping, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const formattedAddress = `${shipping.street}, ${shipping.city}, ${shipping.state} ${shipping.postalCode}, ${shipping.country}`;

    try {
      setSubmitting(true);
      setError(null);
      await orderService.createOrder({
        shippingAddress: formattedAddress,
        notes: notes || undefined,
      });

      // Clear local cart state by refreshing with backend
      await refreshCart();

      // Navigate to orders
      navigate('/orders', { state: { orderSuccess: true } });
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Order placement failed. Please verify item stock availability.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <Link
          to="/cart"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.4rem',
            color: 'var(--text-muted)',
            fontSize: '0.9rem',
          }}
        >
          <ArrowLeft size={16} /> Return to Cart
        </Link>
      </div>

      <h1 style={{ fontSize: '1.85rem', marginBottom: '1.5rem' }}>Complete Your Order</h1>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
            gap: '2rem',
            alignItems: 'start',
          }}
        >
          {/* Left Column: Shipping & Payment */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            {/* Shipping Address Card */}
            <div className="card">
              <h3 style={{ fontSize: '1.15rem', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <CheckCircle2 size={18} color="var(--primary)" /> Shipping Address
              </h3>

              <div className="form-group">
                <label className="form-label">Street Address</label>
                <input
                  type="text"
                  name="street"
                  className="form-input"
                  value={shipping.street}
                  onChange={handleChange}
                  required
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">City</label>
                  <input
                    type="text"
                    name="city"
                    className="form-input"
                    value={shipping.city}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">State / Province</label>
                  <input
                    type="text"
                    name="state"
                    className="form-input"
                    value={shipping.state}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Postal / Zip Code</label>
                  <input
                    type="text"
                    name="postalCode"
                    className="form-input"
                    value={shipping.postalCode}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Country</label>
                  <input
                    type="text"
                    name="country"
                    className="form-input"
                    value={shipping.country}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Special Delivery Instructions (Optional)</label>
                <textarea
                  name="notes"
                  className="form-textarea"
                  rows={2}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                />
              </div>
            </div>

            {/* Payment Method Card */}
            <div className="card">
              <h3 style={{ fontSize: '1.15rem', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <CreditCard size={18} color="var(--primary)" /> Payment Method
              </h3>

              <div
                style={{
                  padding: '1rem',
                  border: '1px solid var(--primary)',
                  background: 'var(--primary-light)',
                  borderRadius: 'var(--radius-md)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                }}
              >
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>Enterprise Direct Invoice / Card</div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                    Corporate net-30 terms or credit card on file
                  </div>
                </div>
                <ShieldCheck size={24} color="var(--emerald)" />
              </div>
            </div>
          </div>

          {/* Right Column: Order Review */}
          <div className="card" style={{ padding: '1.75rem', position: 'sticky', top: '90px' }}>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '1.25rem' }}>Review Items</h3>

            <div
              style={{
                display: 'flex',
                flexDirection: 'column',
                gap: '0.75rem',
                maxHeight: '260px',
                overflowY: 'auto',
                marginBottom: '1.25rem',
                paddingRight: '0.5rem',
              }}
            >
              {cart.items.map((item) => (
                <div key={item.id} className="flex-between" style={{ fontSize: '0.88rem' }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>{item.productName}</div>
                    <div className="text-muted text-sm">
                      {item.quantity} × ${Number(item.unitPrice).toFixed(2)}
                    </div>
                  </div>
                  <span style={{ fontWeight: 600 }}>${Number(item.subtotal).toFixed(2)}</span>
                </div>
              ))}
            </div>

            <div
              style={{
                borderTop: '1px solid var(--border-subtle)',
                paddingTop: '1rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.6rem',
                marginBottom: '1.5rem',
              }}
            >
              <div className="flex-between text-muted text-sm">
                <span>Items Subtotal</span>
                <span>${Number(cart.totalAmount).toFixed(2)}</span>
              </div>
              <div className="flex-between text-muted text-sm">
                <span>Shipping</span>
                <span style={{ color: 'var(--emerald)' }}>Free</span>
              </div>
              <div
                className="flex-between"
                style={{
                  borderTop: '1px solid var(--border-subtle)',
                  paddingTop: '0.75rem',
                  marginTop: '0.25rem',
                }}
              >
                <span style={{ fontSize: '1.1rem', fontWeight: 700 }}>Total</span>
                <span className="price-tag" style={{ fontSize: '1.4rem' }}>
                  ${Number(cart.totalAmount).toFixed(2)}
                </span>
              </div>
            </div>

            <button type="submit" disabled={submitting} className="btn btn-primary btn-lg" style={{ width: '100%' }}>
              {submitting ? <div className="spinner" style={{ width: 20, height: 20 }} /> : 'Confirm and Place Order'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};
