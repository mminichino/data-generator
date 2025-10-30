
export type ColumnType =
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
    options?: NumberOptions | SetOptions;
}

export interface TableSchema {
    id: string;
    name: string;
    columns: ColumnDefinition[];
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
    schemas: TableSchema[];
    rowCount: number;
    connection?: DatabaseConnection;
}
