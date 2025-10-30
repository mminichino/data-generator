'use client';

import { useState } from 'react';
import {
  ColumnDefinition,
  ColumnType,
  NumberOptions,
  SetOptions,
} from '../types/schema';

interface ColumnEditorProps {
  column: ColumnDefinition;
  onSave: (column: ColumnDefinition) => void;
  onCancel: () => void;
}

const columnTypes: { value: ColumnType; label: string }[] = [
  { value: 'number', label: 'Number' },
  { value: 'uuid', label: 'UUID' },
  { value: 'firstName', label: 'First Name' },
  { value: 'lastName', label: 'Last Name' },
  { value: 'fullName', label: 'Full Name' },
  { value: 'set', label: 'Set' },
  { value: 'zipcode', label: 'Zipcode' },
  { value: 'dollarAmount', label: 'Dollar Amount' },
  { value: 'streetNumber', label: 'Street Number' },
  { value: 'streetAddress', label: 'Street Address' },
  { value: 'city', label: 'City' },
  { value: 'state', label: 'State (US)' },
  { value: 'creditCard', label: 'Credit Card Number' },
  { value: 'accountNumber', label: 'Account Number' },
  { value: 'email', label: 'Email Address' },
  { value: 'phoneNumber', label: 'Phone Number' },
  { value: 'text', label: 'Text' },
  { value: 'productName', label: 'Product Name' },
  { value: 'productType', label: 'Product Type' },
  { value: 'manufacturer', label: 'Manufacturer' },
  { value: 'timestamp', label: 'Timestamp' },
  { value: 'date', label: 'Date' },
  { value: 'ipAddress', label: 'IP Address' },
  { value: 'macAddress', label: 'MAC Address' },
];

export default function ColumnEditor({
  column,
  onSave,
  onCancel,
}: ColumnEditorProps) {
  const [editedColumn, setEditedColumn] = useState<ColumnDefinition>(column);

  const handleTypeChange = (type: ColumnType) => {
    const newColumn = { ...editedColumn, type };
    
    // Initialize default options based on type
    if (type === 'number' || type === 'dollarAmount') {
      const numberOptions: NumberOptions = {
        isDecimal: type === 'dollarAmount',
        digits: type === 'dollarAmount' ? 10 : 5,
        decimalPlaces: type === 'dollarAmount' ? 2 : undefined,
      };
      newColumn.options = numberOptions;
    } else if (type === 'set') {
      const setOptions: SetOptions = { members: [] };
      newColumn.options = setOptions;
    } else {
      newColumn.options = undefined;
    }
    
    setEditedColumn(newColumn);
  };

  const handleSave = () => {
    if (!editedColumn.name.trim()) {
      alert('Please enter a column name');
      return;
    }
    
    if (editedColumn.type === 'set') {
      const setOptions = editedColumn.options as SetOptions;
      if (!setOptions || setOptions.members.length === 0) {
        alert('Please define at least one set member');
        return;
      }
    }
    
    onSave(editedColumn);
  };

  const renderTypeSpecificOptions = () => {
    if (editedColumn.type === 'number' || editedColumn.type === 'dollarAmount') {
      const options = (editedColumn.options || {
        isDecimal: false,
        digits: 5,
      }) as NumberOptions;

      return (
        <div className="space-y-3 mt-4 p-4 bg-gray-50 rounded">
          <div>
            <label className="flex items-center text-gray-800">
              <input
                type="checkbox"
                checked={options.isDecimal}
                onChange={(e) =>
                  setEditedColumn({
                    ...editedColumn,
                    options: {
                      ...options,
                      isDecimal: e.target.checked,
                      decimalPlaces: e.target.checked ? 2 : undefined,
                    },
                  })
                }
                className="mr-2"
              />
              Decimal Number
            </label>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1 text-gray-800">
              Total Digits
            </label>
            <input
              type="number"
              value={options.digits}
              onChange={(e) =>
                setEditedColumn({
                  ...editedColumn,
                  options: {
                    ...options,
                    digits: parseInt(e.target.value) || 1,
                  },
                })
              }
              min="1"
              max="20"
              className="w-full border rounded px-3 py-2 text-gray-800"
            />
          </div>
          {options.isDecimal && (
            <div>
              <label className="block text-sm font-medium mb-1 text-gray-800">
                Decimal Places
              </label>
              <input
                type="number"
                value={options.decimalPlaces || 2}
                onChange={(e) =>
                  setEditedColumn({
                    ...editedColumn,
                    options: {
                      ...options,
                      decimalPlaces: parseInt(e.target.value) || 0,
                    },
                  })
                }
                min="0"
                max="10"
                className="w-full border rounded px-3 py-2 text-gray-800"
              />
            </div>
          )}
        </div>
      );
    }

    if (editedColumn.type === 'set') {
      const options = (editedColumn.options || { members: [] }) as SetOptions;
      const [newMember, setNewMember] = useState('');

      return (
        <div className="mt-4 p-4 bg-gray-50 rounded">
          <label className="block text-sm font-medium mb-2 text-gray-800">Set Members</label>
          <div className="flex gap-2 mb-3">
            <input
              type="text"
              value={newMember}
              onChange={(e) => setNewMember(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter' && newMember.trim()) {
                  setEditedColumn({
                    ...editedColumn,
                    options: {
                      members: [...options.members, newMember.trim()],
                    },
                  });
                  setNewMember('');
                }
              }}
              placeholder="Enter member and press Enter"
              className="flex-1 border rounded px-3 py-2 text-gray-800"
            />
            <button
              type="button"
              onClick={() => {
                if (newMember.trim()) {
                  setEditedColumn({
                    ...editedColumn,
                    options: {
                      members: [...options.members, newMember.trim()],
                    },
                  });
                  setNewMember('');
                }
              }}
              className="bg-blue-600 text-white px-3 py-2 rounded hover:bg-blue-700"
            >
              Add
            </button>
          </div>
          <div className="space-y-1">
            {options.members.map((member, index) => (
              <div
                key={index}
                className="flex justify-between items-center bg-white p-2 rounded"
              >
                <span className="text-gray-800">{member}</span>
                <button
                  type="button"
                  onClick={() => {
                    const newMembers = options.members.filter(
                      (_, i) => i !== index
                    );
                    setEditedColumn({
                      ...editedColumn,
                      options: { members: newMembers },
                    });
                  }}
                  className="text-red-600 hover:text-red-800 text-sm"
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
        </div>
      );
    }

    return null;
  };

  return (
    <div className="border-2 border-blue-500 rounded-lg p-4 bg-blue-50 mb-4">
      <h4 className="text-lg font-semibold mb-4 text-gray-800">
        {column.name ? 'Edit Column' : 'New Column'}
      </h4>

      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1 text-gray-800">Column Name</label>
          <input
            type="text"
            value={editedColumn.name}
            onChange={(e) =>
              setEditedColumn({ ...editedColumn, name: e.target.value })
            }
            className="w-full border rounded px-3 py-2 text-gray-800"
            placeholder="e.g., user_id, email, created_at"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1 text-gray-800">Column Type</label>
          <select
            value={editedColumn.type}
            onChange={(e) => handleTypeChange(e.target.value as ColumnType)}
            className="w-full border rounded px-3 py-2 text-gray-800"
          >
            {columnTypes.map((type) => (
              <option key={type.value} value={type.value}>
                {type.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="flex items-center text-gray-800">
            <input
              type="checkbox"
              checked={editedColumn.nullable}
              onChange={(e) =>
                setEditedColumn({ ...editedColumn, nullable: e.target.checked })
              }
              className="mr-2"
            />
            Nullable
          </label>
        </div>

        {renderTypeSpecificOptions()}

        <div className="flex gap-3 pt-2">
          <button
            onClick={handleSave}
            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
          >
            Save Column
          </button>
          <button
            onClick={onCancel}
            className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}
