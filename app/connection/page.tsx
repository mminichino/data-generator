'use client';

import { useState } from 'react';
import { useSchemaStore } from '../store/SchemaStore';
import { DatabaseConnection } from '../types/schema';

export default function ConnectionPage() {
    const { connection, setConnection } = useSchemaStore();
    const [formData, setFormData] = useState<DatabaseConnection>(
        connection || {
            type: 'postgresql',
            host: 'localhost',
            port: 5432,
            database: '',
            username: '',
            password: '',
        }
    );

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setConnection(formData);
        alert('Database connection saved!');
    };

    const handleTypeChange = (type: DatabaseConnection['type']) => {
        const defaultPorts = {
            postgresql: 5432,
            mysql: 3306,
            sqlite: 0,
            sqlserver: 1433,
        };
        setFormData({ ...formData, type, port: defaultPorts[type] });
    };

    return (
        <div className="max-w-2xl mx-auto">
            <h1 className="text-3xl font-bold mb-6">Database Connection</h1>

            <form onSubmit={handleSubmit} className="space-y-4 bg-white p-6 rounded-lg shadow">
                <div>
                    <label className="block text-sm font-medium mb-1 text-gray-800">Database Type</label>
                    <select
                        value={formData.type}
                        onChange={(e) =>
                            handleTypeChange(e.target.value as DatabaseConnection['type'])
                        }
                        className="w-full border rounded px-3 py-2 text-gray-800"
                    >
                        <option value="postgresql">PostgreSQL</option>
                        <option value="mysql">MySQL</option>
                        <option value="sqlite">SQLite</option>
                        <option value="sqlserver">SQL Server</option>
                    </select>
                </div>

                {formData.type !== 'sqlite' && (
                    <>
                        <div>
                            <label className="block text-sm font-medium mb-1 text-gray-800">Host</label>
                            <input
                                type="text"
                                value={formData.host}
                                onChange={(e) =>
                                    setFormData({ ...formData, host: e.target.value })
                                }
                                className="w-full border rounded px-3 py-2 text-gray-800"
                                placeholder="localhost or IP address"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium mb-1 text-gray-800">Port</label>
                            <input
                                type="number"
                                value={formData.port}
                                onChange={(e) =>
                                    setFormData({ ...formData, port: parseInt(e.target.value) })
                                }
                                className="w-full border rounded px-3 py-2 text-gray-800"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium mb-1 text-gray-800">Username</label>
                            <input
                                type="text"
                                value={formData.username}
                                onChange={(e) =>
                                    setFormData({ ...formData, username: e.target.value })
                                }
                                className="w-full border rounded px-3 py-2 text-gray-800"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium mb-1 text-gray-800">Password</label>
                            <input
                                type="password"
                                value={formData.password}
                                onChange={(e) =>
                                    setFormData({ ...formData, password: e.target.value })
                                }
                                className="w-full border rounded px-3 py-2 text-gray-800"
                            />
                        </div>
                    </>
                )}

                <div>
                    <label className="block text-sm font-medium mb-1 text-gray-800">Database Name</label>
                    <input
                        type="text"
                        value={formData.database}
                        onChange={(e) =>
                            setFormData({ ...formData, database: e.target.value })
                        }
                        className="w-full border rounded px-3 py-2 text-gray-800"
                        placeholder={formData.type === 'sqlite' ? 'database.db' : 'database_name'}
                        required
                    />
                </div>

                <button
                    type="submit"
                    className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
                >
                    Save Connection
                </button>
            </form>

            {connection && (
                <div className="mt-6 p-4 bg-green-50 border border-green-200 rounded">
                    <p className="text-green-800">
                        ✓ Connection configured for <strong>{connection.database}</strong> on{' '}
                        {connection.type}
                    </p>
                </div>
            )}
        </div>
    );
}
