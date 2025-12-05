import { create } from 'zustand';
import { SchemaCollection, DatabaseConnection } from '../types/schema';
import { useAuthStore } from './auth/AuthStore';

interface SchemaStoreState {
    schemas: SchemaCollection[];
    connection: DatabaseConnection | null;
    loading: boolean;
    error: string | null;
}

interface SchemaStoreActions {
    loadFromServer: () => Promise<void>;
    addSchema: (schema: SchemaCollection) => Promise<void>;
    updateSchema: (id: string, schema: SchemaCollection) => Promise<void>;
    deleteSchema: (id: string) => Promise<void>;
    setConnection: (connection: DatabaseConnection) => Promise<void>;
    clearLocal: () => void;
}

export const useSchemaStore = create<SchemaStoreState & SchemaStoreActions>()((set, get) => ({
    schemas: [],
    connection: null,
    loading: false,
    error: null,

    loadFromServer: async () => {
        const token = useAuthStore.getState().token;
        if (!token) {
            set({ schemas: [], connection: null, error: null, loading: false });
            return;
        }
        try {
            set({ loading: true, error: null });
            const [schemasRes, connRes] = await Promise.all([
                fetch('/api/user/schemas', { headers: { Authorization: `Bearer ${token}` } }),
                fetch('/api/user/connection', { headers: { Authorization: `Bearer ${token}` } }),
            ]);

            const schemasJson = await schemasRes.json();
            const connJson = await connRes.json();

            if (!schemasRes.ok) throw new Error(schemasJson.error || 'Failed to load schemas');
            if (!connRes.ok && connRes.status !== 404) throw new Error(connJson.error || 'Failed to load connection');

            set({ schemas: schemasJson.schemas ?? [], connection: connRes.ok ? connJson.connection : null, loading: false, error: null });
        } catch (e: any) {
            set({ loading: false, error: e?.message || 'Failed to load', schemas: [], connection: null });
        }
    },

    addSchema: async (schema) => {
        const token = useAuthStore.getState().token;
        if (!token) return;
        const prev = get().schemas;
        set({ schemas: [...prev, schema] });
        const res = await fetch('/api/user/schemas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
            body: JSON.stringify({ schema }),
        });
        if (!res.ok) {
            set({ schemas: prev });
        }
    },

    updateSchema: async (id, schema) => {
        const token = useAuthStore.getState().token;
        if (!token) return;
        const prev = get().schemas;
        set({ schemas: prev.map((s) => (s.id === id ? schema : s)) });
        const res = await fetch(`/api/user/schemas/${encodeURIComponent(id)}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
            body: JSON.stringify({ schema }),
        });
        if (!res.ok) {
            set({ schemas: prev });
        }
    },

    deleteSchema: async (id) => {
        const token = useAuthStore.getState().token;
        if (!token) return;
        const prev = get().schemas;
        set({ schemas: prev.filter((s) => s.id !== id) });
        const res = await fetch(`/api/user/schemas/${encodeURIComponent(id)}`, {
            method: 'DELETE',
            headers: { Authorization: `Bearer ${token}` },
        });
        if (!res.ok) {
            set({ schemas: prev });
        }
    },

    setConnection: async (connection) => {
        const token = useAuthStore.getState().token;
        if (!token) return;
        const prev = get().connection;
        set({ connection });
        const res = await fetch('/api/user/connection', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
            body: JSON.stringify({ connection }),
        });
        if (!res.ok) {
            set({ connection: prev });
        }
    },

    clearLocal: () => set({ schemas: [], connection: null, error: null, loading: false }),
}));

useAuthStore.subscribe((auth) => {
    if (auth.currentUser && auth.token) {
        useSchemaStore.getState().loadFromServer().then();
    } else {
        useSchemaStore.getState().clearLocal();
    }
});
