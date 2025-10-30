'use client';

import { useState } from 'react';
import {
    TableSchema,
    ColumnDefinition,
    ColumnType,
    NumberOptions,
    SetOptions,
} from '../types/schema';
import ColumnEditor from './ColumnEditor';
import { generateUUID } from '../lib/utils';

interface SchemaEditorProps {
    schema: TableSchema;
    onSave: (schema: TableSchema) => void;
    onCancel: () => void;
}

export default function SchemaEditor({
                                         schema,
                                         onSave,
                                         onCancel,
                                     }: SchemaEditorProps) {
    const [editedSchema, setEditedSchema] = useState<TableSchema>(schema);
    const [editingColumn, setEditingColumn] = useState<ColumnDefinition | null>(
        null
    );

    const handleAddColumn = () => {
        const newColumn: ColumnDefinition = {
            id: generateUUID(),
            name: '',
            type: 'text',
            nullable: false,
        };
        setEditingColumn(newColumn);
    };

    const handleEditColumn = (column: ColumnDefinition) => {
        setEditingColumn(column);
    };

    const handleSaveColumn = (column: ColumnDefinition) => {
        const existingIndex = editedSchema.columns.findIndex(
            (c) => c.id === column.id
        );
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
        setEditingColumn(null);
    };

    const handleDeleteColumn = (columnId: string) => {
        setEditedSchema({
            ...editedSchema,
            columns: editedSchema.columns.filter((c) => c.id !== columnId),
        });
    };

    const handleSave = () => {
        if (!editedSchema.name.trim()) {
            alert('Please enter a table name');
            return;
        }
        if (editedSchema.columns.length === 0) {
            alert('Please add at least one column');
            return;
        }
        onSave(editedSchema);
    };

    return (
        <div className="border rounded-lg p-6 bg-white shadow-lg">
            <h2 className="text-2xl font-bold mb-4 text-gray-800">
                {schema.name ? 'Edit Schema' : 'New Schema'}
            </h2>

            <div className="mb-6">
                <label className="block text-sm font-medium mb-2 text-gray-800">Table Name</label>
                <input
                    type="text"
                    value={editedSchema.name}
                    onChange={(e) =>
                        setEditedSchema({ ...editedSchema, name: e.target.value })
                    }
                    className="w-full border rounded px-3 py-2 text-gray-800"
                    placeholder="e.g., users, products, orders"
                />
            </div>

            <div className="mb-6">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-xl font-semibold text-gray-800">Columns</h3>
                    <button
                        onClick={handleAddColumn}
                        className="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700"
                    >
                        Add Column
                    </button>
                </div>

                {editingColumn ? (
                    <ColumnEditor
                        column={editingColumn}
                        onSave={handleSaveColumn}
                        onCancel={() => setEditingColumn(null)}
                    />
                ) : (
                    <div className="space-y-2">
                        {editedSchema.columns.map((column) => (
                            <div
                                key={column.id}
                                className="flex justify-between items-center border rounded p-3 bg-gray-50"
                            >
                                <div>
                                    <span className="font-medium text-gray-800">{column.name}</span>
                                    <span className="text-gray-800 ml-2">({column.type})</span>
                                    {!column.nullable && (
                                        <span className="text-red-500 ml-1">*</span>
                                    )}
                                </div>
                                <div className="flex gap-2">
                                    <button
                                        onClick={() => handleEditColumn(column)}
                                        className="text-blue-600 hover:text-blue-800"
                                    >
                                        Edit
                                    </button>
                                    <button
                                        onClick={() => handleDeleteColumn(column.id)}
                                        className="text-red-600 hover:text-red-800"
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <div className="flex gap-3">
                <button
                    onClick={handleSave}
                    className="bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700"
                >
                    Save Schema
                </button>
                <button
                    onClick={onCancel}
                    className="bg-gray-500 text-white px-6 py-2 rounded hover:bg-gray-600"
                >
                    Cancel
                </button>
            </div>
        </div>
    );
}
