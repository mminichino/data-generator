import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { SchemaCollection, DatabaseConnection } from '../types/schema';
import { useAuthStore } from './auth/AuthStore';

// We persist a map keyed by username to keep state separate per user
type PerUserState = Record<string, { schemas: SchemaCollection[]; connection: DatabaseConnection | null }>;

interface SchemaStore {
    // derived getters for current user
    schemas: SchemaCollection[];
    connection: DatabaseConnection | null;
    addSchema: (schema: SchemaCollection) => void;
    updateSchema: (id: string, schema: SchemaCollection) => void;
    deleteSchema: (id: string) => void;
    setConnection: (connection: DatabaseConnection) => void;
    clearSchemas: () => void;
}

interface InternalState {
    byUser: PerUserState;
    currentUser: string | null;
}

const initialPerUser = (): PerUserState => ({
    // lazily filled when a user logs in
});

export const useSchemaStore = create<SchemaStore & InternalState>()(
    persist(
        (set, get) => ({
            byUser: initialPerUser(),
            currentUser: useAuthStore.getState().currentUser,
            get schemas() {
                const user = get().currentUser;
                if (!user) return [];
                return get().byUser[user]?.schemas ?? [];
            },
            get connection() {
                const user = get().currentUser;
                if (!user) return null;
                return get().byUser[user]?.connection ?? null;
            },
            addSchema: (schema) =>
                set((state) => {
                    const user = state.currentUser;
                    if (!user) return state;
                    const existing = state.byUser[user] ?? { schemas: [], connection: null };
                    return { byUser: { ...state.byUser, [user]: { ...existing, schemas: [...existing.schemas, schema] } } } as any;
                }),
            updateSchema: (id, schema) =>
                set((state) => {
                    const user = state.currentUser;
                    if (!user) return state;
                    const existing = state.byUser[user] ?? { schemas: [], connection: null };
                    return {
                        byUser: {
                            ...state.byUser,
                            [user]: {
                                ...existing,
                                schemas: existing.schemas.map((s) => (s.id === id ? schema : s)),
                            },
                        },
                    } as any;
                }),
            deleteSchema: (id) =>
                set((state) => {
                    const user = state.currentUser;
                    if (!user) return state;
                    const existing = state.byUser[user] ?? { schemas: [], connection: null };
                    return {
                        byUser: {
                            ...state.byUser,
                            [user]: {
                                ...existing,
                                schemas: existing.schemas.filter((s) => s.id !== id),
                            },
                        },
                    } as any;
                }),
            setConnection: (connection) =>
                set((state) => {
                    const user = state.currentUser;
                    if (!user) return state;
                    const existing = state.byUser[user] ?? { schemas: [], connection: null };
                    return { byUser: { ...state.byUser, [user]: { ...existing, connection } } } as any;
                }),
            clearSchemas: () =>
                set((state) => {
                    const user = state.currentUser;
                    if (!user) return state;
                    const existing = state.byUser[user] ?? { schemas: [], connection: null };
                    return { byUser: { ...state.byUser, [user]: { ...existing, schemas: [] } } } as any;
                }),
        }),
        {
            name: 'schema-collection-storage-by-user',
            partialize: (state) => ({ byUser: state.byUser }),
        }
    )
);

// Mirror currentUser from AuthStore into SchemaStore so subscribers update on login/logout
useAuthStore.subscribe((auth) => {
    (useSchemaStore as any).setState({ currentUser: auth.currentUser });
});
