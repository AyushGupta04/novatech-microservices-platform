import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { UserPlus, Mail, Lock, User as UserIcon, Phone, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Register: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    phoneNumber: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await register({
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        password: formData.password,
        phoneNumber: formData.phoneNumber || undefined,
      });
      navigate('/products', { replace: true });
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Registration failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex-center" style={{ minHeight: '80vh', padding: '1.5rem 1rem' }}>
      <div className="card" style={{ maxWidth: '480px', width: '100%', padding: '2.25rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <div
            className="flex-center"
            style={{
              width: 48,
              height: 48,
              borderRadius: 'var(--radius-md)',
              background: 'var(--primary-light)',
              color: 'var(--primary)',
              margin: '0 auto 1rem',
            }}
          >
            <UserPlus size={24} />
          </div>
          <h2 style={{ fontSize: '1.5rem', marginBottom: '0.4rem' }}>Create an Account</h2>
          <p className="text-muted text-sm">Join NovaTech for fast and secure enterprise shopping</p>
        </div>

        {error && (
          <div className="alert alert-error">
            <AlertCircle size={16} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
            <div className="form-group">
              <label className="form-label">First Name</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  name="firstName"
                  className="form-input"
                  placeholder="John"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                  style={{ paddingLeft: '2.2rem' }}
                />
                <UserIcon
                  size={15}
                  color="var(--text-dim)"
                  style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Last Name</label>
              <input
                type="text"
                name="lastName"
                className="form-input"
                placeholder="Doe"
                value={formData.lastName}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Email Address</label>
            <div style={{ position: 'relative' }}>
              <input
                type="email"
                name="email"
                className="form-input"
                placeholder="name@example.com"
                value={formData.email}
                onChange={handleChange}
                required
                style={{ paddingLeft: '2.2rem' }}
              />
              <Mail
                size={15}
                color="var(--text-dim)"
                style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Phone Number (Optional)</label>
            <div style={{ position: 'relative' }}>
              <input
                type="tel"
                name="phoneNumber"
                className="form-input"
                placeholder="+1 555-0199"
                value={formData.phoneNumber}
                onChange={handleChange}
                style={{ paddingLeft: '2.2rem' }}
              />
              <Phone
                size={15}
                color="var(--text-dim)"
                style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }}
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
            <div className="form-group">
              <label className="form-label">Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="password"
                  name="password"
                  className="form-input"
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  style={{ paddingLeft: '2.2rem' }}
                />
                <Lock
                  size={15}
                  color="var(--text-dim)"
                  style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Confirm Password</label>
              <input
                type="password"
                name="confirmPassword"
                className="form-input"
                placeholder="••••••••"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <button type="submit" disabled={loading} className="btn btn-primary" style={{ width: '100%', marginTop: '0.75rem' }}>
            {loading ? <div className="spinner" style={{ width: 18, height: 18 }} /> : 'Create Account'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--primary)', fontWeight: 600 }}>
            Sign In
          </Link>
        </p>
      </div>
    </div>
  );
};
