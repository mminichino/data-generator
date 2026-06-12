'use client';

import { useState } from 'react';
import { useSchemaStore } from '../store/SchemaStore';
import { SchemaCollection, TableSchema } from '../types/schema';
import GenerationProgress from '../components/GenerationProgress';
import { useGenerationJob } from '../lib/useGenerationJob';

export default function GeneratePage() {
  const { schemas, connection } = useSchemaStore();
  const [selectedSchema, setSelectedSchema] = useState('');
  const [rowCount, setRowCount] = useState(100);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const { status, isRunning, connectionWarning, startGeneration, cancelGeneration, reset } =
    useGenerationJob();

  const handleGenerate = async () => {
    const schemaCollection = schemas.find((s) => s.id === selectedSchema);
    const type = connection?.type;

    if (!schemaCollection) {
      alert('Please select a schema');
      return;
    }

    if (!type) {
      alert('Please configure a database connection first');
      return;
    }

    const payload: SchemaCollection = {
      ...schemaCollection,
      tables: (schemaCollection.tables || []).map((t: TableSchema) => ({ ...t, count: rowCount })),
    };

    setErrorMessage(null);
    try {
      await startGeneration(type, payload);
    } catch (error) {
      console.error('Error starting generation:', error);
      setErrorMessage(error instanceof Error ? error.message : 'Failed to start generation');
    }
  };

  const handleStop = () => {
    void cancelGeneration();
  };

  const handleDismiss = () => {
    reset();
    setErrorMessage(null);
  };

  return (
    <div className="row">
      <div className="col-lg-12">
        <div className="card">
          <div className="card-header">
            <h4 className="card-title">Generate Test Data</h4>
          </div>
          <div className="card-body">
            <div className="basic-form">
              <div className="row">
                <div className="col-md-6">
                  <div className="form-group mb-3">
                    <label className="form-label">Select Schema</label>
                    <select
                      className="form-control"
                      value={selectedSchema}
                      onChange={(e) => setSelectedSchema(e.target.value)}
                      disabled={isRunning}
                    >
                      <option value="">-- Select a schema --</option>
                      {schemas.map((schema) => (
                        <option key={schema.id} value={schema.id}>
                          {schema.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
                <div className="col-md-6">
                  <div className="form-group mb-3">
                    <label className="form-label">Number of Rows</label>
                    <input
                      type="number"
                      className="form-control"
                      min="1"
                      max="1000000"
                      value={rowCount}
                      onChange={(e) => setRowCount(parseInt(e.target.value, 10) || 1)}
                      disabled={isRunning}
                    />
                  </div>
                </div>
              </div>

              {errorMessage && (
                <div className="alert alert-danger py-2" role="alert">
                  {errorMessage}
                </div>
              )}

              <div className="d-flex gap-2">
                <button
                  onClick={handleGenerate}
                  className="btn btn-primary"
                  disabled={!selectedSchema || isRunning}
                >
                  <i className="fa fa-refresh me-2"></i>
                  {isRunning ? 'Generating…' : 'Generate Data'}
                </button>
                {status && !isRunning && (
                  <button type="button" className="btn btn-outline-secondary" onClick={handleDismiss}>
                    Dismiss
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>

        {status && (
          <GenerationProgress
            status={status}
            connectionWarning={connectionWarning}
            onStop={handleStop}
          />
        )}
      </div>
    </div>
  );
}
