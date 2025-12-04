'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '../store/auth/AuthStore';

export default function LoginPage() {
  const router = useRouter();
  const login = useAuthStore((s) => s.login);
  const currentUser = useAuthStore((s) => s.currentUser);
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    router.prefetch('/');
  }, [router]);

  useEffect(() => {
    if (currentUser) {
      router.replace('/');
    }
  }, [currentUser, router]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const res = await login(username, password);

      if (!res.ok) {
        setError(res.message || 'Login failed');
        return;
      }

      router.push('/');
    } catch (err) {
      console.error('Login error:', err);
      setError('An unexpected error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card">
            <div className="card-header">
              <h4 className="card-title">Login</h4>
            </div>
            <div className="card-body">
              <form onSubmit={onSubmit} className="basic-form">
                <div className="form-group mb-3">
                  <label className="form-label">Username or Email</label>
                  <input
                      className="form-control"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      placeholder="admin or user@example.com"
                      disabled={loading}
                      autoFocus
                  />
                </div>
                <div className="form-group mb-3">
                  <label className="form-label">Password</label>
                  <input
                      type="password"
                      className="form-control"
                      onChange={(e) => setPassword(e.target.value)}
                      disabled={loading}
                  />
                </div>
                {error && <div className="alert alert-danger mb-3">{error}</div>}
                <button className="btn btn-primary" type="submit" disabled={loading}>
                  {loading ? 'Signing in...' : 'Login'}
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
  );
}
