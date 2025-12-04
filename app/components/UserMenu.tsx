'use client';

import { useState } from 'react';
import { useAuthStore } from '../store/auth/AuthStore';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

export default function UserMenu() {
  const currentUser = useAuthStore((s) => s.currentUser);
  const logout = useAuthStore((s) => s.logout);
  const [open, setOpen] = useState(false);
  const router = useRouter();

  if (!currentUser) {
    return (
      <Link className="btn btn-outline-primary" href="/login">
        Login
      </Link>
    );
  }

  return (
    <div className="dropdown position-relative">
      <button
        className="btn btn-outline-secondary dropdown-toggle"
        onClick={() => setOpen((o) => !o)}
        type="button"
      >
        {currentUser.username}
      </button>
      <div
        className={`dropdown-menu dropdown-menu-end ${open ? 'show' : ''}`}
        style={{ position: 'absolute', right: 0, left: 'auto' }}
      >
        <Link className="dropdown-item" href="/users" onClick={() => setOpen(false)}>
          User Administration
        </Link>
        <div className="dropdown-divider"></div>
        <button
          className="dropdown-item text-danger"
          onClick={() => {
            logout();
            setOpen(false);
            router.replace('/login');
          }}
        >
          Logout
        </button>
      </div>
    </div>
  );
}
