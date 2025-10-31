'use client';

import { useState } from 'react';

export default function ConnectionPage() {
  const [connectionString, setConnectionString] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [connected, setConnected] = useState(false);

  const handleConnect = () => {
    if (!connectionString.trim()) {
      alert('Connection string is required');
      return;
    }
    // TODO: Implement actual connection logic
    setConnected(true);
    alert('Connected successfully!');
  };

  const handleDisconnect = () => {
    setConnected(false);
    alert('Disconnected');
  };

  return (
    <div className="row">
      <div className="col-lg-12">
        <div className="card">
          <div className="card-header">
            <h4 className="card-title">Database Connection</h4>
          </div>
          <div className="card-body">
            <div className="basic-form">
              <div className="form-group mb-3">
                <label className="form-label">Connection String</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="jdbc:postgresql://localhost:5432/mydb"
                  value={connectionString}
                  onChange={(e) => setConnectionString(e.target.value)}
                  disabled={connected}
                />
              </div>

              <div className="row">
                <div className="col-md-6">
                  <div className="form-group mb-3">
                    <label className="form-label">Username</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Enter username"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      disabled={connected}
                    />
                  </div>
                </div>
                <div className="col-md-6">
                  <div className="form-group mb-3">
                    <label className="form-label">Password</label>
                    <input
                      type="password"
                      className="form-control"
                      placeholder="Enter password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      disabled={connected}
                    />
                  </div>
                </div>
              </div>

              {connected && (
                <div className="alert alert-success alert-dismissible fade show">
                  <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" className="me-2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <path d="M8 14s1.5 2 4 2 4-2 4-2"></path>
                    <line x1="9" y1="9" x2="9.01" y2="9"></line>
                    <line x1="15" y1="9" x2="15.01" y2="9"></line>
                  </svg>
                  <strong>Connected!</strong> Your database connection is active.
                </div>
              )}

              <div className="mt-4">
                {!connected ? (
                  <button onClick={handleConnect} className="btn btn-primary">
                    <i className="fa fa-plug me-2"></i>Connect
                  </button>
                ) : (
                  <button onClick={handleDisconnect} className="btn btn-danger">
                    <i className="fa fa-times me-2"></i>Disconnect
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
