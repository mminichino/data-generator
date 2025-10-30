'use client';

import { useState } from 'react';
import { useSchemaStore } from '../store/SchemaStore';

export default function GeneratePage() {
    const { schemas, connection } = useSchemaStore();
    const [selectedSchemas, setSelectedSchemas] = useState<string[]>([]);
    const [rowCount, setRowCount] = useState(100);
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState<any>(null);

    const toggleSchema = (schemaId: string) => {
        if (selectedSchemas.includes(schemaId)) {
            setSelectedSchemas(selectedSchemas.filter((id) => id !== schemaId));
        } else {
            setSelectedSchemas([...selectedSchemas, schemaId]);
        }
    };

    const handleGenerate = async () => {
        if (selectedSchemas.length === 0) {
            alert('Please select at least one schema');
            return;
        }

        setLoading(true);
        setResult(null);

        try {
            const selectedSchemaObjects = schemas.filter((s) =>
                selectedSchemas.includes(s.id)
            );

            const response = await fetch('/api/generate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    schemas: selectedSchemaObjects,
                    rowCount,
                    connection,
                }),
            });

            const data = await response.json();

            if (response.ok) {
                setResult(data);
            } else {
                alert(`Error: ${data.error}`);
            }
        } catch (error) {
            alert(`Error: ${error}`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <h1 className="text-3xl font-bold mb-6">Generate Data</h1>

            {schemas.length === 0 ? (
                <div className="text-center py-12 text-gray-800">
                    No schemas available. Please create schemas first.
                </div>
            ) : (
                <div className="space-y-6">
                    <div className="bg-white p-6 rounded-lg shadow">
                        <h2 className="text-xl font-semibold mb-4 text-gray-800">Select Schemas</h2>
                        <div className="space-y-2">
                            {schemas.map((schema) => (
                                <label
                                    key={schema.id}
                                    className="flex items-center p-3 border rounded hover:bg-gray-50 cursor-pointer"
                                >
                                    <input
                                        type="checkbox"
                                        checked={selectedSchemas.includes(schema.id)}
                                        onChange={() => toggleSchema(schema.id)}
                                        className="mr-3"
                                    />
                                    <div>
                                        <span className="font-medium text-gray-800">{schema.name}</span>
                                        <span className="text-gray-800 ml-2">
                      ({schema.columns.length} columns)
                    </span>
                                    </div>
                                </label>
                            ))}
                        </div>
                    </div>

                    <div className="bg-white p-6 rounded-lg shadow">
                        <label className="block text-sm font-medium mb-2 text-gray-800">
                            Number of Rows per Table
                        </label>
                        <input
                            type="number"
                            value={rowCount}
                            onChange={(e) => setRowCount(parseInt(e.target.value) || 1)}
                            min="1"
                            max="10000"
                            className="w-full border rounded px-3 py-2 text-gray-800"
                        />
                    </div>

                    {!connection && (
                        <div className="bg-yellow-50 border border-yellow-200 p-4 rounded">
                            <p className="text-yellow-800">
                                ⚠️ No database connection configured. Data will be generated but not inserted into a database.
                            </p>
                        </div>
                    )}

                    <button
                        onClick={handleGenerate}
                        disabled={loading}
                        className="w-full bg-green-600 text-white py-3 rounded hover:bg-green-700 disabled:bg-gray-400"
                    >
                        {loading ? 'Generating...' : 'Generate Data'}
                    </button>

                    {result && (
                        <div className="bg-white p-6 rounded-lg shadow">
                            <h2 className="text-xl font-semibold mb-4 text-gray-800">Result</h2>
                            <div className="space-y-4">
                                {Object.entries(result.data).map(([tableName, rows]: [string, any]) => (
                                    <div key={tableName}>
                                        <h3 className="font-medium mb-2 text-gray-800">{tableName}</h3>
                                        <div className="bg-gray-50 p-4 rounded overflow-x-auto">
                      <pre className="text-sm text-gray-800">
                        {JSON.stringify(rows.slice(0, 5), null, 2)}
                      </pre>
                                            {rows.length > 5 && (
                                                <p className="text-gray-800 mt-2">
                                                    ... and {rows.length - 5} more rows
                                                </p>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                            {result.message && (
                                <p className="mt-4 text-green-600">{result.message}</p>
                            )}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
