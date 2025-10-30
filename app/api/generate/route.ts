import { NextRequest, NextResponse } from 'next/server';
import { GenerateDataRequest, ColumnDefinition, ColumnType } from '../../types/schema';
import { generateUUID } from '../../lib/utils';

// Fake data generators
const generators: Record<
    ColumnType,
    (options?: any) => string | number
> = {
    number: (options) => {
        const { isDecimal, digits, decimalPlaces } = options || { isDecimal: false, digits: 5 };
        if (isDecimal) {
            const max = Math.pow(10, digits - (decimalPlaces || 2)) - 1;
            return parseFloat((Math.random() * max).toFixed(decimalPlaces || 2));
        }
        return Math.floor(Math.random() * Math.pow(10, digits));
    },
    uuid: () => generateUUID(),
    firstName: () => {
        const names = ['John', 'Jane', 'Michael', 'Emily', 'David', 'Sarah', 'Chris', 'Lisa', 'Matt', 'Anna'];
        return names[Math.floor(Math.random() * names.length)];
    },
    lastName: () => {
        const names = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez'];
        return names[Math.floor(Math.random() * names.length)];
    },
    fullName: () => {
        return `${generators.firstName()} ${generators.lastName()}`;
    },
    set: (options) => {
        const { members } = options || { members: [] };
        return members[Math.floor(Math.random() * members.length)] || '';
    },
    zipcode: () => String(Math.floor(10000 + Math.random() * 90000)),
    dollarAmount: (options) => {
        return parseFloat((Math.random() * 10000).toFixed(2));
    },
    streetNumber: () => Math.floor(100 + Math.random() * 9900),
    streetAddress: () => {
        const streets = ['Main St', 'Oak Ave', 'Pine Rd', 'Maple Dr', 'Cedar Ln', 'Elm St', 'Washington Blvd'];
        return `${generators.streetNumber()} ${streets[Math.floor(Math.random() * streets.length)]}`;
    },
    city: () => {
        const cities = ['New York', 'Los Angeles', 'Chicago', 'Houston', 'Phoenix', 'Philadelphia', 'San Antonio', 'San Diego'];
        return cities[Math.floor(Math.random() * cities.length)];
    },
    state: () => {
        const states = ['NY', 'CA', 'TX', 'FL', 'IL', 'PA', 'OH', 'GA', 'NC', 'MI'];
        return states[Math.floor(Math.random() * states.length)];
    },
    creditCard: () => {
        const prefix = ['4', '5', '3'][Math.floor(Math.random() * 3)];
        return prefix + String(Math.floor(Math.random() * 1e15)).padStart(15, '0');
    },
    accountNumber: () => String(Math.floor(1e9 + Math.random() * 9e9)),
    email: () => {
        const domains = ['email.com', 'test.com', 'example.com', 'mail.com'];
        const name = generators.firstName();
        return `${name}${Math.floor(Math.random() * 1000)}@${domains[Math.floor(Math.random() * domains.length)]}`;
    },
    phoneNumber: () => {
        return `${Math.floor(200 + Math.random() * 800)}-${Math.floor(200 + Math.random() * 800)}-${String(Math.floor(Math.random() * 10000)).padStart(4, '0')}`;
    },
    text: () => {
        const words = ['lorem', 'ipsum', 'dolor', 'sit', 'amet', 'consectetur', 'adipiscing', 'elit'];
        const length = Math.floor(3 + Math.random() * 8);
        return Array.from({ length }, () => words[Math.floor(Math.random() * words.length)]).join(' ');
    },
    productName: () => {
        const adjectives = ['Pro', 'Ultra', 'Premium', 'Elite', 'Classic'];
        const products = ['Widget', 'Gadget', 'Tool', 'Device', 'System'];
        return `${adjectives[Math.floor(Math.random() * adjectives.length)]} ${products[Math.floor(Math.random() * products.length)]}`;
    },
    productType: () => {
        const types = ['Electronics', 'Furniture', 'Clothing', 'Food', 'Toys', 'Books'];
        return types[Math.floor(Math.random() * types.length)];
    },
    manufacturer: () => {
        const manufacturers = ['TechCorp', 'GlobalMfg', 'InnovateInc', 'MegaBrand', 'QualityCo'];
        return manufacturers[Math.floor(Math.random() * manufacturers.length)];
    },
    timestamp: () => new Date(Date.now() - Math.random() * 365 * 24 * 60 * 60 * 1000).toISOString(),
    date: () => new Date(Date.now() - Math.random() * 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    ipAddress: () => {
        return `${Math.floor(Math.random() * 256)}.${Math.floor(Math.random() * 256)}.${Math.floor(Math.random() * 256)}.${Math.floor(Math.random() * 256)}`;
    },
    macAddress: () => {
        return Array.from({ length: 6 }, () =>
            Math.floor(Math.random() * 256)
                .toString(16)
                .padStart(2, '0')
        ).join(':');
    },
};

function generateValue(column: ColumnDefinition): any {
    if (column.nullable && Math.random() < 0.1) {
        return null;
    }

    const generator = generators[column.type];
    return generator ? generator(column.options) : null;
}

export async function POST(request: NextRequest) {
    try {
        const body: GenerateDataRequest = await request.json();
        const { schemas, rowCount, connection } = body;

        const data: Record<string, any[]> = {};

        for (const schema of schemas) {
            const rows = [];
            for (let i = 0; i < rowCount; i++) {
                const row: Record<string, any> = {};
                for (const column of schema.columns) {
                    row[column.name] = generateValue(column);
                }
                rows.push(row);
            }
            data[schema.name] = rows;
        }

        // TODO: Implement actual database insertion logic here
        let message = `Generated ${rowCount} rows for ${schemas.length} table(s)`;

        if (connection) {
            message += ` (Database insertion not yet implemented - would insert to ${connection.database})`;
            // TODO: Implement actual database insertion logic here
        }

        return NextResponse.json({
            success: true,
            message,
            data,
        });
    } catch (error: any) {
        return NextResponse.json(
            { success: false, error: error.message },
            { status: 500 }
        );
    }
}
