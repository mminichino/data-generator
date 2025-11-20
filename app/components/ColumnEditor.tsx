'use client';

import { useEffect, useState } from 'react';
import { ColumnDefinition, NumberOptions, SetOptions } from '../types/schema';

interface ColumnEditorProps {
  column: ColumnDefinition;
  onSave: (column: ColumnDefinition) => void;
  onCancel: () => void;
}

export default function ColumnEditor({ column, onSave, onCancel }: ColumnEditorProps) {
  const [editedColumn, setEditedColumn] = useState<ColumnDefinition>(column);

  const handleSave = () => {
    if (!editedColumn.name.trim()) {
      alert('Column name is required');
      return;
    }
    onSave(editedColumn);
  };

  const handleTypeChange = (newType: ColumnDefinition['type']) => {
    const updated = { ...editedColumn, type: newType };

    if (newType === 'number' && !updated.options) {
      updated.options = { isDecimal: false, digits: 5 } as NumberOptions;
    } else if (newType === 'set' && !updated.options) {
      updated.options = { members: [''] } as SetOptions;
    } else if (newType !== 'number' && newType !== 'set') {
      updated.options = undefined;
    }
    
    setEditedColumn(updated);
  };

  const updateNumberOptions = (updates: Partial<NumberOptions>) => {
    const currentOptions = (editedColumn.options as NumberOptions) || { isDecimal: false, digits: 5 };
    setEditedColumn({
      ...editedColumn,
      options: { ...currentOptions, ...updates }
    });
  };

  const updateSetOptions = (members: string[]) => {
    setEditedColumn({
      ...editedColumn,
      options: { members } as SetOptions
    });
  };

  const addSetMember = () => {
    const currentMembers = ((editedColumn.options as SetOptions)?.members || []);
    updateSetOptions([...currentMembers, '']);
  };

  const updateSetMember = (index: number, value: string) => {
    const currentMembers = [...((editedColumn.options as SetOptions)?.members || [])];
    currentMembers[index] = value;
    updateSetOptions(currentMembers);
  };

  const removeSetMember = (index: number) => {
    const currentMembers = ((editedColumn.options as SetOptions)?.members || []);
    updateSetOptions(currentMembers.filter((_, i) => i !== index));
  };

  // Ensure mutually exclusive Primary Key and Nullable on initial load/changes
  useEffect(() => {
    if (editedColumn.primaryKey && editedColumn.nullable) {
      setEditedColumn((prev) => ({ ...prev, nullable: false }));
    }
  }, [editedColumn.primaryKey, editedColumn.nullable]);

  return (
    <div className="card border-primary">
      <div className="card-header">
        <h5 className="card-title">
          {column.name ? `Edit Column: ${column.name}` : 'Add New Column'}
        </h5>
      </div>
      <div className="card-body">
        <div className="basic-form">
          <div className="row">
            <div className="col-md-6">
              <div className="form-group mb-3">
                <label className="form-label">Column Name</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Enter column name"
                  value={editedColumn.name}
                  onChange={(e) =>
                    setEditedColumn({ ...editedColumn, name: e.target.value })
                  }
                />
              </div>
            </div>
            <div className="col-md-6">
              <div className="form-group mb-3">
                <label className="form-label">Data Type</label>
                <select
                  className="form-control"
                  value={editedColumn.type}
                  onChange={(e) => handleTypeChange(e.target.value as ColumnDefinition['type'])}
                >
                  <option value="sequentialNumber">Sequential Number</option>
                  <option value="text">Text</option>
                  <option value="number">Number</option>
                  <option value="boolean">Boolean</option>
                  <option value="uuid">UUID</option>
                  <option value="firstName">First Name</option>
                  <option value="lastName">Last Name</option>
                  <option value="fullName">Full Name</option>
                  <option value="email">Email</option>
                  <option value="phoneNumber">Phone Number</option>
                  <option value="streetAddress">Street Address</option>
                  <option value="city">City</option>
                  <option value="state">State</option>
                  <option value="zipcode">Zip Code</option>
                  <option value="creditCard">Credit Card</option>
                  <option value="accountNumber">Account Number</option>
                  <option value="dollarAmount">Dollar Amount</option>
                  <option value="productName">Product Name</option>
                  <option value="productType">Product Type</option>
                  <option value="manufacturer">Manufacturer</option>
                  <option value="date">Date</option>
                  <option value="timestamp">Timestamp</option>
                  <option value="ipAddress">IP Address</option>
                  <option value="macAddress">MAC Address</option>
                  <option value="set">Set</option>
                </select>
              </div>
            </div>
          </div>

          <div className="row">
            <div className="col-md-6">
              <div className="form-check mb-3">
                <input
                  type="checkbox"
                  className="form-check-input"
                  id={`primaryKey-${editedColumn.id}`}
                  checked={!!editedColumn.primaryKey}
                  onChange={(e) => {
                    const checked = e.target.checked;
                    setEditedColumn({
                      ...editedColumn,
                      primaryKey: checked,
                      // Primary Key cannot be nullable
                      nullable: checked ? false : editedColumn.nullable,
                    });
                  }}
                />
                <label className="form-check-label" htmlFor={`primaryKey-${editedColumn.id}`}>
                  Primary Key
                </label>
              </div>
            </div>
            <div className="col-md-6">
              <div className="form-check mb-3">
                <input
                  type="checkbox"
                  className="form-check-input"
                  id={`nullable-${editedColumn.id}`}
                  checked={!!editedColumn.nullable}
                  onChange={(e) => {
                    const checked = e.target.checked;
                    setEditedColumn({
                      ...editedColumn,
                      nullable: checked,
                      // If allowing nulls, ensure primary key is unset
                      primaryKey: checked ? false : editedColumn.primaryKey,
                    });
                  }}
                />
                <label className="form-check-label" htmlFor={`nullable-${editedColumn.id}`}>
                  Allow null values (nullable)
                </label>
              </div>
            </div>
          </div>

          {editedColumn.type === 'number' && (
            <div className="card mb-3 bg-light">
              <div className="card-body">
                <h6 className="card-subtitle mb-3">Number Options</h6>
                <div className="row">
                  <div className="col-md-4">
                    <div className="form-group mb-3">
                      <label className="form-label">Digits</label>
                      <input
                        type="number"
                        className="form-control"
                        min="1"
                        max="20"
                        value={(editedColumn.options as NumberOptions)?.digits || 5}
                        onChange={(e) => updateNumberOptions({ digits: parseInt(e.target.value) || 5 })}
                      />
                    </div>
                  </div>
                  <div className="col-md-4">
                    <div className="form-check mt-4">
                      <input
                        type="checkbox"
                        className="form-check-input"
                        id="isDecimal"
                        checked={(editedColumn.options as NumberOptions)?.isDecimal || false}
                        onChange={(e) => updateNumberOptions({ isDecimal: e.target.checked })}
                      />
                      <label className="form-check-label" htmlFor="isDecimal">
                        Is Decimal
                      </label>
                    </div>
                  </div>
                  {(editedColumn.options as NumberOptions)?.isDecimal && (
                    <div className="col-md-4">
                      <div className="form-group mb-3">
                        <label className="form-label">Decimal Places</label>
                        <input
                          type="number"
                          className="form-control"
                          min="1"
                          max="10"
                          value={(editedColumn.options as NumberOptions)?.decimalPlaces || 2}
                          onChange={(e) => updateNumberOptions({ decimalPlaces: parseInt(e.target.value) || 2 })}
                        />
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {editedColumn.type === 'set' && (
            <div className="card mb-3 bg-light">
              <div className="card-body">
                <h6 className="card-subtitle mb-3">Set Members</h6>
                {((editedColumn.options as SetOptions)?.members || []).map((member, index) => (
                  <div key={index} className="row mb-2">
                    <div className="col-10">
                      <input
                        type="text"
                        className="form-control"
                        placeholder="Enter set member value"
                        value={member}
                        onChange={(e) => updateSetMember(index, e.target.value)}
                      />
                    </div>
                    <div className="col-2">
                      <button
                        type="button"
                        className="btn btn-danger btn-sm w-100"
                        onClick={() => removeSetMember(index)}
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                ))}
                <button
                  type="button"
                  className="btn btn-sm btn-secondary mt-2"
                  onClick={addSetMember}
                >
                  + Add Member
                </button>
              </div>
            </div>
          )}

          <div className="mt-3">
            <button onClick={handleSave} className="btn btn-primary me-2">
              Save Column
            </button>
            <button onClick={onCancel} className="btn btn-light">
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
