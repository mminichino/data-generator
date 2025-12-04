'use client';

import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export type UserRecord = {
  username: string;
  password: string;
};

interface AuthState {
  users: Record<string, UserRecord>;
  currentUser: string | null;
  hydrated: boolean;
  login: (username: string, password: string) => { ok: boolean; message?: string };
  logout: () => void;
  createUser: (username: string, password: string) => { ok: boolean; message?: string };
  deleteUser: (username: string) => { ok: boolean; message?: string };
  changePassword: (username: string, password: string) => { ok: boolean; message?: string };
}

const isValidUsername = (u: string) => {
  const alnum = /^[a-zA-Z0-9_\-\.]{3,}$/;
  const email = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return alnum.test(u) || email.test(u);
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      users: { 'admin': { username: 'admin', password: 'admin' } },
      currentUser: null,
      hydrated: false,
      login: (username, password) => {
        const u = username.trim();
        const user = get().users[u];
        if (!user) return { ok: false, message: 'User not found' };
        if (user.password !== password) return { ok: false, message: 'Invalid password' };
        set({ currentUser: u });
        return { ok: true };
      },
      logout: () => set({ currentUser: null }),
      createUser: (username, password) => {
        const u = username.trim();
        if (!isValidUsername(u)) return { ok: false, message: 'Invalid username. Use alphanumeric or email.' };
        const users = { ...get().users };
        if (users[u]) return { ok: false, message: 'User already exists' };
        users[u] = { username: u, password };
        set({ users });
        return { ok: true };
      },
      deleteUser: (username) => {
        const u = username.trim();
        if (u === 'admin') return { ok: false, message: 'Cannot delete admin user' };
        const users = { ...get().users };
        if (!users[u]) return { ok: false, message: 'User not found' };
        delete users[u];
        const curr = get().currentUser;
        set({ users, currentUser: curr === u ? null : curr });
        return { ok: true };
      },
      changePassword: (username, password) => {
        const u = username.trim();
        const users = { ...get().users };
        if (!users[u]) return { ok: false, message: 'User not found' };
        users[u] = { ...users[u], password };
        set({ users });
        return { ok: true };
      },
    }),
    {
      name: 'auth-storage',
      onRehydrateStorage: () => (state) => {
        setTimeout(() => {
          const users = state?.users && state.users['admin']
            ? state.users
            : { ...(state?.users || {}), admin: { username: 'admin', password: 'admin' } } as Record<string, UserRecord>;
          (useAuthStore as any).setState({ users, hydrated: true });
        }, 0);
      },
    }
  )
);
