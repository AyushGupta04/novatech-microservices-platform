import React, { useState, useEffect } from 'react';
import { useLocation, Link } from 'react-router-dom';
import { Package, XCircle, CheckCircle2, AlertCircle, ShoppingBag, MapPin } from 'lucide-react';
import { orderService } from '../services/orderService';
import { Order } from '../types';
import { StatusBadge } from '../components/StatusBadge';

export const OrdersPage: React.FC = () => {
  const location = useLocation();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState<number | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(
    (location.state as { orderSuccess?: boolean })?.orderSuccess
      ? 'Order successfully placed! Inventory has been reserved.'
      : null
  );
  const [error, setError] = useState<string | null>(null);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await orderService.getMyOrders();
      setOrders(data);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to load orders';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleCancelOrder = async (orderId: number, orderNumber: string) => {
    if (!window.confirm(`Are you sure you want to cancel order #${orderNumber}? This will release the allocated inventory.`)) {
      return;
    }

    try {
      setCancellingId(orderId);
      setError(null);
      const updated = await orderService.cancelOrder(orderId);
      setOrders((prev) => prev.map((o) => (o.id === orderId ? updated : o)));
      setSuccessMessage(`Order #${orderNumber} cancelled successfully. Inventory has been restocked.`);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to cancel order';
      setError(msg);
    } finally {
      setCancellingId(null);
    }
  };

  if (loading && orders.length === 0) {
    return (
      <div className="flex-center" style={{ minHeight: '60vh', flexDirection: 'column', gap: '1rem' }}>
        <div className="spinner" style={{ width: 36, height: 36 }} />
        <p className="text-muted text-sm">Loading your orders...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex-between" style={{ marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', marginBottom: '0.25rem' }}>My Orders</h1>
          <p className="text-muted text-sm">Track your shipments and historical purchases</p>
        </div>
        <Link to="/products" className="btn btn-secondary btn-sm">
          Browse More
        </Link>
      </div>

      {successMessage && (
        <div className="alert alert-success">
          <CheckCircle2 size={16} />
          <span>{successMessage}</span>
        </div>
      )}

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      {orders.length === 0 ? (
        <div className="card flex-center" style={{ minHeight: '40vh', flexDirection: 'column', gap: '1rem', padding: '3rem' }}>
          <ShoppingBag size={42} color="var(--text-dim)" />
          <h2>No Orders Yet</h2>
          <p className="text-muted">You haven't placed any orders with NovaTech yet.</p>
          <Link to="/products" className="btn btn-primary">
            Explore Hardware Catalog
          </Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {orders.map((order) => {
            const canCancel = order.status === 'PENDING' || order.status === 'CONFIRMED';
            return (
              <div key={order.id} className="card">
                {/* Header Row */}
                <div
                  className="flex-between"
                  style={{
                    paddingBottom: '1rem',
                    borderBottom: '1px solid var(--border-subtle)',
                    flexWrap: 'wrap',
                    gap: '1rem',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <div
                      style={{
                        width: 42,
                        height: 42,
                        borderRadius: 'var(--radius-md)',
                        background: 'var(--bg-surface)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'var(--primary)',
                      }}
                    >
                      <Package size={20} />
                    </div>
                    <div>
                      <div style={{ fontWeight: 700, fontSize: '1.05rem' }}>Order #{order.orderNumber}</div>
                      <div className="text-muted text-sm">
                        Placed on {new Date(order.createdAt).toLocaleDateString()} at{' '}
                        {new Date(order.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </div>
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <StatusBadge status={order.status} />

                    {canCancel && (
                      <button
                        onClick={() => handleCancelOrder(order.id, order.orderNumber)}
                        disabled={cancellingId === order.id}
                        className="btn btn-danger btn-sm"
                      >
                        {cancellingId === order.id ? (
                          <div className="spinner" style={{ width: 14, height: 14 }} />
                        ) : (
                          <>
                            <XCircle size={14} /> Cancel
                          </>
                        )}
                      </button>
                    )}
                  </div>
                </div>

                {/* Items snapshot table */}
                <div style={{ padding: '1rem 0', display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                  {order.items.map((item) => (
                    <div
                      key={item.id}
                      className="flex-between"
                      style={{
                        padding: '0.6rem 0.75rem',
                        background: 'var(--bg-surface)',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '0.88rem',
                      }}
                    >
                      <div>
                        <span style={{ fontWeight: 600 }}>{item.productName}</span>{' '}
                        <span className="font-mono text-sm text-muted">({item.sku})</span>
                      </div>
                      <div style={{ display: 'flex', gap: '1.5rem', alignItems: 'center' }}>
                        <span className="text-muted">
                          {item.quantity} × ${Number(item.unitPrice).toFixed(2)}
                        </span>
                        <span style={{ fontWeight: 700 }}>${Number(item.subtotal).toFixed(2)}</span>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Bottom Row: Address and Total */}
                <div
                  className="flex-between"
                  style={{
                    paddingTop: '0.75rem',
                    borderTop: '1px solid var(--border-subtle)',
                    flexWrap: 'wrap',
                    gap: '1rem',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                    <MapPin size={14} color="var(--text-dim)" />
                    <span>{order.shippingAddress}</span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                    <span className="text-muted text-sm">Total Paid:</span>
                    <span className="price-tag" style={{ fontSize: '1.25rem' }}>
                      ${Number(order.totalAmount).toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
