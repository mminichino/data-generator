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
    <div className="row">
      <div className="col-12">
        <div className="card">
          <div className="card-header">
            <h4 className="card-title">Table Schemas</h4>
            <button
              onClick={handleCreateNew}
              className="btn btn-primary"
            >
              + Create New Schema
            </button>
          </div>
          <div className="card-body">
            {showEditor && editingSchema ? (
              <SchemaEditor
                schema={editingSchema}
                onSave={handleSave}
                onCancel={handleCancel}
              />
            ) : (
              <div className="row">
                {schemas.length === 0 ? (
                  <div className="col-12">
                    <div className="text-center py-5">
                      <p className="text-muted">No schemas created yet. Click "Create New Schema" to get started.</p>
                    </div>
                  </div>
                ) : (
                  schemas.map((schema) => (
                    <div key={schema.id} className="col-xl-6 col-lg-12">
                      <div className="card">
                        <div className="card-header d-flex justify-content-between align-items-center">
                          <h5 className="card-title">{schema.name}</h5>
                          <div>
                            <button
                              onClick={() => handleEdit(schema)}
                              className="btn btn-success btn-sm me-2"
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
                              className="btn btn-danger btn-sm"
                            >
                              Delete
                            </button>
                          </div>
                        </div>
                        <div className="card-body">
                          <p className="text-muted mb-3">
                            {schema.columns.length} column(s)
                          </p>
                          <div className="table-responsive">
                            <table className="table table-responsive-md">
                              <thead>
                                <tr>
                                  <th>Column Name</th>
                                  <th>Type</th>
                                  <th>Required</th>
                                </tr>
                              </thead>
                              <tbody>
                                {schema.columns.map((col) => (
                                  <tr key={col.id}>
                                    <td><strong>{col.name}</strong></td>
                                    <td><span className="badge badge-primary">{col.type}</span></td>
                                    <td>
                                      {!col.nullable ? (
                                        <span className="badge badge-danger">Required</span>
                                      ) : (
                                        <span className="badge badge-secondary">Optional</span>
                                      )}
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
