'use client';

import { useEffect, useState } from 'react';
import { TableSchema, ColumnDefinition } from '../types/schema';
import ColumnEditor from './ColumnEditor';
import { generateUUID } from '../lib/utils';

interface SchemaEditorProps {
  schema: TableSchema;
  onSave: (schema: TableSchema) => void;
  onCancel: () => void;
  nosql?: boolean;
}

export default function SchemaEditor({ schema, onSave, onCancel, nosql = false }: SchemaEditorProps) {
  const [editedSchema, setEditedSchema] = useState<TableSchema>(schema);
  const [editingColumn, setEditingColumn] = useState<ColumnDefinition | null>(null);
  const [showColumnEditor, setShowColumnEditor] = useState(false);

  useEffect(() => {
    if (nosql && !editedSchema.keyFormat && !schema.keyFormat) {
      setEditedSchema((prev) => ({ ...prev, keyFormat: `{{ __table__ }}:{{ __uuid__ }}` }));
    }
  }, [nosql, editedSchema.keyFormat, schema.keyFormat]);

  const handleAddColumn = () => {
    const newColumn: ColumnDefinition = {
      id: generateUUID(),
      name: '',
      type: 'text',
      nullable: false,
    };
    setEditingColumn(newColumn);
    setShowColumnEditor(true);
  };

  const handleEditColumn = (column: ColumnDefinition) => {
    setEditingColumn(column);
    setShowColumnEditor(true);
  };

  const handleSaveColumn = (column: ColumnDefinition) => {
    const existingIndex = editedSchema.columns.findIndex((c) => c.id === column.id);
    if (existingIndex >= 0) {
      const newColumns = [...editedSchema.columns];
      newColumns[existingIndex] = column;
      setEditedSchema({ ...editedSchema, columns: newColumns });
    } else {
      setEditedSchema({
        ...editedSchema,
        columns: [...editedSchema.columns, column],
      });
    }
    setShowColumnEditor(false);
    setEditingColumn(null);
  };

  const handleDeleteColumn = (columnId: string) => {
    setEditedSchema({
      ...editedSchema,
      columns: editedSchema.columns.filter((c) => c.id !== columnId),
    });
  };

  const handleCancelColumn = () => {
    setShowColumnEditor(false);
    setEditingColumn(null);
  };

  const handleSave = () => {
    if (!editedSchema.name.trim()) {
      alert('Table name is required');
      return;
    }
    if (editedSchema.columns.length === 0) {
      alert('At least one column is required');
      return;
    }

    const payload: TableSchema = nosql ? editedSchema : { ...editedSchema, keyFormat: undefined } as TableSchema;
    onSave(payload);
  };

  return (
    <div className="card">
      <div className="card-header">
        <h4 className="card-title">
          {schema.name ? `Edit Table: ${schema.name}` : 'Create New Table'}
        </h4>
      </div>
      <div className="card-body">
        <div className="basic-form">
          <div className="form-group mb-3">
            <label className="form-label">Table Name</label>
            <input
              type="text"
              className="form-control"
              placeholder="Enter table name"
              value={editedSchema.name}
              onChange={(e) =>
                setEditedSchema({ ...editedSchema, name: e.target.value })
              }
            />
          </div>

          {nosql && (
            <div className="form-group mb-3">
              <label className="form-label">Key Format</label>
              <input
                type="text"
                className="form-control"
                placeholder="{{ __table__ }}:{{ __uuid__ }}"
                value={editedSchema.keyFormat || ''}
                onChange={(e) => setEditedSchema({ ...editedSchema, keyFormat: e.target.value })}
              />
              <small className="form-text text-muted">Default: {'{{'} __table__ {'}}'}:{'{{'} __uuid__ {'}}'}</small>
            </div>
          )}

          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5>Columns</h5>
            <button
              onClick={handleAddColumn}
              className="btn btn-primary btn-sm"
            >
              <i className="fa fa-plus me-2"></i>Add Column
            </button>
          </div>

          {showColumnEditor && editingColumn ? (
            <div className="mb-4">
              <ColumnEditor
                column={editingColumn}
                onSave={handleSaveColumn}
                onCancel={handleCancelColumn}
              />
            </div>
          ) : null}

          {editedSchema.columns.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-responsive-md">
                <thead>
                  <tr>
                    <th>Column Name</th>
                    <th>Type</th>
                    <th>Nullable</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {editedSchema.columns.map((column) => (
                    <tr key={column.id}>
                      <td><strong>{column.name}</strong></td>
                      <td><span className="badge badge-primary">{column.type}</span></td>
                      <td>
                        {column.nullable ? (
                          <span className="badge badge-success">Yes</span>
                        ) : (
                          <span className="badge badge-danger">No</span>
                        )}
                      </td>
                      <td>
                        <button
                          onClick={() => handleEditColumn(column)}
                          className="btn btn-warning btn-sm me-2"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => {
                            if (confirm(`Delete column "${column.name}"?`)) {
                              handleDeleteColumn(column.id);
                            }
                          }}
                          className="btn btn-danger btn-sm"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="alert alert-info">
              No columns added yet. Click "Add Column" to get started.
            </div>
          )}

          <div className="mt-4">
            <button onClick={handleSave} className="btn btn-success me-2">
              Save Table
            </button>
            <button onClick={onCancel} className="btn btn-secondary">
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
