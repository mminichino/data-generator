'use client';

import { useEffect, useMemo, useState } from 'react';
import { useAuthStore } from '../store/auth/AuthStore';
import { useRouter } from 'next/navigation';

interface User {
  id: string;
  username: string;
  createdAt: string;
  isDefaultAdmin: boolean;
}

export default function UsersAdminPage() {
  const { currentUser, token, createUser, deleteUser, changePassword } = useAuthStore();
  useRouter();

  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [newUsername, setNewUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [message, setMessage] = useState<{ type: 'success' | 'error' | 'info'; text: string } | null>(null);

  const isAdmin = currentUser?.username === 'admin';

  const fetchUsers = async () => {
    if (!token) return;

    try {
      const response = await fetch('/api/auth/users', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setUsers(data.users || []);
      } else {
        setMessage({ type: 'error', text: 'Failed to load users' });
      }
    } catch (error) {
      console.error('Error fetching users:', error);
      setMessage({ type: 'error', text: 'Error loading users' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAdmin && token) {
      fetchUsers();
    } else {
      setLoading(false);
    }
  }, [isAdmin, token]);

  const userList = useMemo(() =>
          users.map(u => u.username).sort(),
      [users]
  );

  if (loading) {
    return (
        <div className="row">
          <div className="col-12">
            <div className="text-center">
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
            </div>
          </div>
        </div>
    );
  }

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

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);

    const res = await createUser(newUsername, newPassword);
    if (!res.ok) {
      setMessage({ type: 'error', text: res.message || 'Failed to create user' });
    } else {
      setMessage({ type: 'success', text: 'User created successfully' });
      setNewUsername('');
      setNewPassword('');
      fetchUsers();
    }
  };

  const handleDelete = async (email: string) => {
    if (!confirm(`Delete user "${email}"?`)) return;

    setMessage(null);
    const res = await deleteUser(email);

    if (res.ok) {
      setMessage({ type: 'success', text: 'User deleted successfully' });
      fetchUsers();
    } else {
      setMessage({ type: 'error', text: res.message || 'Failed to delete user' });
    }
  };

  const handleChangePassword = async (email: string) => {
    const newPwd = prompt(`Enter new password for ${email}`);
    if (newPwd == null || newPwd.trim() === '') return;

    setMessage(null);

    const res = await changePassword(email, '', newPwd);

    if (res.ok) {
      setMessage({ type: 'success', text: 'Password updated successfully' });
    } else {
      setMessage({ type: 'error', text: res.message || 'Failed to update password' });
    }
  };

  return (
      <div className="row">
        <div className="col-xl-6 col-lg-12">
          <div className="card">
            <div className="card-header d-flex justify-content-between align-items-center">
              <h4 className="card-title">Users</h4>
              <button
                  className="btn btn-sm btn-primary"
                  onClick={fetchUsers}
                  disabled={loading}
              >
                Refresh
              </button>
            </div>
            <div className="card-body">
              {message && (
                  <div className={`alert alert-${message.type === 'success' ? 'success' : message.type === 'error' ? 'danger' : 'info'}`}>
                    {message.text}
                  </div>
              )}
              <div className="table-responsive">
                <table className="table table-responsive-md">
                  <thead>
                  <tr>
                    <th>Username</th>
                    <th>Actions</th>
                  </tr>
                  </thead>
                  <tbody>
                  {userList.length === 0 ? (
                      <tr>
                        <td colSpan={2} className="text-center">No users found</td>
                      </tr>
                  ) : (
                      userList.map((username) => {
                        const user = users.find(u => u.username === username);
                        return (
                            <tr key={username}>
                              <td>
                                {username}
                                {username === 'admin' && (
                                    <span className="badge light badge-primary ms-2" style={{ marginLeft: '1rem' }}>admin</span>
                                )}
                                {username === currentUser?.username && (
                                    <span className="badge light badge-success ms-2" style={{ marginLeft: '1rem' }}>current</span>
                                )}
                                {user?.isDefaultAdmin && (
                                    <span className="badge light badge-warning ms-2" style={{ marginLeft: '1rem' }}>default password</span>
                                )}
                              </td>
                              <td>
                                <button
                                    className="btn btn-sm btn-secondary me-2"
                                    onClick={() => handleChangePassword(username)}
                                >
                                  Change Password
                                </button>
                                <button
                                    className="btn btn-sm btn-danger"
                                    disabled={username === 'admin'}
                                    onClick={() => handleDelete(username)}
                                    style={{ marginLeft: '1rem' }}
                                >
                                  Delete
                                </button>
                              </td>
                            </tr>
                        );
                      })
                  )}
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
                <button
                    className="btn btn-primary"
                    type="submit"
                    disabled={!newUsername || !newPassword}
                >
                  Create User
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
  );
}
