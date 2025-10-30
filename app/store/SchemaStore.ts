import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { TableSchema, DatabaseConnection } from '../types/schema';

interface SchemaStore {
    schemas: TableSchema[];
    connection: DatabaseConnection | null;
    addSchema: (schema: TableSchema) => void;
    updateSchema: (id: string, schema: TableSchema) => void;
    deleteSchema: (id: string) => void;
    setConnection: (connection: DatabaseConnection) => void;
    clearSchemas: () => void;
}

export const useSchemaStore = create<SchemaStore>()(
    persist(
        (set) => ({
            schemas: [],
            connection: null,
            addSchema: (schema) =>
                set((state) => ({ schemas: [...state.schemas, schema] })),
            updateSchema: (id, schema) =>
                set((state) => ({
                    schemas: state.schemas.map((s) => (s.id === id ? schema : s)),
                })),
            deleteSchema: (id) =>
                set((state) => ({
                    schemas: state.schemas.filter((s) => s.id !== id),
                })),
            setConnection: (connection) => set({ connection }),
            clearSchemas: () => set({ schemas: [] }),
        }),
        {
            name: 'schema-storage',
        }
    )
);
