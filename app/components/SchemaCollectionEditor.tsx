'use client';

import { useState } from 'react';
import { SchemaCollection, TableSchema, ColumnDefinition } from '../types/schema';
import SchemaEditor from './SchemaEditor';
import { generateUUID } from '../lib/utils';

interface SchemaCollectionEditorProps {
  schema: SchemaCollection;
  onSave: (schema: SchemaCollection) => void;
  onCancel: () => void;
}

export default function SchemaCollectionEditor({ schema, onSave, onCancel }: SchemaCollectionEditorProps) {
  const [edited, setEdited] = useState<SchemaCollection>({
    ...schema,
    tables: schema.tables || [],
  });

  const [editingTable, setEditingTable] = useState<TableSchema | null>(null);
  const [showTableEditor, setShowTableEditor] = useState(false);
  const [sampledTableId, setSampledTableId] = useState<string | null>(null);
  const [samples, setSamples] = useState<any[]>([]);

  const handleAddTable = () => {
    const newTable: TableSchema = {
      id: generateUUID(),
      name: '',
      columns: [],
    };
    setEditingTable(newTable);
    setShowTableEditor(true);
  };

  const handleEditTable = (table: TableSchema) => {
    setEditingTable(table);
    setShowTableEditor(true);
  };

  const handleSaveTable = (table: TableSchema) => {
    const idx = edited.tables.findIndex(t => t.id === table.id);
    if (idx >= 0) {
      const newTables = [...edited.tables];
      newTables[idx] = table;
      setEdited({ ...edited, tables: newTables });
    } else {
      setEdited({ ...edited, tables: [...edited.tables, table] });
    }
    setShowTableEditor(false);
    setEditingTable(null);
  };

  const handleDeleteTable = (id: string) => {
    setEdited({ ...edited, tables: edited.tables.filter(t => t.id !== id) });
  };

  const handleCancelTable = () => {
    setShowTableEditor(false);
    setEditingTable(null);
  };

  const handleSave = () => {
    if (!edited.name.trim()) {
      alert('Schema name is required');
      return;
    }
    if (edited.tables.length === 0) {
      alert('Please add at least one table');
      return;
    }
    onSave(edited);
  };

  const handleSample = async (table: TableSchema) => {
    try {
      setSampledTableId(table.id);
      setSamples([]);
      const collectionForSample: SchemaCollection = {
        id: edited.id,
        name: edited.name,
        nosql: edited.nosql,
        tables: [table],
      };
      const resp = await fetch('/api/generate?sample=true', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(collectionForSample),
      });
      if (!resp.ok) {
        const err = await resp.json().catch(() => ({}));
        throw new Error(err?.message || 'Failed to get samples');
      }
      const json = await resp.json();
      setSamples(json.samples || []);
    } catch (e) {
      console.error('Sample error', e);
      alert('Failed to generate samples');
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <h4 className="card-title">{schema.name ? `Edit Schema: ${schema.name}` : 'Create New Schema'}</h4>
      </div>
      <div className="card-body">
        <div className="basic-form">
          <div className="row">
            <div className="col-md-6">
              <div className="form-group mb-3">
                <label className="form-label">Schema Name</label>
                <input
                  type="text"
                  className="form-control"
                  value={edited.name}
                  onChange={(e) => setEdited({ ...edited, name: e.target.value })}
                />
              </div>
            </div>
            <div className="col-md-3">
              <div className="form-check mt-4">
                <input
                  type="checkbox"
                  className="form-check-input"
                  id={`nosql-${edited.id}`}
                  checked={edited.nosql}
                  onChange={(e) => {
                    const checked = e.target.checked;
                    // When enabling NoSQL, prefill default keyFormat for tables missing one
                    let updatedTables = edited.tables;
                    if (checked) {
                      updatedTables = (edited.tables || []).map(t => ({
                        ...t,
                        keyFormat: t.keyFormat || `'${t.name}', $uuid`,
                      }));
                    }
                    setEdited({
                      ...edited,
                      nosql: checked,
                      tables: updatedTables,
                    });
                  }}
                />
                <label className="form-check-label" htmlFor={`nosql-${edited.id}`}>NoSQL</label>
              </div>
            </div>
          </div>

          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5>Tables</h5>
            <button onClick={handleAddTable} className="btn btn-primary btn-sm">
              <i className="fa fa-plus me-2"></i>Add Table
            </button>
          </div>

          {showTableEditor && editingTable ? (
            <div className="mb-4">
              <SchemaEditor
                schema={editingTable}
                nosql={edited.nosql}
                onSave={handleSaveTable}
                onCancel={handleCancelTable}
              />
            </div>
          ) : null}

          {edited.tables.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-responsive-md">
                <thead>
                  <tr>
                    <th>Table Name</th>
                    <th>Columns</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {edited.tables.map((t) => (
                    <tr key={t.id}>
                      <td><strong>{t.name}</strong></td>
                      <td>{t.columns.length}</td>
                      <td>
                        <button onClick={() => handleEditTable(t)} className="btn btn-warning btn-sm me-2">Edit</button>
                        <button onClick={() => handleSample(t)} className="btn btn-info btn-sm me-2">Sample</button>
                        <button onClick={() => { if (confirm(`Delete table "${t.name}"?`)) { handleDeleteTable(t.id); } }} className="btn btn-danger btn-sm">Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="alert alert-info">No tables added yet.</div>
          )}

          <div className="mt-4">
            <button onClick={handleSave} className="btn btn-success me-2">Save Schema</button>
            <button onClick={onCancel} className="btn btn-secondary">Cancel</button>
          </div>

          {samples && samples.length > 0 && (
            <div className="card mt-4">
              <div className="card-header">
                <h5 className="card-title mb-0">Sample Rows {sampledTableId ? `(10)` : ''}</h5>
              </div>
              <div className="card-body">
                <div className="table-responsive">
                  <table className="table table-responsive-md">
                    <thead>
                      <tr>
                        {Object.keys(samples[0] || {}).map((key) => (
                          <th key={key}>{key}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {samples.map((row, idx) => (
                        <tr key={idx}>
                          {Object.values(row).map((v: any, i) => (
                            <td key={i}>{String(v)}</td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
