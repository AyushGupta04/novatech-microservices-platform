import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ShoppingBag,
  Zap,
  Truck,
  RotateCcw,
  CheckCircle2,
  Mail,
  ArrowRight,
  Lock,
} from 'lucide-react';

export const Footer: React.FC = () => {
  const [email, setEmail] = useState('');
  const [subscribed, setSubscribed] = useState(false);

  const handleSubscribe = (e: React.FormEvent) => {
    e.preventDefault();
    if (email.trim()) {
      setSubscribed(true);
      setTimeout(() => setSubscribed(false), 3000);
      setEmail('');
    }
  };

  return (
    <footer
      style={{
        borderTop: '1px solid var(--border-subtle)',
        background: 'linear-gradient(180deg, #0f1524 0%, #080b13 100%)',
        paddingTop: '3.5rem',
        paddingBottom: '2rem',
        marginTop: 'auto',
      }}
    >
      <div
        style={{
          maxWidth: '1320px',
          margin: '0 auto',
          padding: '0 1.5rem',
        }}
      >
        {/* Top 4-Column Grid */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
            gap: '2.5rem',
            marginBottom: '3rem',
          }}
        >
          {/* Col 1: Brand & Newsletter */}
          <div style={{ maxWidth: '340px' }}>
            <Link to="/" className="brand" style={{ marginBottom: '0.85rem', display: 'inline-flex' }}>
              <div className="brand-icon">
                <ShoppingBag size={18} />
              </div>
              <span>Novamart</span>
            </Link>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', lineHeight: '1.6', marginBottom: '1.25rem' }}>
              High-performance engineering workstations, studio audio, and next-gen peripherals. Engineered for power users and creators.
            </p>

            <form onSubmit={handleSubscribe} style={{ display: 'flex', gap: '0.4rem' }}>
              <div style={{ position: 'relative', flex: 1 }}>
                <Mail
                  size={15}
                  style={{
                    position: 'absolute',
                    left: '10px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    color: 'var(--text-dim)',
                  }}
                />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Join insider newsletter..."
                  required
                  style={{
                    width: '100%',
                    padding: '0.55rem 0.75rem 0.55rem 2.1rem',
                    background: 'var(--bg-input)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-md)',
                    color: '#fff',
                    fontSize: '0.85rem',
                    outline: 'none',
                  }}
                />
              </div>
              <button
                type="submit"
                className="btn btn-primary btn-sm"
                style={{ padding: '0.55rem 0.85rem', flexShrink: 0 }}
                title="Subscribe"
              >
                {subscribed ? <CheckCircle2 size={15} color="#fff" /> : <ArrowRight size={15} />}
              </button>
            </form>
            {subscribed && (
              <p style={{ color: 'var(--emerald)', fontSize: '0.78rem', marginTop: '0.4rem', fontWeight: 500 }}>
                ✓ Subscribed! Welcome to VIP hardware drops.
              </p>
            )}
          </div>

          {/* Col 2: Store Catalog */}
          <div>
            <h4
              style={{
                fontSize: '0.92rem',
                textTransform: 'uppercase',
                letterSpacing: '0.06em',
                color: '#fff',
                marginBottom: '1rem',
              }}
            >
              Hardware Catalog
            </h4>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
              <li>
                <Link to="/products" style={{ color: 'var(--text-muted)', fontSize: '0.88rem', transition: 'color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.color = '#fff'} onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}>
                  Laptops & Workstations
                </Link>
              </li>
              <li>
                <Link to="/products" style={{ color: 'var(--text-muted)', fontSize: '0.88rem', transition: 'color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.color = '#fff'} onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}>
                  Audio & Acoustics
                </Link>
              </li>
              <li>
                <Link to="/products" style={{ color: 'var(--text-muted)', fontSize: '0.88rem', transition: 'color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.color = '#fff'} onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}>
                  Smartphones & Displays
                </Link>
              </li>
              <li>
                <Link to="/products" style={{ color: 'var(--text-muted)', fontSize: '0.88rem', transition: 'color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.color = '#fff'} onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}>
                  Wearables & Trackers
                </Link>
              </li>
              <li>
                <Link to="/products" style={{ color: 'var(--text-muted)', fontSize: '0.88rem', transition: 'color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.color = '#fff'} onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}>
                  All Hardware (Browse All)
                </Link>
              </li>
            </ul>
          </div>

          {/* Col 3: Customer Care & Services */}
          <div>
            <h4
              style={{
                fontSize: '0.92rem',
                textTransform: 'uppercase',
                letterSpacing: '0.06em',
                color: '#fff',
                marginBottom: '1rem',
              }}
            >
              Account & Support
            </h4>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
              <li>
                <Link to="/orders" style={{ color: 'var(--text-muted)', fontSize: '0.88rem', transition: 'color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.color = '#fff'} onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}>
                  Track Order Status
                </Link>
              </li>
              <li>
                <Link to="/cart" style={{ color: 'var(--text-muted)', fontSize: '0.88rem', transition: 'color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.color = '#fff'} onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}>
                  Shopping Cart
                </Link>
              </li>
              <li>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.88rem', cursor: 'pointer', transition: 'color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.color = '#fff'} onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}>
                  Express Shipping Policy
                </span>
              </li>
              <li>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.88rem', cursor: 'pointer', transition: 'color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.color = '#fff'} onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}>
                  30-Day Returns & Exchanges
                </span>
              </li>
              <li>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.88rem', cursor: 'pointer', transition: 'color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.color = '#fff'} onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}>
                  24/7 Enterprise Help Center
                </span>
              </li>
            </ul>
          </div>

          {/* Col 4: Trust Badges */}
          <div>
            <h4
              style={{
                fontSize: '0.92rem',
                textTransform: 'uppercase',
                letterSpacing: '0.06em',
                color: '#fff',
                marginBottom: '1rem',
              }}
            >
              Enterprise Guarantees
            </h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                <Lock size={15} color="#818cf8" style={{ flexShrink: 0 }} />
                <span>256-Bit SSL Encrypted Checkout</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                <Zap size={15} color="#38bdf8" style={{ flexShrink: 0 }} />
                <span>Real-Time Atomic Stock Locking</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                <Truck size={15} color="#10b981" style={{ flexShrink: 0 }} />
                <span>Insured 24-Hour Dispatch</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                <RotateCcw size={15} color="#f59e0b" style={{ flexShrink: 0 }} />
                <span>Instant Restock on Order Cancel</span>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Bar: Copyright & Live System Indicator */}
        <div
          style={{
            paddingTop: '1.5rem',
            borderTop: '1px solid var(--border-subtle)',
            display: 'flex',
            flexWrap: 'wrap',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: '1rem',
            fontSize: '0.82rem',
            color: 'var(--text-dim)',
          }}
        >
          <div>
            © {new Date().getFullYear()} Novamart Technologies Inc. All rights reserved. · Privacy · Terms · Security
          </div>

          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.5rem',
              background: 'rgba(16, 185, 129, 0.08)',
              border: '1px solid rgba(16, 185, 129, 0.25)',
              padding: '0.35rem 0.8rem',
              borderRadius: 'var(--radius-full)',
              color: 'var(--emerald)',
              fontWeight: 500,
              fontSize: '0.78rem',
            }}
          >
            <span
              style={{
                width: '7px',
                height: '7px',
                borderRadius: '50%',
                backgroundColor: 'var(--emerald)',
                boxShadow: '0 0 8px var(--emerald)',
                animation: 'pulse 2s infinite',
              }}
            />
            <span>All Microservices Operational · 99.99% Uptime</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
