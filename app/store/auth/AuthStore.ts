'use client';

import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export type UserRecord = {
    id: string;
    username: string;
};

interface AuthState {
    currentUser: UserRecord | null;
    token: string | null;
    hydrated: boolean;
    login: (username: string, password: string) => Promise<{ ok: boolean; message?: string }>;
    logout: () => Promise<{ ok: boolean; message?: string }>;
    createUser: (username: string, password: string) => Promise<{ ok: boolean; message?: string }>;
    deleteUser: (username: string) => Promise<{ ok: boolean; message?: string }>;
    changePassword: (username: string, oldPassword: string, newPassword: string) => Promise<{ ok: boolean; message?: string }>;
    verifySession: () => Promise<boolean>;
}

const isValidUsername = (u: string) => {
    const alnum = /^[a-zA-Z0-9_\-.]{3,}$/;
    const email = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return alnum.test(u) || email.test(u);
};

export const useAuthStore = create<AuthState>()(
    persist(
        (set, get) => ({
            currentUser: null,
            token: null,
            hydrated: false,

            login: async (username, password) => {
                const u = username.trim();

                try {
                    const response = await fetch('/api/auth/login', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ username: u, password }),
                    });

                    const data = await response.json();

                    if (!response.ok) {
                        return { ok: false, message: data.error || 'Login failed' };
                    }

                    set({
                        currentUser: { id: data.user.id, username: data.user.username },
                        token: data.token
                    });

                    return { ok: true };
                } catch (error) {
                    console.error('Login error:', error);
                    return { ok: false, message: 'Network error' };
                }
            },

            logout: async () => {
                const token = get().token;

                try {
                    if (token) {
                        await fetch('/api/auth/logout', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ token }),
                        });
                    }

                    set({ currentUser: null, token: null });
                    return { ok: true };
                } catch (error) {
                    console.error('Logout error:', error);
                    set({ currentUser: null, token: null });
                    return { ok: true };
                }
            },

            createUser: async (username, password) => {
                const u = username.trim();

                if (!isValidUsername(u)) {
                    return { ok: false, message: 'Invalid username. Use alphanumeric or email.' };
                }

                try {
                    const response = await fetch('/api/auth/register', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ username: u, password }),
                    });

                    const data = await response.json();

                    if (!response.ok) {
                        return { ok: false, message: data.error || 'Registration failed' };
                    }

                    return { ok: true, message: 'User created successfully' };
                } catch (error) {
                    console.error('Registration error:', error);
                    return { ok: false, message: 'Network error' };
                }
            },

            deleteUser: async (username) => {
                const u = username.trim();
                const token = get().token;

                if (u === 'admin') {
                    return { ok: false, message: 'Cannot delete admin user' };
                }

                if (!token) {
                    return { ok: false, message: 'Not authenticated' };
                }

                try {
                    const response = await fetch('/api/auth/delete', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${token}`
                        },
                        body: JSON.stringify({ username: u }),
                    });

                    const data = await response.json();

                    if (!response.ok) {
                        return { ok: false, message: data.error || 'Delete failed' };
                    }

                    if (get().currentUser?.username === u) {
                        set({ currentUser: null, token: null });
                    }

                    return { ok: true };
                } catch (error) {
                    console.error('Delete error:', error);
                    return { ok: false, message: 'Network error' };
                }
            },

            changePassword: async (username, oldPassword, newPassword) => {
                const u = username.trim();
                const token = get().token;

                if (!token) {
                    return { ok: false, message: 'Not authenticated' };
                }

                try {
                    const response = await fetch('/api/auth/change-password', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${token}`
                        },
                        body: JSON.stringify({ username: u, oldPassword, newPassword }),
                    });

                    const data = await response.json();

                    if (!response.ok) {
                        return { ok: false, message: data.error || 'Password change failed' };
                    }

                    return { ok: true, message: 'Password changed successfully' };
                } catch (error) {
                    console.error('Change password error:', error);
                    return { ok: false, message: 'Network error' };
                }
            },

            verifySession: async () => {
                const token = get().token;

                if (!token) {
                    set({ currentUser: null });
                    return false;
                }

                try {
                    const response = await fetch('/api/auth/verify', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ token }),
                    });

                    if (!response.ok) {
                        set({ currentUser: null, token: null });
                        return false;
                    }

                    return true;
                } catch (error) {
                    console.error('Verify session error:', error);
                    set({ currentUser: null, token: null });
                    return false;
                }
            },
        }),
        {
            name: 'auth-storage',
            partialize: (state) => ({
                currentUser: state.currentUser,
                token: state.token,
            }),
            onRehydrateStorage: () => (state) => {
                setTimeout(() => {
                    useAuthStore.setState({ hydrated: true });
                    if (state?.token) {
                        state.verifySession();
                    }
                }, 0);
            },
        }
    )
);
