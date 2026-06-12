
export type ColumnType =
    | 'sequentialNumber'
    | 'boolean'
    | 'number'
    | 'uuid'
    | 'firstName'
    | 'lastName'
    | 'fullName'
    | 'set'
    | 'word'
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
    | 'airportOrigCode'
    | 'airportOrigName'
    | 'airportOrigCity'
    | 'airportDestCode'
    | 'airportDestName'
    | 'airportDestCity'
    | 'airlineCode'
    | 'airlineName'
    | 'bookingCode'
    | 'cabinCode'
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

export interface WordOptions {
    value: string;
}

export interface ColumnDefinition {
    id: string;
    name: string;
    type: ColumnType;
    nullable: boolean;
    primaryKey?: boolean;
    options?: NumberOptions | SetOptions | WordOptions;
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
    type: 'redis' | 'postgres' | 'mysql' | 'sqlite' | 'sqlserver' | 'couchbase';
    hostname: string;
    port: number;
    database: string;
    schema: string;
    scope?: string;
    collection?: string;
    username: string;
    password: string;
    ssl?: boolean;
    json?: boolean;
    tlsSkipVerify?: boolean;
}

export interface GenerateDataRequest {
    schema: SchemaCollection;
    rowCount: number;
    connection?: DatabaseConnection;
}

export type GenerationStatus = 'RUNNING' | 'COMPLETED' | 'CANCELLED' | 'FAILED';

export interface GenerationJobStatus {
    jobId: string;
    status: GenerationStatus;
    totalRecords: number;
    completedRecords: number;
    percentComplete: number;
    recordsPerSecond: number;
    message?: string;
    startedAt: number;
    updatedAt: number;
}

export interface StartGenerationResponse {
    jobId: string;
    status: GenerationStatus;
    totalRecords: number;
}
