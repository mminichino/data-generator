'use client';

import { useState } from 'react';
import { useSchemaStore } from './store/SchemaStore';
import { TableSchema, ColumnDefinition } from './types/schema';
import SchemaEditor from './components/SchemaEditor';
import { generateUUID } from './lib/utils';

export default function Home() {
  const { schemas, addSchema, updateSchema, deleteSchema } = useSchemaStore();
  const [editingSchema, setEditingSchema] = useState<TableSchema | null>(null);
  const [showEditor, setShowEditor] = useState(false);

  const handleCreateNew = () => {
    const newSchema: TableSchema = {
      id: generateUUID(),
      name: '',
      columns: [],
    };
    setEditingSchema(newSchema);
    setShowEditor(true);
  };

  const handleEdit = (schema: TableSchema) => {
    setEditingSchema(schema);
    setShowEditor(true);
  };

  const handleSave = (schema: TableSchema) => {
    if (schemas.find((s) => s.id === schema.id)) {
      updateSchema(schema.id, schema);
    } else {
      addSchema(schema);
    }
    setShowEditor(false);
    setEditingSchema(null);
  };

  const handleCancel = () => {
    setShowEditor(false);
    setEditingSchema(null);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Table Schemas</h1>
        <button
          onClick={handleCreateNew}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Create New Schema
        </button>
      </div>

      {showEditor && editingSchema ? (
        <SchemaEditor
          schema={editingSchema}
          onSave={handleSave}
          onCancel={handleCancel}
        />
      ) : (
        <div className="space-y-4">
          {schemas.length === 0 ? (
            <div className="text-center py-12 text-gray-800">
              No schemas created yet. Click "Create New Schema" to get started.
            </div>
          ) : (
            schemas.map((schema) => (
              <div
                key={schema.id}
                className="border rounded-lg p-4 bg-white shadow"
              >
                <div className="flex justify-between items-start">
                  <div>
                    <h2 className="text-xl font-semibold text-gray-800">{schema.name}</h2>
                    <p className="text-gray-800 mt-1">
                      {schema.columns.length} column(s)
                    </p>
                    <div className="mt-2 space-y-1">
                      {schema.columns.map((col) => (
                        <div key={col.id} className="text-sm text-gray-800">
                          <span className="font-medium">{col.name}</span>
                          <span className="text-gray-800"> - {col.type}</span>
                          {!col.nullable && (
                            <span className="text-red-500 ml-1">*</span>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleEdit(schema)}
                      className="bg-green-600 text-white px-3 py-1 rounded hover:bg-green-700"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => {
                        if (
                          confirm(
                            `Are you sure you want to delete "${schema.name}"?`
                          )
                        ) {
                          deleteSchema(schema.id);
                        }
                      }}
                      className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
