
export type ColumnType =
    | 'sequentialNumber'
    | 'boolean'
    | 'number'
    | 'uuid'
    | 'firstName'
    | 'lastName'
    | 'fullName'
    | 'set'
    | 'zipcode'
    | 'dollarAmount'
    | 'streetNumber'
    | 'streetAddress'
    | 'city'
    | 'state'
    | 'creditCard'
    | 'accountNumber'
    | 'email'
    | 'phoneNumber'
    | 'text'
    | 'productName'
    | 'productType'
    | 'manufacturer'
    | 'timestamp'
    | 'date'
    | 'ipAddress'
    | 'macAddress';

export interface NumberOptions {
    isDecimal: boolean;
    digits: number;
    decimalPlaces?: number;
}

export interface SetOptions {
    members: string[];
}

export interface ColumnDefinition {
    id: string;
    name: string;
    type: ColumnType;
    nullable: boolean;
    primaryKey?: boolean;
    options?: NumberOptions | SetOptions;
}

export interface TableSchema {
    id: string;
    name: string;
    count?: number;
    keyFormat?: string;
    columns: ColumnDefinition[];
}

export interface SchemaCollection {
    id: string;
    name: string;
    nosql: boolean;
    tables: TableSchema[];
}

export interface DatabaseConnection {
    type: 'postgresql' | 'mysql' | 'sqlite' | 'sqlserver';
    host: string;
    port: number;
    database: string;
    username: string;
    password: string;
}

export interface GenerateDataRequest {
    schema: SchemaCollection;
    rowCount: number;
    connection?: DatabaseConnection;
}
