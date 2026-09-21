import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { ShoppingBag, ShoppingCart, User as UserIcon, LogOut, Shield, Package, Sparkles } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, isAdmin, login, logout } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();
  const location = useLocation();
  const [demoMenuOpen, setDemoMenuOpen] = useState(false);
  const [loggingIn, setLoggingIn] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleQuickLogin = async (email: string, pass: string) => {
    try {
      setLoggingIn(true);
      setDemoMenuOpen(false);
      await login(email, pass);
      if (email.includes('admin')) {
        navigate('/admin');
      } else {
        navigate('/products');
      }
    } catch {
      alert('Quick login failed. Please ensure services are running.');
    } finally {
      setLoggingIn(false);
    }
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
          <Link to="/" className="brand">
            <div className="brand-icon">
              <ShoppingBag size={18} />
            </div>
            <span>NovaTech</span>
          </Link>

          <nav className="nav-links">
            <Link
              to="/products"
              className={`nav-link ${location.pathname === '/products' || location.pathname === '/' ? 'active' : ''}`}
            >
              <span>Hardware</span>
            </Link>

            {isAuthenticated && (
              <Link
                to="/orders"
                className={`nav-link ${location.pathname === '/orders' ? 'active' : ''}`}
              >
                <Package size={15} />
                <span>My Orders</span>
              </Link>
            )}

            {isAdmin && (
              <Link
                to="/admin"
                className={`nav-link ${location.pathname.startsWith('/admin') ? 'active' : ''}`}
              >
                <Shield size={15} color="#818cf8" />
                <span>Admin Console</span>
                <span className="admin-pill">Staff</span>
              </Link>
            )}
          </nav>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
          {/* 1-Click Demo Account Switcher (For Live Client Demos) */}
          <div style={{ position: 'relative' }}>
            <button
              onClick={() => setDemoMenuOpen((prev) => !prev)}
              className="btn btn-secondary btn-sm"
              style={{
                padding: '0.42rem 0.75rem',
                fontSize: '0.78rem',
                borderColor: 'rgba(129, 140, 248, 0.35)',
                color: '#818cf8',
                background: 'rgba(79, 70, 229, 0.08)',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.35rem',
              }}
              title="Switch demo profile instantly"
            >
              <Sparkles size={13} />
              <span style={{ fontWeight: 600 }}>{isAuthenticated ? (isAdmin ? 'Admin' : 'Customer') : 'Demo Accounts'}</span>
            </button>

            {demoMenuOpen && (
              <div
                className="card"
                style={{
                  position: 'absolute',
                  top: '120%',
                  right: 0,
                  width: '270px',
                  padding: '0.75rem',
                  boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.5)',
                  zIndex: 100,
                  border: '1px solid var(--border-strong)',
                  background: 'var(--bg-surface)',
                }}
              >
                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginBottom: '0.5rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  Switch Role / Account
                </div>
                <button
                  disabled={loggingIn}
                  onClick={() => handleQuickLogin('admin@ecommerce.com', 'Admin123!')}
                  className="btn btn-secondary btn-sm"
                  style={{
                    width: '100%',
                    justifyContent: 'flex-start',
                    marginBottom: '0.4rem',
                    padding: '0.55rem 0.65rem',
                    background: isAdmin ? 'rgba(79, 70, 229, 0.18)' : undefined,
                    borderColor: isAdmin ? '#818cf8' : undefined,
                  }}
                >
                  <Shield size={14} color="#818cf8" />
                  <div style={{ textAlign: 'left', lineHeight: 1.25, flex: 1 }}>
                    <div style={{ fontSize: '0.82rem', fontWeight: 600, color: '#fff' }}>
                      Admin Console {isAdmin && '✓'}
                    </div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-dim)' }}>admin@ecommerce.com</div>
                  </div>
                </button>
                <button
                  disabled={loggingIn}
                  onClick={() => handleQuickLogin('user@ecommerce.com', 'User123!')}
                  className="btn btn-secondary btn-sm"
                  style={{
                    width: '100%',
                    justifyContent: 'flex-start',
                    padding: '0.55rem 0.65rem',
                    background: !isAdmin && isAuthenticated ? 'rgba(16, 185, 129, 0.15)' : undefined,
                    borderColor: !isAdmin && isAuthenticated ? 'var(--emerald)' : undefined,
                  }}
                >
                  <UserIcon size={14} color="var(--emerald)" />
                  <div style={{ textAlign: 'left', lineHeight: 1.25, flex: 1 }}>
                    <div style={{ fontSize: '0.82rem', fontWeight: 600, color: '#fff' }}>
                      Customer Account {!isAdmin && isAuthenticated && '✓'}
                    </div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-dim)' }}>user@ecommerce.com</div>
                  </div>
                </button>
              </div>
            )}
          </div>

          {/* Cart Trigger */}
          <Link to="/cart" className="btn btn-secondary cart-badge-container" style={{ padding: '0.5rem 0.85rem' }}>
            <ShoppingCart size={17} />
            <span style={{ fontSize: '0.88rem' }}>Cart</span>
            {cartCount > 0 && <span className="badge">{cartCount}</span>}
          </Link>

          {/* User Status / Login Buttons */}
          {isAuthenticated ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.45rem',
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-subtle)',
                  padding: '0.35rem 0.75rem',
                  borderRadius: 'var(--radius-full)',
                  fontSize: '0.85rem',
                }}
              >
                <div
                  style={{
                    width: '24px',
                    height: '24px',
                    borderRadius: '50%',
                    background: isAdmin ? 'rgba(79, 70, 229, 0.3)' : 'rgba(16, 185, 129, 0.2)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: isAdmin ? '#818cf8' : 'var(--emerald)',
                    fontWeight: 700,
                    fontSize: '0.75rem',
                  }}
                >
                  {user?.firstName?.charAt(0) || 'U'}
                </div>
                <span style={{ fontWeight: 600, color: '#fff' }}>{user?.firstName}</span>
                {isAdmin && (
                  <span
                    style={{
                      fontSize: '0.68rem',
                      fontWeight: 700,
                      color: '#818cf8',
                      background: 'rgba(79, 70, 229, 0.2)',
                      padding: '1px 6px',
                      borderRadius: 'var(--radius-full)',
                    }}
                  >
                    ADMIN
                  </span>
                )}
              </div>
              <button
                onClick={handleLogout}
                className="btn btn-outline btn-sm"
                title="Logout"
                style={{ padding: '0.45rem 0.65rem' }}
              >
                <LogOut size={15} />
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Link to="/login" className="btn btn-outline btn-sm">
                Login
              </Link>
              <Link to="/register" className="btn btn-primary btn-sm">
                Register
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
