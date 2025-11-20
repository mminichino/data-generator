'use client';

import { useState } from 'react';
import { useSchemaStore } from './store/SchemaStore';
import { SchemaCollection } from './types/schema';
import SchemaCollectionEditor from './components/SchemaCollectionEditor';
import { generateUUID } from './lib/utils';

export default function Home() {
  const { schemas, addSchema, updateSchema, deleteSchema } = useSchemaStore();
  const [editingSchema, setEditingSchema] = useState<SchemaCollection | null>(null);
  const [showEditor, setShowEditor] = useState(false);

  const handleCreateNew = () => {
    const newSchema: SchemaCollection = {
      id: generateUUID(),
      name: '',
      nosql: false,
      tables: [],
    };
    setEditingSchema(newSchema);
    setShowEditor(true);
  };

  const handleEdit = (schema: SchemaCollection) => {
    setEditingSchema(schema);
    setShowEditor(true);
  };

  const handleSave = (schema: SchemaCollection) => {
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
              <SchemaCollectionEditor
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
                          <p className="text-muted mb-3">{schema.tables.length} table(s)</p>
                          {schema.tables.length > 0 && (
                            <div className="table-responsive">
                              <table className="table table-responsive-md">
                                <thead>
                                  <tr>
                                    <th>Table Name</th>
                                    <th>Columns</th>
                                  </tr>
                                </thead>
                                <tbody>
                                  {schema.tables.map((t) => (
                                    <tr key={t.id}>
                                      <td><strong>{t.name}</strong></td>
                                      <td><span className="badge badge-primary">{t.columns.length}</span></td>
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                            </div>
                          )}
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
