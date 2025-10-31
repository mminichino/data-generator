'use client';

import { useState } from 'react';
import { useSchemaStore } from '../store/SchemaStore';

export default function GeneratePage() {
  const { schemas, connection } = useSchemaStore();
  const [selectedSchema, setSelectedSchema] = useState('');
  const [rowCount, setRowCount] = useState(100);
  const [generatedData, setGeneratedData] = useState<any[]>([]);

  const handleGenerate = async () => {
    const schema = schemas.find(s => s.id === selectedSchema);

    if (!schema) {
      alert('Please select a schema');
      return;
    }

    try {
      const response = await fetch('/api/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          schema: schema,
          rowCount,
          connection,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to generate data');
      }

      const result = await response.json();
      const tableData = result.data[schema.name];

      if (!tableData || !Array.isArray(tableData)) {
        throw new Error('Invalid data format received from server');
      }

      setGeneratedData(tableData);
      alert(`Generated ${tableData.length} rows successfully!`);
    } catch (error) {
      console.error('Error generating data:', error);
      alert('Failed to generate data');
    }
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
                      max="10000"
                      value={rowCount}
                      onChange={(e) => setRowCount(parseInt(e.target.value))}
                    />
                  </div>
                </div>
              </div>

              <button
                onClick={handleGenerate}
                className="btn btn-primary"
                disabled={!selectedSchema}
              >
                <i className="fa fa-refresh me-2"></i>Generate Data
              </button>
            </div>
          </div>
        </div>

        {generatedData.length > 0 && (
          <div className="card mt-4">
            <div className="card-header">
              <h4 className="card-title">Generated Data Preview</h4>
              <span className="badge badge-primary">{generatedData.length} rows</span>
            </div>
            <div className="card-body">
              <div className="table-responsive">
                <table className="table table-responsive-md">
                  <thead>
                    <tr>
                      {Object.keys(generatedData[0] || {}).map((key) => (
                        <th key={key}>{key}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {generatedData.slice(0, 10).map((row, index) => (
                      <tr key={index}>
                        {Object.values(row).map((value: any, i) => (
                          <td key={i}>{String(value)}</td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
                {generatedData.length > 10 && (
                  <div className="text-center mt-3">
                    <small className="text-muted">
                      Showing 10 of {generatedData.length} rows
                    </small>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
