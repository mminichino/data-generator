'use client';

import { useEffect, useMemo, useState } from 'react';

type DbType = 'redis' | 'postgres';

export default function ConnectionPage() {
  // Form state
  const [type, setType] = useState<DbType>('redis');
  const [hostname, setHostname] = useState<string>('localhost');
  const [port, setPort] = useState<string>('6379');
  const [username, setUsername] = useState<string>('default');
  const [password, setPassword] = useState<string>('');
  const [database, setDatabase] = useState<string>('0');
  const [schema, setSchema] = useState<string>('');
  const [useSsl, setUseSsl] = useState<boolean>(false);

  // Connection state
  const [connected, setConnected] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [statusChecking, setStatusChecking] = useState<boolean>(false);

  // Indicator color based on connection (use Bootstrap colors for visibility)
  const indicatorColor = useMemo(() => (connected ? 'bg-success' : 'bg-secondary'), [connected]);

  // When type changes, pre-populate fields per requirements
  useEffect(() => {
    if (type === 'redis') {
      setPort('6379');
      setUsername('default');
      setDatabase('0');
      setSchema('');
    } else if (type === 'postgres') {
      setPort('5432');
      setUsername('postgres');
      // Leave database empty for postgres by default
      setDatabase('');
      setSchema('public');
    }
  }, [type]);

  // Check connection status on mount
  useEffect(() => {
    const checkStatus = async () => {
      try {
        setStatusChecking(true);
        const res = await fetch('/api/database/status');
        if (res.ok) {
          const json = await res.json();
          setConnected(Boolean(json?.connected));
        }
      } catch (e) {
        // ignore, keep default disconnected
        console.error('Failed to fetch status', e);
      } finally {
        setStatusChecking(false);
      }
    };
    checkStatus();
  }, []);

  const handleConnect = async () => {
    setLoading(true);
    try {
      const payload = {
        type,
        hostname: hostname,
        port: Number(port),
        username,
        password,
        database,
        schema,
        useSsl,
      };
      const res = await fetch('/api/database/connect', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        console.error('Failed to connect:', err);
        throw new Error(err?.message || 'Failed to connect');
      }
      setConnected(true);
    } catch (e: any) {
      alert(e?.message || 'Failed to connect');
      setConnected(false);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="row">
      <div className="col-lg-12">
        <div className="card">
          <div className="card-header d-flex align-items-center justify-content-start gap-2">
            <h4 className="card-title m-0 me-2">Database Connection</h4>
            <span
              className={`d-inline-block rounded-circle ${indicatorColor}`}
              style={{ width: 10, height: 10 }}
              aria-label={connected ? 'Connected' : 'Disconnected'}
            />
            {statusChecking && (
              <span className="text-muted small ms-2">Checking status...</span>
            )}
          </div>
          <div className="card-body">
            <div className="basic-form">
              <div className="row">
                <div className="col-md-4">
                  <div className="form-group mb-3">
                    <label className="form-label">Type</label>
                    <select
                      className="form-control"
                      value={type}
                      onChange={(e) => setType(e.target.value as DbType)}
                      disabled={connected || loading}
                    >
                      <option value="redis">redis</option>
                      <option value="postgres">postgres</option>
                    </select>
                  </div>
                </div>
                <div className="col-md-8">
                  <div className="form-group mb-3">
                    <label className="form-label">Hostname</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="localhost"
                      value={hostname}
                      onChange={(e) => setHostname(e.target.value)}
                      disabled={connected || loading}
                    />
                  </div>
                </div>
              </div>

              <div className="row">
                <div className="col-md-4">
                  <div className="form-group mb-3">
                    <label className="form-label">Port</label>
                    <input
                      type="number"
                      className="form-control"
                      placeholder={type === 'redis' ? '6379' : '5432'}
                      value={port}
                      onChange={(e) => setPort(e.target.value)}
                      disabled={connected || loading}
                    />
                  </div>
                </div>
                <div className="col-md-4">
                  <div className="form-group mb-3">
                    <label className="form-label">Username</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder={type === 'redis' ? 'default' : 'postgres'}
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      disabled={connected || loading}
                    />
                  </div>
                </div>
                <div className="col-md-4">
                  <div className="form-group mb-3">
                    <label className="form-label">Password</label>
                    <input
                      type="password"
                      className="form-control"
                      placeholder="Enter password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      disabled={connected || loading}
                    />
                  </div>
                </div>
              </div>

              <div className="row">
                <div className="col-md-4">
                  <div className="form-group mb-3">
                    <label className="form-label">Database</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder={type === 'redis' ? '0' : ''}
                      value={database}
                      onChange={(e) => setDatabase(e.target.value)}
                      disabled={connected || loading}
                    />
                  </div>
                </div>
                <div className="col-md-4">
                  <div className="form-group mb-3">
                    <label className="form-label">Schema</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder=""
                      value={schema}
                      onChange={(e) => setSchema(e.target.value)}
                      disabled={connected || loading}
                    />
                  </div>
                </div>
                <div className="col-md-4 d-flex align-items-end">
                  <div className="form-group mb-3 form-check">
                    <input
                      id="useSsl"
                      type="checkbox"
                      className="form-check-input me-2"
                      checked={useSsl}
                      onChange={(e) => setUseSsl(e.target.checked)}
                      disabled={connected || loading}
                    />
                    <label htmlFor="useSsl" className="form-check-label">UseSSL</label>
                  </div>
                </div>
              </div>

              <div className="mt-4">
                <button
                  onClick={handleConnect}
                  className="btn btn-primary"
                  disabled={connected || loading}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                      Connecting...
                    </>
                  ) : (
                    <>
                      <i className="fa fa-plug me-2"></i>
                      Connect
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
