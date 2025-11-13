import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
    try {
        const body = await request.json();
        console.log(`Connect body: ${JSON.stringify(body)}`)
        const backendResponse = await fetch('http://localhost:8084/api/database/connect', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        });

        if (!backendResponse.ok) {
            console.log(`Connect responded with: ${backendResponse.status}`);
            return NextResponse.json(
                { response: backendResponse.json() },
                { status: backendResponse.status }
            );
        }

        const result = await backendResponse.json();
        return NextResponse.json(result);
    } catch (error) {
        console.error('Error calling connect endpoint:', error);
        return NextResponse.json(
            { error: 'Failed to call connect API' },
            { status: 500 }
        );
    }
}
