'use client';

import { useMemo, useState } from 'react';
import { useAuthStore } from '../store/auth/AuthStore';
import { useRouter } from 'next/navigation';

export default function UsersAdminPage() {
  const { users, currentUser, createUser, deleteUser, changePassword } = useAuthStore();
  const router = useRouter();

  // Only admin can access user administration
  const isAdmin = currentUser === 'admin';
  const userList = useMemo(() => Object.values(users).map(u => u.username).sort(), [users]);

  const [newUsername, setNewUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [message, setMessage] = useState<string | null>(null);

  if (!isAdmin) {
    return (
      <div className="row">
        <div className="col-12">
          <div className="alert alert-warning">
            You must be logged in as <strong>admin</strong> to manage users.
          </div>
        </div>
      </div>
    );
  }

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);
    const res = createUser(newUsername, newPassword);
    if (!res.ok) {
      setMessage(res.message || 'Failed to create user');
    } else {
      setMessage('User created');
      setNewUsername('');
      setNewPassword('');
    }
  };

  const handleDelete = (u: string) => {
    if (!confirm(`Delete user "${u}"?`)) return;
    const res = deleteUser(u);
    setMessage(res.ok ? 'User deleted' : res.message || 'Failed to delete');
  };

  const handleChangePassword = (u: string) => {
    const pwd = prompt(`Enter new password for ${u}`);
    if (pwd == null) return;
    const res = changePassword(u, pwd);
    setMessage(res.ok ? 'Password updated' : res.message || 'Failed to update password');
  };

  return (
    <div className="row">
      <div className="col-xl-6 col-lg-12">
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h4 className="card-title">Users</h4>
          </div>
          <div className="card-body">
            {message && <div className="alert alert-info">{message}</div>}
            <div className="table-responsive">
              <table className="table table-responsive-md">
                <thead>
                  <tr>
                    <th>Username</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {userList.map((u) => (
                    <tr key={u}>
                      <td>
                        {u}
                        {u === 'admin' && <span className="badge light badge-primary ms-2" style={{ marginLeft: '1rem' }}>admin</span>}
                        {u === currentUser && <span className="badge light badge-success ms-2" style={{ marginLeft: '1rem' }}>current</span>}
                      </td>
                      <td>
                        <button className="btn btn-sm btn-secondary me-2" onClick={() => handleChangePassword(u)}>
                          Change Password
                        </button>
                        <button
                          className="btn btn-sm btn-danger"
                          disabled={u === 'admin'}
                          onClick={() => handleDelete(u)}
                          style={{ marginLeft: '1rem' }}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <div className="col-xl-6 col-lg-12">
        <div className="card">
          <div className="card-header">
            <h4 className="card-title">Create New User</h4>
          </div>
          <div className="card-body">
            <form className="basic-form" onSubmit={handleCreate}>
              <div className="form-group mb-3">
                <label className="form-label">Username (alphanumeric or email)</label>
                <input
                  className="form-control"
                  value={newUsername}
                  onChange={(e) => setNewUsername(e.target.value)}
                  placeholder="username or user@example.com"
                />
              </div>
              <div className="form-group mb-3">
                <label className="form-label">Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                />
              </div>
              <button className="btn btn-primary" type="submit" disabled={!newUsername || !newPassword}>
                Create User
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
